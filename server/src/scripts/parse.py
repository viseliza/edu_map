from docx import Document
from docx.oxml.table import CT_Tbl
from docx.table import Table
import re
import json
import sys
import zipfile
from lxml import etree
from typing import List, Dict, Optional, Tuple
from io import BytesIO

# === КОНФИГУРАЦИЯ ===
CONTENT_MARKERS = [
    "Содержание учебного материала", "Практические занятия", "Практическая подготовка",
    "Самостоятельная работа обучающихся", "Самостоятельная работа студентов", 
    "Самостоятельная работа", "В том числе в форме практической подготовки", "В том числе",
]

TABLE_HEADER_KEYWORDS = {
    'name': ['наименование', 'раздел', 'тема'],
    'content': ['содержание', 'материал', 'занятия', 'работ'],
    'hours': ['объем', 'час', 'часов'],
}

NAMESPACES = {
    'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
    'r': 'http://schemas.openxmlformats.org/officeDocument/2006/relationships',
}

def normalize(text: str) -> str:
    return re.sub(r'\s+', ' ', text or '').strip()

def to_int(value: str) -> int:
    try:
        return int(str(value).strip().replace(' ', ''))
    except:
        return 0

def get_text_from_element(elem) -> str:
    """Извлекает текст из XML-элемента w:t"""
    texts = elem.xpath('.//w:t/text()', namespaces=NAMESPACES)
    return ' '.join(t.strip() for t in texts if t.strip())

def parse_table_xml(tbl_elem) -> List[List[str]]:
    """Парсит XML-таблицу в список строк"""
    rows = []
    for row in tbl_elem.xpath('./w:tr', namespaces=NAMESPACES):
        cells = []
        # Учитываем ячейки и объединённые (gridSpan)
        for cell in row.xpath('./w:tc | ./w:tc/w:tcPr/w:vMerge', namespaces=NAMESPACES):
            if cell.tag.endswith('tc'):
                text = get_text_from_element(cell)
                cells.append(normalize(text))
        if cells:
            rows.append(cells)
    return rows

def extract_tables_from_docx(file_path: str) -> List[Tuple[List[List[str]], str]]:
    """Извлекает все таблицы из DOCX, включая колонтитулы, через прямой парсинг XML"""
    tables = []
    
    with zipfile.ZipFile(file_path) as zf:
        # Файлы для поиска таблиц
        xml_files = [
            'word/document.xml',      # Основное содержание
            *[f for f in zf.namelist() if f.startswith('word/header') and f.endswith('.xml')],
            *[f for f in zf.namelist() if f.startswith('word/footer') and f.endswith('.xml')],
        ]
        
        for xml_path in xml_files:
            if xml_path not in zf.namelist():
                continue
                
            try:
                xml_content = zf.read(xml_path)
                root = etree.fromstring(xml_content)
                
                # Ищем все таблицы <w:tbl>
                tbl_elements = root.xpath('//w:tbl', namespaces=NAMESPACES)
                source = 'body' if 'document.xml' in xml_path else ('header' if 'header' in xml_path else 'footer')
                
                for tbl in tbl_elements:
                    rows = parse_table_xml(tbl)
                    if rows and any(any(cell for cell in row) for row in rows):  # Не пустая таблица
                        tables.append((rows, source))
                        
            except Exception as e:
                print(f"⚠️ Ошибка парсинга {xml_path}: {e}", file=sys.stderr)
                continue
                
    return tables

def is_target_table(rows: List[List[str]]) -> bool:
    """Определяет, является ли таблица тематическим планом по ключевым словам"""
    # Берём первые 5 строк для анализа заголовков
    sample = ' '.join(' '.join(cell.lower() for cell in row) for row in rows[:5])
    
    has_name = any(kw in sample for kw in TABLE_HEADER_KEYWORDS['name'])
    has_content = any(kw in sample for kw in TABLE_HEADER_KEYWORDS['content'])
    has_hours = any(kw in sample for kw in TABLE_HEADER_KEYWORDS['hours'])
    
    # Дополнительный сигнал: нумерация колонок "1 2 3 4"
    has_numbered = bool(re.search(r'\b1\s+2\s+3\s+4\b', sample))
    
    return (has_name and has_content and has_hours) or has_numbered

def find_target_table(rows_list: List[Tuple[List[List[str]], str]]) -> Optional[List[List[str]]]:
    """Находит целевую таблицу"""
    # 1. Ищем по ключевым словам в заголовках
    for rows, source in rows_list:
        if is_target_table(rows):
            return rows
    
    # 2. Если не нашли — берём самую большую таблицу
    valid = [(rows, len(rows)) for rows, _ in rows_list if len(rows) > 5]
    if valid:
        return max(valid, key=lambda x: x[1])[0]
    
    return None

def is_header_row(text: str) -> bool:
    t = normalize(text).lower()
    if not t or re.fullmatch(r'[\d\s,.-]+', t):
        return True
    if all(kw in t for kw in ['наименование', 'содержание']) and 'объем' in t:
        return True
    return False

def parse_hours_and_competencies(text: str) -> Tuple[str, str, str]:
    text = normalize(text)
    
    # Компетенции
    comp_patterns = [
        r'((?:\d+[.,]?\d*\s*,\s*)+\d+[.,]?\d*)\s*$',
        r'((?:[ОП]К\s*\d+[.,]?\d*\s*,?\s*)+)\s*$',
    ]
    competencies = ""
    for pattern in comp_patterns:
        match = re.search(pattern, text, re.I)
        if match:
            competencies = normalize(match.group(1)).replace(' ', '')
            text = text[:match.start()].strip()
            break
    
    # Часы
    hours = ""
    hours_match = re.search(r'(?<!\d)(\d+)(?!\d)\s*$', text)
    if hours_match:
        hours = hours_match.group(1)
        text = text[:hours_match.start()].strip()
    
    return text, hours, competencies

def is_section_marker(text: str) -> bool:
    return bool(re.match(r'^Раздел\s*\d+', text, re.I))

def parse_topic_marker(text: str) -> Optional[Dict]:
    match = re.match(r'^Тема\s*(\d+(?:\.\d+)?\.?)\s*(.*)$', text, re.I)
    if match:
        return {'number': match.group(1).rstrip('.'), 'rest': normalize(match.group(2))}
    return None

def find_content_marker(text: str) -> Optional[str]:
    lower = text.lower()
    candidates = [(text.lower().find(m.lower()), m) for m in CONTENT_MARKERS if m.lower() in lower]
    return min(candidates, key=lambda x: x[0])[1] if candidates else None

def split_by_content_marker(text: str) -> Tuple[str, str, str]:
    marker = find_content_marker(text)
    if not marker:
        return text, "", ""
    idx = text.lower().find(marker.lower())
    return normalize(text[:idx]), marker, normalize(text[idx + len(marker):])

def parse_table_rows(rows: List[List[str]]) -> List[Dict]:
    """Парсит строки таблицы в структурированные данные"""
    result = []
    current_section = None
    current_topic = None
    
    for row in rows:
        row_text = " ".join(cell for cell in row if cell.strip())
        if not row_text.strip() or is_header_row(row_text):
            continue
        
        # === РАЗДЕЛ ===
        if is_section_marker(row_text):
            _, hours, competencies = parse_hours_and_competencies(row_text)
            current_section = {
                "section": row_text, "hours": hours, "competencies": competencies, "topics": []
            }
            result.append(current_section)
            current_topic = None
            continue
        
        # === ТЕМА ===
        topic_info = parse_topic_marker(row_text)
        if topic_info:
            if current_section is None:
                current_section = {"section": "", "hours": "", "competencies": "", "topics": []}
                result.append(current_section)
            
            _, hours, competencies = parse_hours_and_competencies(row_text)
            topic_key = f"Тема {topic_info['number']}"
            
            before_marker, marker, after_marker = split_by_content_marker(topic_info['rest'])
            topic_name = topic_key if not before_marker or before_marker == topic_key else f"{topic_key} {before_marker}".strip()
            
            if current_topic is None or current_topic["topic_key"] != topic_key:
                current_topic = {
                    "topic_key": topic_key, "topic": topic_name, "hours_total": 0, "items": []
                }
                current_section["topics"].append(current_topic)
            elif topic_name != topic_key and current_topic["topic"] == topic_key:
                current_topic["topic"] = topic_name
            
            kind = marker if marker else "content"
            item_text = after_marker if after_marker else (before_marker if before_marker != topic_key else "")
            
            if kind != "content" or item_text or hours or competencies:
                current_topic["items"].append({
                    "kind": kind, "text": item_text, "hours": hours, "competencies": competencies
                })
                current_topic["hours_total"] += to_int(hours)
            continue
        
        # === ПРОДОЛЖЕНИЕ ТЕКУЩЕЙ ТЕМЫ ===
        if current_topic:
            core, hours, competencies = parse_hours_and_competencies(row_text)
            before_marker, marker, after_marker = split_by_content_marker(core)
            current_topic["items"].append({
                "kind": marker if marker else "content",
                "text": after_marker if after_marker else before_marker,
                "hours": hours, "competencies": competencies
            })
            current_topic["hours_total"] += to_int(hours)
    
    return result

def main():
    if len(sys.argv) < 2:
        print("Использование: python parser.py <файл.docx>", file=sys.stderr)
        sys.exit(1)
    
    file_path = sys.argv[1]
    
    # Извлекаем таблицы через XML-парсинг
    all_tables = extract_tables_from_docx(file_path)
    if not all_tables:
        print("❌ Таблицы не найдены в документе", file=sys.stderr)
        sys.exit(1)
    
    print(f"ℹ️ Найдено таблиц: {len(all_tables)}", file=sys.stderr)
    
    target_rows = find_target_table(all_tables)
    if target_rows is None:
        print("❌ Целевая таблица не определена", file=sys.stderr)
        # Для отладки: выводим первые строки всех таблиц
        for i, (rows, src) in enumerate(all_tables[:3], 1):
            print(f"\nТаблица {i} ({src}):", file=sys.stderr)
            for row in rows[:3]:
                print(f"  {row}", file=sys.stderr)
        sys.exit(1)
    
    data = parse_table_rows(target_rows)
    print(json.dumps(data, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    main()
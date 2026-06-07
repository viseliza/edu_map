import { execFile, execFileSync } from 'child_process';
import { existsSync, writeFileSync } from 'fs';
import path from 'path';
import { promisify } from 'util';
import { platform } from 'os';
import { requestWithRetry } from '../Request';

export class Subject {
    private href: string;
    private name: string;

    constructor(name: string, href: string) {
        this.name = name;
        this.href = href;
    }

    async get() {
        const response = await requestWithRetry(this.href, {
            responseType: 'arraybuffer'
        });

        const filePath = `./files/${this.name}.docx`;

        if (!existsSync(filePath))
            writeFileSync(filePath, response.data);

        this.convert(filePath);
        return await this.parseDoc(filePath.replace('files', 'tmp'));
    }

    private getLibreOfficePath(): string {
        const plat = platform();

        if (plat === 'win32') {
            // Проверяем несколько возможных путей установки на Windows
            const possiblePaths = [
                'C:\\Program Files\\LibreOffice\\program\\soffice.exe',
                'C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe',
                process.env.LIBREOFFICE_PATH, // Можно задать в .env
            ];

            const found = possiblePaths.find(p => p && existsSync(p));
            if (!found) {
                throw new Error(
                    'LibreOffice not found. Install it or set LIBREOFFICE_PATH env variable.\n' +
                    'Expected paths:\n' + possiblePaths.filter(Boolean).join('\n')
                );
            }
            return found;
        }

        // Linux/macOS: полагаемся на PATH
        return 'soffice';
    }

    convert(file: string) {
        const outputPath = './tmp';
        const outputFile = path.join(outputPath, path.basename(file));

        // Если файл уже сконвертирован — пропускаем
        if (existsSync(outputFile)) {
            return;
        }

        const libreOffice = this.getLibreOfficePath();
        const absFile = path.resolve(file);

        try {
            execFileSync(libreOffice, [
                '--headless',
                '--convert-to',
                'docx',
                '--outdir',
                outputPath,
                absFile,
            ], {
                stdio: 'pipe', // Чтобы ошибки не засоряли консоль
                windowsHide: true, // Скрыть окно на Windows
            });
        } catch (error: any) {
            console.error('LibreOffice conversion failed:', {
                error: error.message,
                stderr: error.stderr?.toString(),
                command: `${libreOffice} --headless --convert-to docx ${absFile}`
            });
            throw new Error(`Failed to convert ${file}: ${error.message}`);
        }
    }

    async parseDoc(filePath: string) {
        const scriptPath = path.resolve(__dirname.split('dist')[0], 'src/scripts/parse.py');

        const execFileAsync = promisify(execFile);

        const { stdout } = await execFileAsync('python', [
            scriptPath,
            filePath.replace('files', 'tmp'),
        ]);

        return JSON.parse(stdout);
    }
}

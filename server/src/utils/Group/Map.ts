import * as cheerio from 'cheerio';
import axios from "axios";
import { Group, GroupMapParse } from "src/types";
import { InternalServerErrorException } from '@nestjs/common';

export class GroupMap {
    private groups: Array<Group>;

    constructor(groups: Group[]) {  
        this.groups = groups
    }

    async forEach(callback: (data: any) => Promise<void>): Promise<number> {
        console.time('GroupMap.forEach');
        let counter = 0;

        for (const group of this.groups) {
            const result = await this.parseGroupPage(group);
            await callback(result);
            counter++;
        }

        console.timeEnd('GroupMap.forEach');
        return counter;
    }

    async parseGroupPage(group: Group): Promise<GroupMapParse> {
        const values = [] as string[];
        const size = 6;
        const groupNameSplited = group.name.split('_');
        const studyMode = {
            undefined: 'очная',
            '30': 'заочная',
        };
        
        try {
            const response = await axios.get(
                `https://portal.novsu.ru/search/groups/i.2500/?page=search&grpname=${groupNameSplited[0]}`,{
                headers: {
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
                    'Accept': 'text/html,application/xhtml+xml',
                }
            });
            const $ = cheerio.load(response.data);
            
            $('#npe_instance_2500_npe_content').find('ul li').each((_, row) => {
                values.push(
                    $(row)
                        .text()
                        .split(':')[1]
                        .replaceAll('\n', '')
                        .replaceAll('\t', '')
                        .trim()
                );
            });
        } catch (error) {
            throw new InternalServerErrorException(
                `Ошибка при получении данных с сайта: ${error instanceof Error ? error.message : error}`
            );
        }

        // Объединение таблиц, если их несколько по 6 строк
        const studyModesValues = Array.from({ length: Math.ceil(values.length / size) }, (_, index) =>
            values.slice(index * size, index * size + size)
        );

        // Поиск нужной таблицы по 6-ой строке, по форме обучения обучения
        const studyModeValues = studyModesValues.find(studyModeValues => 
            studyModeValues[5] === studyMode[groupNameSplited[1]]
        ) as string[];

        return ({
            'group': {
                id: group.id as number,
                name: group.name,
                course: parseInt(studyModeValues[1]),
            }, 
            'education_field': {
                speciality: studyModeValues[2],
                profile: studyModeValues[3],
            }
        }); 
    }
}
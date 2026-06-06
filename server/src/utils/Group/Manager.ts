import { InternalServerErrorException } from "@nestjs/common";
import { readdirSync, rmdirSync } from "fs";
import * as cheerio from 'cheerio';
import axios from "axios";
import { Group, GroupCreate } from "src/types";

export class GroupManager {
    static async deleteTrash() {
        const dir = readdirSync('./public/docs');
        dir.forEach((subdir: string) => {
            const splited = subdir.split('.');
            const date = new Date(`${splited[1]}.${splited[0]}.${splited[2]}`);
            if ((date.getTime() + (1000 * 3600 * 24 * 7)) < new Date().getTime()) {
                rmdirSync(`public/docs/${subdir}`);
                console.log("| PASS | " + new Date().toLocaleTimeString() + ` | FOLDER '${subdir}' HAS BEEN DELETED`);
            }
        })
    }

    static async pushGroups(): Promise<GroupCreate[]> {
        let groupsData: Array<GroupCreate> = [];

        try {
            const response = await axios.get('https://portal.novsu.ru/univer/timetable/spo/', {
                headers: {
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
                    'Accept': 'text/html,application/xhtml+xml',
                }
            });
            const $ = cheerio.load(response.data);

            $('#npe_instance_125460_npe_content').find('tr').each((_, row) => {
                $(row).find('td').find('a').each((_, cell) => {
                    groupsData = [
                        ...groupsData, 
                        { 'name': $(cell).text() }
                    ];
                })
            })
        } catch (error) {
            throw new InternalServerErrorException(
                `Ошибка при получении данных с сайта: ${error instanceof Error ? error.message : error}`
            );
        }

        return groupsData;
    }
}
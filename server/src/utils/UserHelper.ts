import { get } from "axios";
import { load } from "cheerio";

export class UserHelper {
    private username: string;

    constructor(username: string) { 
        this.username = username;
    }

    /** Парсинг роли преподавателя по логину пользователя
     * 
     * @returns - строка 'TEACHER', если пользователь является преподавателем, иначе undefined
     * @throws - выбрасывает ошибку, если произошла ошибка при выполнении запроса
     */
    async teacherRoleParsing(): Promise<string | void> {
        try {
            const response = await get(`https://portal.novsu.ru/person/detail/${this.username}/r.3453.0.4/i.3453.0.0/?mode=test`);
            const $ = load(response.data);

            const category = $('b:contains("Преподаватель (242)")').text()
            
            if (category) 
                return 'TEACHER';
        } catch (error) {
            console.error('Ошибка:', error);
        }
    }

    /** Парсинг номера группы студента по логину пользователя
     * 
     * @returns - строка с номером группы, если пользователь является студентом, иначе undefined
     * @throws - выбрасывает ошибку, если произошла ошибка при выполнении запроса
     */
    async studentGroupParsing(): Promise<string | undefined> {
        try {
            const response = await get(`https://people.novsu.ru/NovSUScheduleService/ScheduleProxy?uid=${this.username}`);

            const match = response.data.match(/src="([^"]+)"/);
            const src = match ? match[1] : null;

            if (!src) 
                return undefined;

            const url = new URL(src);
            const name = url.searchParams.get('name');

            return name as string | undefined;
        } catch (error) {
            console.error('Ошибка:', error);
        }
    }
}
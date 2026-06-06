import { UnauthorizedException } from "@nestjs/common";
import { EducationField } from "@prisma/client";
import * as cheerio from 'cheerio';
import { PROFILES } from "src/common";
import { PdfParser, requestWithRetry, Subject } from "src/utils";
import { GetURLArguments } from "src/types";

export class EducationPlan {
    private readonly specialtiesURL = 'https://portal.novsu.ru/study/umk1/college/i.187729/?id=1978521';
    private educationFields: Array<EducationField>;
    private stage: number = 0;

    constructor(educationField: EducationField[]) {
        this.educationFields = educationField;
    }

    async main() {
        const [{ profileURL, plans }] = await this.forEach(this.educationFields, this.getPlanURL);
        // const profileURL = 'https://portal.novsu.ru/study/umk1/college/i.187729/?id=1991961';
        return await this.getSubjects(profileURL);
    }

    async forEach(data: any[], callback: (data: any) => any) {
        const result: Array<any> = [];

        for (const item of data) {
            result.push(await callback.call(this, item));
        }

        return result;
    }

    async parseTable<T extends boolean>(
        url: string,
        callback: (data: string, href: string) => Promise<string | null>,
        offErrors: T
    ): Promise<T extends true ? Array<any> | null : string> {
        let resultURLs: Array<string> | null = [];

        try {
            const response = await requestWithRetry(url);
            const $ = cheerio.load(response.data);

            // Собираем элементы в массив, чтобы итерироваться через for...of
            const links = $('.filetable').find('tbody tr td a').toArray();

            for (const row of links) {
                const match = await callback(
                    $(row).text().replaceAll('\n', '').replaceAll('\t', '').trim(),
                    row.attribs.href
                );

                if (match) {
                    resultURLs.push(match);
                }
            }
        } catch (error) {
            console.error(error);
            if (!offErrors) throw error;
        }

        if (!offErrors && !resultURLs?.length) {
            throw new UnauthorizedException(
                '[ ERROR ] Не получилось получить ссылку на стадии ' + this.stage
            );
        }

        return (offErrors ? resultURLs : resultURLs?.[0]) as any;
    }

    async getURL<T extends boolean = false>(
        stage: number,
        tableURL: string,
        match?: any,
        args?: GetURLArguments<T>
    ): Promise<T extends true ? Array<any> | null : string> {
        this.stage = stage;
        if (args?.matchReplace)
            match = args.matchReplace();


        return this.parseTable(
            tableURL,
            async (row, href) => {
                return args?.callback
                    ? await args.callback(row, href)
                    : await this.callback(match, row, href);
            },
            (args?.offErrors ?? false) as T
        );
    }

    async getPlanURL(educationField: EducationField) {
        const {
            speciality,
            profile
        } = educationField;

        const specilatyURL = await this.getURL(1, this.specialtiesURL, speciality);
        const profileURL = await this.getURL(2, specilatyURL, profile, {
            matchReplace: () => {
                let profileReplaced = profile.replace('("Профессионалитет")', '').trim();
                return profileReplaced = profileReplaced in PROFILES
                    ? PROFILES[profileReplaced]
                    : profileReplaced;
            }
        });
        const educationPlansURL = await this.getURL(3, profileURL, "Учебные планы");
        const educationPlanURL = await this.getURL(4, educationPlansURL, speciality, {
            matchReplace: () => {
                return speciality.split('  ')[1] as string;
            }
        });

        return {
            profileURL,
            plans: educationPlanURL
        }
    }

    async getSubjects(profileURL: string) {
        const educationSubjectsURL = await this.getURL(5, profileURL, "Учебно-методический комплекс");
        const educationSubjects = await this.getURL(6, educationSubjectsURL, null, {
            callback: (name, href) => {
                return {
                    name: name.split(' ').slice(1).join(' '),
                    href: href.replace('//?', '/?')
                };
            },
            offErrors: true
        });

        return await this.forEach(educationSubjects as any[], async ({ name, href }) => {
            return await this.getURL(7, href, null, {
                callback: async (row, hrefDoc) => {
                    const subject = new Subject(name, "https://portal.novsu.ru" + hrefDoc);
                    return { [name]: await subject.get() }; 
                },
                offErrors: true
            });
        });
    }

    callback(data: string, row: string, href: string) {
        return row
            .replaceAll('_', ' ')
            .includes(
                data.replaceAll('  ', ' ')
            ) ? href : null;
    }


    // /file/2241690
    async parseEducationPlan(url: string) {
        const pdfParser = new PdfParser();
        const parsed = await pdfParser.parsePdf("https://portal.novsu.ru" + url);

        console.log(parsed)
    }
};
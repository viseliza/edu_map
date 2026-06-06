import { UnauthorizedException } from "@nestjs/common";
import { EducationField } from "@prisma/client";
import * as cheerio from 'cheerio';
import axios from "axios";
import { PROFILES } from "src/common";
import { PdfParser } from "src/utils";

export class EducationParser {
    private readonly firstStageUrl = 'https://portal.novsu.ru/study/umk1/college/i.187729//?id=1978521';
    private educationFields: Array<EducationField>;
    private stage: number = 0;

    constructor(educationField: EducationField[]) {
        this.educationFields = educationField;
    }

    async forEach(callback: (data: any) => void) {
        for (const educationField of this.educationFields) {
            const { 
                speciality,
                profile
            } = educationField;

            // const secondStageUrl = await this.firstStage(speciality);
            // const thirdStageUrl = await this.secondStage(profile, secondStageUrl);
            // const educationDocumentsUrl = await this.educationDocuments(thirdStageUrl);
            // const educationPlansUrl = await this.educationPlans(speciality, educationDocumentsUrl);
            const parsedPlan = await this.parseEducationPlan(/* educationPlansUrl */ "/file/2241690");
        }
    }

    async parseTable(url: string, callback: (data: string, href: string) => string | null) {
        let nextStageUrl: string | null = null;
        const response = await axios.get(url, {
            headers: {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36',
                'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
                'Accept-Language': 'ru-RU,ru;q=0.9,en;q=0.8',
                'Connection': 'keep-alive',
            }
        });
        const $ = cheerio.load(response.data);
        
        $('.filetable').find('tbody tr td a').each((_, row) => {
            nextStageUrl = callback(
                $(row)
                    .text()
                    .replaceAll('\n', '')
                    .replaceAll('\t', '')
                    .trim(),
                row.attribs.href
            );

            if (nextStageUrl)
                return false;
        });

        if (!nextStageUrl)
            throw new UnauthorizedException('[ ERROR ] Не получилось получить ссылку на стадии ' + this.stage);

        return nextStageUrl;
    }

    callback(data: string, row: string, href: string) {
        return row
            .replaceAll('_', ' ')
            .includes(
                data.replaceAll('  ', ' ')
            ) ? href : null;
    }

    /** Первая стадия получения данных - получение ссылки на документы специальности */
    async firstStage(speciality: string) {
        this.stage = 1;
        const secondStageUrl = await this.parseTable(this.firstStageUrl, (row, href) => {
            return this.callback(speciality, row, href);
        });

        return secondStageUrl;
    }

    /** Вторая стадия получения данных - получение ссылки на документ с планом обучения */
    async secondStage(profile: string, url: string) {
        this.stage = 2;
        profile = profile.replace('("Профессионалитет")', '').trim();
        profile = profile in PROFILES 
            ? PROFILES[profile] 
            : profile;

        const thirdStageUrl = await this.parseTable(url, (row, href) => {
            return this.callback(profile, row, href);
        });
        
        return thirdStageUrl;
    }    

    /** Первая стадия получения данных - получение ссылки на документы специальности */
    async educationDocuments(url: string) {
        this.stage = 3;
        const educationDocuments = await this.parseTable(url, (row, href) => {
            return this.callback("Учебные планы", row, href);
        });

        return educationDocuments;
    }

    /** Первая стадия получения данных - получение ссылки на документы специальности */
    async educationPlans(speciality: string, url: string) {
        this.stage = 4;

        const educationPlans = await this.parseTable(url, (row, href) => {
            const specialityWithoutCode = speciality.split('  ')[1] as string;
            return this.callback(specialityWithoutCode, row, href);
        });

        return educationPlans;
    }

    async parseEducationPlan(url: string) {
        const pdfParser = new PdfParser();
        const parsed = await pdfParser.parsePdf("https://portal.novsu.ru" + url);
     
        console.log(parsed)
    }
};
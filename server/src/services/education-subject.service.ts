import { Injectable } from "@nestjs/common";
import { PrismaService } from "src/prisma/prisma.service";
import { EducationImport } from "src/utils";

@Injectable()
export class EducationSubjectService {
    constructor(private prisma: PrismaService) { }

    async create(data) {
        return await this.prisma.educationSubject.create({
            data
        });
    }

    async update(name: string, education_plan_id: number, data: any) {
        return await this.prisma.educationSubject.update({
            where: {
                education_plan_id_name: {
                    name, education_plan_id
                }
            },
            data
        });
    }

    // service.ts
    async import(education_plan_id: number) {
        const educationImport = new EducationImport();
        const subjects = educationImport.fill(education_plan_id);

        // Обрабатываем пакетами по 50 записей для производительности
        const BATCH_SIZE = 50;
        const stats = { created: 0, updated: 0, errors: 0 };

        for (let i = 0; i < subjects.length; i += BATCH_SIZE) {
            const batch = subjects.slice(i, i + BATCH_SIZE);

            try {
                await this.prisma.$transaction(
                    batch.map((subject) =>
                        this.prisma.educationSubject.upsert({
                            where: {
                                // Используем составной ключ, который Prisma генерирует автоматически
                                education_plan_id_name: {
                                    education_plan_id,
                                    name: subject.name,
                                },
                            },
                            update: {
                                hours: subject.hours,
                                themes: subject.themes,
                            },
                            create: {
                                name: subject.name,
                                hours: subject.hours,
                                themes: subject.themes,
                                education_plan_id,
                            },
                        })
                    )
                );

                // Простая статистика: считаем, что все в батче успешно (можно доработать)
                stats.created += batch.length;
            } catch (error: any) {
                console.error(`Batch error: ${error.message}`);
                stats.errors += batch.length;
            }
        }

        return {
            stats,
            subjects
        };
    }
}
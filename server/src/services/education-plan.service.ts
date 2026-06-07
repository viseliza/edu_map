import { Injectable, UnsupportedMediaTypeException } from "@nestjs/common";
import { PrismaService } from "src/prisma/prisma.service";
import { EducationPlan } from "src/utils";
import { EducationSubjectService } from "./education-subject.service";

@Injectable()
export class EducationPlanService {
    constructor(
        private prisma: PrismaService,
        private readonly educationSubjectService: EducationSubjectService,
    ) { }

    async create(data: { year: number, education_field_id: number }) {
        return await this.prisma.educationPlan.create({
            data: {
                year: data.year,
                education_field: {
                    connect: {
                        id: data.education_field_id
                    }
                }
            }
        });
    }

    async get(id: number) {
        return await this.prisma.educationField.findMany({
            where: {
                id: { in: [ id ] },
            },
            include: {
                education_plan: {
                    include: {
                        education_subjects: true
                    }
                }
            }
        });
    }

    async fill() {
        let educationFields = await this.get(12);
        if (!educationFields[0].education_plan) {
            await this.create({ year: 2022, education_field_id: 12 });
            educationFields = await this.get(12);
        }

        const educationPlan = new EducationPlan(educationFields);
        const subjects = await educationPlan.main();
        const updated = Array<Promise<any>>();

        educationFields.forEach(async (educationField) => {
            const educationPlan = educationField.education_plan;
            let educationSubjects: any;

            if (!educationPlan?.education_subjects.length) {
                const { subjects } = await this.educationSubjectService.import(educationPlan!.id);
                educationSubjects = subjects;
            } else {
                educationSubjects = educationPlan!.education_subjects;
            }
            
            educationField.education_plan!.education_subjects = educationSubjects.map((subject) => {
                if (educationPlan?.education_field_id === educationField.id) {
                    const existingSubject = subjects.find((s: any[]) => {
                        if (s.length && Object.keys(s[0])[0] == subject.name) {
                            updated.push(this.educationSubjectService.update(subject.name, educationPlan!.id, {
                                themes: Object.values(s[0])[0]
                            }));
                            return Object.values(s[0])[0];
                        } 
                    });
                    return existingSubject === undefined 
                        ? []
                        : existingSubject; 
                }
            });
        });
        
        return educationFields;
    }
}
import { Injectable } from "@nestjs/common";
import { PrismaService } from "src/prisma/prisma.service";
import { GroupService } from "./group.service";
import { EducationPlan, GroupMap } from "src/utils";
import { GroupMapParse } from "src/types";

@Injectable()
export class EducationFieldService {
    constructor(
        private prisma: PrismaService,
        private readonly groupService: GroupService,
    ) { }

    async createMany() {
        const groups = await this.groupService.findAll();
        const groupMap = new GroupMap(groups);
        
        const count = await groupMap.forEach(async (data: GroupMapParse) => {
            const { group, education_field } = data;

            await this.groupService.update({
                where: {
                    name: group.name
                },
                data: { 
                    course: group.course,
                    education_field: {
                        connectOrCreate: {
                            where: {
                                speciality_profile: {
                                    speciality: education_field.speciality,
                                    profile: education_field.profile
                                }
                            },
                            create: {
                                speciality: education_field.speciality,
                                profile: education_field.profile
                            }
                        }
                    }
                }
            })
        });

        return count;
    }

    async create(data: { speciality: string, profile: string }) {
        return await this.prisma.educationField.create({
            data
        });
    }

    async get() {
        return await this.prisma.educationField.findMany({
            where: {
                id: { in: [ 12 ] },
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

    async update(data: any, where: { id: number }) {
        return await this.prisma.educationField.update({
            where,
            data
        });
    }
}
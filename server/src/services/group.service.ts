import { Injectable, UnauthorizedException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { Group, Prisma } from '@prisma/client';
import { GroupEntity } from 'src/entities';

@Injectable()
export class GroupService {
    constructor(private prisma: PrismaService) { }

    async getByUserId(credentials_id: number): Promise<Group> {
        const group = await this.prisma.group.findFirst({
            where: {
                users: {
                    some: {
                        credentials_id
                    }
                }
            }
        });

        if (!group)
            throw new Error(`Группа для пользователя с credentials_id ${credentials_id} не найдена`);

        return group;
    }

    async get(where): Promise<Group> {
        const group = await this.prisma.group.findUnique({
            where: {
                name: where.name
            }
        });

        if (!group)
            throw new Error(`Группа с name ${where.name} не найдена`);

        return group;
    }

    async findAll():  Promise<GroupEntity[]> {
        return await this.prisma.group.findMany({});
    }

    async create(data: Prisma.GroupCreateInput): Promise<Group> {
        return await this.prisma.group.create({
            data
        });
    }

    async createMany(data: Prisma.GroupCreateManyInput[]): Promise<Prisma.BatchPayload> {
        return await this.prisma.group.createMany({
            data
        });
    }

    async update(params: {
        where: Prisma.GroupWhereUniqueInput;
        data: Prisma.GroupUpdateInput;
    }): Promise<Group> {
        const { data, where } = params;
        return await this.prisma.group.update({
            data,
            where,
        });
    }

    async delete() {
        try {
            return await this.prisma.group.deleteMany({});
        } catch (e) {
            throw new UnauthorizedException(e)
        }
    }
}
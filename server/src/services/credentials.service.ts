import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { Credentials, Prisma } from '@prisma/client';
import * as bcrypt from 'bcrypt';
import { CredentialsEntity } from 'src/entities';

@Injectable()
export class CredentialsService {
	constructor(private prisma: PrismaService) { }

	async findOne(where: Prisma.CredentialsWhereUniqueInput): Promise<CredentialsEntity | null> {
		return await this.prisma.credentials.findFirst({ where });
	}

	create(data: Prisma.CredentialsCreateInput): Promise<Credentials> {		
		const salt = bcrypt.genSaltSync(Number(process.env.SALT_ROUNDS));
		const password = bcrypt.hashSync(data.password, salt);
		return this.prisma.credentials.create({
			data: {
				username: data.username,
				password
			},
		});
	}

	
	// async update(params: {
	// 	where: Prisma.CredentialsWhereUniqueInput;
	// 	data: Prisma.CredentialsUpdateInput;
	// }): Promise<Credentials> {
	// 	const { data, where } = params;
	// 	return this.prisma.credentials.update({
	// 		data,
	// 		where,
	// 	});
	// }

	async delete(where: Prisma.CredentialsWhereUniqueInput): Promise<Credentials> {
		return this.prisma.credentials.delete({
			where,
		});
	}
}
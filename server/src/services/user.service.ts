import { Injectable, NotFoundException } from "@nestjs/common";
import { User } from "@prisma/client";
import { CreateUserDTO } from "src/dto/user/create";
import { PrismaService } from "src/prisma/prisma.service";

@Injectable()
export class UserService {
	constructor(private prisma: PrismaService) { }
	
	async getByUsername(username: string): Promise<User> {
		const response = await this.prisma.user.findFirst({
			where: {
				credentials: {
					username
				}
			}
		});

		if (!response) 
			throw new NotFoundException('Пользователь не найден!');

		return response;
	}

	async create(data: CreateUserDTO): Promise<User> {
		return await this.prisma.user.create({ data });
	}
}

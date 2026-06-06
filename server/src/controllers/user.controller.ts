import {
	Controller,
	Get,
	Param,
	Post,
} from '@nestjs/common';
import { UserService } from '../services/user.service';
import { ApiBearerAuth, ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
// import { AuthGuard } from '../guards/auth.guard';
import { UserDTO } from 'src/dto/user';
import { CreateUserDTO } from 'src/dto/user/create';

@ApiBearerAuth()
@ApiTags('User')
@Controller()
export class UserController {
	constructor(private readonly userService: UserService,) { }
	// @UseGuards(AuthGuard)
	@Get('/:username')
	@ApiOperation({ summary: 'Выборка пользователя из таблицы User по логину' })
	@ApiResponse({
		status: 200,
		description: 'The found record',
		type: UserDTO
	})
	async getByUsername(@Param('username') username: string): Promise<UserDTO> {
		return await this.userService.getByUsername(username);
	}

	@Post('/create')
	@ApiOperation({ summary: 'Создание пользователя' })
	@ApiResponse({
		status: 201,
		description: 'The record has been successfully created.',
		type: CreateUserDTO
	})
	async create(data: CreateUserDTO): Promise<UserDTO> {
		return await this.userService.create(data);
	}
}
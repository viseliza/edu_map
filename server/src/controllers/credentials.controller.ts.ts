import {
    Controller,
    Get,
    Param,
    Post,
    Body,
    Res,
    NotFoundException,
} from '@nestjs/common';
import type {
    Response
} from 'express';
import { 
    ApiBearerAuth, 
    ApiOperation, 
    ApiResponse, 
    ApiTags 
} from '@nestjs/swagger';
import { JwtService } from '@nestjs/jwt';
import { CredentialsService } from '../services/credentials.service';
import { UserService } from '../services/user.service';
// import { UseGuards } from '@nestjs/common';
// import { AuthGuard } from '../guards/auth.guard';
import { UserCreatorStrategy } from '../utils/UserCreatorStrategy';
import { Authorization, UserHelper } from '../utils';
import { IUser } from 'src/types';
import { AuthResponseDto, CredentialsDto } from 'src/dto';
import { Role } from '@prisma/client';
import { UserNovSUEntity } from 'src/entities';


@ApiBearerAuth()
@ApiTags('Credentials')
@Controller()
export class CredentialsController {
    constructor(
        private readonly credentialsService: CredentialsService,
        private readonly userCreator: UserCreatorStrategy,
        private readonly userService: UserService,
        private readonly JwtService: JwtService,
    ) { }

    // @UseGuards(AuthGuard)
    @Get('/:username')
    @ApiOperation({ summary: 'Выборка пользователя из таблицы User по логину' })
    @ApiResponse({
        status: 200,
        description: 'The found record',
        type: CredentialsDto
    })
    async get(@Param('username') username: string): Promise<CredentialsDto> {
        const credentials = await this.credentialsService.findOne({ username });

        if (!credentials) {
            throw new NotFoundException({
                message: 'Пользователь не найден',
                code: 'user_not_found'
            });
        }

        return credentials;
    }

    // Authorization
    @Post('/auth')
    @ApiOperation({ summary: 'Авторизация пользователя через аккаунт NovSU' })
    @ApiResponse({ status: 403, description: 'Forbidden.' })
    @ApiTags('Authorization')
    async authorize (
        @Res({ passthrough: true }) response: Response,
        @Body() data: CredentialsDto
    ): Promise<AuthResponseDto> {
        let user: UserNovSUEntity = await new Authorization(data).auth();
        let credentials = await this.credentialsService.findOne({ username: data.username });
        let role = 'STUDENT' as Role;

        // Создание юзера и профиля
        if (!credentials) {
            /** Класс помощник для получения данных о пользователе */
            const userHelper = new UserHelper(data.username);
            const groupName = await userHelper.studentGroupParsing();
            role = await userHelper.teacherRoleParsing() as Role || role;
            
            // Creating new user based on NovSU profile data
            credentials = await this.credentialsService.create(data);
            user['group_name'] = groupName;
            user['credentials_id'] = credentials.id;
            user['role'] = role;
            
            if (data.username == 'Administrator') {
                user = this.userCreator.getInfo(user as IUser, 'admin');
            } else if (!groupName) {
                user = this.userCreator.getInfo(user as IUser, 'teacher');
            } else {
                user = this.userCreator.getInfo(user as IUser, 'student');
            }
        } else {
            user = await this.userService.getByUsername(data.username);
        }

        return {
            access_token: await this.JwtService.signAsync({
                user_id: credentials.id,
                username: credentials.username
            }),
        }
    }
    
    // @UseGuards(AuthGuard)
    // @ApiOperation({ summary: 'Обновление данных пользователя из таблицы User по логину' })
    // @Patch('/:username')
    // update(
    //     @Param('username') username: string,
    //     @Body() updateLinkDto: Prisma.UserUpdateInput,
    // ) {
    //     return this.credentialsService.update({
    //         where: { username },
    //         data: updateLinkDto,
    //     });
    // }
}
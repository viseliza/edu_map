import {
    Controller,
    Get,
    Param,
    Patch,
    Post,
    Body,
    Delete,
} from '@nestjs/common';
import { GroupService } from '../services/group.service';
import {
    Prisma,
    Group,
} from '@prisma/client';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
// import { NewGroupDTO } from 'src/dto/create.group.dto';


@ApiBearerAuth()
@ApiTags('Group')
@Controller()
export class GroupController {
    constructor(
        private readonly groupService: GroupService,
    ) { }

    @ApiOperation({ summary: 'Выборка группы профиля с заданым credentials' })
    @Get('/:credentials')
    async get(@Param('credentials') credentials: string) {
        return this.groupService.getByUserId( Number(credentials) );
    }

    @ApiOperation({ summary: 'Выборка всех групп в базе' })
    @Get('/')
    async getAllGroups() {
        return await this.groupService.findAll();
    }

    // @ApiOperation({ summary: 'Добавление записи в таблицу групп' })
    // @Post('/group')
    // async create(
    //     @Body()
    //     data: NewGroupDTO,
    // ): Promise<Group> {
    //     let group = await this.groupService.get({ name: data.name })

    //     if (!group) group = await this.groupService.create(data);

    //     return group;
    // }

    @ApiOperation({ summary: 'Добавление нескольких записей в таблицу групп' })
    @Post('/groups')
    async push(
        @Body()
        data: Prisma.GroupCreateManyInput[]
    ) {
        return await this.groupService.createMany(data)
    }

    @ApiOperation({ summary: 'Обновление данных группы по названию' })
    @Patch('/:name')
    update(
        @Param('name') name: string,
        @Body() updateLinkDto: Prisma.GroupUpdateInput,
    ) {
        return this.groupService.update({
            where: { name },
            data: updateLinkDto,
        });
    }

    @ApiOperation({ summary: 'Удаление всех записей групп из таблицы' })
    @Delete('/')
    delete() {
        return this.groupService.delete();
    }
}
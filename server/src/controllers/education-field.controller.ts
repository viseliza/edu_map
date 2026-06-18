import { Body, Controller, Get, Param, Patch, Post } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { Prisma } from "@prisma/client";
import { EducationFieldService } from "src/services/education-field.service";

@ApiBearerAuth()
@ApiTags('Education Field')
@Controller()
export class EducationFieldController {
    constructor(
        private readonly educationFieldService: EducationFieldService,
    ) { }

    @ApiOperation({ summary: 'Заполнение учебных специальностей по группам' })
    @Post('/education-fields')
    async create() {
        return await this.educationFieldService.createMany();
    }

    @ApiOperation({ summary: 'Получение учебной специальности' })
    @Get('/')
    async get() {
        return await this.educationFieldService.get();
    }

    @ApiOperation({ summary: 'Получение всех учебных специальностей' })
    @Get('/all')
    async getAll() {
        return await this.educationFieldService.getAll();
    }
    
    @ApiOperation({ summary: 'Обновление учебных специальностей' })
    @Patch('/:id')
    async update(
        @Body() data: Prisma.EducationFieldUpdateInput,
        @Param('id') id: number
    ) {
        return await this.educationFieldService.update(data, { id });
    }
}
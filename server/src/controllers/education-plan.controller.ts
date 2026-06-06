import { Body, Controller, Get, Patch, Post } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiResponse, ApiTags } from "@nestjs/swagger";
import { CreatePlanDTO } from "src/dto";
import { EducationPlanService } from "src/services/education-plan.service";

@ApiBearerAuth()
@ApiTags('Education Plan')
@Controller()
export class EducationPlanController {
    constructor(
        private readonly educationPlanService: EducationPlanService,
    ) { }
    
    @Post('/')
    @ApiOperation({ summary: 'Создание учебного плана' })
    @ApiResponse({
        status: 201,
        description: 'The record has been successfully created.',
        type: CreatePlanDTO
    })
    async create(@Body() data: CreatePlanDTO) {
        return await this.educationPlanService.create(data);
    }

    @ApiOperation({ summary: 'Получение учебного плана' })
    @Get('/')
    async get() {
        return await this.educationPlanService.get(12);
    }

    @ApiOperation({ summary: 'Получение учебного плана' })
    @Get('/fill')
    async fill() {
        return await this.educationPlanService.fill();
    }
}
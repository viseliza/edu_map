import { Body, Controller, Get, Param, Patch, Post } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { Prisma } from "@prisma/client";
import { EducationSubjectService } from "src/services/education-subject.service";

@ApiBearerAuth()
@ApiTags('Education Subject')
@Controller()
export class EducationSubjectController {
    constructor(
        private readonly educationSubjectService: EducationSubjectService,
    ) { }
    
    @ApiOperation({ summary: 'Редактирование предмета' })
    @Patch('/:name/:education_plan_id')
    async update(
        @Body() data: Prisma.EducationFieldUpdateInput,
        @Param('name') name: string,
        @Param('education_plan_id') education_plan_id: number
    ) {
        return await this.educationSubjectService.update(
            name, 
            education_plan_id, 
            data
        );
    }
}
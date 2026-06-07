import {
    Get,
    Controller,
} from '@nestjs/common';
import { 
    ApiBearerAuth, 
    ApiOperation, 
    ApiTags 
} from '@nestjs/swagger';
import { EducationFieldService } from 'src/services/education-field.service';
import { EducationPlanService } from 'src/services/education-plan.service';
import { GroupService } from 'src/services/group.service';
import { GroupManager } from 'src/utils';

@ApiBearerAuth()
@ApiTags('Admin')
@Controller()
export class AdminController {
    constructor(
        private readonly groupService: GroupService,
        private readonly educationFieldService: EducationFieldService,
        private readonly educationPlanService: EducationPlanService,
    ) { }

    @ApiOperation({ summary: 'Заполнение данных' })
    @Get('fill/')
    async pushGroups() {
        await this.groupService.delete();        
        const groups = await GroupManager.pushGroups();
        console.log(`[GROUP] ${groups.length} groups added`);
        const groupsResponse = await this.groupService.createMany(groups);

        const educationFields = await this.educationFieldService.createMany();
        console.log(`[FIELDS] ${educationFields} education fields added`);

        const educationPlan = await this.educationPlanService.fill();
        console.log(`[PLAN] ${educationPlan.length} subjects added`);

        return {
            groups: groupsResponse,
            educationFields,
            educationPlan
        }
    }
}
import { Module } from '@nestjs/common';
import { AdminController } from '../controllers/admin.controller';
import { PrismaService } from 'src/prisma/prisma.service';
import { GroupService } from 'src/services/group.service';
import { EducationFieldService } from 'src/services/education-field.service';
import { EducationPlanService } from 'src/services/education-plan.service';
import { EducationSubjectService } from 'src/services/education-subject.service';

@Module({
    controllers: [AdminController],
    providers: [
        PrismaService, 
        GroupService,
        EducationFieldService,
        EducationPlanService,
        EducationSubjectService
    ],
})
export class AdminModule { }
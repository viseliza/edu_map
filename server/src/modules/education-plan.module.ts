import { Module } from '@nestjs/common';
import { EducationPlanController } from 'src/controllers/education-plan.controller';
import { PrismaModule } from 'src/prisma/prisma.module';
import { PrismaService } from 'src/prisma/prisma.service';
import { EducationPlanService } from 'src/services/education-plan.service';
import { EducationSubjectService } from 'src/services/education-subject.service';

@Module({
    imports: [PrismaModule],
    controllers: [EducationPlanController],
    providers: [
        PrismaService,
        EducationPlanService,
        EducationSubjectService
    ],
    
})
export class EducationPlanModule { }
import { Module } from '@nestjs/common';
import { EducationSubjectController } from 'src/controllers/education-subject.controller';
import { PrismaModule } from 'src/prisma/prisma.module';
import { PrismaService } from 'src/prisma/prisma.service';
import { EducationSubjectService } from 'src/services/education-subject.service';

@Module({
    imports: [ PrismaModule ],
    controllers: [ EducationSubjectController ],
    providers: [
        PrismaService,
        EducationSubjectService
    ],
    
})
export class EducationSubjectModule { }
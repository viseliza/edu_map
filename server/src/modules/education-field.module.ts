import { Module } from '@nestjs/common';
import { EducationFieldController } from '../controllers/education-field.controller';
import { EducationFieldService } from '../services/education-field.service';
import { PrismaModule } from 'src/prisma/prisma.module';
import { PrismaService } from 'src/prisma/prisma.service';
import { GroupService } from 'src/services/group.service';

@Module({
    imports: [PrismaModule],
    controllers: [EducationFieldController],
    providers: [
        GroupService,
        PrismaService,
        EducationFieldService
    ],
    
})
export class EducationFieldModule { }
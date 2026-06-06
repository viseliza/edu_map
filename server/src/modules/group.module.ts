import { Module } from '@nestjs/common';
import { GroupController } from '../controllers/group.controller';
import { GroupService } from '../services/group.service';
import { PrismaModule } from 'src/prisma/prisma.module';
import { PrismaService } from 'src/prisma/prisma.service';

@Module({
    imports: [PrismaModule],
    controllers: [GroupController],
    providers: [
        PrismaService,
        GroupService
    ],
    
})
export class GroupModule { }
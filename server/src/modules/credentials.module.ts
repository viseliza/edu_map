import { Module } from '@nestjs/common';
import { UserService } from '../services/user.service';
import { PrismaService } from '../prisma/prisma.service';
import { GroupService } from 'src/services/group.service';
import { 
    UserAdmin, 
    UserCreatorStrategy, 
    UserStudent, 
    UserTeacher 
} from '../utils';
import { PrismaModule } from 'src/prisma/prisma.module';
import { CredentialsController } from 'src/controllers/credentials.controller.ts';
import { CredentialsService } from 'src/services/credentials.service';

@Module({
    imports: [PrismaModule],
    controllers: [CredentialsController],
    providers: [
        CredentialsService,
        UserService,
        UserCreatorStrategy,
        UserAdmin,
        UserStudent,
        UserTeacher,
        GroupService,
        PrismaService
    ],
    
})
export class CredentialsModule { }
import { Module } from '@nestjs/common';
import { UserController } from '../controllers/user.controller';
import { UserService } from '../services/user.service';
import { PrismaModule } from 'src/prisma/prisma.module';
import { PrismaService } from 'src/prisma/prisma.service';

@Module({
    imports: [PrismaModule],
    controllers: [UserController],
    providers: [
        PrismaService,
        UserService
    ],
    
})
export class UserModule { }
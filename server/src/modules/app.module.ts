import { Module } from '@nestjs/common';
import { UserModule } from './user.module';
import { RouterModule, Routes } from '@nestjs/core';
import { JwtModule } from '@nestjs/jwt';
import { AdminModule } from './admin.module';
import { CredentialsModule } from './credentials.module';
import { GroupModule } from './group.module';
import { EducationFieldModule } from './education-field.module';
import { EducationSubjectModule } from './education-subject.module';
import { EducationPlanModule } from './education-plan.module';

const routes: Routes = [{
    path: 'api',
    children: [
        { 'path': 'user', module: UserModule },
        { 'path': 'admin', module: AdminModule },
        { 'path': 'group', module: GroupModule },
        { 'path': 'credentials', module: CredentialsModule },
        { 'path': 'education-plan', module: EducationPlanModule },
        { 'path': 'education-field', module: EducationFieldModule },
        { 'path': 'education-subject', module: EducationSubjectModule },
    ]
}];

@Module({
    imports: [
        JwtModule.register({
            global: true,
            secret: process.env.SERVER_JWT_SECRET,
            signOptions: { expiresIn: parseInt(process.env.SERVER_JWT_EXPIRES || '3600') },
        }),
        UserModule,
        AdminModule,
        GroupModule,
        CredentialsModule,
        EducationPlanModule,
        EducationFieldModule,
        EducationSubjectModule,
        RouterModule.register(routes),
    ]
})
export class AppModule { }
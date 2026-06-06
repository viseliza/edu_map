import { UnauthorizedException } from "@nestjs/common";
import { CredentialsEntity, UserNovSUEntity } from "src/entities";
import * as bcrypt from 'bcrypt';
import axios from "axios";

export class Authorization {
    private data: CredentialsEntity;
        
    constructor(data: CredentialsEntity) {
        this.data = data;
    }

    async auth(): Promise<UserNovSUEntity> {
        if (this.data.username === process.env.ADMIN_USERNAME) {
            return await this.authAdmin();
        }

        return await this.authUser();
    }

    async authAdmin(): Promise<UserNovSUEntity> {
        if (this.data.username !== process.env.ADMIN_USERNAME || !await bcrypt.compare(this.data.password, process.env.ADMIN_PASSWORD)) {
            throw new UnauthorizedException({
                message: 'Неверный логин или пароль',
                code: 'invalid_credentials'
            });
        }
        
        return {
            first_name: "Владимир",
            last_name: "Шульцев",
            father_name: "Александрович"
        } as UserNovSUEntity;
    }

    async authUser(): Promise<UserNovSUEntity> {
        try {
            const response = await axios({
                method: 'POST',
                url: 'https://portal.novsu.ru/s.login/',
                data: new URLSearchParams({
                    json: '1',
                    uid: this.data.username,
                    password: this.data.password,
                }).toString()
            });

            if (response.data?.user)
                return this.formatData(response.data.user);    
        } catch (error) {
            console.log(error);
        }

        throw new UnauthorizedException({
            message: 'Неверный логин или пароль',
            code: 'invalid_credentials'
        });
    }

    formatData(data: any): UserNovSUEntity {
        return {
            first_name: data.firstName,
            last_name: data.lastName,
            father_name: data.midName,
            email: data.email,
        }
    }

}

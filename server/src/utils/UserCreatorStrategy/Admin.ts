import { Injectable } from "@nestjs/common";
import { IUser, IUserCreator } from "../../types";
import { UserService } from "src/services/user.service";

@Injectable()
export class UserAdmin implements IUserCreator {
    constructor(
        private readonly userService: UserService
    ) { }

    async getInfo(data: IUser): Promise<any> {
        const payload = {
            first_name: data.first_name,
            last_name: data.last_name,
            father_name: data.father_name,
            role: "ADMIN" as any,
            credentials_id: data.credentials_id
        };


        return await this.userService.create(payload);
    }
}
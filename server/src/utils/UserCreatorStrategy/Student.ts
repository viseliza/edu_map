import { Injectable } from "@nestjs/common";
import { IUser, IUserCreator } from "../../types";
import { UserService } from "src/services/user.service";
import { GroupService } from "src/services/group.service";

@Injectable()
export class UserStudent implements IUserCreator {
    constructor(
        private readonly userService: UserService,
        private readonly groupService: GroupService
    ) { }

    async getInfo(data: IUser): Promise<any> {        
        const group = await this.groupService.get({
            name: data.group_name
        });

        return await this.userService.create({
            first_name: data.first_name,
            last_name: data.last_name,
            father_name: data.father_name,
            role: data.role,
            credentials_id: data.credentials_id,
            group_id: group.id,
            email: data.email
        });
    }
}
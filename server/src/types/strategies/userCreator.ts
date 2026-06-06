import { Strategy } from ".";
import { IUser } from "../user";

export interface IUserCreator {
    getInfo(userData: IUser, name?: string): any;
}

export interface ICreateUserStrategy extends Strategy<IUserCreator>, IUserCreator {  }
import { Role } from "@prisma/client";

export type authCreditials = {
    username: string;
    password: string;
};

export type authResponse = {
    access_token: string;
};

export interface IUser extends Record<string, unknown> {
    id?: number;
    email?: string;
    first_name: string;
    last_name: string;
    father_name: string;
    role: Role;
    credentials_id: number | null;
};
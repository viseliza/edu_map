import { ApiProperty } from "@nestjs/swagger";
import { Role } from "@prisma/client";

export class CreateUserDTO {    
    @ApiProperty({ required: false })
    email?: string | null;

    @ApiProperty()
    first_name!: string;

    @ApiProperty()
    last_name!: string;

    @ApiProperty({ required: false })
    father_name!: string | null;

    @ApiProperty({ enum: Role })
    role!: Role;

    @ApiProperty({ required: false })
    credentials_id!: number | null;

    @ApiProperty({ required: false })
    group_id?: number | null;
}
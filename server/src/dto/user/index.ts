import { ApiProperty } from '@nestjs/swagger';
import { Role } from '@prisma/client';

export * from './get-username';

export class UserDTO {  
    @ApiProperty()
    id!: number;

    @ApiProperty({ required: false })
    email?: string | null;

    @ApiProperty({ required: false })
    first_name?: string | null;

    @ApiProperty({ required: false })
    last_name?: string | null;

    @ApiProperty({ required: false })
    father_name?: string | null;

    @ApiProperty({ enum: Role })
    role!: Role;

    @ApiProperty({ required: false })
    credentials_id?: number | null;

    @ApiProperty({ required: false })
    group_id?: number | null;
}
import { ApiProperty } from "@nestjs/swagger";

export class GetUsernameDTO {
    @ApiProperty()
    username!: string;
}
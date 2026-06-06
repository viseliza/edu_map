import { ApiProperty } from "@nestjs/swagger";

export class CreatePlanDTO { 
    @ApiProperty()
    year!: number;

    @ApiProperty()
    education_field_id!: number;
}
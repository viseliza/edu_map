import {
    Entity,
    Column,
} from 'typeorm';

@Entity('education_field')
export class EducationFieldEntity {
    @Column({ unique: true })
    speciality!: string;

    @Column()
    profile!: string;
}
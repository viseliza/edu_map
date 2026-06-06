import {
    Entity,
    Column,
} from 'typeorm';
@Entity('users')
export class UserNovSUEntity {
    @Column()
    first_name!: string;

    @Column()
    last_name!: string;

    @Column({ nullable: true })
    father_name!: string | null;
    
    @Column({ nullable: true, unique: true })
    email?: string | null;
}
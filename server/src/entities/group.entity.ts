import { Entity, PrimaryGeneratedColumn, Column, OneToMany } from 'typeorm';
import { UserEntity } from './user.entity';

@Entity('groups')
export class GroupEntity {
    @PrimaryGeneratedColumn()
    id!: number;

    @Column({ unique: true })
    name!: string;

    @Column({ nullable: true })
    course!: number | null;

    @Column({ nullable: true })
    education_field_id!: number | null;

    @OneToMany(() => UserEntity, (user) => user.group)
    users?: UserEntity[];
}
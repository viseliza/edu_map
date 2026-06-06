import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    ManyToOne,
    OneToOne,
    JoinColumn,
} from 'typeorm';
import { CredentialsEntity, GroupEntity } from '.';

@Entity('users')
export class UserEntity {
    @PrimaryGeneratedColumn()
    id!: number;

    @Column({ nullable: true, unique: true })
    email!: string | null;

    @Column()
    first_name!: string;

    @Column()
    last_name!: string;

    @Column({ nullable: true })
    father_name!: string | null;

    @Column({
        type: 'enum',
        enum: ['STUDENT', 'TEACHER', 'ADMIN'],
        default: 'STUDENT',
    })
    role!: 'STUDENT' | 'TEACHER' | 'ADMIN';

    // Связь один к одному с Credentials (владеющая сторона)
    @OneToOne(() => CredentialsEntity, (credentials) => credentials.user, {
        nullable: true,
    })
    @JoinColumn({ name: 'credentials_id' })
    credentials!: CredentialsEntity;

    @Column({ nullable: true, unique: true })
    credentials_id!: number | null;

    // Связь многие к одному с Group (сторонняя сторона)
    @ManyToOne(() => GroupEntity, (group) => group.users, {
        nullable: true,
    })
    @JoinColumn({ name: 'group_id' })
    group!: GroupEntity;

    @Column({ nullable: true })
    group_id!: number | null;
}
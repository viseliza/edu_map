import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    OneToOne,
} from 'typeorm';
import { UserEntity } from './user.entity';

@Entity('credentials')
export class CredentialsEntity {
    @PrimaryGeneratedColumn()
    id?: number;

    @Column({ unique: true })
    username!: string;

    @Column()
    password!: string;

    @OneToOne(() => UserEntity, (user) => user.credentials)
    user?: UserEntity;
}
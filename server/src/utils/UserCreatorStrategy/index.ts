import { Injectable } from '@nestjs/common';
import { ICreateUserStrategy, IUser, IUserCreator } from '../../types';
import { BaseStrategy } from '../BaseStrategy';
import { UserAdmin } from './Admin';
import { UserStudent } from './Student';
import { UserTeacher } from './Teacher';

export * from './Admin';
export * from './Student';
export * from './Teacher';

@Injectable()
/** Класс для работы с сервисами */
export class UserCreatorStrategy extends BaseStrategy<IUserCreator> implements ICreateUserStrategy {
    constructor(
        private readonly admin: UserAdmin,
        private readonly student: UserStudent,
        private readonly teacher: UserTeacher
    ) {
        super();

        this.use('admin', admin);
        this.use('student', student);
        this.use('teacher', teacher);
    }

    /***********************/
    /* * * * I N F O * * * */
    /***********************/

    getInfo(data: IUser, name: string): any {
        if (!this.strategies[name]) {
            throw new Error('Такой стратегии не существует');
        }
        return this.strategies[name].getInfo(data);
    }
}
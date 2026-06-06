import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { PrismaClient } from '@prisma/client';

@Injectable()
export class PrismaService extends PrismaClient 
    implements OnModuleInit, OnModuleDestroy {
        
    /** Функция для подключения к базе данных при инициализации модуля.
     * В случае неудачного подключения, будет предпринято до 5 попыток с задержкой в 3 секунды между ними.
     */
    async onModuleInit() {
        let retries = 5;

        while (retries) {
            try {
                await this.$connect();
                break;
            } catch (e) {
                console.log('Prisma connect failed, retrying...', retries);
                retries--;
                await new Promise(res => setTimeout(res, 3000));
            }
        }
    }

    /** Функция для выполнения запросов с автоматическими повторными попытками при возникновении ошибки P2024 (RecordNotFound)
     * 
     * @param fn - функция, которая выполняет запрос к базе данных и возвращает Promise
     * @param retries - количество попыток повторного выполнения запроса (по умолчанию 5)
     * @param delay - задержка между попытками в миллисекундах (по умолчанию 500, будет увеличиваться экспоненциально)
     * @returns - результат выполнения запроса, если он успешен, или выбрасывает ошибку, если все попытки исчерпаны или возникает другая ошибка
     */
    async withRetry<T>(
        fn: () => Promise<T>,
        retries = 5,
        delay = 500
    ): Promise<T> {
        try {
            return await fn();
        } catch (error: any) {
            if (retries === 0) throw error;

            if (error.code === 'P2024') {
            console.log(`Retrying query... attempts left: ${retries}`);
            await new Promise(res => setTimeout(res, delay));
            return this.withRetry(fn, retries - 1, delay * 2);
            }

            throw error;
        }
    }

    async onModuleDestroy() {
        await this.$disconnect();
    }
}
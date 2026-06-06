// utils/request.ts
import axios, { AxiosResponse, AxiosRequestConfig } from 'axios';
import { getRandomHeaders } from './UserAgent';

export async function requestWithRetry(
    url: string, 
    args: AxiosRequestConfig = {},
    retries = 3,
    delay = 1000
): Promise<AxiosResponse> {  // ← Явный возврат типа
    for (let attempt = 1; attempt <= retries; attempt++) {
        try {
            const response = await axios.get(url, {
                timeout: 10000,
                ...args,
                headers: {
                    ...getRandomHeaders(args.headers?.['Referer']),
                    ...args.headers,
                },
            });
            
            return response; // ← Всегда возвращает AxiosResponse
        } catch (error: any) {
            // ... логика повторных попыток ...
            if (attempt === retries) {
                throw error; // ← Если все попытки исчерпаны — выбрасываем ошибку
            }
            // ...
        }
    }
    
    // Эта строка никогда не выполнится, но TS может ругаться без неё
    throw new Error('Unexpected end of requestWithRetry');
}
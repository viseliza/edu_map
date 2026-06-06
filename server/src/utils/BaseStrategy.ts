import { Strategy } from "../types";

export class BaseStrategy<T> implements Strategy<T> {
    /** Список стратегий сервисов */
    public strategies: Record<string, T> = {};

    /** Добавление сигнатуры в стратегию 
     * 
     * @param code - кодовое название сервиса
     * @param strategy - класс стратегии сервиса
     */
    use(code: string, strategy: T) {
        this.strategies[code] = strategy;
    }
}
export * from './userCreator';

export interface Strategy<T> {
    strategies: Record<string, T>;
    use(code: string, strategy: T): void;
};
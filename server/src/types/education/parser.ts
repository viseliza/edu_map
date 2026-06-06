export type GetURLArguments<T extends boolean = false> = {
    offErrors?: T;
    callback?: (row: string, href: string) => any;
    matchReplace?: () => string;
};
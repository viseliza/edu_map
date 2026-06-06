export const USER_AGENTS = [
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
];

export function getRandomHeaders(referer?: string) {
    const ua = USER_AGENTS[Math.floor(Math.random() * USER_AGENTS.length)];
    const isChrome = ua.includes('Chrome');
    
    return {
        'User-Agent': ua,
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
        'Accept-Language': 'ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7',
        'Accept-Encoding': 'gzip, deflate, br',
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1',
        'Sec-Fetch-Dest': isChrome ? 'document' : undefined,
        'Sec-Fetch-Mode': isChrome ? 'navigate' : undefined,
        'Sec-Fetch-Site': isChrome ? 'none' : undefined,
        'Sec-Fetch-User': isChrome ? '?1' : undefined,
        'Cache-Control': 'max-age=0',
        'TE': 'trailers', // Для Firefox
        ...(referer && { 'Referer': referer }),
    };
}
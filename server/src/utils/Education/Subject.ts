import axios from 'axios';
import { execFile, execFileSync } from 'child_process';
import { existsSync, writeFileSync } from 'fs';
import path from 'path';
import { promisify } from 'util';
import { requestWithRetry } from '../Request';

export class Subject {
    private href: string;
    private name: string;

    constructor(name: string, href: string) {
        this.name = name;
        this.href = href;
    }

    async get() {
        const response = await requestWithRetry(this.href, {
            responseType: 'arraybuffer'
        });

        const filePath = `./files/${this.name}.docx`;
        
        if (!existsSync(filePath)) 
            writeFileSync(filePath, response.data);
        
        this.convert(filePath);
        return await this.parseDoc(filePath.replace('files', 'tmp'));
    }

    convert(file: string) {
        if (!existsSync('./tmp/' + file)) {
            execFileSync('soffice', [
                '--headless',
                '--convert-to',
                'docx',
                '--outdir',
                './tmp',
                file,
            ]);
        }
    }

    async parseDoc(filePath: string) {
        const scriptPath = path.resolve(__dirname.split('dist')[0], 'src/scripts/parse.py');

        const execFileAsync = promisify(execFile);

        const { stdout } = await execFileAsync('python3', [
            scriptPath,
            filePath.replace('files', 'tmp'),
        ]);

        return JSON.parse(stdout);
    }
}

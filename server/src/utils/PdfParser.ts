import axios from 'axios';
import fs from 'fs';
import { fromPath } from 'pdf2pic';
import Tesseract from 'tesseract.js';
import sharp from 'sharp';

export class PdfParser {
    async parsePdf(url: string) {
        console.time()
        const res = await axios.get(url, {
            responseType: 'arraybuffer',
        });

        fs.writeFileSync('/tmp/file.pdf', res.data);

        const converter = fromPath('/tmp/file.pdf', {
            density: 1000, // было 300 → делай 400-600
            saveFilename: 'page',
            savePath: '/tmp',
            format: 'png',
            width: 2480,   // 👈 добавь
            height: 3508,  // 👈 A4 в 300+ DPI
        });

        const images = await converter.bulk(-1);

        let text = '';

        for (const img of images) {
            const processedPath = `/tmp/processed-${img.page}.png`;

            // 🔥 улучшение картинки
            await sharp(img.path as string)
                .grayscale()
                .normalize()
                .sharpen()
                .threshold(150)
                .toFile(processedPath);

            const result = await Tesseract.recognize(
                processedPath,
                'rus+eng'
            );

            text += result.data.text + '\n';
        }

        console.timeEnd()
        return text;
    }
}
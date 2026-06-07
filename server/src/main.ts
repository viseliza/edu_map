import { NestFactory } from '@nestjs/core';
import { AppModule } from './modules/app.module';
import { NestExpressApplication } from '@nestjs/platform-express';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
// import * as helpers from '../public/scripts/helpers';
// import * as exphbs from 'express-handlebars';
// import * as cookieParser from 'cookie-parser';
import cookieParser from 'cookie-parser';
import * as dotenv from 'dotenv';
// import { CronStart } from './cron/cron';
import { join } from 'path';
dotenv.config();

async function bootstrap() {
	const app = await NestFactory.create<NestExpressApplication>(
		AppModule, 
		{ cors: true }
	);
	app.use(cookieParser());
	app.enableCors({
		origin: [
		'http://localhost:18001',
		'http://10.0.2.2:18001',
		/http:\/\/192\.168\.\d+\.\d+:18001/, 
		],
		methods: 'GET,POST,PUT,DELETE,PATCH',
		credentials: true,
	});

	const config = new DocumentBuilder()
		.setTitle('EduMap')
		.setDescription('Сервер для структурированного представления учебной программы направления подготовки студентов колледжа')
		.setVersion('1.0')
		.addTag('Authorization')
		.build();
	const document = SwaggerModule.createDocument(app, config);
	SwaggerModule.setup('api', app, document);

	app.setBaseViewsDir(join(__dirname, '..', 'views'));

	// const hbs = exphbs.create({
	// 	defaultLayout: 'main', 
	// 	layoutsDir: join(__dirname, '../views', 'layouts'),
	// 	helpers
	// });


	// app.engine('handlebars', hbs.engine);
	// app.setViewEngine('handlebars');

	// const cron = new CronStart();
	await app.listen(18001);
}
bootstrap();
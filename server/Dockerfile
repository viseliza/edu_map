# Используем образ, где Python уже настроен
FROM python:3.11-slim AS python-base

# Установка Node.js поверх Python
RUN apt-get update && apt-get install -y \
    curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Установка Python-пакетов (в этом образе можно без флага)
RUN pip install --no-cache-dir python-docx lxml

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npx prisma generate --schema=./src/schemas/postgresql.prisma

EXPOSE 18001
CMD ["sh", "-c", "npm run prisma:push && npm run prisma:dev && npm run start:dev"]
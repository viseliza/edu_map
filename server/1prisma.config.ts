import "dotenv/config";
import { defineConfig, env } from "prisma/config";

export default defineConfig({
    schema: "src/schemas/postgresql.prisma",
    migrations: {
        path: "prisma/migrations",
    },
    datasource: {
        url: env("DIRECT_URL"),
    },
});
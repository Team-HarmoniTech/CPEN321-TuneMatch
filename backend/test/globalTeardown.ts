import { server } from "../src/index";

export default async () => {
    server.close((err) => {
        process.exit(err ? 1 : 0);
    });
};
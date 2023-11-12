import { server } from "@src/index"; // Assuming your Express app instance is exported from index.ts
import request from 'superwstest';

describe("GET /me", () => {
    test("name", async () => {
        const messages: string[] = [];
        const response = await request(server).get("/users/test");
        expect(response.body).toBe("");
    });
});
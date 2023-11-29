import { server } from "@src/index";
import request from 'superwstest';

describe("Ban user", () => {
    // Input: Valid user ID
    // Expected status code: 200
    // Expected behavior: User is banned
    // Expected output: User with ID ${userId} has been banned.
    // ChatGPT usage: None
    test("Valid user ID", async () => {
        const res = await request(server)
            .put(`/reports/ban/testUser1`)

        expect(res.statusCode).toBe(200);
        expect(res.body).toHaveProperty('message');
        expect(res.body).toEqual({ message: 'testUsername1 is banned: true.' });
    });
});
import { server } from "@src/index";
import request from 'superwstest';
import { originalDate } from "../../globalSetup";

describe("Get top matches", () => {
    // Input: user-id is an existing user
    // Expected status code: 200
    // Expected behavior: Return list of matched users
    // Expected output: {userId, username, profile-pic, match}[]
    // ChatGPT usage: None
    test("Existing user", async () => {
      const res = await request(server)
        .get('/me/matches')
        .set('user-id', 'testUser1');

      expect(res.statusCode).toBe(200);
      expect(Array.isArray(res.body)).toBeTruthy();
      res.body.forEach(match => {
        expect(match).toHaveProperty('userId');
        expect(match).toHaveProperty('username');
        expect(match).toHaveProperty('profilePic');
        expect(match).toHaveProperty('match');
      });
    });

    // Input: user-id doesn’t exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: This executing user does not exist
    // ChatGPT usage: None
    test("Non-existing user", async () => {
      const res = await request(server)
        .get('/me/matches')
        .set('user-id', 'nonExistingUserId');

      expect(res.statusCode).toBe(401);
      expect(res.body).toEqual({ error : "This executing user does not exist" });
    });

    // Input: user-id is an existing user but computationsComputed is false
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: User connections were not computed within 60000 milliseconds
    // ChatGPT usage: None
    test("Timeout", async () => {
      const testConstantDate = new Date('2023-01-01T12:00:00Z');
      global.Date = originalDate;

      const res = await request(server)
        .get('/me/matches')
        .set('user-id', 'testUser2');

      expect(res.statusCode).toBe(400);
      expect(res.body).toEqual({ error : "User connections were not computed within 60000 milliseconds." });

      global.Date = jest.fn().mockImplementation(() => testConstantDate) as any;
      global.Date.now = jest.fn().mockReturnValue(+testConstantDate);
    }, 70000);
  });
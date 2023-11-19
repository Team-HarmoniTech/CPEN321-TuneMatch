import { server } from "@src/index";
import request from 'superwstest';

describe("Search User", () => {
    // Input: user-id is an existing user
    // Expected status code: 200
    // Expected behavior: Return list of users related to search param
    // Expected output: {userId, username, profile-pic, match}[]
    test("Existing user", async () => {
      const res = await request(server)
        .get('/users/search')
        .set('user-id', 'testUser1')
        .query({ q: 'tes' });

      expect(res.statusCode).toBe(200);
      expect(Array.isArray(res.body)).toBeTruthy();
      res.body.forEach(user => {
        expect(user).toHaveProperty('userId');
        expect(user).toHaveProperty('username');
        expect(user).toHaveProperty('profilePic');
        expect(user).toHaveProperty('match');
    });
    });
  
    // Input: q is not included
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: Error JSON indicating which fields were invalid
    test("Missing q", async () => {
      const res = await request(server)
        .get('/users/search')
        .set('user-id', 'testUser1');

      expect(res.statusCode).toBe(400);
      expect(res.body).toEqual({
        errors: [
          {
            type: 'field',
            msg: 'Invalid value',
            path: 'q',
            location: 'query'
          }
        ]
      });
    });

    // Input: user-id doesn’t exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: This executing user does not exist
    test("Non-existing user", async () => {
      const res = await request(server)
        .get('/users/search')
        .set('user-id', 'nonExistingUserId')
        .query({ q: 'tes' });

      expect(res.statusCode).toBe(401);
      expect(res.body).toEqual({ error : "This executing user does not exist" });
    });
  });
import { server } from "@src/index";
import { userMatchingService, userService } from "@src/services";
import request from 'superwstest';

describe("Get match", () => {
    // Input: user-id is an existing user, spotify_id is an existing user
    // Expected status code: 200
    // Expected behavior: Return matched users data
    // Expected output: userId, username, profile-pic, match
    // ChatGPT usage: None
    test("Existing executer and existing searched and match uncomputed", async () => {
      const res = await request(server)
        .get('/me/match/testUser1')
        .set('user-id', 'testUser2');

      expect(res.statusCode).toBe(200);
      expect(res.body).toHaveProperty('userId', 'testUser1');
      expect(res.body).toHaveProperty('username', 'testUsername1');
      expect(res.body).toHaveProperty('profilePic');
      expect(res.body).toHaveProperty('match', 96.45);
    });

    // Input: user-id is an existing user
    // Expected status code: 200
    // Expected behavior: Return matched users data
    // Expected output: userId, username, profile-pic, match
    // ChatGPT usage: None
    test("User is trying to search for match with himself", async () => {
      const res = await request(server)
        .get('/me/match/testUser1')
        .set('user-id', 'testUser1');

      expect(res.statusCode).toBe(200);
      expect(res.body).toHaveProperty('userId', 'testUser1');
      expect(res.body).toHaveProperty('username', 'testUsername1');
      expect(res.body).toHaveProperty('profilePic');
      expect(res.body).not.toHaveProperty('match');
    });

    // Input: user-id is an existing user, spotify_id is an existing user
    // Expected status code: 200
    // Expected behavior: Return matched users data
    // Expected output: userId, username, profile-pic, match
    // ChatGPT usage: None
    test("Existing executer and existing searched and match computed", async () => {
      const executingUser = await userService.getUserBySpotifyId("testUser1");
      const otherUser = await userService.getUserBySpotifyId("testUser2");
      await userMatchingService.getMatch(executingUser.id, otherUser.id);

      const res = await request(server)
        .get('/me/match/testUser1')
        .set('user-id', 'testUser2');

      expect(res.statusCode).toBe(200);
      expect(res.body).toHaveProperty('userId', 'testUser1');
      expect(res.body).toHaveProperty('username', 'testUsername1');
      expect(res.body).toHaveProperty('profilePic');
      expect(res.body).toHaveProperty('match', 96.45);
    });

    // Input: user-id is an existing user, spotify_id doesn't exist
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: User not found
    // ChatGPT usage: None
    test("Existing executer and non existing searched", async () => {
      const res = await request(server)
        .get('/me/match/nonExistingSpotifyId')
        .set('user-id', 'testUser1');

      expect(res.statusCode).toBe(400);
      expect(res.body).toEqual({ error : "User not found." });
    });

    // Input: user-id doesn’t exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: This executing user does not exist
    // ChatGPT usage: None
    test("Non-existing executer", async () => {
      const res = await request(server)
        .get('/me/match/testUser1')
        .set('user-id', 'nonExistingUserId');

      expect(res.statusCode).toBe(401);
      expect(res.body).toEqual({ error : "This executing user does not exist" });
    });
});
import { server } from "@src/index";
import request from 'superwstest';

describe("Get self", () => {
    // Input: user-id is an existing user, full-profile parameter is false/doesn't exist
    // Expected status code: 200
    // Expected behavior: Return current user's data
    // Expected output: userId, username, profile-pic
    test("Existing user", async () => {
        const res = await request(server)
            .get('/me')
            .set('user-id', 'testUser1')
  
        expect(res.statusCode).toBe(200);
        expect(res.body).toHaveProperty('userId');
        expect(res.body).toHaveProperty('username');
        expect(res.body).toHaveProperty('profilePic');
    });
  
    // Input: user-id is an existing user, full-profile parameter is true
    // Expected status code: 200
    // Expected behavior: Return current user's full data
    // Expected output: userId, username, profile pic, bio, top artists, top genres
    test("Existing user full profile", async () => {
        const res = await request(server)
            .get('/me')
            .set('user-id', 'testUser1')
            .query({ fullProfile: true });
  
        expect(res.statusCode).toBe(200);
        expect(res.body).toHaveProperty('userId');
        expect(res.body).toHaveProperty('username');
        expect(res.body).toHaveProperty('profilePic');
        expect(res.body).toHaveProperty('bio');
        expect(res.body).toHaveProperty('topArtists');
        expect(res.body).toHaveProperty('topGenres');
    });

    // Input: user-id doesn’t exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: This executing user does not exist
    test("Non-existing user", async () => {
        const res = await request(server)
            .get('/me')
            .set('user-id', 'nonExistingUserId');
  
        expect(res.statusCode).toBe(401);
        expect(res.body).toEqual({ error: "This executing user does not exist" });
    });

    // Input: user-id is not included
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: Error JSON indicating which fields were invalid
    test("Missing user-id", async () => {
        const res = await request(server)
            .get('/me');
  
        expect(res.statusCode).toBe(400);
    });

    // Input: user-id is that of banned user
    // Expected status code: 403
    // Expected behavior: Return error message
    // Expected output: This executing user is banned
    test("Banned user", async () => {
        const res = await request(server)
            .get('/me')
            .set('user-id', 'bannedUser');
  
        expect(res.statusCode).toBe(403);
        expect(res.body).toEqual({ error: "This executing user is banned" });
    });
  });
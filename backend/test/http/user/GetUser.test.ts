import { server } from "@src/index";
import request from 'superwstest';

describe("Get user", () => {
    // Input: user-id is an existing user, full-profile parameter is false/doesn't exist
    // Expected status code: 200
    // Expected behavior: Return current user's data
    // Expected output: userId, username, profile-pic
    // ChatGPT usage: None
    test("Existing user", async () => {
        const res = await request(server)
            .get('/users/testUser1')
  
        expect(res.statusCode).toBe(200);
        expect(res.body).toHaveProperty('userId');
        expect(res.body).toHaveProperty('username');
        expect(res.body).toHaveProperty('profilePic');
    });
  
    // Input: user-id is an existing user, full-profile parameter is true
    // Expected status code: 200
    // Expected behavior: Return current user's full data
    // Expected output: userId, username, profile pic, bio, top artists, top genres
    // ChatGPT usage: None
    test("Existing user full profile", async () => {
        const res = await request(server)
            .get('/users/testUser1')
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
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: User not found
    // ChatGPT usage: None
    test("Non-existing user", async () => {
        const res = await request(server)
            .get('/users/nonExistingSpotifyId')
  
        expect(res.statusCode).toBe(400);
        expect(res.body).toEqual({ error : "User not found." });
    });
  });
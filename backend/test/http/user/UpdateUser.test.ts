import { server } from "@src/index";
import request from 'superwstest';

describe("Update user", () => {
    // Input: user-id is an existing user, valid user data
    // Expected status code: 200
    // Expected behavior: Return list of users related to search param
    // Expected output: {userId, username, profile-pic, match}[]
    test("Existing user with valid user data", async () => {
      const validUserData = {
        username: 'UpdatedUser',
        top_artists: ['UpdatedArtist1', 'UpdatedArtist2'],
        top_genres: ['UpdatedGenre1', 'UpdatedGenre2'],
        pfp_url: 'TestPfp',
        bio: 'Updated bio'
      };
  
      const res = await request(server)
        .put('/me/update')
        .set('user-id', 'testUser1')
        .send({ userData: validUserData });
  
      expect(res.statusCode).toBe(200);
      expect(res.body).toHaveProperty('userId', 'testUser1');
      expect(res.body).toHaveProperty('username', validUserData.username);
      expect(res.body).toHaveProperty('profilePic', validUserData.pfp_url);
      expect(res.body).toHaveProperty('bio', validUserData.bio);
    });
  
    // Input: invalid user data
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: Error JSON indicating which fields were invalid
    test("Invalid user data", async () => {
      const invalidUserData = {
        top_artists: 123
      };
  
      const res = await request(server)
        .put('/me/update')
        .set('user-id', 'testUser1')
        .send({ userData: invalidUserData });
  
      expect(res.statusCode).toBe(400);
      expect(res.body).toEqual({
        errors: [
          {
            type: 'field',
            value: 123,
            msg: 'Invalid value',
            path: 'userData.top_artists',
            location: 'body'
          }
        ]
      });
    });

    // Input: Body without any fields
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: At least one of username, top_artists, top_genres, pfp_url, bio must be provided
    test("Body without fields", async () => {
      const res = await request(server)
      .put('/me/update')
      .set('user-id', 'testUser1')
      .send({ userData: {
        top_artists: undefined
      } });

      expect(res.statusCode).toBe(400);
      expect(res.body).toEqual({
        errors: [
          {
            type: 'field',
            location: 'body',
            msg: "At least one of username, top_artists, top_genres, pfp_url, bio must be provided",
            path: "",
            value: { userData: {} }
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
        .put('/me/update')
        .set('user-id', 'nonExistingUserId')
        .send({ userData: { username: 'UpdatedUser' } });

      expect(res.statusCode).toBe(401);
      expect(res.body).toEqual({ error : "This executing user does not exist" });
    });
  });
import { server } from "@src/index";
import request from 'superwstest';

describe("Create new user", () => {
    // Input: valid user data
    // Expected status code: 200
    // Expected behavior: Return created user's data
    // Expected output: userId, username, profile-pic, bio, top artists, top genres
    test("Valid user data", async () => {
      const validUserData = {
        spotify_id: 'newValidSpotifyId',
        username: 'NewUser',
        top_artists: ["TopArtist1"],
        top_genres: ["TopGenre1"],
      };
  
      const res = await request(server)
        .post('/users/create')
        .send({ userData: validUserData });
  
      expect(res.statusCode).toBe(200);
      expect(res.body).toHaveProperty('userId', validUserData.spotify_id);
      expect(res.body).toHaveProperty('username', validUserData.username);
      expect(res.body).toHaveProperty('profilePic');
      expect(res.body).toHaveProperty('bio');
      expect(res.body).toHaveProperty('topArtists', validUserData.top_artists);
      expect(res.body).toHaveProperty('topGenres', validUserData.top_genres);
    });
  
    // Input: invalid user data
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: Error JSON indicating which fields were invalid
    test("Invalid user data", async () => {
      const invalidUserData = {
        username: 'NewUser',
        top_artists: ['Artist1', 'Artist2'],
        top_genres: ['Genre1', 'Genre2']
      };
  
      const res = await request(server)
        .post('/users/create')
        .send({ userData: invalidUserData });
  
      expect(res.statusCode).toBe(400);
      expect(res.body).toEqual({
        "errors": [
            {
                "type": "field",
                "msg": "Invalid value",
                "path": "userData.spotify_id",
                "location": "body"
            }
        ]
      });
    });
  });
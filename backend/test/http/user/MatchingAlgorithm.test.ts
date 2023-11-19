import { server } from "@src/index";
import { userMatchingService, userService } from "@src/services";
import request from 'superwstest';

describe("Get top matches", () => {
    // Input: 3 users join
    // Expected status code: 200
    // Expected behavior: Matches will be computed as expected
    // Expected output: Match with user1: 109.8, user2: 96.45, user3: 77.45
    test("Matching Algorithm", async () => {
        const validUserData1 = {
          spotify_id: 'newUser1',
          username: 'NewUser1',
          top_artists: ["TopArtist1"],
          top_genres: ["TopGenre1"]
        };
  
        const validUserData2 = {
          spotify_id: 'newUser2',
          username: 'NewUser2',
          top_artists: ["TopArtist1", "TopArtist2"],
          top_genres: ["TopGenre1"]
        };
  
        const validUserData3 = {
          spotify_id: 'newUser3',
          username: 'NewUser3',
          top_artists: ["TopArtist1", "TopArtist2"],
          top_genres: ["TopGenre1", "TopGenre2"]
        };
    
        const res1 = await request(server)
          .post('/users/create')
          .send({ userData: validUserData1 });
  
        const res2 = await request(server)
          .post('/users/create')
          .send({ userData: validUserData2 });
        
        const res3 = await request(server)
          .post('/users/create')
          .send({ userData: validUserData3 });

        const res4 = await request(server)
          .get('/me/matches')
          .set('user-id', 'testUser1');
  
        expect(res4.statusCode).toBe(200);
        expect(Array.isArray(res4.body)).toBeTruthy();

        const newUser1Match = res4.body.find(obj => obj.userId === 'newUser1');
        expect(newUser1Match).toBeDefined();
        expect(newUser1Match.match).toBe(109.8);

        const newUser2Match = res4.body.find(obj => obj.userId === 'newUser2');
        expect(newUser2Match).toBeDefined();
        expect(newUser2Match.match).toBe(96.45);

        const newUser3Match = res4.body.find(obj => obj.userId === 'newUser3');
        expect(newUser3Match).toBeDefined();
        expect(newUser3Match.match).toBe(77.45);
    });

    // Input: Matching with a max computation of 1
    // Expected status code: 200
    // Expected behavior: testUser1 should only have 1 matches
    // Expected output: Match with userX: 109.8
    test("Matching Algorithm", async () => {
        const executingUser = await userService.getUserBySpotifyId("testUser1");
        await userMatchingService.matchNewUser(executingUser.id, 1);

        const res = await request(server)
          .get('/me/matches')
          .set('user-id', 'testUser1');
  
        expect(res.statusCode).toBe(200);
        expect(Array.isArray(res.body)).toBeTruthy();
        expect(res.body).toHaveLength(1);
    });
});
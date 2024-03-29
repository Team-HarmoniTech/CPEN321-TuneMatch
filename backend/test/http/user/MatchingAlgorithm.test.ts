import { server } from "@src/index";
import { database, userMatchingService, userService } from "@src/services";
import request from 'superwstest';

async function bruteForceMatches(userId) {
  const matchedUsers = [];
  const allUsers = await database.user.findMany();

  for (const user of allUsers) {
      if (user.id === userId) {
          continue;
      }

      const matchPercent = await userMatchingService.getMatch(userId, user.id);

      matchedUsers.push({ match: matchPercent, userId: user.spotify_id });
  }

  return matchedUsers.sort((a, b) => b.match - a.match);
}

function generate30UniqueRandomInts1tox(x = 100): number[] {
  const result: number[] = [];
  while (result.length < 30) {
    const randomInt = Math.floor(Math.random() * x) + 1;
    if (!result.includes(randomInt)) {
      result.push(randomInt);
    }
  }
  return result;
}

describe("Get top matches", () => {
    // Input: 3 users join
    // Expected status code: 200
    // Expected behavior: Matches will be computed as expected
    // Expected output: Match with user1: 109.8, user2: 96.45, user3: 77.45
    // ChatGPT usage: None
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
    // ChatGPT usage: None
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

    // Expected status code: 200
    // Expected behavior: NRF1: At least 60% of users in the top 20 generated by algorithm is 
    //                    present in the top 50 generated by BF
    // Expected output: Test passes when we check to make sure our algorithm is 60% accurate
    const NUM_USERS = 500;
    const MAX_COMPUTED = 50;
    // ChatGPT usage: None
    test("Matching Algorithm NFR1 Test", async () => {
      (database as any).reset();
      
      for (let i = 0; i < NUM_USERS;  i++) {
        const user = await userService.createUser({
          spotify_id: `testUser${i}`,
          username: `testUsername${i}`,
          top_artists: generate30UniqueRandomInts1tox().map(n => `artist_${n}`),
          top_genres: generate30UniqueRandomInts1tox().map(n => `genre_${n}`),
        });
        await userMatchingService.matchNewUser(user.id, MAX_COMPUTED);
      }

      const algoUser = await userService.createUser({
        spotify_id: "matchingAlgoUser",
        username: "matchingAlgoUsername",
        top_artists: generate30UniqueRandomInts1tox().map(n => `artist_${n}`),
        top_genres: generate30UniqueRandomInts1tox().map(n => `genre_${n}`),
      });
      await userMatchingService.matchNewUser(algoUser.id, MAX_COMPUTED);

      const topAlgorithmMatches = (await userMatchingService.getTopMatches(algoUser.id))
      .map(item => {
        return {
          match: item.match,
          userId: item.spotify_id
        };
      })
      .slice(0, MAX_COMPUTED*0.6);

      const topBruteForceMatches = (await bruteForceMatches(algoUser.id)).slice(0, NUM_USERS*(1-0.6));

      const commonMatchesCount = topBruteForceMatches.filter(bfMatch =>
        topAlgorithmMatches.some(algoMatch => algoMatch.userId === bfMatch.userId)
      ).length;

      const overlapPercentage = (commonMatchesCount / topAlgorithmMatches.length) * 100;

      console.log(overlapPercentage);
      expect(overlapPercentage).toBeGreaterThanOrEqual(60);
    }, 180000);

    // Expected status code: 200
    // Expected behavior: NFR2: The amortized computation of a user's connections must be under 
    //                    15 seconds
    // Expected output: The average computation time is under 15s
    // ChatGPT usage: None
    test("Matching Algorithm NFR2 Test", async () => {
      (database as any).reset();
      let totalMs = 0;
      let totalUsers = 0;
      
      for (let i = 0; i < 500;  i++) {
        totalUsers++;
        const user = await userService.createUser({
          spotify_id: `testUser${i}`,
          username: `testUsername${i}`,
          top_artists: generate30UniqueRandomInts1tox().map(n => `artist_${n}`),
          top_genres: generate30UniqueRandomInts1tox().map(n => `genre_${n}`),
        });
        const start = new Date();
        await userMatchingService.matchNewUser(user.id, MAX_COMPUTED);
        totalMs += (Date.now() - start.getTime());
      }

      totalUsers++;
      const algoUser = await userService.createUser({
        spotify_id: "matchingAlgoUser",
        username: "matchingAlgoUsername",
        top_artists: generate30UniqueRandomInts1tox().map(n => `artist_${n}`),
        top_genres: generate30UniqueRandomInts1tox().map(n => `genre_${n}`),
      });
      const start = new Date();
      await userMatchingService.matchNewUser(algoUser.id, MAX_COMPUTED);
      totalMs += (Date.now() - start.getTime());

      const averageTimePerComputationMs = totalMs / totalUsers;

      console.log(averageTimePerComputationMs);
      expect(averageTimePerComputationMs).toBeLessThanOrEqual(15000);
    }, 180000);
});
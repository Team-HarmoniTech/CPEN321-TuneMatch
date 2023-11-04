import { describe } from '@jest/globals';
import { database, userService } from '@src/services';

afterEach(async () => {
    await database.user.deleteMany();
    await database.connection.deleteMany();
    await database.report.deleteMany();
    await database.session.deleteMany();
});

describe('User Service', () => {
    it('should create user', async () => {
        const userData = {
            spotify_id: "hqicxpdjfu5251lfxc13260kz",
            username: "bim bam bom",
            top_artists: [],
            top_genres: [],
            pfp_url: "http://myurl.com/url/test"
        }

        const userCreated = await userService.createUser(userData);
        const userQueried = await userService.getUserBySpotifyId(userData.spotify_id);

        // Check if userCreated and userQueried contain the same fields as userData
        expect(userCreated).toEqual(expect.objectContaining(userData));
        expect(userQueried).toEqual(expect.objectContaining(userData));

        // Check if userQueried contain the same fields as userCreated
        expect(userQueried).toEqual(expect.objectContaining(userCreated));
    });
});
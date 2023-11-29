import { server } from "@src/index";
import { database } from "@src/services";

beforeEach((done) => {
    jest.resetModules();
    server.listen(0, 'localhost', done);
});

beforeEach(async () => {
    /* Reset Data */
    (database as any).reset();
    await database.user.createMany({
        data: [
            {
                spotify_id: "testUser1",
                username: "testUsername1",
                top_artists: ["TopArtist1"],
                top_genres: ["TopGenre1"],
                connectionComputed: true
            },
            {
                spotify_id: "testUser2",
                username: "testUsername2",
                top_artists: ["TopArtist1", "TopArtist2"],
                top_genres: ["TopGenre1"]
            },
            {
                spotify_id: "bannedUser",
                username: "bannedUserName",
                top_artists: [],
                top_genres: [],
                is_banned: true
            }
        ]
    });
});

afterEach((done) => {
    server.close(done);
});
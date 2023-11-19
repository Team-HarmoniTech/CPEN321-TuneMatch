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
                top_artists: ["TopArtist1"],
                top_genres: ["TopGenre1"]
            },
            {
                spotify_id: "testUser3",
                username: "testUsername3",
                top_artists: ["TopArtist1"],
                top_genres: ["TopGenre1"]
            },
            {
                spotify_id: "testUser4",
                username: "testUsername4",
                top_artists: ["TopArtist1"],
                top_genres: ["TopGenre1"]
            },
            {
                spotify_id: "testUser5",
                username: "testUsername5",
                top_artists: ["TopArtist1"],
                top_genres: ["TopGenre1"]
            },
            {
                spotify_id: "testUser6",
                username: "testUsername6",
                top_artists: ["TopArtist1"],
                top_genres: ["TopGenre1"]
            },
            {
                spotify_id: "bannedUser",
                username: "bannedUserName",
                top_artists: [],
                top_genres: [],
                is_banned: true
            },
        ]
    });

    await database.report.createMany({
        data: [
            {
                id: 1,
                offending_user_id: 2,
                reporting_user_id: 1,
                reason: "OFFENSIVE_LANGUAGE",    
                reason_text: "Bad behaviour",      
                report_context: "Context or additional details",
                timestamp: "2023-11-18T12:34:56.789Z"
            },
            {
                id: 2,
                offending_user_id: 1,
                reporting_user_id: 2,
                reason: "PLAYLIST_ABUSE",    
                reason_text: "Abusing playlist",      
                report_context: "Context or additional details",
                timestamp: "2023-11-01T12:34:56.789Z"
            },
        ]
    });
});

afterEach((done) => {
    server.close(done);
});
import { server } from "@src/index";
import { database, userService } from "@src/services";

beforeEach((done) => {
    jest.resetModules();
    server.listen(0, 'localhost', done);
});

beforeEach(async () => {
    /* Reset Data */
    (database as any).reset();
    await userService.createUser({
        spotify_id: "testUser1",
        username: "testUsername1",
        top_artists: [],
        top_genres: []
    });
    await userService.createUser({
        spotify_id: "testUser2",
        username: "testUsername2",
        top_artists: [],
        top_genres: []
    });
})

afterEach((done) => {
    server.close(done);
});
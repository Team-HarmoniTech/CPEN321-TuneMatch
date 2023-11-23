import { server } from "@src/index";
import { userService } from "@src/services";
import request from "superwstest";

describe("Session Join", () => {
    it("should create a new session on empty join", async () => {
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join"
        });

        const startTime = Date.now();
        const timeout = 10000;
        while (Date.now() - startTime < timeout) {
            if ((await userService.getUserBySpotifyId("testUser1")).session) {
                expect((await userService.getUserBySpotifyId("testUser1")).session).toBeDefined();
                break;
            }
        
            // Poll every 500 milliseconds (adjust as needed)
            await new Promise(resolve => setTimeout(resolve, 500));
        }
        expect((await userService.getUserBySpotifyId("testUser1")).session).toBeDefined();

        await socket1.close();
    });

    it("should join another user's session", async () => {
        const socket1 = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join"
        });
        const socket2 = await request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join",
            body: {
                userId: "testUser1"
            }
        })
        .expectJson();

        expect((await userService.getUserBySpotifyId("testUser1")).session.id)
            .toEqual((await userService.getUserBySpotifyId("testUser2")).session.id);

        await socket1.close();
        await socket2.close();
    });

    it("should leave previous session to join other user", async () => {
        await userService.createUser({
            spotify_id: "testUser3",
            username: "testUsername3",
            top_artists: [],
            top_genres: []
        });

        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson();
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();
        const socket3 = request(server).ws("/socket", { headers: { "user-id": "testUser3" } })
        .expectJson()
        .expectJson();

        await socket2.sendJson({
            method: "SESSION",
            action: "join"
        })
        .expectJson();

        await socket3.sendJson({
            method: "SESSION",
            action: "join"
        })
        .expectJson();

        await socket1.sendJson({
            method: "SESSION",
            action: "join",
            body: {
                userId: "testUser2"
            }
        })
        .expectJson();
        await socket2.expectJson();

        await socket1.sendJson({
            method: "SESSION",
            action: "join",
            body: {
                userId: "testUser3"
            }
        })
        .expectJson();

        await socket2.expectJson({
            method: "SESSION",
            action: "leave",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null
            },
            from:"testUser1"
        });

        await socket3.expectJson({
            method: "SESSION",
            action: "join",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null
            },
            from:"testUser1"
        });

        expect((await userService.getUserBySpotifyId("testUser1")).session.id)
            .toEqual((await userService.getUserBySpotifyId("testUser3")).session.id);

        await socket1.close();
        await socket2.close();
    });

    it("should do nothing on join user in same session", async () => {
        const socket1 = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join"
        })
        const socket2 = await request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join",
            body: {
                userId: "testUser1"
            }
        })
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join",
            body: {
                userId: "testUser1"
            }
        });

        expect((await userService.getUserBySpotifyId("testUser1")).session.id)
            .toEqual((await userService.getUserBySpotifyId("testUser2")).session.id);

        await socket1.close();
        await socket2.close();
    });

    it("should error on join another user who is not in a session", async () => {
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } });
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join",
            body: { userId: "testUser2" }
        })
        .expectJson({
            method: "SESSION",
            action: "error",
            body: "User is not in a session."
        });

        await socket1.close();
        await socket2.close();
    });

    it("should error join another user who does not exist", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join",
            body: { userId: "fakeUser" }
        })
        .expectJson({ 
            method: "SESSION",
            action: "error",
            body: "User does not exist"
        })
        .close();
    });

    it("should update session on join", async () => {
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();

        await socket2.sendJson({
            method: "SESSION",
            action: "join"
        })
        .expectJson();

        socket1.sendJson({
            method: "SESSION",
            action: "join",
            body: {
                userId: "testUser2"
            }
        })
        .expectJson();

        socket2.expectJson({
            method: "SESSION",
            action:"join", 
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null
            },
            from: "testUser1"
        });

        await socket1.close();
        await socket2.close();
    });

    it("should update friends on join", async () => {
        await userService.addFriend(1, 2);
        await userService.addFriend(2, 1);

        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson()
        .expectJson({
            method: "FRIENDS",
            action: "update",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null,
                currentSong: null,
                currentSource: { 
                    type: "session"
                }
            },
            from: "testUser1"
        });
        socket1.sendJson({
            method: "SESSION",
            action: "join"
        })
        .expectJson()
        
        await socket1.close();
        await socket2.close();
    });

    it("should receive refresh on join", async () => {
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join"
        });
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join",
            body: {
                userId: "testUser1"
            }
        })
        .expectJson({
            method: "SESSION",
            action: "refresh",
            body: {
                members: [
                    {
                        userId: "testUser1",
                        username: "testUsername1",
                        profilePic: null
                    }
                ],
                running: false,
                queue: []
            }
        });

        await socket1.close();
        await socket2.close();
    });
});
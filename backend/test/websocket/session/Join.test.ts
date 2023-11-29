import { server } from "@src/index";
import { userService } from "@src/services";
import request from "superwstest";
import { testConstantDate } from "../../globalSetup";

describe("Session Join", () => {

    // Input: The body is empty
    // Expected behavior: a new session is created with the user
    // Expected output: None
    // ChatGPT usage: None
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
         
            await new Promise(resolve => setTimeout(resolve, 500));
        }
        expect((await userService.getUserBySpotifyId("testUser1")).session).toBeDefined();

        await socket1.close();
    });

    // Input: The body contains a valid user id
    // Expected behavior: the user is added to the other user's session
    // Expected output: None
    // ChatGPT usage: None
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

    // Input: The body contains a valid user id of a user not in the current user's session
    // Expected behavior: the user is removed from their original session and added to the other user's session
    // Expected output: None
    // ChatGPT usage: None
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

    // Input: The body contains a valid user id of a user in the current user's session
    // Expected behavior: nothing happens
    // Expected output: None
    // ChatGPT usage: None
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

    // Input: The body contains a valid user id of a user not in a session
    // Expected behavior: nothing happens
    // Expected output: returns an error with the message "User is not in a session."
    // ChatGPT usage: None
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
            Error: "User is not in a session."
        });

        await socket1.close();
        await socket2.close();
    });

    // Input: The body contains an invalid user id
    // Expected behavior: nothing happens
    // Expected output: returns an error with the message "User does not exist"
    // ChatGPT usage: None
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
            Error: "User does not exist"
        })
        .close();
    });

    // Input: A user joins a session
    // Expected behavior: All members in the session receive an update that the user has join
    // Expected output: None
    // ChatGPT usage: None
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

    // Input: A user joins a session
    // Expected behavior: All friends of the user get an update that the user is now in a session
    // Expected output: None
    // ChatGPT usage: None
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
                    type: "session",
                    members: ["testUsername1"]
                },
                lastUpdated: testConstantDate.toISOString()
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

    // Input: A user joins a session
    // Expected behavior: The user receives information on the current queue and other members of the session
    // Expected output: Information on the current queue and other members of the session
    // ChatGPT usage: None
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
                timeStamp: testConstantDate.toISOString(),
                queue: []
            }
        });

        await socket1.close();
        await socket2.close();
    });
});
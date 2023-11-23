import { server } from "@src/index";
import { userService } from "@src/services";
import request from "superwstest";

describe("Login", () => {
    it("should require a user-id header", async () => {
        await request(server).ws("/socket")
        .expectClosed(1008, "No Authentication Provided").close();
    });
    
    it("should reject a non-existent user", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "fakeUser" } })
        .expectClosed(1008, "User fakeUser does not exist").close();
    });

    it("should accept an existing user", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectUpgrade(x => true)
        .expectJson()
        .close();
    });

    it("should reject an user's second connection", async () => {
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } });
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectClosed(1008, "Duplicate User 1 tried to connect").close();
        await socket1.close();
    });

    it("should refresh with a user's requests on login", async () => {
        await userService.createUser({
            spotify_id: "testUser3",
            username: "testUsername3",
            top_artists: [],
            top_genres: []
        });

        await userService.addFriend(1, 3);
        await userService.addFriend(2, 1);
        
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson({
            method: "REQUESTS",
            action: "refresh",
            body: {
                requesting: [
                    {
                        userId: "testUser3",
                        username: "testUsername3",
                        profilePic: null,
                    }
                ],
                requested: [
                    {
                        userId: "testUser2",
                        username: "testUsername2",
                        profilePic: null,
                    }
                ]
            }
        });
    });

    it("should refresh with a user's friends on login", async () => {
        await userService.addFriend(1, 2);
        await userService.addFriend(2, 1);
        await userService.updateUserStatus(2, {
            name: "Them Changes",
            uri: "spotify:track:7CH99b2i1TXS5P8UUyWtnM"
        }, {
            type: "album",
            name: "Drunk",
            uri: "spotify:album:7vHBQDqwzB7uDvoE5bncMM"
        });

        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson({
            method: "FRIENDS",
            action: "refresh",
            body: [
                {
                    userId: "testUser2",
                    username: "testUsername2",
                    profilePic: null,
                    currentSong: {
                        name: "Them Changes",
                        uri: "spotify:track:7CH99b2i1TXS5P8UUyWtnM"
                    }, 
                    currentSource: {
                        type: "album",
                        name: "Drunk",
                        uri: "spotify:album:7vHBQDqwzB7uDvoE5bncMM"
                    }
                }
            ]
        }).close();
    });
});
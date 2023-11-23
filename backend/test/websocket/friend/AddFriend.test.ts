import { server } from "@src/index";
import { userService } from "@src/services";
import request from "superwstest";

describe("Adding Friends", () => {
    it("should send a friend request", async () => {
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson().expectJson();
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } }).expectJson().expectJson();

        socket1.sendJson({
            method: "REQUESTS",
            action: "add",
            body: {
                userId: "testUser2"
            }
        });
        socket2.expectJson({
            method: "REQUESTS",
            action: "add",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null,
                currentSong: null,
                currentSource: null
            }
        });
        await socket1.close();
        await socket2.close();

        expect((await userService.getUserFriendsRequests(1)).requesting).toHaveLength(1);
        expect((await userService.getUserFriendsRequests(1)).requesting[0]).toHaveProperty("spotify_id", "testUser2");
        expect((await userService.getUserFriendsRequests(2)).requested).toHaveLength(1);
        expect((await userService.getUserFriendsRequests(2)).requested[0]).toHaveProperty("spotify_id", "testUser1");
    });

    it("should accept a friend request", async () => {
        await userService.addFriend(2, 1);

        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "REQUESTS",
            action: "add",
            body: {
                userId: "testUser2"
            }
        })
        .expectJson()
        .close();

        expect((await userService.getUserFriends(1))).toHaveLength(1);
        expect((await userService.getUserFriends(1))[0]).toHaveProperty("spotify_id", "testUser2");
    });

    it("should update sender on accept friend request", async () => {
        await userService.addFriend(2, 1);
        await userService.updateUserStatus(1, {
            name: "Them Changes",
            uri: "spotify:track:7CH99b2i1TXS5P8UUyWtnM"
        }, {
            type: "album",
            name: "Drunk",
            uri: "spotify:album:7vHBQDqwzB7uDvoE5bncMM"
        });
        await userService.updateUserStatus(2, undefined, { type: "session" });

        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson().expectJson();
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } }).expectJson().expectJson();

        socket1.sendJson({
            method: "REQUESTS",
            action: "add",
            body: {
                userId: "testUser2"
            }
        }).expectJson({
            method: "FRIENDS",
            action: "update",
            body: {
                userId: "testUser2",
                username: "testUsername2",
                profilePic: null,
                currentSong: null,   
                currentSource: { type: "session" }
            }
        });
        socket2.expectJson({
            method: "REQUESTS",
            action: "add",
            body: {
                userId: "testUser1",
                username: "testUsername1",
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
        });
        
        await socket1.close();
        await socket2.close();
    });

    it("should reject the addition of a user that doesn't exist", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "REQUESTS",
            action: "add",
            body: {
                userId: "not real"
            }
        })
        .expectJson({
            method:"REQUESTS",
            action:"error",
            body: "User to add does not exist"
        })
        .close();
    });
});
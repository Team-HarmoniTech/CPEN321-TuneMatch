import { server } from "@src/index";
import { userService } from "@src/services";
import request from "superwstest";
import { testConstantDate } from "../../globalSetup";

describe("Adding Friends", () => {

    // Input: userId is a valid user id
    // Expected behavior: The requested user receives the request, the request is reflected in the database
    // Expected output: None
    // ChatGPT usage: None
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
                currentSource: null,
                lastUpdated: testConstantDate.toISOString()
            }
        });
        await socket1.close();
        await socket2.close();

        expect((await userService.getUserFriendsRequests(1)).requesting).toHaveLength(1);
        expect((await userService.getUserFriendsRequests(1)).requesting[0]).toHaveProperty("spotify_id", "testUser2");
        expect((await userService.getUserFriendsRequests(2)).requested).toHaveLength(1);
        expect((await userService.getUserFriendsRequests(2)).requested[0]).toHaveProperty("spotify_id", "testUser1");
    });

    // Input: userId is a valid user id
    // Expected behavior: The acceptance is reflected in the database
    // Expected output: None
    // ChatGPT usage: None
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

    // Input: userId is a valid user id
    // Expected behavior: The acceptance is reflected in the database
    // Expected output: An update from the user who was just accepted
    // ChatGPT usage: None
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
        }).expectJson()
        // .expectJson({
        //     method: "FRIENDS",
        //     action: "update",
        //     body: {
        //         userId: "testUser2",
        //         username: "testUsername2",
        //         profilePic: null,
        //         currentSong: null,   
        //         currentSource: { 
        //             type: "session"
        //         },
        //         lastUpdated: testConstantDate.toISOString()
        //     }
        // });
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
                },
                lastUpdated: testConstantDate.toISOString()
            }
        });
        
        await socket1.close();
        await socket2.close();
    });

    // Input: userId is an invalid user id
    // Expected behavior: Nothing changes internally
    // Expected output: An error message with the body "User to add does not exist"
    // ChatGPT usage: None
    it("should reject the addition of a user that doesn't exist", async () => {
        expect(await userService.getUserById(-1)).toBe(null);
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
            Error: "User to add does not exist"
        })
        .close();

        expect((await userService.getUserFriendsRequests(1)).requesting).toHaveLength(0);
    });
});
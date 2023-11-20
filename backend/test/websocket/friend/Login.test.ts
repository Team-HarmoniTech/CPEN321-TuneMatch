import { server } from "@src/index";
import { userService } from "@src/services";
import request from "superwstest";

describe("Login", () => {

    // Input: no authentication header is provided
    // Expected behavior: Nothing changes internally
    // Expected output: socket is closed with the message "No Authentication Provided"
    it("should require a user-id header", async () => {
        await request(server).ws("/socket")
        .expectClosed(1008, "No Authentication Provided").close();
    });
    
    // Input: the user-id provided is invalid
    // Expected behavior: Nothing changes internally
    // Expected output: socket is closed with the message "User {user-id} does not exist"
    it("should reject a non-existent user", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "fakeUser" } })
        .expectClosed(1008, "User fakeUser does not exist").close();
    });

    // Input: the user-id provided is invalid
    // Expected behavior: The user is added to the list of connections
    // Expected output: socket is connected as normal
    it("should accept an existing user", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectUpgrade(x => true)
        .expectJson()
        .close();
    });

    // Input: the user-id provided is valid but the user has already connected
    // Expected behavior: Nothing changes internally
    // Expected output: socket is closed with the message ""Duplicate User {id} tried to connect"
    it("should reject an user's second connection", async () => {
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } });
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectClosed(1008, "Duplicate User 1 tried to connect")
        .close();
        await socket1.close();
    });

    // Input: the user-id provided is valid
    // Expected behavior: Nothing changes internally
    // Expected output: A list of all the user's friend requests
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

    // Input: the user-id provided is valid
    // Expected behavior: Nothing changes internally
    // Expected output: A list of all the user's friends and their statuses
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
import { server } from "@src/index";
import { userService } from "@src/services";
import request from "superwstest";
import { testConstantDate } from "../../globalSetup";

describe("Removing Friends", () => {

    // Input: the userId provided is valid
    // Expected behavior: The provided user is removed from the user's friend requests
    // Expected output: None
    // ChatGPT usage: None
    it("should remove a friend request", async () => {
        await userService.addFriend(1, 2);

        expect((await userService.getUserFriendsRequests(1)).requesting).toHaveLength(1);

        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson({
            method: "REQUESTS",
            action: "refresh",
            body: {
                requesting: [
                    {
                        userId: "testUser2",
                        username: "testUsername2",
                        profilePic: null,
                    }
                ],
                requested: []
            }
        }).sendJson({
            method: "REQUESTS",
            action: "remove",
            body: {
                userId: "testUser2"
            }
        })
        .close();

        const startTime = Date.now();
        const timeout = 10000;
        while (Date.now() - startTime < timeout) {
            if ((await userService.getUserFriendsRequests(1)).requesting.length === 0) {
                expect((await userService.getUserFriendsRequests(1)).requesting).toHaveLength(0);
                break;
            }
         
            await new Promise(resolve => setTimeout(resolve, 500));
        }
        expect((await userService.getUserFriendsRequests(1)).requesting).toHaveLength(0);
    });

    // Input: the userId provided is valid
    // Expected behavior: The provided user is removed from the user's friends
    // Expected output: None
    // ChatGPT usage: None
    it("should remove a friend", async () => {
        await userService.addFriend(1, 2);
        await userService.addFriend(2, 1);

        expect(await userService.getUserFriends(1)).toHaveLength(1);

        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();

        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson({
            method: "FRIENDS",
            action: "refresh",
            body: [
                {
                    userId: "testUser2",
                    username: "testUsername2",
                    profilePic: null,
                    currentSong: null,
                    currentSource: null,
                    lastUpdated: testConstantDate.toISOString()
                }
            ]
        })
        .expectJson();

        await socket1.sendJson({
            method: "REQUESTS",
            action: "remove",
            body: {
                userId: "testUser2"
            }
        });

        await socket2.expectJson({
            method: "REQUESTS",
            action: "remove",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null
            }
        }).close();
        await socket1.close();

        const startTime = Date.now();
        const timeout = 10000;
        while (Date.now() - startTime < timeout) {
            if ((await userService.getUserFriends(1)).length === 0) {
                expect(await userService.getUserFriends(1)).toHaveLength(0);
                break;
            }
         
            await new Promise(resolve => setTimeout(resolve, 500));
        }
        expect(await userService.getUserFriends(1)).toHaveLength(0);
    });

    // Input: the userId provided is invalid
    // Expected behavior: Nothing changes internally
    // Expected output: An error with the message "User to remove does not exist"
    // ChatGPT usage: None
    it("should reject the removal of a user that doesn't exist", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "REQUESTS",
            action: "remove",
            body: {
                userId: "fakeUser"
            }
        })
        .expectJson({
            Error: "User to remove does not exist"
        })
        .close();
    });
});
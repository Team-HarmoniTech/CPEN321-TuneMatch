import { server } from "@src/index";
import { userService } from "@src/services";
import request from "superwstest";

describe("Removing Friends", () => {
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
        
            // Poll every 500 milliseconds (adjust as needed)
            await new Promise(resolve => setTimeout(resolve, 500));
        }
        expect((await userService.getUserFriendsRequests(1)).requesting).toHaveLength(0);
    });

    it("should remove a friend", async () => {
        await userService.addFriend(1, 2);
        await userService.addFriend(2, 1);

        expect(await userService.getUserFriends(1)).toHaveLength(1);

        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson({
            method: "FRIENDS",
            action: "refresh",
            body: [
                {
                    userId: "testUser2",
                    username: "testUsername2",
                    profilePic: null,
                    currentSong: null,
                    currentSource: null
                }
            ]
        })
        .expectJson()
        .sendJson({
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
            if ((await userService.getUserFriends(1)).length === 0) {
                expect(await userService.getUserFriends(1)).toHaveLength(0);
                break;
            }
        
            // Poll every 500 milliseconds (adjust as needed)
            await new Promise(resolve => setTimeout(resolve, 500));
        }
    });

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
            method: "REQUESTS",
            action: "error",
            body: "User to remove does not exist"
        })
        .close();
    });
});
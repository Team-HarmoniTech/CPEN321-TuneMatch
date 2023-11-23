import { server } from "@src/index";
import { database, userService } from "@src/services";
import { SessionService } from "@src/services/SessionService";
import request from "superwstest";
import { Song_ThemChanges } from "../songModels";

describe("Session Leave", () => {
    let sessionService: SessionService;
    beforeEach(() => {
        sessionService = new SessionService();
    });
    
    afterEach(() => {
        jest.clearAllMocks();
    });


    it("should leave a session", async () => {
        const updateSpy = jest.spyOn(database.session, 'update');

        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson();
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();

        await socket2.sendJson({
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
            action: "leave"
        });
        await socket2.expectJson();

        expect(updateSpy).toHaveBeenCalledWith({
            where: { id: 1 },
            data: { members: { disconnect: { id: 1 } } }
        });

        await socket1.close();
        await socket2.close();
    });

    it("should destroy session on last leave", async () => {
        const deleteSpy = jest.spyOn(database.session, 'delete');
        await userService.addFriend(1, 2);
        await userService.addFriend(2, 1);

        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson();
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();

        await socket1.sendJson({
            method: "SESSION",
            action: "join"
        })
        .expectJson()
        await socket2.expectJson()

        await socket1.sendJson({
            method: "SESSION",
            action: "leave"
        });
        await socket2.expectJson();

        expect(deleteSpy).toHaveBeenCalledTimes(1);
        expect(deleteSpy).toHaveBeenCalledWith({ where: { id: 1 } });

        await socket1.close();
        await socket2.close();
    });

    it("should update session on leave", async () => {
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson();
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();

        await socket2.sendJson({
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
            action: "leave"
        });
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

        await socket1.close();
        await socket2.close();
    });

    it("should update friends on leave", async () => {
        await userService.addFriend(1, 2);
        await userService.addFriend(2, 1);
        await userService.updateUserStatus(1, Song_ThemChanges, undefined);

        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson();
        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();

        await socket1.sendJson({
            method: "SESSION",
            action: "join"
        })
        .expectJson().sendJson({
            method: "SESSION",
            action: "leave"
        });

        await socket2.expectJson()
        .expectJson({
            method: "FRIENDS",
            action: "update",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null,
                currentSong: Song_ThemChanges,
                currentSource: null,
            },
            from:"testUser1"
        });

        await socket1.close();
        await socket2.close();
    });
});
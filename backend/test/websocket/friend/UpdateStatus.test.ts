import { server } from "@src/index";
import { database, sessionService, userService } from "@src/services";
import request from "superwstest";
import { testConstantDate } from "../../globalSetup";
import { Album_Drunk, Album_Speakerboxxx, Song_HeyYa, Song_ThemChanges } from "../songModels";

describe("Update Status", () => {
    beforeEach(async () => {
        await userService.addFriend(1, 2);
        await userService.addFriend(2, 1);
    });

    // Input: The body only contains a song
    // Expected behavior: the user's status is updated in the database and friends receive the update
    // Expected output: None
    // ChatGPT usage: None
    it("should update status with song only", async () => {
        let user = await database.user.update({
            where: { spotify_id: "testUser1" },
            data: {
                current_song: Song_ThemChanges,
                current_source: Album_Drunk
            }
        });

        expect(user).toHaveProperty("current_song", Song_ThemChanges);
        expect(user).toHaveProperty("current_source", Album_Drunk);

        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "FRIENDS",
            action: "update",
            body: {
                song: Song_HeyYa
            }
        });
        await socket2.expectJson({
            method: "FRIENDS",
            action: "update",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null,
                currentSong: Song_HeyYa,
                currentSource: Album_Drunk,
                lastUpdated: testConstantDate.toISOString()
            },
            from: "testUser1"
        });
        
        user = await database.user.findUnique({ where: { spotify_id: "testUser1" } });
        expect(user).toHaveProperty("current_song", Song_HeyYa);
        expect(user).toHaveProperty("current_source", Album_Drunk);

        await socket1.close();
        await socket2.close();
    });

    // Input: The body only contains a source
    // Expected behavior: the user's status is updated in the database and friends receive the update
    // Expected output: None
    // ChatGPT usage: None
    it("should update status with source only", async () => {
        let user = await database.user.update({
            where: { spotify_id: "testUser1" },
            data: {
                current_song: Song_ThemChanges,
                current_source: Album_Drunk
            }
        });

        expect(user).toHaveProperty("current_song", Song_ThemChanges);
        expect(user).toHaveProperty("current_source", Album_Drunk);

        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "FRIENDS",
            action: "update",
            body: {
                source: Album_Speakerboxxx
            }
        });
        await socket2.expectJson({
            method: "FRIENDS",
            action: "update",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null,
                currentSong: Song_ThemChanges,
                currentSource: Album_Speakerboxxx,
                lastUpdated: testConstantDate.toISOString()
            },
            from: "testUser1"
        });

        user = await database.user.findUnique({ where: { spotify_id: "testUser1" } });
        expect(user).toHaveProperty("current_song", Song_ThemChanges);
        expect(user).toHaveProperty("current_source", Album_Speakerboxxx);

        await socket1.close();
        await socket2.close();
    });

    // Input: The body contains a song and source
    // Expected behavior: the user's status is updated in the database and friends receive the update
    // Expected output: None
    // ChatGPT usage: None
    it("should update status with song and source", async () => {
        let user = await database.user.update({
            where: { spotify_id: "testUser1" },
            data: {
                current_song: Song_ThemChanges,
                current_source: Album_Drunk
            }
        });

        expect(user).toHaveProperty("current_song", Song_ThemChanges);
        expect(user).toHaveProperty("current_source", Album_Drunk);

        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "FRIENDS",
            action: "update",
            body: {
                song: Song_HeyYa,
                source: Album_Speakerboxxx
            }
        });
        await socket2.expectJson({
            method: "FRIENDS",
            action: "update",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null,
                currentSong: Song_HeyYa,
                currentSource: Album_Speakerboxxx,
                lastUpdated: testConstantDate.toISOString()
            },
            from: "testUser1"
        });

        user = await database.user.findUnique({ where: { spotify_id: "testUser1" } });
        expect(user).toHaveProperty("current_song", Song_HeyYa);
        expect(user).toHaveProperty("current_source", Album_Speakerboxxx);

        await socket1.close();
        await socket2.close();
    });

    // Input: The body contains a source and the user is in a session
    // Expected behavior: The user's source is not updated
    // Expected output: None
    // ChatGPT usage: None
    it("should not update source during a session", async () => {
        await database.user.update({
            where: { spotify_id: "testUser1" },
            data: {
                current_song: Song_ThemChanges
            }
        });
        await sessionService.joinSession(1);
        

        let user = await database.user.findUnique({ where: { spotify_id: "testUser1" } });
        expect(user).toHaveProperty("current_song", Song_ThemChanges);
        expect(user).toHaveProperty("current_source", { 
            type: "session",
            members: [
                "testUsername1"
            ]
        });

        const socket2 = request(server).ws("/socket", { headers: { "user-id": "testUser2" } })
        .expectJson()
        .expectJson();
        const socket1 = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson()
        .sendJson({
            method: "SESSION",
            action: "join"
        })
        .sendJson({
            method: "FRIENDS",
            action: "update",
            body: {
                song: Song_HeyYa,
                source: Album_Speakerboxxx
            }
        });
        await socket2.expectJson({
            method: "FRIENDS",
            action: "update",
            body: {
                userId: "testUser1",
                username: "testUsername1",
                profilePic: null,
                currentSong: Song_HeyYa,
                currentSource: { 
                    type: "session",
                    members: ["testUsername1"] 
                },
                lastUpdated: testConstantDate.toISOString()
            },
            from: "testUser1"
        });

        user = await database.user.findUnique({ where: { spotify_id: "testUser1" } });
        expect(user).toHaveProperty("current_song", Song_HeyYa);
        expect(user).toHaveProperty("current_source", { type: "session", members: ["testUsername1"] });

        await socket1.close();
        await socket2.close();
    });

    // Input: The user has not uet connected
    // Expected behavior: the user's status is null
    // Expected output: None
    // ChatGPT usage: None
    it("should have null status before connect", async () => {
        expect(await userService.getUserBySpotifyId("testUser1")).toHaveProperty("current_song", null);
    });
});
import { server } from "@src/index";
import { socketService } from "@src/services";
import request from "superwstest";

describe("Websocket", () => {
    it("should add websocket to map", async () => {
        expect(await socketService.retrieveById(1)).toBeUndefined();
        const socket = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } });
        expect(await socketService.retrieveById(1)).toBeDefined();
        await socket.close();
    });

    it("should sustain websocket connections simultaneously", async () => {
        const socket1 = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } });
        await request(server).ws("/socket", { headers: { "user-id": "testUser2" } }).expectUpgrade(() => true).close();
        socket1.close();
    });

    it("should error on incorrect method", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson().expectJson()
        .sendJson({ method: "invalid method"})
        .expectJson({ Error: "Received data has an invalid method" })
        .close();
    });

    it("should error on incorrect action", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson().expectJson()
        .sendJson({
            method: "SESSION",
            action: "invalid action"
        })
        .expectJson({ Error: "Session endpoint invalid action does not exist." })
        .close();
    });

    it("should error on invalid json", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson().expectJson()
        .sendText("{ {{]/test: 10")
        .expectJson({ Error: "Received data is not formatted correctly" })
        .close();
    });

    it("should start ping pong", async () => {
        let pingCount = 0;
        const socket = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } });

        await socket.on("ping", () => {
            pingCount++;
        });

        expect(pingCount).toBe(1);
        await socket.close();
    });

    it("should reply ping to a pong", async () => {
        let pingCount = 0;
        const socket = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } });

        await socket.on("ping", () => {
            pingCount++;
        });

        /* Reset count and send pong */
        pingCount = 0;
        await socket.pong();

        /* We respond after 20s so we should definitely have received a ping */
        await new Promise(f => setTimeout(f, 21000));
        expect(pingCount).toBeGreaterThanOrEqual(1);
        await socket.close();
    }, 30000);
});
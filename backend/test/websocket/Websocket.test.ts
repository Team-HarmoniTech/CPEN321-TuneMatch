import { server } from "@src/index";
import { socketService } from "@src/services";
import request from "superwstest";

describe("Websocket", () => {
    // Input: A user connects
    // Expected behavior: They are added to the socketService
    // Expected output: None
    it("should add websocket to map", async () => {
        expect(await socketService.retrieveById(1)).toBeUndefined();
        const socket = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } });
        expect(await socketService.retrieveById(1)).toBeDefined();
        await socket.close();
    });

    // Input: Multiple users connect
    // Expected behavior: They are added to the socketService
    // Expected output: None
    it("should sustain websocket connections simultaneously", async () => {
        expect(await socketService.retrieveById(1)).toBeUndefined();
        expect(await socketService.retrieveById(2)).toBeUndefined();
        const socket1 = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } });
        const socket2 = await request(server).ws("/socket", { headers: { "user-id": "testUser2" } }).expectUpgrade(() => true);
        expect(await socketService.retrieveById(1)).toBeDefined();
        expect(await socketService.retrieveById(2)).toBeDefined();
        await socket2.close();
        await socket1.close();
    });

    // Input: The user provides an invalid method
    // Expected behavior: Nothing changes
    // Expected output: returns an error with the message "Received data has an invalid method"
    it("should error on incorrect method", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson().expectJson()
        .sendJson({ method: "invalid method" })
        .expectJson({ Error: "Received data has an invalid method" })
        .close();
    });

    // Input: The user doesn't provide a method
    // Expected behavior: Nothing changes
    // Expected output: returns an error with the message "Received data is missing fields"
    it("should error on missing fields", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson().expectJson()
        .sendJson({})
        .expectJson({ Error: "Received data is missing fields" })
        .close();
    });

    // Input: The user provides an invalid action
    // Expected behavior: Nothing changes
    // Expected output: returns an error with the message "{method} endpoint {invalid action} does not exist."
    it("should error on incorrect action", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson().expectJson()
        .sendJson({
            method: "SESSION",
            action: "invalid action"
        })
        .expectJson({ Error: "Session endpoint invalid action does not exist." })
        .sendJson({
            method: "FRIENDS",
            action: "invalid action"
        })
        .expectJson({ Error: "Friends endpoint invalid action does not exist." })
        .sendJson({
            method: "REQUESTS",
            action: "invalid action"
        })
        .expectJson({ Error: "Requests endpoint invalid action does not exist." })
        .close();
    });

    // Input: The user provides invalid json
    // Expected behavior: Nothing changes
    // Expected output: returns an error with the message "Received data is not formatted correctly"
    it("should error on invalid json", async () => {
        await request(server).ws("/socket", { headers: { "user-id": "testUser1" } }).expectJson().expectJson()
        .sendText("{ {{]/test: 10")
        .expectJson({ Error: "Received data is not formatted correctly" })
        .close();
    });

    // Input: The user connects
    // Expected behavior: Nothing changes
    // Expected output: received a ping
    it("should start ping pong", async () => {
        let pingCount = 0;
        const socket = await request(server).ws("/socket", { headers: { "user-id": "testUser1" } });

        await socket.on("ping", () => {
            pingCount++;
        });

        expect(pingCount).toBe(1);
        await socket.close();
    });

    // Input: The user sends a pong
    // Expected behavior: Nothing changes
    // Expected output: received a ping after 20 seconds
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
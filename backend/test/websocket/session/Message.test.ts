import { server } from "@src/index";
import request from "superwstest";

describe("Session Message", () => {
    const message = {
        method: "SESSION",
        action: "message",
        body: {
            text: "this is a message between users",
            timestamp: new Date().toISOString()
        }
    }

    const message2 = {
        method: "SESSION",
        action: "message",
        body: {
            text: "this is another message between users!!@&$^(",
            timestamp: new Date().toISOString()
        }
    }

    // Input: The user is in a session
    // Expected behavior: The message is passed to all other members of the session
    // Expected output: None
    // ChatGPT usage: None
    it("should pass messages between session members", async () => {
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
        .expectJson();

        await socket2.sendJson({
            method: "SESSION",
            action: "join",
            body: {
                userId: "testUser1"
            }
        })
        .expectJson();
        await socket1.expectJson();

        await socket1.sendJson(message);
        await socket2.expectJson({ ...message, from: "testUser1" });

        await socket2.sendJson(message2);
        await socket1.expectJson({ ...message2, from: "testUser2" });

        await socket1.close();
        await socket2.close();
    });

    // Input: The user is not in a session
    // Expected behavior: Nothing changes
    // Expected output: returns an error with the message "User is not in a session."
    // ChatGPT usage: None
    it("should do nothing when not in a session", async () => {
        const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson();

        await socket1.sendJson(message)
        .expectJson({
            Error: "User is not in a session."
        });

        await socket1.close();
    });
});
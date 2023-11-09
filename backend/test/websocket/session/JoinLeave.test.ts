import { server } from "@src/index"; // Assuming your Express app instance is exported from index.ts
import request from 'superwstest';

describe("GET /me", () => {
    beforeEach((done) => {
        server.listen(0, 'localhost', done);
    });

    afterEach((done) => {
        server.close(done);
    });

    test("responds with 200 OK", async () => {
        const messages: string[] = [];
        const response = await request(server).get("/users/test");
        expect(response.body).toBe("");
        //await request(server).ws("/socket", { headers: { "user-id": "test" }}).expectJson({});
        //const respone = await request(server)
        // const ws = new WebSocket("ws://localhost:3000/socket", { headers: { "user-id": "test"} });
        // ws.send("Hello, WebSocket!");
        // ws.on("message", function message(data) {
        //     messages.push(data.toString());
        //     console.log(messages);
        // });
    });
});
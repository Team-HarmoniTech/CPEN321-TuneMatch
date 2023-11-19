import { server } from "@src/index";
import { userService } from "@src/services";
import request from 'superwstest';

describe("Delete user", () => {
    // Input: user-id is an existing user
    // Expected status code: 200
    // Expected behavior: Return current user's data
    // Expected output: User has been deleted!
    test("Existing user", async () => {
      const res = await request(server)
      .delete('/me/delete')
      .set('user-id', 'testUser1');

      expect(res.statusCode).toBe(200);
      expect(res.text).toBe("User has been deleted!");
    });

    // Input: user-id doesn't exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: This executing user does not exist
    test("Non-existing user", async () => {
      const res = await request(server)
      .delete('/me/delete')
      .set('user-id', 'nonExistingUserId');

      expect(res.statusCode).toBe(401);
      expect(res.body).toEqual({ error: "This executing user does not exist" });
    });

    // Input: user is in an active session
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: Cannot delete a user with an active websocket
    test("User is in active session", async () => {
      const socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
      .expectJson()
      .expectJson()
      .sendJson({
          method: "SESSION",
          action: "join"
      });

      const startTime = Date.now();
        const timeout = 10000;
        while (Date.now() - startTime < timeout) {
            if ((await userService.getUserBySpotifyId("testUser1")).session) {
                expect((await userService.getUserBySpotifyId("testUser1")).session).toBeDefined();
                break;
            }
        
            // Poll every 500 milliseconds (adjust as needed)
            await new Promise(resolve => setTimeout(resolve, 500));
        }
      expect((await userService.getUserBySpotifyId("testUser1")).session).toBeDefined();

      const res = await request(server)
      .delete('/me/delete')
      .set('user-id', 'testUser1');

      expect(res.statusCode).toBe(400);
      expect(res.body).toEqual({ error : "Cannot delete a user with an active websocket" });

      await socket1.close();
    });
});
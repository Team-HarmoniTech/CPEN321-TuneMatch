import { server } from "@src/index";
import request from 'supertest';

describe("Delete user", () => {
    // Input: user-id is an existing user
    // Expected status code: 200
    // Expected behavior: Return current user's data
    // Expected output: User has been deleted!
    test("Existing user", async () => {

    });
  
    // Input: user-id doesn't exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: This executing user does not exist
    test("Non-existing user", async () => {

    });

    // Input: user is in an active session
    // Expected status code: 403
    // Expected behavior: Return error message
    // Expected output: Cannot delete a user with an active websocket
    test("User is in active session", async () => {

    });
  });
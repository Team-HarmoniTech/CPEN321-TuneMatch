import { server } from "@src/index";
import request from 'supertest';

describe("Get match", () => {
    // Input: user-id is an existing user, spotify_id is an existing user
    // Expected status code: 200
    // Expected behavior: Return matched users data
    // Expected output: userId, username, profile-pic, match
    test("Existing executer and existing searched", async () => {

    });

    // Input: user-id is an existing user, spotify_id doesn't exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: User not found
    test("Existing executer and non existing searched", async () => {

    });

    // Input: user-id doesn’t exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: This executing user does not exist
    test("Non-existing executer", async () => {

    });
  });
import { server } from "@src/index";
import request from 'supertest';

describe("Get self", () => {
    // Input: user-id is an existing user
    // Expected status code: 200
    // Expected behavior: Return list of users related to search param
    // Expected output: {userId, username, profile-pic, match}[]
    test("Existing user", async () => {

    });
  
    // Input: q is not included
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: Error JSON indicating which fields were invalid
    test("Missing q", async () => {

    });

    // Input: user-id doesn’t exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: This executing user does not exist
    test("Non-existing user", async () => {

    });
  });
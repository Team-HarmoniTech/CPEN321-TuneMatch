import { server } from "@src/index";
import request from 'supertest';

describe("Get self", () => {
    // Input: user-id is an existing user, valid user data
    // Expected status code: 200
    // Expected behavior: Return list of users related to search param
    // Expected output: {userId, username, profile-pic, match}[]
    test("Existing user with valid user data", async () => {

    });
  
    // Input: invalid user data
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: Error JSON indicating which fields were invalid
    test("Invalid user data", async () => {

    });

    // Input: Body wihtout any fields
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: At least one of username, top_artists, top_genres, pfp_url, bio must be provided
    test("Body without fields", async () => {

    });

    // Input: user-id doesn’t exist
    // Expected status code: 401
    // Expected behavior: Return error message
    // Expected output: This executing user does not exist
    test("Non-existing user", async () => {

    });
  });
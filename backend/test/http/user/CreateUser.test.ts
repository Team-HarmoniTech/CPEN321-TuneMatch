import { server } from "@src/index";
import request from 'supertest';

describe("Create new user", () => {
    // Input: valid user data
    // Expected status code: 201
    // Expected behavior: Return created user's data
    // Expected output: userId, username, profile-pic, bio, top artists, top genres
    test("Valid user data", async () => {

    });
  
    // Input: invalid user data
    // Expected status code: 400
    // Expected behavior: Return error message
    // Expected output: Error JSON indicating which fields were invalid
    test("Invalid user data", async () => {

    });
  });
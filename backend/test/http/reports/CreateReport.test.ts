import { server } from "@src/index";
import request from 'superwstest';

describe("Create report", () => {
    // Input: Valid report data
    // Expected status code: 200
    // Expected behavior: Return generated report
    // Expected output: Generated report
    test("Valid user data", async () => {
        const res = await request(server)
            .post('/reports/create')
            .set('user-id', 'testUser1') // Replace with a valid user ID
            .send({
                offenderId: 'testUser2', // Replace with a valid offender user ID
                reason: 'Sample Reason',
                text: 'Description of the report',
                context: 'Context or additional details'
            })
    
        expect(res.statusCode).toBe(200);
        expect(res.body).toHaveProperty('id');
        expect(res.body).toHaveProperty('offending_user_id', 2);
        expect(res.body).toHaveProperty('reporting_user_id', 1);
        expect(res.body).toHaveProperty('reason');
        expect(res.body).toHaveProperty('reason_text');
        expect(res.body).toHaveProperty('report_context');
        expect(res.body).toHaveProperty('timestamp');
      });

    // Input: Invalid report data (missing offenderId)
    // Expected status code: 200
    // Expected behavior: Return error message
    // Expected output: Generated report
    test("Valid user data", async () => {
        const res = await request(server)
            .post('/reports/create')
            .set('user-id', 'testUser1') // Replace with a valid user ID
            .send({
                reason: 'Sample Reason',
                text: 'Description of the report',
                context: 'Context or additional details'
            })

        expect(res.statusCode).toBe(400);
        expect(res.body).toEqual({
            "errors": [
                {
                    "type": "field",
                    "msg": "Invalid value",
                    "path": "offenderId",
                    "location": "body"
                }
            ]
          });
    
      });
});
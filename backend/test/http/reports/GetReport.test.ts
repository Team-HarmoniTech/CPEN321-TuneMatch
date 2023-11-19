import { server } from "@src/index";
import request from 'superwstest';

describe("Get report", () => {
    // Input: Valid dates
    // Expected status code: 200
    // Expected behavior: Return list of reports
    // Expected output: List of reports
    test("Valid user data with data query", async () => {
        const dateFrom = new Date(2023, 10, 1).toISOString();
        const dateTo = new Date(2023, 11, 31).toISOString();

        const res = await request(server)
            .get('/reports')
            .query({ dateFrom, dateTo })

        console.log(res.body);

        expect(res.statusCode).toBe(400);
        expect(Array.isArray(res.body)).toBeTruthy();
        res.body.forEach(report => {
            expect(report).toHaveProperty('id');
            expect(report).toHaveProperty('offending_user_id', 2);
            expect(report).toHaveProperty('reporting_user_id', 1);
            expect(report).toHaveProperty('reason');
            expect(report).toHaveProperty('reason_text');
            expect(report).toHaveProperty('report_context');
            expect(report).toHaveProperty('timestamp');
        }); 
    });

    // Input: Valid dates
    // Expected status code: 200
    // Expected behavior: Return list of reports
    // Expected output: List of reports
    test("Valid user data without date query", async () => {
        const res = await request(server)
            .get('/reports')

        console.log(res.body);

        expect(res.statusCode).toBe(400);
        expect(Array.isArray(res.body)).toBeTruthy();
        res.body.forEach(report => {
            expect(report).toHaveProperty('id');
            expect(report).toHaveProperty('offending_user_id', 2);
            expect(report).toHaveProperty('reporting_user_id', 1);
            expect(report).toHaveProperty('reason');
            expect(report).toHaveProperty('reason_text');
            expect(report).toHaveProperty('report_context');
            expect(report).toHaveProperty('timestamp');
        });

        expect(res.body.length).toBe(2);
    });
});
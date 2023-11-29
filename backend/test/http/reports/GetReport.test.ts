import { server } from "@src/index";
import { database } from "@src/services";
import request from 'superwstest';
import { originalDate } from "../../globalSetup";

describe("Get report", () => {
    beforeEach(async () => {
        global.Date = originalDate;
        global.Date.now = originalDate.now;

        await database.report.createMany({
            data: [
                {
                    id: 2,
                    offending_user_id: 1,
                    reporting_user_id: 2,
                    reason: "PLAYLIST_ABUSE",    
                    reason_text: "Abusing playlist",      
                    report_context: "Context or additional details",
                    timestamp: new Date("2024-01-15T00:00:00.000Z")
                },
                {
                    id: 1,
                    offending_user_id: 2,
                    reporting_user_id: 1,
                    reason: "OFFENSIVE_LANGUAGE",    
                    reason_text: "Bad behaviour",      
                    report_context: "Context or additional details",
                    timestamp: new Date("2023-11-15T00:00:00.000Z")
                },
                {
                    id: 2,
                    offending_user_id: 1,
                    reporting_user_id: 2,
                    reason: "PLAYLIST_ABUSE",    
                    reason_text: "Abusing playlist",      
                    report_context: "Context or additional details",
                    timestamp: new Date("2022-12-01T00:00:00.000Z")
                },
            ]
        });
    });

    // Input: Valid dates
    // Expected status code: 200
    // Expected behavior: Return list of reports
    // Expected output: List of reports
    // ChatGPT usage: None
    test("Valid user data with data query", async () => {
        const dateFrom = new Date("2023-10-01T00:00:00.000Z").toISOString();
        const dateTo = new Date("2023-11-29T00:00:00.000Z").toISOString();

        const res = await request(server)
            .get('/reports')
            .query({ dateFrom, dateTo });

        expect(res.statusCode).toBe(200);
        expect(res.body).toHaveLength(1);
        console.log(res.body);
        expect(res.body).toStrictEqual([
            {
                id: 1,
                offending_user_id: 2,
                reporting_user_id: 1,
                reason: "OFFENSIVE_LANGUAGE",    
                reason_text: "Bad behaviour",      
                report_context: "Context or additional details",
                timestamp: "2023-11-15T00:00:00.000Z"
            },
        ]);
    });

    // Input: Valid dates
    // Expected status code: 200
    // Expected behavior: Return list of reports
    // Expected output: List of reports
    // ChatGPT usage: None
    test("Valid user data without date query", async () => {
        const res = await request(server)
            .get('/reports');

        expect(res.statusCode).toBe(200);
        console.log(res.body);
        expect(res.body).toHaveLength(3);
        expect(res.body).toStrictEqual([
            {
                id: 2,
                offending_user_id: 1,
                reporting_user_id: 2,
                reason: "PLAYLIST_ABUSE",    
                reason_text: "Abusing playlist",      
                report_context: "Context or additional details",
                timestamp: "2024-01-15T00:00:00.000Z"
            },
            {
                id: 1,
                offending_user_id: 2,
                reporting_user_id: 1,
                reason: "OFFENSIVE_LANGUAGE",    
                reason_text: "Bad behaviour",      
                report_context: "Context or additional details",
                timestamp: "2023-11-15T00:00:00.000Z"
            },
            {
                id: 2,
                offending_user_id: 1,
                reporting_user_id: 2,
                reason: "PLAYLIST_ABUSE",    
                reason_text: "Abusing playlist",      
                report_context: "Context or additional details",
                timestamp: "2022-12-01T00:00:00.000Z"
            }
        ])
    });
});
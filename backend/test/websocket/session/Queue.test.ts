import { server } from "@src/index";
import { sessionService } from "@src/services";
import request from "superwstest";
import { originalDate } from "test/globalSetup";

describe("Session Queue", () => {
    const testQueue = [
        {
            uri: "testUri",
            durationMs: 2000, // 2 seconds
            title: `2 second song #1`,
            artist: "test artist"
        },
        {
            uri: "testUri",
            durationMs: 2000, // 2 seconds
            title: `2 second song #2`,
            artist: "test artist"
        },
        {
            uri: "testUri",
            durationMs: 2000, // 2 seconds
            title: `2 second song #3`,
            artist: "test artist"
        }
    ];
    let socket1;

    beforeEach(async () => {
        socket1 = request(server).ws("/socket", { headers: { "user-id": "testUser1" } })
        .expectJson()
        .expectJson();

        await socket1.sendJson({
            method: "SESSION",
            action: "join"
        }).expectJson();

        global.Date = originalDate;
        global.Date.now = originalDate.now;
    });

    afterEach(async () => {
        await socket1.close();
    });


    // Input: none
    // Expected behavior: see the it statement
    // Expected output: none
       // ChatGPT usage: None
    it("should create with an empty queue", async () => {
        expect(await sessionService.getQueue(1)).toHaveProperty("running", false);
        expect(await sessionService.getQueue(1)).toHaveProperty("queue", []);
    });

    // Input: none
    // Expected behavior: see the it statement
    // Expected output: none
    it("should pop songs after the correct amount of time", async () => {
        let queueData;
        for (let i = 0; i < 5; i++) {
            await socket1.sendJson({
                method: "SESSION",
                action: "queueAdd",
                body: {
                    uri: "testUri",
                    durationMs: 2000, // 2 seconds
                    title: `2 second song #${i + 1}`,
                    artist: "test artist"
                }
            });
        }

        await new Promise(f => setTimeout(f, 3000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("2 second song #2");
        expect(queueData.queue).toHaveLength(4);
        
        await new Promise(f => setTimeout(f, 6000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("2 second song #5");
        expect(queueData.queue).toHaveLength(1);
    });

    // Input: queue replace
    // Expected behavior: see the it statement
    // Expected output: none
    it("should replace the entire queue while paused", async () => {
        await socket1.sendJson({
            method: "SESSION",
            action: "queueReplace",
            body: testQueue
        });

        await new Promise(f => setTimeout(f, 2000));
        const queueData = await sessionService.getQueue(1);
        expect(queueData.running).toBe(false);
        expect(queueData.queue).toStrictEqual(testQueue);
    });

    // Input: queue replace
    // Expected behavior: see the it statement
    // Expected output: none
    it("should replace the entire queue while playing", async () => {
        let queueData;
        for (let i = 0; i < 5; i++) {
            await socket1.sendJson({
                method: "SESSION",
                action: "queueAdd",
                body: {
                    uri: "testUri",
                    durationMs: 2000, // 2 seconds
                    title: `2 second song #${i + 1}`,
                    artist: "test artist"
                }
            });
        }

        await new Promise(f => setTimeout(f, 3000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("2 second song #2");
        expect(queueData.queue).toHaveLength(4);


        await socket1.sendJson({
            method: "SESSION",
            action: "queueReplace",
            body: testQueue
        });

        await new Promise(f => setTimeout(f, 2000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.running).toBe(false);
        expect(queueData.queue).toStrictEqual(testQueue);
    });

    // Input: add songs
    // Expected behavior: see the it statement
    // Expected output: none
    it("should add songs while paused", async () => {
        let queueData;
        await socket1.sendJson({
            method: "SESSION",
            action: "queueReplace",
            body: testQueue
        });

        await socket1.sendJson({
            method: "SESSION",
            action: "queueAdd",
            body: {
                uri: "testUri",
                durationMs: 2000, // 2 seconds
                title: "Add to the front",
                artist: "test artist",
                index: 0,
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("Add to the front");
        expect(queueData.queue).toHaveLength(4);

        await socket1.sendJson({
            method: "SESSION",
            action: "queueAdd",
            body: {
                uri: "testUri",
                durationMs: 2000, // 2 seconds
                title: "Add to the end",
                artist: "test artist"
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[queueData.queue.length - 1].title).toBe("Add to the end");
        expect(queueData.queue).toHaveLength(5);

        await socket1.sendJson({
            method: "SESSION",
            action: "queueAdd",
            body: {
                uri: "testUri",
                durationMs: 2000, // 2 seconds
                title: "Add to the second index",
                artist: "test artist",
                index: 2,
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[2].title).toBe("Add to the second index");
        expect(queueData.queue).toHaveLength(6);
    });

    // Input: add songs
    // Expected behavior: see the it statement
    // Expected output: none
    it("should add songs while playing", async () => {
        let queueData;
        for (let i = 0; i < 3; i++) {
            await socket1.sendJson({
                method: "SESSION",
                action: "queueAdd",
                body: {
                    uri: "testUri",
                    durationMs: 10000, // 10 seconds
                    title: `10 second song #${i + 1}`,
                    artist: "test artist"
                }
            });
        }

        await socket1.sendJson({
            method: "SESSION",
            action: "queueAdd",
            body: {
                uri: "testUri",
                durationMs: 2000, // 2 seconds
                title: "Add to the front",
                artist: "test artist",
                index: 0,
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[1].title).toBe("Add to the front");
        expect(queueData.queue).toHaveLength(4);

        await socket1.sendJson({
            method: "SESSION",
            action: "queueAdd",
            body: {
                uri: "testUri",
                durationMs: 2000, // 2 seconds
                title: "Add to the end",
                artist: "test artist"
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[queueData.queue.length - 1].title).toBe("Add to the end");
        expect(queueData.queue).toHaveLength(5);

        await socket1.sendJson({
            method: "SESSION",
            action: "queueAdd",
            body: {
                uri: "testUri",
                durationMs: 2000, // 2 seconds
                title: "Add to the second index",
                artist: "test artist",
                index: 2,
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[3].title).toBe("Add to the second index");
        expect(queueData.queue).toHaveLength(6);
    });

    // Input: skip
    // Expected behavior: see the it statement
    // Expected output: none
    it("should skip the currently playing song", async () => {
        for (let i = 0; i < 5; i++) {
            await socket1.sendJson({
                method: "SESSION",
                action: "queueAdd",
                body: {
                    uri: "testUri",
                    durationMs: 2000, // 2 seconds
                    title: `2 second song #${i + 1}`,
                    artist: "test artist"
                }
            });
        }

        await new Promise(f => setTimeout(f, 1000));
        for (let i = 0; i < 3; i++) {
            await socket1.sendJson({
                method: "SESSION",
                action: "queueSkip"
            });
        }

        await new Promise(f => setTimeout(f, 1000));
        const queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("2 second song #4");
        expect(queueData.queue).toHaveLength(2);
    });

    // Input: skip
    // Expected behavior: see the it statement
    // Expected output: none
    it("should skip the first song in the queue", async () => {
        await socket1.sendJson({
            method: "SESSION",
            action: "queueReplace",
            body: [ ...testQueue, ...testQueue ]
        });

        await new Promise(f => setTimeout(f, 1000));
        for (let i = 0; i < 4; i++) {
            await socket1.sendJson({
                method: "SESSION",
                action: "queueSkip"
            });
        }

        await new Promise(f => setTimeout(f, 1000));
        const queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("2 second song #2");
        expect(queueData.queue[1].title).toBe("2 second song #3");
        expect(queueData.queue).toHaveLength(2);
    });

    // Input: drag
    // Expected behavior: see the it statement
    // Expected output: none
    it("should drag songs", async () => {
        await socket1.sendJson({
            method: "SESSION",
            action: "queueReplace",
            body: testQueue
        }).sendJson({
            method: "SESSION",
            action: "queueDrag",
            body: {
                startIndex: 0,
                endIndex: 1
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        let queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("2 second song #2");
        expect(queueData.queue[1].title).toBe("2 second song #1");
        expect(queueData.queue[2].title).toBe("2 second song #3");
        expect(queueData.queue).toHaveLength(3);

        await socket1.sendJson({
            method: "SESSION",
            action: "queueDrag",
            body: {
                startIndex: 2,
                endIndex: 0
            }
        });
        
        await new Promise(f => setTimeout(f, 1000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("2 second song #3");
        expect(queueData.queue[1].title).toBe("2 second song #2");
        expect(queueData.queue[2].title).toBe("2 second song #1");
        expect(queueData.queue).toHaveLength(3);
    });

    // Input: drag with an end index of -1
    // Expected behavior: see the it statement
    // Expected output: none
    it("should drag songs to the back", async () => {
        await socket1.sendJson({
            method: "SESSION",
            action: "queueReplace",
            body: testQueue
        }).sendJson({
            method: "SESSION",
            action: "queueDrag",
            body: {
                startIndex: 0,
                endIndex: -1
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        const queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("2 second song #2");
        expect(queueData.queue[1].title).toBe("2 second song #3");
        expect(queueData.queue[2].title).toBe("2 second song #1");
        expect(queueData.queue).toHaveLength(3);
    });

    // Input: drag with an end index of 0
    // Expected behavior: see the it statement
    // Expected output: none
    it("should drag songs to the front", async () => {
        await socket1.sendJson({
            method: "SESSION",
            action: "queueReplace",
            body: testQueue
        }).sendJson({
            method: "SESSION",
            action: "queueDrag",
            body: {
                startIndex: 2,
                endIndex: 0
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        const queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("2 second song #3");
        expect(queueData.queue[1].title).toBe("2 second song #1");
        expect(queueData.queue[2].title).toBe("2 second song #2");
        expect(queueData.queue).toHaveLength(3);
    });

    // Input: pause and resume
    // Expected behavior: see the it statement
    // Expected output: none
    it("should pause and resume from the same place", async () => {
        let queueData;
        for (let i = 0; i < 2; i++) {
            await socket1.sendJson({
                method: "SESSION",
                action: "queueAdd",
                body: {
                    uri: "testUri",
                    durationMs: 5000, // 2 seconds
                    title: `5 second song #${i + 1}`,
                    artist: "test artist"
                }
            });
        }

        await new Promise(f => setTimeout(f, 3000));
        await socket1.sendJson({
            method: "SESSION",
            action: "queuePause"
        });
        await new Promise(f => setTimeout(f, 5000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("5 second song #1");

        await socket1.sendJson({
            method: "SESSION",
            action: "queueResume"
        });
        await new Promise(f => setTimeout(f, 1000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("5 second song #1");
        
        await new Promise(f => setTimeout(f, 3000));
        queueData = await sessionService.getQueue(1);
        expect(queueData).toBe({});
        expect(queueData.queue[0].title).toBe("5 second song #2");
    }, 15000);

    // Input: seek method
    // Expected behavior: see the it statement
    // Expected output: none
    it("should seek inside the current song", async () => {
        let queueData;
        await socket1.sendJson({
            method: "SESSION",
            action: "queueAdd",
            body: {
                uri: "testUri",
                durationMs: 20000, // 2 seconds
                title: "20 second song",
                artist: "test artist"
            }
        });

        await new Promise(f => setTimeout(f, 1000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.queue[0].title).toBe("20 second song");
        await socket1.sendJson({
            method: "SESSION",
            action: "queueSeek",
            body: {
                seekPosition: 17000
            }
        });

        await new Promise(f => setTimeout(f, 4000));
        queueData = await sessionService.getQueue(1);
        expect(queueData.running).toBe(false);
        expect(queueData.queue).toHaveLength(0);
    });
});
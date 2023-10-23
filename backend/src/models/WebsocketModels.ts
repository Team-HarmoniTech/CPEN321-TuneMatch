import WebSocket = require("ws");

export type SocketMethod = "SESSION" | "PLAYING";
export abstract class SocketMessage {
    constructor(
        public method: SocketMethod,
        public action: string,
        public body?: object,
        public from?: string
    ) { }
}
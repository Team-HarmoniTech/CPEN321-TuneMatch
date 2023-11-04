import WebSocket = require("ws");

export type SocketMethod = "SESSION" | "FRIENDS" | "REQUESTS";
export class SocketMessage {
  constructor(
    public method: SocketMethod,
    public action: string,
    public body?: any,
    public from?: string,
  ) {}
}

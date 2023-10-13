import { Mutex } from "async-mutex";
import { SocketMessage } from "./WebsocketModels";

export class SessionMessage extends SocketMessage {
    constructor(
        public action: string,
        public body?: object
    ) {
        super("SESSION", action, body);
    }
}

export type SessionQueue = {
    queue: string[],
    lock: Mutex
}


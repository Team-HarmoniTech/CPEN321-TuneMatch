import { BiMap } from "@jsdsl/bimap";
import { PrismaClient } from "@prisma/client";
import { Mutex } from "async-mutex";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class WebSocketService {
    connections: BiMap<number, WebSocket> = new BiMap();
    private connectionsLock: Mutex = new Mutex();
    private database = new PrismaClient();

    async addConnection(userId: number, socket: WebSocket) {
        await this.connectionsLock.runExclusive(() => {
            this.connections.set(userId, socket);
        });
    }

    async removeConnectionById(userId: number) {
        await this.connectionsLock.runExclusive(() => {
            this.connections.removeByKey(userId);
        });
    }

    async removeConnectionBySocket(socket: WebSocket) {
        await this.connectionsLock.runExclusive(() => {
            this.connections.removeByValue(socket);
        });
    }

    async retrieveById(userId: number): Promise<WebSocket> {
        return await this.connectionsLock.runExclusive(() => {
            const socket = this.connections.getFromKey(userId);
            return socket;
        });
    }

    async retrieveBySocket(socket: WebSocket): Promise<number> {
        return await this.connectionsLock.runExclusive(() => {
            const id = this.connections.getFromValue(socket);
            return id;
        });
    }

    async broadcast(userIds: number[], message: SocketMessage) {
        await this.connectionsLock.runExclusive(() => {
            userIds.forEach(id => {
                const socket = this.connections.getFromKey(id);
                if (socket) socket.send(JSON.stringify(message));
            });
        });
    }
}
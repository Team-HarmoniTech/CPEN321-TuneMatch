import { BiMap } from "@jsdsl/bimap";
import { Mutex } from "async-mutex";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class WebSocketService {
    connections: BiMap<number, WebSocket> = new BiMap();
    private connectionsLock: Mutex = new Mutex();

    async addConnection(userId: number, socket: WebSocket) {
        await this.connectionsLock.runExclusive(async () => {
            const duplicate = !!(await this.connections.getFromKey(userId));
            if (duplicate) {
                socket.close(1008, `Duplicate User ${userId} tried to connect`);
                return;
            }
            this.connections.set(userId, socket);
            console.log(`WEBSOCKET: User ${userId} connected`);
        });
    }

    async removeConnectionById(userId: number) {
        await this.connectionsLock.runExclusive(() => {
            this.connections.removeByKey(userId);
            console.log(`WEBSOCKET: User ${userId} disconnected`);
        });
    }

    async removeConnectionBySocket(socket: WebSocket) {
        await this.connectionsLock.runExclusive(() => {
            const id = this.connections.getFromValue(socket);
            this.connections.removeByValue(socket); 
            console.log(`WEBSOCKET: User ${id} disconnected`);
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
import { BiMap } from "@jsdsl/bimap";
import { SocketMessage } from "@models/WebsocketModels";
import logger from "@src/logger";
import logger from "@src/logger";
import { Mutex } from "async-mutex";
import WebSocket = require("ws");

export class WebSocketService {
  connections: BiMap<number, WebSocket> = new BiMap();
  private connectionsLock: Mutex = new Mutex();

  // ChatGPT Usage: No
  async addConnection(userId: number, socket: WebSocket) {
    await this.connectionsLock.runExclusive(async () => {
      const duplicate = !!(await this.connections.getFromKey(userId));
      if (duplicate) {
        socket.close(1008, `Duplicate User ${userId} tried to connect`);
        return;
      }
      this.connections.set(userId, socket);
      logger.log(`WEBSOCKET: User ${userId} connected`);
    });
  }

  // ChatGPT Usage: No
  async removeConnectionById(userId: number) {
    await this.connectionsLock.runExclusive(() => {
      this.connections.removeByKey(userId);
      logger.log(`WEBSOCKET: User ${userId} disconnected`);
    });
  }

  // ChatGPT Usage: No
  async removeConnectionBySocket(socket: WebSocket) {
    await this.connectionsLock.runExclusive(() => {
      const id = this.connections.getFromValue(socket);
      this.connections.removeByValue(socket);
      logger.log(`WEBSOCKET: User ${id} disconnected`);
    });
  }

  // ChatGPT Usage: No
  async retrieveById(userId: number): Promise<WebSocket> {
    return await this.connectionsLock.runExclusive(() => {
      const socket = this.connections.getFromKey(userId);
      return socket;
    });
  }

  // ChatGPT Usage: No
  async retrieveBySocket(socket: WebSocket): Promise<number> {
    return await this.connectionsLock.runExclusive(() => {
      const id = this.connections.getFromValue(socket);
      return id;
    });
  }

  // ChatGPT Usage: No
  async broadcast(userIds: number[], message: SocketMessage) {
    await this.connectionsLock.runExclusive(() => {
      userIds.forEach((id) => {
        const socket = this.connections.getFromKey(id);
        if (socket) socket.send(JSON.stringify(message));
      });
    });
  }
}

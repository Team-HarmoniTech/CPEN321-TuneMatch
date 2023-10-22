import { WebSocket } from "ws";
import { database } from "..";
import { SessionMessage } from "../models/SessionModels";

export class SessionController {
    private sessionDB = database.session;

    // Websocket Routes
    async acceptRequest(ws: WebSocket, req: SessionMessage, currentUserId: number) {
        switch (req.action) {
            case "join":
                await this.join(ws, req, currentUserId);
                break;
            case "leave":
                await this.leave(ws, req, currentUserId);
                break;
            case "queue":
                await this.queue(ws, req, currentUserId);
                break;
            case "message":
                await this.message(ws, req, currentUserId);
                break;
            default:
                ws.send(JSON.stringify({ Error: `Session endpoint ${req.action} does not exist.`}));
        }
    }

    async message(ws: WebSocket, req: SessionMessage, currentUserId: number) {

    }

    async queue(ws: WebSocket, req: SessionMessage, currentUserId: number) {

    }

    async join(ws: WebSocket, req: SessionMessage, currentUserId: number) {
        
    }

    async leave(ws: WebSocket, req: SessionMessage, currentUserId: number) {

    }
}
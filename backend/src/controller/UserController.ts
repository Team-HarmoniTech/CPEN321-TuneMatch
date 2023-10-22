import { NextFunction, Request, Response } from "express";
import { database, userService } from "..";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class UserController {
    private userDB = database.user;

    async get(req: Request, res: Response, next: NextFunction) {
        const user = userService.getUser(req.internal_id);
        res.send(user);
    }

    async topMatches(req: Request, res: Response, next: NextFunction) {
        const matches = ""
        res.send(matches);
    }

    async insert(req: Request, res: Response, next: NextFunction) {
        
    }

    async update(req: Request, res: Response, next: NextFunction) {
        
    }

    async delete(req: Request, res: Response, next: NextFunction) {

    }

    // Websocket Routes
    async updateCurrentlyPlaying(ws: WebSocket, message: SocketMessage, currentUserId: number) {
        message.body["from"] = currentUserId;
        userService.broadcastToFriends(currentUserId, message);
    }
}
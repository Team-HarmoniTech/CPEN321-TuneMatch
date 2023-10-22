import { User } from "@prisma/client";
import { NextFunction, Request, Response } from "express";
import { database, userMatchingService, userService } from "..";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class UserController {
    private userDB = database.user;

    async get(req: Request, res: Response, next: NextFunction) {
        const user = userService.getUser(req.internal_id);
        res.send(user);
    }

    async topMatches(req: Request, res: Response, next: NextFunction) {
        const matches = await userMatchingService.getTopMatches(req.currentUserId);
        res.send(matches);
    }

    async insert(req: Request, res: Response, next: NextFunction) {
        const user = await userService.upsertUser(req.body['userData']);
        userMatchingService.matchNewUser(user.id);
        res.send(user);
    }

    async update(req: Request, res: Response, next: NextFunction) {
        const user = await userService.upsertUser(req.body['userData'], req.currentUserId);
        res.send(user);
    }

    async delete(req: Request, res: Response, next: NextFunction) {
        const user = await userService.upsertUser(req.body['userData'], req.currentUserId);
        res.send(user);
    }

    // Websocket Routes
    async updateCurrentlyPlaying(ws: WebSocket, message: SocketMessage, currentUser: User) {
        try {
            message.body["from"] = currentUser.id;
            userService.broadcastToFriends(currentUser.id, message);
        } catch(err) {
            ws.send(JSON.stringify({ success: false, Error: err }));
        }
    }
}
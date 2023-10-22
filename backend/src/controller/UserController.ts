import { NextFunction, Request, Response } from "express";
import { userMatchingService, userService } from "..";
import { exportUsers } from "../models/SessionModels";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class UserController {

    // REST Routes
    async get(req: Request, res: Response, next: NextFunction) {
        const user = userService.getUserById(req.currentUserId);
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
        if (req.params['spotify_id'] && req.currentUserSpotifyId !== req.params['spotify_id']) {
            throw { message: 'You many not update other users', statusCode: 400 };
        }
        const user = await userService.upsertUser(req.body['userData'], req.currentUserId);
        res.send(user);
    }

    async delete(req: Request, res: Response, next: NextFunction) {
        if (req.params['spotify_id'] && req.currentUserSpotifyId !== req.params['spotify_id']) {
            throw { message: 'You many not delete other users', statusCode: 400 };
        }
        const user = await userService.upsertUser(req.body['userData'], req.currentUserId);
        res.send(user);
    }

    async getFriendsCurrentlyPlaying(req: Request, res: Response, next: NextFunction) {
        const friends = await userService.getUserFriends(req.currentUserId);
        res.send(exportUsers(friends, true));
    }

    // Websocket Routes
    async updateCurrentlyPlaying(ws: WebSocket, message: SocketMessage, currentUserId: number) {
        try {
            message.body["from"] = currentUserId;
            userService.broadcastToFriends(currentUserId, message);
        } catch(err) {
            ws.send(JSON.stringify({ success: false, Error: err }));
        }
    }
}
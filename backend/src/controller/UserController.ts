import { NextFunction, Request, Response } from "express";
import { userMatchingService, userService } from "..";
import { transformUser, transformUsers } from "../models/SessionModels";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class UserController {

    // REST Routes
    async get(req: Request, res: Response, next: NextFunction) {
        /* Use currentUserId for the /me endpoint */
        const userId = (req.url.startsWith("/me")) ? req.currentUserSpotifyId : req.params.spotify_id;
        const user = await userService.getUserBySpotifyId(userId);
        res.send(transformUser(user, (user) => {
            return req.query.fullProfile ? { bio:user.bio } : { };
        }));
    }

    async topMatches(req: Request, res: Response, next: NextFunction) {
        const matches = await userMatchingService.getTopMatches(req.currentUserId);
        res.send(transformUsers(matches, (user) => {
            return { match: user.match };
        }));
    }

    async insert(req: Request, res: Response, next: NextFunction) {
        const user = await userService.upsertUser(req.body.userData);
        userMatchingService.matchNewUser(user.id);
        res.send(user);
    }

    async update(req: Request, res: Response, next: NextFunction) {
        const user = await userService.upsertUser(req.body.userData, req.currentUserId);
        res.send(user);
    }

    async delete(req: Request, res: Response, next: NextFunction) {
        const user = await userService.deleteUser(req.currentUserId);
        res.send(user);
    }

    async searchUsers(req: Request, res: Response, next: NextFunction) {
        const options = await userService.searchUsers(req.params.search_term, Number(req.query.max));
        res.send(transformUsers(options));
    }

    // Websocket Routes
    async updateCurrentlyPlaying(ws: WebSocket, message: SocketMessage, currentUserId: number) {
        try {
            userService.broadcastToFriends(currentUserId, message);
        } catch(err) {
            ws.send(JSON.stringify({ success: false, Error: err.message }));
        }
    }
}
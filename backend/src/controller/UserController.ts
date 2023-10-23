import { NextFunction, Request, Response } from "express";
import { userMatchingService, userService } from "..";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class UserController {

    // REST Routes
    async get(req: Request, res: Response, next: NextFunction) {
        const user = await userService.getUserBySpotifyId(req.params.spotify_id);
        res.send(user);
    }

    async topMatches(req: Request, res: Response, next: NextFunction) {
        const matches = await userMatchingService.getTopMatches(req.currentUserId);
        res.send(matches.map(u => ({
            id: u.spotify_id,
            username: u.username,
            profilePic: u.pfp_url,
            match: u.match
        })));
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

    async getFriends(req: Request, res: Response, next: NextFunction) {
        const friends = await userService.getUserFriends(req.currentUserId);
        res.send(friends.map(u => ({
            id: u.spotify_id,
            username: u.username,
            profilePic: u.pfp_url,
            currentlyPlaying: u.currently_listening,
            session: !!u.sessionId
        })));
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
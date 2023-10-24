import { NextFunction, Request, Response } from "express";
import { userMatchingService, userService } from "..";
import { transformUser, transformUsers } from "../models/SessionModels";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class UserController {

    // REST Routes
    async get(req: Request, res: Response, next: NextFunction) {
        const user = await userService.getUserBySpotifyId(req.params.spotify_id);
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
        if (req.params.spotify_id && req.currentUserSpotifyId !== req.params.spotify_id) {
            throw { message: 'You many not update other users', statusCode: 400 };
        }
        const user = await userService.upsertUser(req.body.userData, req.currentUserId);
        res.send(user);
    }

    async delete(req: Request, res: Response, next: NextFunction) {
        if (req.params.spotify_id && req.currentUserSpotifyId !== req.params.spotify_id) {
            throw { message: 'You many not delete other users', statusCode: 400 };
        }
        const user = await userService.deleteUser(req.currentUserId);
        res.send(user);
    }

    async getFriends(req: Request, res: Response, next: NextFunction) {
        const friends = await userService.getUserFriends(req.currentUserId);
        res.send(transformUsers(friends, (user) => {
            return { currentlyPlaying: user.currently_listening, session: !!user.sessionId };
        }));
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
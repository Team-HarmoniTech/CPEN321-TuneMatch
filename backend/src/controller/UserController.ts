import { NextFunction, Request, Response } from "express";
import { userMatchingService, userService } from "..";
import { FriendsMessage, transformUser, transformUsers } from "../models/UserModels";
import WebSocket = require("ws");

export class UserController {

    // REST Routes
    async getUser(req: Request, res: Response, next: NextFunction) {
        /* Use currentUserId for the /me endpoint */
        const userId = (req.url.startsWith("/me")) ? req.currentUserSpotifyId : req.params.spotify_id;
        const user = await userService.getUserBySpotifyId(userId);
        if (!user) {
            throw { message: `User not found.`, statusCode: 400 };
        }
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

    async insertUser(req: Request, res: Response, next: NextFunction) {
        const user = await userService.createUser(req.body.userData);
        userMatchingService.matchNewUser(user.id);
        res.send(transformUser(user));
    }

    async updateUser(req: Request, res: Response, next: NextFunction) {
        const user = await userService.updateUser(req.body.userData, req.currentUserId);
        res.send(transformUser(user, (user) => {
            return { bio: user.bio };
        }));
    }

    async deleteUser(req: Request, res: Response, next: NextFunction) {
        await userService.deleteUser(req.currentUserId);
    }

    async searchUsers(req: Request, res: Response, next: NextFunction) {
        const options = await userService.searchUsers(req.params.search_term, Number(req.query.max));
        res.send(transformUsers(options));
    }

    // Websocket Route Dispatcher
    async acceptRequest(ws: WebSocket, message: FriendsMessage, currentUserId: number) {
        const func = (this)[message.action];
        if (!func) {
            ws.send(JSON.stringify({ Error: `Session endpoint ${message.action} does not exist.`}));
        } else {
            try {
                await func(ws, message, currentUserId);
            } catch (err) {
                ws.send(JSON.stringify(new FriendsMessage("error", err.message)));
            }
        }
    }

    // Websocket Routes
    async refresh(ws: WebSocket, message: FriendsMessage, currentUserId: number) {
        const friends = await userService.getUserFriends(currentUserId);

        ws.send(JSON.stringify(new FriendsMessage(
            "refresh", 
            transformUsers(friends, (user) => {
                return { 
                    currentlyPlaying: user.currently_playing, 
                    session: !!user.sessionId, 
                };
            })
        )));
    }

    async update(ws: WebSocket, message: FriendsMessage, currentUserId: number) {
        const user = await userService.updateUser({ currently_playing: message?.body.currentlyPlaying }, currentUserId);
        await userService.broadcastToFriends(currentUserId, 
            new FriendsMessage("update", transformUser(user, (user) => {
                return { 
                    currentlyPlaying: user.currently_playing, 
                    session: !!user.sessionId, 
                };
            }))
        );
    }
}
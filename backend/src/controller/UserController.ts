import { NextFunction, Request, Response } from "express";
import { userMatchingService, userService } from "..";
import { FriendsMessage, transformUser, transformUsers } from "../models/UserModels";
import WebSocket = require("ws");

export class UserController {

    // REST Routes
    // ChatGPT Usage: No
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

    // ChatGPT Usage: No
    async topMatches(req: Request, res: Response, next: NextFunction) {
        const matches = await userMatchingService.getTopMatches(req.currentUserId);
        res.send(transformUsers(matches, (user) => {
            return { match: user.match };
        }));
    }

    // ChatGPT Usage: No
    async insertUser(req: Request, res: Response, next: NextFunction) {
        console.log("hi")
        const user = await userService.createUser(req.body.userData);
        userMatchingService.matchNewUser(user.id);
        res.send(transformUser(user));
    }

    // ChatGPT Usage: No
    async updateUser(req: Request, res: Response, next: NextFunction) {
        const user = await userService.updateUser(req.body.userData, req.currentUserId);
        res.send(transformUser(user, (user) => {
            return { bio: user.bio };
        }));
    }

    // ChatGPT Usage: No
    async deleteUser(req: Request, res: Response, next: NextFunction) {
        await userService.deleteUser(req.currentUserId);
    }

    // ChatGPT Usage: No
    async searchUsers(req: Request, res: Response, next: NextFunction) {
        const options = await userService.searchUsers(req.body["search_term"], Number(req.query.max));
        res.send(transformUsers(options.filter(u => u.id !== req.currentUserId)));
    }

    // Websocket Route Dispatcher
    // ChatGPT Usage: No
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
    // ChatGPT Usage: No
    async refresh(ws: WebSocket, message: FriendsMessage, currentUserId: number) {
        const friends = await userService.getUserFriends(currentUserId);

        ws.send(JSON.stringify(new FriendsMessage(
            "refresh", 
            transformUsers(friends, (user) => {
                return { 
                    currentSong: user.current_song, 
                    currentSource: user.current_source
                };
            })
        )));
    }

    // ChatGPT Usage: No
    async update(ws: WebSocket, message: FriendsMessage, currentUserId: number) {
        const user = await userService.updateUserStatus(currentUserId, message?.body.song, message?.body.source);
        await userService.broadcastToFriends(currentUserId, 
            new FriendsMessage("update", transformUser(user, (user) => {
                return { 
                    currentSong: user.current_song, 
                    currentSource: user.current_source
                };
            }))
        );
    }
}
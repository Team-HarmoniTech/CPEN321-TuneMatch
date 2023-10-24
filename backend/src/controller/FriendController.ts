import { NextFunction, Request, Response } from "express";
import { userService } from "..";
import { transformUsers } from "../models/SessionModels";
import WebSocket = require("ws");

export class FriendController {

    // REST Routes
    async getFriends(req: Request, res: Response, next: NextFunction) {
        const friends = await userService.getUserFriends(req.currentUserId);
        res.send(transformUsers(friends, (user) => {
            return { currentlyPlaying: user.currently_listening, session: !!user.sessionId };
        }));
    }

    async getRequests(req: Request, res: Response, next: NextFunction) {
        const friendRequests = await userService.getUserFriendsRequests(req.currentUserId);
        res.send({ 
            requesting: transformUsers(friendRequests.requesting), 
            requested: transformUsers(friendRequests.requested)
        });
    }

    async add(req: Request, res: Response, next: NextFunction) {
        if (req.currentUserSpotifyId === req.params.spotify_id) {
            throw { message: `You cannot add yourself.`, statusCode: 400 };
        }
        await userService.addFriend(req.currentUserId, req.params.spotify_id);
    }

    async remove(req: Request, res: Response, next: NextFunction) {
        if (req.currentUserSpotifyId === req.params.spotify_id) {
            throw { message: `You cannot remove yourself.`, statusCode: 400 };
        }
        await userService.removeFriend(req.currentUserId, req.params.spotify_id);
    }
}
import { User } from "@prisma/client";
import { NextFunction, Request, Response } from "express";
import { database } from "../services";

// ChatGPT Usage: No
export async function findCurrentUser(req: Request, res: Response, next: NextFunction) {
    const userId: string = req.headers['user-id'] || "";
    const user: User = await database.user.findUnique({
        where: { spotify_id: userId }
    });
    if (!user && userId !== "") {
        res.status(400).send({ error: 'This executing user does not exist'});
        return;
    }
    if (user && user.is_banned) {
        res.status(400).send({ error: 'This executing user is banned'});
        return;
    }
    if (user) {
        req.currentUserId = user.id;
        req.currentUserSpotifyId = user.spotify_id;
    }
    next();
}
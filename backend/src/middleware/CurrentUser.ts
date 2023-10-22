import { User } from "@prisma/client";
import { NextFunction, Request, Response } from "express";
import { database } from "..";

export async function findCurrentUser(req: Request, res: Response, next: NextFunction) {
    const userId: string = req.headers['user-id'];
    const user: User = await database.user.findFirst({
        where: {
            internal_id: userId
        }
    });
    if (!user || !userId) {
        res.status(400).send({ error: 'This user does not exist'});
    }
    if (user.is_banned) {
        res.status(400).send({ error: 'This user is banned'});
    }
    req.currentUserId = user.id;
    next();
}
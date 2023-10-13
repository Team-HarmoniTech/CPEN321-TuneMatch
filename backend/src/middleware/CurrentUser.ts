import { PrismaClient, User } from "@prisma/client";
import { NextFunction, Request, Response } from "express";

const database = new PrismaClient();

export async function findCurrentUser(req: Request, res: Response, next: NextFunction) {
    const userId: string = req.headers['user-id'];
    const user: User = await database.user.findFirst({
        where: {
            internal_id: userId
        }
    });
    if (user) {
        if (user.is_banned) {
            throw new Error("This user is banned.");
        }
        req.currentUserId = user.id;
    }
    next();
}
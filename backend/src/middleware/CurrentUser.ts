import { User } from "@prisma/client";
import { database } from "@src/services";
import { NextFunction, Request, Response } from "express";

// ChatGPT Usage: No
export async function findCurrentUser(
  req: Request,
  res: Response,
  next: NextFunction,
) {
  const userId: string = req.headers["user-id"] as string || "";
  const user: User = await database.user.findUnique({
    where: { spotify_id: userId },
  });
  if (!user && userId !== "") {
    res.status(401).send({ error: "This executing user does not exist" });
    return;
  }
  if (user && user.is_banned) {
    res.status(403).send({ error: "This executing user is banned" });
    return;
  }
  if (user) {
    req.headers.currentUserId = user.id.toString();
    req.headers.currentUserSpotifyId = user.spotify_id;
  }
  next();
}

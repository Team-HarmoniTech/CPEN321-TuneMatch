import {
  FriendsMessage,
  transformObject,
  transformUser,
  transformUsers,
} from "@models/UserModels";
import { userMatchingService, userService } from "@src/services";
import { NextFunction, Request, Response } from "express";
import WebSocket = require("ws");

export class UserController {
  // REST Routes
  // ChatGPT Usage: No
  async getUser(req: Request, res: Response, next: NextFunction) {
    /* Use currentUserId for the /me endpoint */
    const userId = req.url.startsWith("/me")
      ? req.headers.currentUserSpotifyId as string
      : req.params.spotify_id;
    const user = await userService.getUserBySpotifyId(userId);
    if (!user) {
      throw { message: `User not found.`, statusCode: 400 };
    }
    res.send(
      await transformUser(user, async (user) => {
        return req.query.fullProfile
          ? {
              bio: user.bio,
              topArtists: user.top_artists,
              topGenres: user.top_genres,
            }
          : {};
      }),
    );
  }

  // ChatGPT Usage: No
  async topMatches(req: Request, res: Response, next: NextFunction) {
    const matches = await userMatchingService.getTopMatches(Number(req.headers.currentUserId));
    res.send(
      await transformUsers(matches, async (user) => {
        return { match: user.match };
      }),
    );
  }

  // ChatGPT Usage: No
  async getMatch(req: Request, res: Response, next: NextFunction) {
    const otherUser = await userService.getUserBySpotifyId(
      req.params.spotify_id,
    );
    if (!otherUser) {
      throw { message: `User not found.`, statusCode: 400 };
    }
    const match = await userMatchingService.getMatch(
      Number(req.headers.currentUserId),
      otherUser.id,
    );
    res.send(
      await transformUser(otherUser, async () => {
        return { match: match };
      }),
    );
  }

  // ChatGPT Usage: No
  async insertUser(req: Request, res: Response, next: NextFunction) {
    const user = await userService.createUser(req.body.userData);
    userMatchingService.matchNewUser(user.id);
    res.send(await transformUser(user, async (user) => {
      return {
          bio: user.bio,
          topArtists: user.top_artists,
          topGenres: user.top_genres,
        }
      }),
    );
  }

  // ChatGPT Usage: No
  async updateUser(req: Request, res: Response, next: NextFunction) {
    const user = await userService.updateUser(
      req.body.userData,
      Number(req.headers.currentUserId),
    );
    res.send(
      await transformUser(user, async (user) => {
        return { bio: user.bio };
      }),
    );
  }

  // ChatGPT Usage: No
  async deleteUser(req: Request, res: Response, next: NextFunction) {
    await userService.deleteUser(Number(req.headers.currentUserId));
  }

  // ChatGPT Usage: No
  async searchUsers(req: Request, res: Response, next: NextFunction) {
    const users = await userService.searchUsers(
      Number(req.headers.currentUserId),
      req.query.q as string,
      Number(req.query.max),
    );
    res.send(
      await transformUsers(users, async (user) => {
        return {
          match: await userMatchingService.getMatch(
            user.id,
            Number(req.headers.currentUserId),
          ),
        };
      }),
    );
  }

  // Websocket Route Dispatcher
  // ChatGPT Usage: No
  async acceptRequest(
    ws: WebSocket,
    message: FriendsMessage,
    currentUserId: number,
  ) {
    const func = this[message.action];
    if (!func) {
      ws.send(
        JSON.stringify({
          Error: `Session endpoint ${message.action} does not exist.`,
        }),
      );
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

    ws.send(
      JSON.stringify(
        new FriendsMessage(
          "refresh",
          await transformUsers(friends, async (user) => {
            return {
              currentSong: transformObject(user.current_song),
              currentSource: transformObject(user.current_source),
            };
          }),
        ),
      ),
    );
  }

  // ChatGPT Usage: No
  async update(ws: WebSocket, message: FriendsMessage, currentUserId: number) {
    const user = await userService.updateUserStatus(
      currentUserId,
      message?.body?.song,
      message?.body?.source,
    );
  }
}

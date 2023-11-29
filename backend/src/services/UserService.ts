import { FriendsMessage, transformFriend } from "@models/UserModels";
import { Friend, Prisma, Session, User } from "@prisma/client";
import { database, socketService, userService } from "@src/services";

export class UserService {
  private userDB = database.user;
  private friendDB = database.friend;

  // ChatGPT Usage: Partial
  async getUserFriends(userId: number): Promise<User[]> {
    const user = await this.userDB.findUnique({
      where: { id: userId },
      include: {
        requested: { include: { requesting: true } },
        requesting: { include: { requested: true, } },
      },
    });
    // Only keep overlapping values
    return user.requested.map(f => f.requesting).filter((requested) =>
      user.requesting.map(f => f.requested).some((requesting) => requesting.id === requested.id),
    );
  }

  // ChatGPT Usage: Partial
  async getUserFriendsRequests(
    userId: number,
  ): Promise<{ requesting: User[]; requested: User[] }> {
    const user = await this.userDB.findUnique({
      where: { id: userId },
      include: {
        requested: { include: { requesting: true } },
        requesting: { include: { requested: true, } },
      },
    });
    const allRequesting = user.requesting.map(f => f.requested);
    const allRequested = user.requested.map(f => f.requesting);
    // Filter out overlapping users
    const requesting = allRequesting.filter(
      (requesting) =>
        !allRequested.some((requested) => requested.id === requesting.id),
    );
    const requested = allRequested.filter(
      (requested) =>
        !allRequesting.some((requesting) => requesting.id === requested.id),
    );

    return { requesting, requested };
  }

  // ChatGPT Usage: No
  async addFriend(userId: number, requestedId: number): Promise<User> {
    await this.friendDB.create({
      data: { 
        requested: { connect: { id: requestedId } }, 
        requesting: { connect: { id: userId } }
      },
      include: {
        requesting: true
      }
    });
    return this.userDB.findUniqueOrThrow({ where: { id: userId } });
  }

  // ChatGPT Usage: No
  async removeFriend(userId: number, otherId: number): Promise<User> {
    await this.friendDB.deleteMany({
      where:  {
        requested_id: userId,
        requesting_id: otherId,
      }
    });
    await this.friendDB.deleteMany({
      where:  {
        requested_id: otherId,
        requesting_id: userId,
      }
    });
    return this.userDB.findUnique({ where: { id: userId } });
  }

  async broadcastToFriends(userId: number, message: FriendsMessage) {
    const user = await this.getUserById(userId);
    const recipients = (await this.getUserFriends(user.id)).map(
      (user) => user.id,
    );

    message = { ...message, from: user.spotify_id };
    await socketService.broadcast(recipients, message);
  }

  // ChatGPT Usage: No
  async getUserBySpotifyId(
    spotify_id: string,
  ): Promise<
    User & { requesting: Friend[]; requested: Friend[]; session: Session }
  > {
    try {
      const user = await database.user.findUnique({
        where: { spotify_id: spotify_id },
        include: {
          requested: { include: { requesting: true } },
          requesting: { include: { requested: true } },
          session: true,
        },
      });
      user.requested.map(f => f.requesting);
      user.requesting.map(f => f.requested);
      return user as any;
    } catch {
      return null;
    }
  }

  // ChatGPT Usage: No
  async getUserById(
    id: number,
  ): Promise<
    User & { requesting: User[]; requested: User[]; session: Session }
  > {
    try {
      const user = await database.user.findUnique({
        where: { id: id },
        include: {
          requested: { include: { requesting: true } },
          requesting: { include: { requested: true } },
          session: true,
        },
      });
      user.requested.map(f => f.requesting);
      user.requesting.map(f => f.requested);
      return user as any;
    } catch {
      return null;
    }
  }

  // ChatGPT Usage: No
  async createUser(userData: object): Promise<User> {
    return await this.userDB.create({
      data: <Prisma.UserCreateInput>userData,
    });
  }

  // ChatGPT Usage: No
  async updateUser(userData: object, userId: number): Promise<User> {
    return await this.userDB.update({
      where: {
        id: userId,
      },
      data: <Prisma.UserUpdateInput>userData,
    });
  }

  // ChatGPT Usage: No
  async deleteUser(userId: number) {
    if (await socketService.retrieveById(userId)) {
      throw {
        message: "Cannot delete a user with an active websocket",
        statusCode: 400,
      };
    }
    await this.userDB.delete({
      where: { id: userId },
    });
  }

  // ChatGPT Usage: No
  async getRandomUser(notIn: number[]): Promise<User> {
    return await this.userDB.findFirst({
      where: { id: { notIn: notIn } },
    });
  }

  // ChatGPT Usage: No
  async searchUsers(
    userId: number,
    search: string,
    max?: number,
  ): Promise<User[]> {
    return await this.userDB.findMany({
      where: {
        username: { contains: search },
        id: { not: userId },
      },
      take: max || 50,
    });
  }

  // ChatGPT Usage: No
  async updateUserStatus(
    userId: number,
    song?: { name: string, uri: string },
    source?: { type: string; [key: string]: any },
  ): Promise<User> {
    let updateData: any = {
      last_updated: new Date()
    };

    /* If they are in a session don't update the source */
    if ((source !== undefined && ((await this.getUserById(userId)).current_source as any)?.type !== "session") || source === null) {
      updateData["current_source"] = source === null ? Prisma.DbNull : source;
    }
    if (song !== undefined) {
      updateData["current_song"] = song === null ? Prisma.DbNull : song;
    }

    /* Update user */
    const user = await this.updateUser(updateData, userId);

    /* Inform friends */
    await userService.broadcastToFriends(
      userId,
      new FriendsMessage(
        "update",
        await transformFriend(user)
      ),
    );

    return user;
  }
}

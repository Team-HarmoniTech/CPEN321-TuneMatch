import { FriendsMessage, transformUser } from "@models/UserModels";
import { Prisma, Session, User } from "@prisma/client";
import { database, socketService, userService } from "@src/services";

export class UserService {
  private userDB = database.user;

  // ChatGPT Usage: Partial
  async getUserFriends(userId: number): Promise<User[]> {
    const user = await this.userDB.findUnique({
      where: { id: userId },
      include: {
        requested: true,
        requesting: true,
      },
    });
    // Only keep overlapping values
    return user.requested.filter((requested) =>
      user.requesting.some((requesting) => requesting.id === requested.id),
    );
  }

  // ChatGPT Usage: Partial
  async getUserFriendsRequests(
    userId: number,
  ): Promise<{ requesting: User[]; requested: User[] }> {
    const user = await this.userDB.findUnique({
      where: { id: userId },
      include: {
        requested: true,
        requesting: true,
      },
    });
    // Filter out overlapping users
    const requesting = user.requesting.filter(
      (requesting) =>
        !user.requested.some((requested) => requested.id === requesting.id),
    );
    const requested = user.requested.filter(
      (requested) =>
        !user.requesting.some((requesting) => requesting.id === requested.id),
    );

    return { requesting, requested };
  }

  // ChatGPT Usage: No
  async addFriend(userId: number, requestedId: number): Promise<User> {
    try {
      return await this.userDB.update({
        where: { id: userId },
        data: { requested: { connect: { id: requestedId } } },
      });
    } catch {
      throw { message: "User does not exist", statusCode: 400 };
    }
  }

  // ChatGPT Usage: No
  async removeFriend(userId: number, requestedId: number): Promise<User> {
    try {
      return await this.userDB.update({
        where: { id: userId },
        data: {
          requesting: { disconnect: { id: requestedId } },
          requested: { disconnect: { id: requestedId } },
        },
      });
    } catch {
      throw { message: "User does not exist", statusCode: 400 };
    }
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
    User & { requesting: User[]; requested: User[]; session: Session }
  > {
    return await this.userDB.findUnique({
      where: { spotify_id: spotify_id },
      include: {
        requested: true,
        requesting: true,
        session: true,
      },
    });
  }

  // ChatGPT Usage: No
  async getUserById(
    id: number,
  ): Promise<
    User & { requesting: User[]; requested: User[]; session: Session }
  > {
    return await this.userDB.findUnique({
      where: { id: id },
      include: {
        requested: true,
        requesting: true,
        session: true,
      },
    });
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
    const friends = await this.getUserFriends(userId);
    return await this.userDB.findMany({
      where: {
        username: { contains: search },
        id: { notIn: [userId, ...friends.map((f) => f.id)] },
      },
      take: max || 50,
    });
  }

  // ChatGPT Usage: No
  async updateUserStatus(
    userId: number,
    song?: string,
    source?: { type: string; uri?: string },
  ): Promise<User> {
    let user: any = await this.getUserById(userId);
    let updateData: any = {};
    /* If they are in a session don't update the source */
    if (source !== undefined) {
        updateData["current_source"] = source === null ? Prisma.DbNull : source;
    }
    if (song !== undefined) {
      updateData["current_song"] = song;
    }

    /* Update user */
    user = await this.updateUser(updateData, userId);

    /* Inform friends */
    await userService.broadcastToFriends(
      userId,
      new FriendsMessage(
        "update",
        await transformUser(user, async (user) => {
          return {
            currentSong: user.current_song,
            currentSource: user.current_source,
          };
        }),
      ),
    );

    return user;
  }
}

import { SocketMessage } from "@models/WebsocketModels";
import { Prisma, User } from "@prisma/client";

export type VisibleUser = {
  userId: String;
  username: String;
  profilePic: String;
  [key: string]: any;
};

export type VisibleFriend = {
  userId: String;
  username: String;
  profilePic: String;
  currentSong: Object,
  currentSource: Object,
  lastUpdated: String
};

// ChatGPT Usage: Partial
export async function transformUsers(
  users: User[],
  extras?: (user) => Promise<object>,
): Promise<VisibleUser[]> {
  return await Promise.all(
    users.map(async (user) => await transformUser(user, extras)),
  );
}

// ChatGPT Usage: Partial
export async function transformUser(
  user: User,
  extras?: (user) => Promise<object>,
): Promise<VisibleUser> {
  return {
    userId: user.spotify_id,
    username: user.username,
    profilePic: user.pfp_url,
    ...(extras ? await extras(user) : {}),
  };
}

export async function transformFriend(user: User): Promise<VisibleFriend> {
  return await transformUser(user, async (user) => {
    return {
      currentSong: transformObject(user.current_song),
      currentSource: transformObject(user.current_source),
      lastUpdated: user.last_updated.toISOString()
    };
  }) as VisibleFriend;
}

export async function transformFriends(users: User[]): Promise<VisibleFriend[]> {
  return await Promise.all(
    users.map(async (user) => await transformFriend(user)),
  ) as VisibleFriend[];
}

export function transformObject(input: any) {
  if (input && !Array.isArray(input)) {
    return input === Prisma.DbNull ? null : input;
  }
  return input;
}

export class FriendsMessage extends SocketMessage {
  constructor(
    public action: string,
    public body?: any,
  ) {
    super("FRIENDS", action, body);
  }
}

export class RequestsMessage extends SocketMessage {
  constructor(
    public action: string,
    public body?: any,
  ) {
    super("REQUESTS", action, body);
  }
}

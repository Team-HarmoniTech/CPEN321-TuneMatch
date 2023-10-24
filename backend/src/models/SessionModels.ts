import { Session, User } from "@prisma/client";
import { Mutex } from "async-mutex";
import { SocketMessage } from "./WebsocketModels";

export class SessionMessage extends SocketMessage {
    constructor(
        public action: string,
        public body?: any
    ) {
        super("SESSION", action, body);
    }
}
export type SessionWithMembers = Session & { members: User[] };
export type UserWithSession = User & { session: Session };

export type SessionQueue = {
    queue: string[],
    lock: Mutex
}

export type VisibleUser = {
    id: String,
    username: String,
    profilePic: String,
    [key: string]: any;
}

export function transformUsers(users: User[], extras?: (user) => object): VisibleUser[] {
    return users.map(u => transformUser(u, extras));
}
export function transformUser(user: User, extras?: (user) => object): VisibleUser {
    if (!user) {
        throw { message: `User not found.`, statusCode: 400 };
    }
    return {
        id: user.spotify_id,
        username: user.username,
        profilePic: user.pfp_url,
        ...(extras ? extras(user): {}),
    }
}
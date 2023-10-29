import { User } from "@prisma/client";
import { SocketMessage } from "./WebsocketModels";

export type VisibleUser = {
    id: String,
    username: String,
    profilePic: String,
    [key: string]: any;
}

// ChatGPT Usage: Partial
export async function transformUsers(users: User[], extras?: (user) => Promise<object>): Promise<VisibleUser[]> {
    return await Promise.all(users.map(async user => await transformUser(user, extras)));
}

// ChatGPT Usage: Partial
export async function transformUser(user: User, extras?:  (user) => Promise<object>): Promise<VisibleUser> {
    return {
        id: user.spotify_id,
        username: user.username,
        profilePic: user.pfp_url,
        ...(extras ? await extras(user): {}),
    }
}

export class FriendsMessage extends SocketMessage {
    constructor(
        public action: string,
        public body?: any
    ) {
        super("FRIENDS", action, body);
    }
}

export class RequestsMessage extends SocketMessage {
    constructor(
        public action: string,
        public body?: any
    ) {
        super("REQUESTS", action, body);
    }
}
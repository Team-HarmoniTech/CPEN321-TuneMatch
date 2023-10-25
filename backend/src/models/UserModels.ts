import { User } from "@prisma/client";
import { SocketMessage } from "./WebsocketModels";

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
    return {
        id: user.spotify_id,
        username: user.username,
        profilePic: user.pfp_url,
        ...(extras ? extras(user): {}),
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
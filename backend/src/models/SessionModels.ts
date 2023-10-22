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
}

export function exportUsers(users: User[]): VisibleUser[] {
    return users.map(u => ({
        id: u.spotify_id,
        username: u.username,
        profilePic: u.pfp_url,
    }));
}
export function exportUser(user: User, currentlyPlaying?: boolean): VisibleUser {
    return exportUsers([user])[0];
}
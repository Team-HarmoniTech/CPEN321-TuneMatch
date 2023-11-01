import { Queue } from "@models/Queue";
import { SocketMessage } from "@models/WebsocketModels";
import { Session, User } from "@prisma/client";
import { Mutex } from "async-mutex";

export class SessionMessage extends SocketMessage {
  constructor(
    public action: string,
    public body?: any,
  ) {
    super("SESSION", action, body);
  }
}
export type SessionWithMembers = Session & { members: User[] };
export type UserWithSession = User & { session: Session };

export type SessionQueue = {
  queue: Queue;
  lock: Mutex;
};

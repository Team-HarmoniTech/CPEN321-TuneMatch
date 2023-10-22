import { Mutex } from "async-mutex";
import { database, socketService } from "..";
import { SessionMessage, SessionQueue } from "../models/SessionModels";

export class SessionService {
    private sessionQueues = new Map<number, SessionQueue>();

    async joinSession(userId: number, sessionId?: number): Promise<number> {
        // Leave old session if exists
        this.leaveSession(userId);
        // Add user to the new session
        if (sessionId) {
            return (await database.session.update({
                where: {
                    id: sessionId
                },
                data: {
                    members: {
                        connect: {
                            id: userId
                        }
                    }
                }
            })).id;
        } else {
            const session = await database.session.create({
                data: {
                    members: {
                        connect: {
                            id: userId
                        }
                    }
                }
            });
            this.sessionQueues.set(session.id, {
                queue: [],
                lock: new Mutex()
            });
            return session.id;
        }
    }

    async leaveSession(userId: number) {
        // Disconnect from old session if exists
        const user = await database.user.findFirst({
            where: {
                id: userId
            },
            include: {
                session: {
                    include: {
                        members: true
                    }
                }
            }
        });
        // If session will be empty delete, otherwise leave
        if (user.session) {
            const toDelete = user.session.members.length === 1;
            if (toDelete) this.sessionQueues.delete(user.session.id);
            await database.user.update({
                where: {
                    id: userId
                },
                data: {
                    session: {
                        disconnect: !toDelete,
                        delete: toDelete,
                    }
                }
            });
        }
    }

    async messageSession(sessionId: number, senderId: number, message: SessionMessage) {
        const session = await database.session.findFirstOrThrow({
            where: {
                id: sessionId
            },
            include: {
                members: true
            }
        });
        const recipients = session.members.map(user => user.id).filter(id => !(id === senderId));
        await socketService.broadcast(recipients, message);
    }

    async queueAdd(sessionId: number, songUri: string, durationMs: number, posAfter?: number) {
        const queueData = this.sessionQueues.get(sessionId);
        queueData.lock.runExclusive(() => {
            queueData.queue.splice(posAfter ?? 0 + 1, 0, songUri);
        });
    }

    async queueNext(sessionId: number) {
        const queueData = this.sessionQueues.get(sessionId);
        queueData.lock.runExclusive(() => {
            queueData.queue.shift();
        });
    }
}
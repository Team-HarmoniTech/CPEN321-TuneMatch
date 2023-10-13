import { PrismaClient } from "@prisma/client";
import { Mutex } from "async-mutex";
import { socketService } from "..";
import { SessionMessage, SessionQueue } from "../models/SessionModels";

export class SessionService {
    private database = new PrismaClient();
    private sessionQueues = new Map<number, SessionQueue>();

    async joinSession(userId: number, sessionId?: number): Promise<number> {
        // Leave old session if exists
        this.leaveSession(userId);
        // Add user to the new session
        if (sessionId) {
            return (await this.database.session.update({
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
            const session = await this.database.session.create({
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
        const user = await this.database.user.findFirst({
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
            await this.database.user.update({
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
        const session = await this.database.session.findFirstOrThrow({
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
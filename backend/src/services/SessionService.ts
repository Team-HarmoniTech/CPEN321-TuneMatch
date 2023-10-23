import { Mutex } from "async-mutex";
import { database, socketService } from "..";
import { SessionQueue, SessionWithMembers } from "../models/SessionModels";

export class SessionService {
    private sessionQueues = new Map<number, SessionQueue>();

    async joinSession(userId: number, otherUserId?: number): Promise<SessionWithMembers> {
        // Leave old session if exists
        this.leaveSession(userId);
        // Add user to the new session
        if (otherUserId) {
            const otherSession = await database.session.findFirstOrThrow({
                where: { members: { some: { id: otherUserId } } },
            });
            console.log(otherSession);
            if (!otherSession) {
                return null;
            } else {
                return await database.session.update({
                    where: { id: otherSession.id },
                    data: { members: { connect: { id: userId } } },
                    include: { members: true }
                });
            }
        } else {
            const session = await database.session.create({
                data: { members: { connect: { id: userId } } },
                include: { members: true }
            });
            this.sessionQueues.set(session.id, {
                queue: [],
                lock: new Mutex()
            });
            return session;
        }
    }

    async leaveSession(userId: number): Promise<SessionWithMembers> {
        // Disconnect from old session if exists
        const user = await database.user.findUnique({
            where: { id: userId },
            include: { session: { include: { members: true } } }
        });
        // If session will be empty delete, otherwise leave
        if (user.session) {
            const toDelete = user.session.members.length === 1;
            if (toDelete) this.sessionQueues.delete(user.session.id);
            await database.user.update({
                where: { id: userId },
                data: { session: {
                        disconnect: !toDelete,
                        delete: toDelete,
                    } }
            });
            return toDelete ? undefined : user.session;
        }
        return undefined;
    }

    async messageSession(sessionId: number, senderId: number, message: any) {
        const session = await database.session.findUnique({
            where: { id: sessionId },
            include: { members: true }
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
import { Mutex } from "async-mutex";
import { database, socketService } from "..";
import { SessionQueue, SessionWithMembers, UserWithSession } from "../models/SessionModels";

export class SessionService {
    private sessionQueues = new Map<number, SessionQueue>();

    async joinSession(userId: number, otherUserId?: number): Promise<SessionWithMembers> {
        // Leave old session if exists
        this.leaveSession(userId);
        // Add user to the new session
        if (otherUserId) {
            const otherUser = await database.user.findFirst({
                where: { id: otherUserId },
                include: { session: true }
            });
            if (!otherUser.sessionId) {
                return null;
            } else {
                return await database.session.update({
                    where: { id: otherUser.sessionId },
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
        const user = await database.user.findFirst({
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

    async messageSession(sender: UserWithSession, message: any) {
        const session = await database.session.findFirstOrThrow({
            where: { id: sender.sessionId },
            include: { members: true }
        });
        const recipients = session.members.map(user => user.id).filter(id => !(id === sender.id));
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
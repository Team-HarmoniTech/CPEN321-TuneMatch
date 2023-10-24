import { Mutex } from "async-mutex";
import { database, socketService } from "..";
import { SessionQueue, SessionWithMembers } from "../models/SessionModels";

export class SessionService {
    private sessionQueues = new Map<number, SessionQueue>();

    async joinSession(userId: number, otherUserId?: number): Promise<SessionWithMembers> {
        // Leave old session if exists
        await this.leaveSession(userId);

        if (otherUserId) {
            // Add user to the other session
            const otherSession = await database.session.findFirstOrThrow({
                where: { members: { some: { id: otherUserId } } },
            });
            if (!otherSession) {
                throw { message:`User is not in a session.`, statusCode: 400 };
            }
            return await database.session.update({
                where: { id: otherSession.id },
                data: { members: { connect: { id: userId } } },
                include: { members: true }
            });
        } else {
            // Create new session for user
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
        // Find user's session if it exists
        const session = await database.session.findFirst({
            where: { members: { some: { id: userId } } },
            include: { members: true }
        });
        // If session will be empty delete, otherwise leave
        if (session && session.members.length <= 1) {
            await database.session.delete({
                where: { id: session.id }
            });
        } else if (session) {
            return await database.session.update({
                where: { id: session.id },
                data: { members: { disconnect: { id: userId } } },
                include: { members: true }
            });
        }
        return undefined;
    }

    async messageSession(sessionId: number, senderId: number, message: any) {
        const session = await database.session.findUnique({
            where: { id: sessionId },
            include: { members: true }
        });
        
        /* Add sending user id to the message */
        const user = session.members.find(u => u.id === senderId);
        message = { ...message, from: user.spotify_id };

        const recipients = session.members.filter(u => u.id !== senderId).map(user => user.id);
        await socketService.broadcast(recipients, message);
    }

    async queueAdd(sessionId: number, songUri: string, durationMs: number, posAfter?: number) {
        const queueData = this.sessionQueues.get(sessionId);
        await queueData.lock.runExclusive(() => {
            queueData.queue.splice((posAfter ?? 0) + 1, 0, songUri);
        });
    }

    async queueNext(sessionId: number) {
        const queueData = this.sessionQueues.get(sessionId);
        await queueData.lock.runExclusive(() => {
            queueData.queue.shift();
        });
    }
}
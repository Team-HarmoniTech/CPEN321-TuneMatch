import { Mutex } from "async-mutex";
import { database, socketService, userService } from "..";
import { Queue, Song } from "../models/Queue";
import { SessionQueue, SessionWithMembers } from "../models/SessionModels";

export class SessionService {
    private sessionQueues = new Map<number, SessionQueue>();
    private sessionDB = database.session;

    // ChatGPT Usage: Partial
    async joinSession(userId: number, otherUserId?: number): Promise<SessionWithMembers> {
        // Leave old session if exists
        await this.leaveSession(userId);

        let session;
        if (otherUserId) {
            // Add user to the other session
            const otherSession = await this.sessionDB.findFirstOrThrow({
                where: { members: { some: { id: otherUserId } } },
            });
            if (!otherSession) {
                throw { message:`User is not in a session.`, statusCode: 400 };
            }
            session = await this.sessionDB.update({
                where: { id: otherSession.id },
                data: { members: { connect: { id: userId } } },
                include: { members: true }
            });
        } else {
            // Create new session for user
            session = await this.sessionDB.create({
                data: { members: { connect: { id: userId } } },
                include: { members: true }
            });
            this.sessionQueues.set(session.id, {
                queue: new Queue(),
                lock: new Mutex()
            });
        }

        if (session) {
            userService.updateUser({ current_source: { type: "session" } }, userId);
        }

        return session;
    }

    // ChatGPT Usage: No
    async leaveSession(userId: number): Promise<SessionWithMembers> {
        // Find user's session if it exists
        let session = await this.sessionDB.findFirst({
            where: { members: { some: { id: userId } } },
            include: { members: true }
        });
        const hadSession = !!session;
        // If session will be empty delete, otherwise leave
        if (hadSession && session.members.length <= 1) {
            await this.sessionDB.delete({
                where: { id: session.id }
            });
            session = undefined;
        } else if (hadSession) {
            session = await this.sessionDB.update({
                where: { id: session.id },
                data: { members: { disconnect: { id: userId } } },
                include: { members: true }
            });
        }
        
        if (hadSession) {
            userService.updateUser({ current_source: null }, userId);
        }

        return session;
    }

    // ChatGPT Usage: No
    async messageSession(sessionId: number, senderId: number, message: any) {
        const session = await this.sessionDB.findUnique({
            where: { id: sessionId },
            include: { members: true }
        });
        
        /* Add sending user id to the message */
        const user = session.members.find(u => u.id === senderId);
        message = { ...message, from: user.spotify_id };

        const recipients = session.members.filter(u => u.id !== senderId).map(user => user.id);
        await socketService.broadcast(recipients, message);
    }

    // ChatGPT Usage: No
    async queueAdd(sessionId: number, songUri: string, durationMs: number, posAfter?: number) {
        const queueData = this.sessionQueues.get(sessionId);
        await queueData.lock.runExclusive(() => {
            queueData.queue.addAfter(new Song(songUri, durationMs), posAfter);
        });
    }

    // ChatGPT Usage: No
    async queueSkip(sessionId: number) {
        const queueData = this.sessionQueues.get(sessionId);
        await queueData.lock.runExclusive(() => {
            queueData.queue.skip();
        });
    }

    // ChatGPT Usage: No
    async queueDrag(sessionId: number, initialPos: number, endPos: number) {
        const queueData = this.sessionQueues.get(sessionId);
        await queueData.lock.runExclusive(() => {
            queueData.queue.drag(initialPos, endPos);
        });
    }

    // ChatGPT Usage: No
    async queuePause(sessionId: number) {
        const queueData = this.sessionQueues.get(sessionId);
        await queueData.lock.runExclusive(() => {
            queueData.queue.stop();
        });
    }

    // ChatGPT Usage: No
    async queuePlay(sessionId: number) {
        const queueData = this.sessionQueues.get(sessionId);
        await queueData.lock.runExclusive(() => {
            queueData.queue.start();
        });
    }

    // ChatGPT Usage: No
    async queueSeek(sessionId: number, seekPosition: number) {
        const queueData = this.sessionQueues.get(sessionId);
        await queueData.lock.runExclusive(() => {
            queueData.queue.seek(seekPosition);
        });
    }
}
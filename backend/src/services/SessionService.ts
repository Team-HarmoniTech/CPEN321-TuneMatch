import { Queue, Song } from "@models/Queue";
import { SessionMessage, SessionQueue, SessionWithMembers } from "@models/SessionModels";
import { transformUser } from "@models/UserModels";
import { Session } from "@prisma/client";
import { database, socketService, userService } from "@src/services";
import { Mutex } from "async-mutex";

export class SessionService {
  private sessionQueues = new Map<number, SessionQueue>();
  private sessionDB = database.session;

  // ChatGPT Usage: Partial
  async joinSession(
    userId: number,
    otherUserId?: string,
  ): Promise<SessionWithMembers> {
    const otherSession = await this.sessionDB.findFirst({
      where: { members: { some: { spotify_id: otherUserId } } },
      include: { members: true },
    });
    if (otherSession.members.some(m => m.id === userId)) {
      /* We are already in the user's session */
      return otherSession;
    }

    /* Leave old session if exists */
    await this.leaveSession(userId);

    let session;
    if (otherUserId) {
      const otherUser = await userService.getUserBySpotifyId(otherUserId);
      if (!otherUser) {
        throw { message: `User does not exist`, statusCode: 400 };
      }
      /* Add user to the other session */
      const otherSession = await this.sessionDB.findFirst({
        where: { members: { some: { spotify_id: otherUserId } } },
      });
      if (!otherSession) {
        throw { message: `User is not in a session.`, statusCode: 400 };
      }
      session = await this.sessionDB.update({
        where: { id: otherSession.id },
        data: { members: { connect: { id: userId } } },
        include: { members: true },
      });
    } else {
      /* Create new session for user */
      session = await this.sessionDB.create({
        data: { members: { connect: { id: userId } } },
        include: { members: true },
      });
      this.sessionQueues.set(session.id, {
        queue: new Queue(),
        lock: new Mutex(),
      });
    }

    /* Update user with session */
    const user = await userService.updateUserStatus(userId, undefined, { type: "session" });
    await this.messageSession(
      session.id,
      userId,
      new SessionMessage(
        "join",
        await transformUser(user),
      ),
    );

    /* Update session before returning */
    session = await this.sessionDB.findFirstOrThrow({
      where: { id: session.id },
      include: { members: true },
    });

    return session;
  }

  // ChatGPT Usage: No
  async leaveSession(userId: number): Promise<void> {
    /* Find user's session if it exists */
    const session = await this.sessionDB.findFirst({
      where: { members: { some: { id: userId } } },
      include: { members: true },
    });

    if (!session) {
      return undefined;
    }

    /* If session will be empty delete, otherwise leave */
    const toDelete = session.members.length <= 1;
    if (toDelete) {
      await this.sessionDB.delete({
        where: { id: session.id },
      });
    } else {
      await this.sessionDB.update({
        where: { id: session.id },
        data: { members: { disconnect: { id: userId } } },
        include: { members: true },
      });
    }

    /* Update user */
    const user = await userService.updateUserStatus(userId, undefined, null);
    if (!toDelete) {
      /* Inform old session */
      await this.messageSession(
        session.id,
        userId,
        new SessionMessage(
          "leave",
          await transformUser(user)
        ),
      );
    }
  }

  // ChatGPT Usage: No
  async getSession(userId: number): Promise<Session> {
    const user = await userService.getUserById(userId);
    if (!user.session) {
      throw { message: `User is not in a session.`, statusCode: 400 };
    }
    return user.session;
  }

  // ChatGPT Usage: No
  async messageSession(sessionId: number, senderId: number, message: any) {
    const session = await this.sessionDB.findUnique({
      where: { id: sessionId },
      include: { members: true },
    });

    /* Add sending user id to the message */
    const user = await userService.getUserById(senderId);
    message = { ...message, from: user.spotify_id };

    const recipients = session.members
      .filter((u) => u.id !== senderId)
      .map((user) => user.id);
    await socketService.broadcast(recipients, message);
  }

  // ChatGPT Usage: No
  async queueReplace(
    sessionId: number,
    newQueue: { uri: string; durationMs: number, title: string, artist: string }[],
  ) {
    const queueData = this.sessionQueues.get(sessionId);
    await queueData.lock.runExclusive(() => {
      queueData.queue.replace(
        newQueue.map((s) => new Song(s.uri, s.durationMs, s.title, s.artist)),
      );
    });
  }

  // ChatGPT Usage: No
  async queueAdd(
    sessionId: number,
    songUri: string,
    durationMs: number,
    title: string, 
    artist: string,
    posAfter?: number,
  ) {
    const queueData = this.sessionQueues.get(sessionId);
    await queueData.lock.runExclusive(() => {
      queueData.queue.addAfter(new Song(songUri, durationMs, title, artist), posAfter);
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

  // ChatGPT Usage: No
  async getQueue(sessionId: number): Promise<any> {
    const queueData = this.sessionQueues.get(sessionId);
    return await queueData.lock.runExclusive(() => {
      const q = queueData.queue;
      return {
        currentlyPlaying: q.currentlyPlaying
          ? {
              uri: q.currentlyPlaying.uri,
              durationMs: q.currentlyPlaying.durationMs,
              timeStarted: q.currentlyPlaying.timeStarted.toISOString(),
              title: q.currentlyPlaying.title,
              artist: q.currentlyPlaying.artist
            }
          : null,
        queue: [...q.songs].map((val) => {
          return { uri: val.uri, durationMs: val.durationMs, title: val.title, artist: val.artist };
        }),
      };
    });
  }
}

import { SessionMessage } from "@models/SessionModels";
import {
  transformUser,
  transformUsers
} from "@models/UserModels";
import { sessionService, userService } from "@src/services";
import { WebSocket } from "ws";

export class SessionController {
  // Websocket Route Dispatcher
  // ChatGPT Usage: No
  async acceptRequest(
    ws: WebSocket,
    message: SessionMessage,
    currentUserId: number,
  ) {
    const func = this[message.action];
    if (!func) {
      ws.send(
        JSON.stringify({
          Error: `Session endpoint ${message.action} does not exist.`,
        }),
      );
    } else {
      try {
        await func(ws, message, currentUserId);
      } catch (err) {
        ws.send(JSON.stringify(new SessionMessage("error", err.message)));
      }
    }
  }

  // WebSocket Routes
  // ChatGPT Usage: No
  async message(ws: WebSocket, message: SessionMessage, currentUserId: number) {
    const session = await sessionService.getSession(currentUserId);
    await sessionService.messageSession(
      session.id,
      currentUserId,
      message,
    );
  }

  // ChatGPT Usage: No
  async queueReplace(
    ws: WebSocket,
    message: SessionMessage,
    currentUserId: number,
  ) {
    const session = await sessionService.getSession(currentUserId);
    await sessionService.queueReplace(session.id, message.body);
    await sessionService.messageSession(
      session.id,
      currentUserId,
      message,
    );
  }

  // ChatGPT Usage: No
  async queueAdd(
    ws: WebSocket,
    message: SessionMessage,
    currentUserId: number,
  ) {
    const { uri, durationMs, index, title, artist } = message.body;
    const session = await sessionService.getSession(currentUserId);
    await sessionService.queueAdd(
      session.id,
      uri,
      durationMs,
      title,
      artist,
      index
    );
    await sessionService.messageSession(
      session.id,
      currentUserId,
      message,
    );
  }

  // ChatGPT Usage: No
  async queueSkip(
    ws: WebSocket,
    message: SessionMessage,
    currentUserId: number,
  ) {
    const session = await sessionService.getSession(currentUserId);
    await sessionService.queueSkip(session.id);
    await sessionService.messageSession(
      session.id,
      currentUserId,
      message,
    );
  }

  // ChatGPT Usage: No
  async queueDrag(
    ws: WebSocket,
    message: SessionMessage,
    currentUserId: number,
  ) {
    const session = await sessionService.getSession(currentUserId);
    const { startIndex, endIndex } = message.body;
    await sessionService.queueDrag(
      session.id,
      startIndex,
      endIndex,
    );
    await sessionService.messageSession(
      session.id,
      currentUserId,
      message,
    );
  }

  // ChatGPT Usage: No
  async queuePause(
    ws: WebSocket,
    message: SessionMessage,
    currentUserId: number,
  ) {
    const session = await sessionService.getSession(currentUserId);
    await sessionService.queuePause(session.id);
    await sessionService.messageSession(
      session.id,
      currentUserId,
      message,
    );
  }

  // ChatGPT Usage: No
  async queueResume(
    ws: WebSocket,
    message: SessionMessage,
    currentUserId: number,
  ) {
    const session = await sessionService.getSession(currentUserId);
    await sessionService.queuePlay(session.id);
    await sessionService.messageSession(
      session.id,
      currentUserId,
      message,
    );
  }

  // ChatGPT Usage: No
  async queueSeek(
    ws: WebSocket,
    message: SessionMessage,
    currentUserId: number,
  ) {
    const session = await sessionService.getSession(currentUserId);
    const { seekPosition } = message.body;
    await sessionService.queueSeek(session.id, seekPosition);
    await sessionService.messageSession(
      session.id,
      currentUserId,
      message,
    );
  }

  // ChatGPT Usage: No
  async join(ws: WebSocket, message: SessionMessage, currentUserId: number) {
    const session = await sessionService.joinSession(
      currentUserId,
      message?.body?.userId || undefined,
    );
    const queue = await sessionService.getQueue(session.id);
    ws.send(
      JSON.stringify(
        new SessionMessage("refresh", {
          members: await transformUsers(
            session.members.filter((x) => x.id !== currentUserId),
          ),
          currentlyPlaying: queue.currentlyPlaying,
          queue: queue.queue,
        }),
      ),
    );
  }

  // ChatGPT Usage: No
  async leave(ws: WebSocket, message: SessionMessage, currentUserId: number) {
    await sessionService.leaveSession(currentUserId);
    const user = await userService.getUserById(currentUserId);
    ws.send(
      JSON.stringify(new SessionMessage("leave", await transformUser(user))),
    );
  }
}

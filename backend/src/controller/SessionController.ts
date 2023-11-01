import { WebSocket } from "ws";
import { SessionMessage } from "../models/SessionModels";
import {
  FriendsMessage,
  transformUser,
  transformUsers,
} from "../models/UserModels";
import { sessionService, userService } from "../services";

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
    const currentUser = await userService.getUserById(currentUserId);
    await sessionService.messageSession(
      currentUser.session.id,
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
    const currentUser = await userService.getUserById(currentUserId);
    await sessionService.queueReplace(currentUser.session.id, message.body);
    await sessionService.messageSession(
      currentUser.session.id,
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
    const { uri, durationMs, index } = message.body;
    const currentUser = await userService.getUserById(currentUserId);
    await sessionService.queueAdd(
      currentUser.session.id,
      uri,
      durationMs,
      index,
    );
    await sessionService.messageSession(
      currentUser.session.id,
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
    const currentUser = await userService.getUserById(currentUserId);
    await sessionService.queueSkip(currentUser.session.id);
    await sessionService.messageSession(
      currentUser.session.id,
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
    const currentUser = await userService.getUserById(currentUserId);
    const { startIndex, endIndex } = message.body;
    await sessionService.queueDrag(
      currentUser.session.id,
      startIndex,
      endIndex,
    );
    await sessionService.messageSession(
      currentUser.session.id,
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
    const currentUser = await userService.getUserById(currentUserId);
    await sessionService.queuePause(currentUser.session.id);
    await sessionService.messageSession(
      currentUser.session.id,
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
    const currentUser = await userService.getUserById(currentUserId);
    await sessionService.queuePlay(currentUser.session.id);
    await sessionService.messageSession(
      currentUser.session.id,
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
    const currentUser = await userService.getUserById(currentUserId);
    const { seekPosition } = message.body;
    await sessionService.queueSeek(currentUser.session.id, seekPosition);
    await sessionService.messageSession(
      currentUser.session.id,
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

    await sessionService.messageSession(
      session.id,
      currentUserId,
      new SessionMessage(
        "join",
        await transformUser(
          session.members.find((x) => x.id === currentUserId),
        ),
      ),
    );
    await userService.broadcastToFriends(
      currentUserId,
      new FriendsMessage(
        "update",
        await transformUser(
          session.members.find((x) => x.id === currentUserId),
          async (user) => {
            return {
              currentSong: user.current_song,
              currentSource: user.current_source,
            };
          },
        ),
      ),
    );
  }

  // ChatGPT Usage: No
  async leave(ws: WebSocket, message: SessionMessage, currentUserId: number) {
    const session = await sessionService.leaveSession(currentUserId);
    const user = await userService.getUserById(currentUserId);
    ws.send(
      JSON.stringify(new SessionMessage("leave", await transformUser(user))),
    );
    await userService.broadcastToFriends(
      currentUserId,
      new FriendsMessage(
        "update",
        await transformUser(user, async (user) => {
          return {
            currentSong: user.current_song,
            currentSource: user.current_source,
          };
        }),
      ),
    );
    if (session) {
      await sessionService.messageSession(
        session.id,
        currentUserId,
        new SessionMessage("leave", await transformUser(user)),
      );
    }
  }
}

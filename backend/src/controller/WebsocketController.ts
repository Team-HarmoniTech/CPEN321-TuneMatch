import { RequestController } from "@controller/RequestController";
import { SessionController } from "@controller/SessionController";
import { UserController } from "@controller/UserController";
import {
  FriendsMessage,
  RequestsMessage,
  transformUsers
} from "@models/UserModels";
import { SocketMessage } from "@models/WebsocketModels";
import {
  database,
  sessionService,
  socketService,
  userService,
} from "@services";
import { Request } from "express";
import { WebSocket } from "ws";

// ChatGPT Usage: No
async function authenticateSocket(socket, req): Promise<number> {
  if (!req.headers["user-id"]) {
    socket.close();
    return;
  }

  const user = await database.user.findUnique({
    where: {
      spotify_id: req.headers["user-id"],
    },
    include: { session: true },
  });
  if (!user) {
    socket.close(1008, `User ${req.headers["user-id"]} does not exist`);
    return;
  }
  await socketService.addConnection(user.id, socket);

  /* Initial Data Push */
  const friends = await userService.getUserFriends(user.id);
  socket.send(
    JSON.stringify(
      new FriendsMessage(
        "refresh",
        await transformUsers(friends, async (user) => {
          return {
            currentSong: user.current_song,
            currentSource: user.current_source,
          };
        }),
      ),
    ),
  );
  const requests = await userService.getUserFriendsRequests(user.id);
  socket.send(
    JSON.stringify(
      new RequestsMessage("refresh", {
        requesting: await transformUsers(requests.requesting),
        requested: await transformUsers(requests.requested),
      }),
    ),
  );

  return user.id;
}

// ChatGPT Usage: No
export async function handleConnection(ws: WebSocket, req: Request) {
  // Authenticate and add to our persistant set of connections
  const currentUserId = await authenticateSocket(ws, req);

  ws.on("error", console.error);

  // ChatGPT Usage: No
  ws.on("message", function message(data) {
    if (data.toString() === "") {
      return;
    }
    try {
      const req: SocketMessage = JSON.parse(data.toString());
      if (!req.method) {
        ws.send(JSON.stringify({ Error: "Received data is missing fields" }));
      } else {
        switch (req.method) {
          case "SESSION":
            new SessionController().acceptRequest(ws, req, currentUserId);
            break;
          case "FRIENDS":
            new UserController().acceptRequest(ws, req, currentUserId);
            break;
          case "REQUESTS":
            new RequestController().acceptRequest(ws, req, currentUserId);
            break;
          default:
            ws.send(
              JSON.stringify({ Error: "Received data has an invalid method" }),
            );
        }
      }
    } catch {
      ws.send(
        JSON.stringify({ Error: "Received data is not formatted correctly" }),
      );
    }
  });

  // ChatGPT Usage: No
  ws.on("close", async function close(code, reason) {
    console.error(`Socket Closed: ${reason.toString()}`);
    const userId = await socketService.retrieveBySocket(ws);
    try {
      if (userId) {
        await sessionService.leaveSession(userId);
        await userService.updateUserStatus(userId, null, null);
      }
    } catch { /* empty */ }
    if (userId) {
      await socketService.removeConnectionBySocket(ws);
    }
  });
}

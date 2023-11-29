import { RequestController } from "@controller/RequestController";
import { SessionController } from "@controller/SessionController";
import { UserController } from "@controller/UserController";
import logger from "@src/logger";
import {
  database,
  sessionService,
  socketService
} from "@src/services";
import { Request } from "express";
import { WebSocket } from "ws";

// ChatGPT Usage: No
async function authenticateSocket(socket, req): Promise<number> {
  if (!req.headers["user-id"]) {
    socket.close(1008, 'No Authentication Provided');
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
  await (new UserController()).refresh(socket, null, user.id);
  await (new RequestController()).refresh(socket, null, user.id);
  /* Start ping pong */
  socket.ping();

  return user.id;
}

// ChatGPT Usage: No
export async function handleConnection(ws: WebSocket, req: Request) {
  /* Authenticate and add to our persistant set of connections */
  const currentUserId = await authenticateSocket(ws, req);

  ws.on("error", logger.err);

  ws.on("pong", async () => {
    /* Reply to the pong after 20s */
    await setTimeout(() => ws.ping(), 20000);
  });

  // ChatGPT Usage: No
  ws.on("message", async function message(data) {
    try {
      const req = JSON.parse(data.toString());
      if (!req.method) {
        ws.send(JSON.stringify({ Error: "Received data is missing fields" }));
      } else {
        logger.dev(`${req.method} ${req?.action || null} from User ${currentUserId}`);
        /* Find function */
        let func;
        switch (req.method) {
          case "SESSION":
            func = new SessionController()[req.action];
            break;
          case "FRIENDS":
            func = new UserController()[req.action];
            break;
          case "REQUESTS":
            func = new RequestController()[req.action];
            break;
          default:
            ws.send(
              JSON.stringify({ Error: "Received data has an invalid method" }),
            );
        }

        /* Call function */
        if (!func) {
          ws.send(
            JSON.stringify({
              Error: `${req.method} endpoint ${req.action} does not exist.`,
            }),
          );
        } else {
          try {
            await func(ws, req, currentUserId);
          } catch (err) {
            logger.err(err.message);
            ws.send(JSON.stringify({
              Error: err.message,
            }));
          }
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
    logger.err(`Socket Closed: ${reason.toString()}`);
    const userId = await socketService.retrieveBySocket(ws);
    try {
      if (userId) {
        await sessionService.leaveSession(userId);
      }
    } catch { /* empty */ }
    if (userId) {
      await socketService.removeConnectionBySocket(ws);
    }
  });
}

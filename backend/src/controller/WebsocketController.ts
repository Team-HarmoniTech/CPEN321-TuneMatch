import { Request } from "express";
import { WebSocket } from "ws";
import { database, sessionService, socketService } from "..";
import { SocketMessage } from "../models/WebsocketModels";
import { SessionController } from "./SessionController";
import { UserController } from "./UserController";

async function authenticateSocket(socket, req): Promise<number> {
	if (!req.headers["user-id"]) {
		socket.close();
		return;
	}

	const user = await database.user.findFirst({
		where: {
			internal_id: req.headers["user-id"].tostring()
		}
	});
	if (!user) {
		socket.close();
		return;
	}
	await socketService.addConnection(user.id, socket);
	return user.id;
}

export async function handleConnection(ws: WebSocket, req: Request) {
	// Authenticate and add to our persistant set of connections
	const currentUserId = await authenticateSocket(ws, req);

	ws.on('error', console.error);

	ws.on('message', function message(data) {
		try {
			const req: SocketMessage = JSON.parse(data.toString());
			if (!req.method) {
				ws.send(JSON.stringify({ Error: "Received data is missing fields"}));
			} else {
				switch (req.method) {
					case "SESSION":
						new SessionController().acceptRequest(ws, req, currentUserId);
						break;
					case "PLAYING":
						new UserController().updateCurrentlyPlaying(ws, req, currentUserId);
						break;
					default:
						ws.send(JSON.stringify({ Error: "Received data has an invalid method"}));
				}
			}
		} catch {
			ws.send(JSON.stringify({ Error: "Received data is not formatted correctly"}));
		}
	});

	ws.on('close', async function close(code, reason) {
		console.error(`Socket Closed: ${reason.toString()}`);
		const userId = await socketService.retrieveBySocket(ws);
		await sessionService.leaveSession(userId);
		await socketService.removeConnectionBySocket(ws);
	});
}


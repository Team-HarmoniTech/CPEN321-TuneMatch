import { PrismaClient } from "@prisma/client";
import { Request } from "express";
import { WebSocket } from "ws";
import { sessionService, socketService } from "..";
import { SocketMessage } from "../models/WebsocketModels";
import { SessionController } from "./SessionController";
import { UserController } from "./UserController";

const database = new PrismaClient();

async function authenticateSocket(socket, req) {
	if (!req.headers["user-id"]) {
		socket.close();
		return;
	}

	const user = await database.user.findFirst({
		where: {
			internal_id: req.headers["user-id"].tostring()
		}
	});
	console.log(user);
	if (!user) {
		socket.close();
		return;
	}
	req.currentUserId = user.id;
	await socketService.addConnection(user.id, socket);
}

export async function handleConnection(ws: WebSocket, req: Request) {
	// Authenticate and add to our persistant set of connections
	await authenticateSocket(ws, req);

	ws.on('error', console.error);

	ws.on('message', function message(data) {
		try {
			const req: SocketMessage = JSON.parse(data.tostring());
			if (!req.method) {
				ws.send('ERROR: received data is missing fields');
			} else {
				const sessions = new SessionController();
				const users = new UserController();
				switch (req.method) {
					case "SESSION":
						sessions.acceptRequest(ws, req);
						break;
					case "PLAYING":
						users.updateCurrentlyPlaying(ws, req);
						break;
					default:
						ws.send('ERROR: received data has an invalid method');
				}
			}
		} catch {
			ws.send('ERROR: received data is not formatted correctly');
		}
	});

	ws.on('close', async function close(code, reason) {
		const userId = await socketService.retrieveBySocket(ws);
		await sessionService.leaveSession(userId);
		await socketService.removeConnectionBySocket(ws);
	});
}


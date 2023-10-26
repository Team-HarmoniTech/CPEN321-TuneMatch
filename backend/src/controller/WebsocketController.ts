import { Prisma } from "@prisma/client";
import { Request } from "express";
import { WebSocket } from "ws";
import { database, sessionService, socketService, userService } from "..";
import { FriendsMessage, RequestsMessage, transformUser, transformUsers } from "../models/UserModels";
import { SocketMessage } from "../models/WebsocketModels";
import { RequestController } from "./RequestController";
import { SessionController } from "./SessionController";
import { UserController } from "./UserController";

// ChatGPT Usage: No
async function authenticateSocket(socket, req): Promise<number> {
	if (!req.headers["user-id"]) {
		socket.close();
		return;
	}

	const user = await database.user.findUnique({
		where: {
			spotify_id: req.headers["user-id"]
		},
		include: { session: true }
	});
	if (!user) {
		socket.close(1008, `User ${req.headers["user-id"]} does not exist`);
		return;
	}
	await socketService.addConnection(user.id, socket);

	/* Initial Data Push */
	const friends = await userService.getUserFriends(user.id);
	socket.send(JSON.stringify(new FriendsMessage(
		"refresh", 
		transformUsers(friends, (user) => {
			return { 
				currentSong: user.current_song, 
				currentSource: user.current_source
			};
		})
	)));
	const requests = await userService.getUserFriendsRequests(user.id);
	socket.send(JSON.stringify(new RequestsMessage(
		"refresh", 
		{ 
			requesting: transformUsers(requests.requesting), 
			requested: transformUsers(requests.requested) 
		}
	)));

	return user.id;
}

// ChatGPT Usage: No
export async function handleConnection(ws: WebSocket, req: Request) {
	// Authenticate and add to our persistant set of connections
	const currentUserId = await authenticateSocket(ws, req);

	ws.on('error', console.error);

	// ChatGPT Usage: No
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
					case "FRIENDS":
						new UserController().acceptRequest(ws, req, currentUserId);
						break;
					case "REQUESTS":
						new RequestController().acceptRequest(ws, req, currentUserId);
						break;
					default:
						ws.send(JSON.stringify({ Error: "Received data has an invalid method"}));
				}
			}
		} catch {
			ws.send(JSON.stringify({ Error: "Received data is not formatted correctly"}));
		}
	});

	// ChatGPT Usage: No
	ws.on('close', async function close(code, reason) {
		console.error(`Socket Closed: ${reason.toString()}`);
		const userId = await socketService.retrieveBySocket(ws);
		if (userId) {
			const session = await sessionService.leaveSession(userId);
			await userService.updateUser({ current_song: null, current_source: Prisma.DbNull }, currentUserId);
			await userService.broadcastToFriends(currentUserId, 
				new FriendsMessage("update", transformUser(session.members.find(x => x.id === currentUserId), (user) => {
					return { 
						currentSong: user.current_song, 
						currentSource: user.current_source
					};
				}))
			);
			if (session) {
				await sessionService.messageSession(session.id, userId, { userLeave: transformUser(session.members.find(x => x.id === userId)) });
			}
			await socketService.removeConnectionBySocket(ws);
		}
	});
}


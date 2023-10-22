import { WebSocket } from "ws";
import { sessionService, userService } from "..";
import { SessionMessage, exportUser, exportUsers } from "../models/SessionModels";

export class SessionController {

    // Websocket Route Dispatcher
    async acceptRequest(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const func = (this)[message.action];
        if (!func) {
            ws.send(JSON.stringify({ Error: `Session endpoint ${message.action} does not exist.`}));
        } else {
            try {
                await func(ws, message, currentUserId);
            } catch (err) {
                throw err
                ws.send(JSON.stringify({ success: false, Error: err.message }));
            }
        }
    }

    async message(ws: WebSocket, message: SessionMessage, currentUserId: number) { 
        const currentUser = await userService.getUserById(currentUserId);
        let toSend = message;
        message.body['userId'] = currentUserId;
        await sessionService.messageSession(currentUser.session.id, currentUserId, message);
    }

    async queueAdd(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const { uri, duration, idx } = message.body;
        const currentUser = await userService.getUserById(currentUserId);
        await sessionService.queueAdd(currentUser.session.id, uri, duration, idx);
    }

    async queueNext(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const currentUser = await userService.getUserById(currentUserId);
        await sessionService.queueNext(currentUser.session.id);
    }

    async join(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const session = await sessionService.joinSession(currentUserId, message?.body?.userId || undefined);
        if (!session) {
            throw new Error(`User with id ${message.body['userId']} does not exist.`);
        }
        ws.send(JSON.stringify({ success: true, members: exportUsers(session.members.filter(x => x.id !== currentUserId)) }));
        await sessionService.messageSession(session.id, currentUserId, { userJoin: exportUser(session.members.find(x => x.id === currentUserId)) });
    }

    async leave(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const session = await sessionService.leaveSession(currentUserId);
        if (session) {
            await sessionService.messageSession(session.id, currentUserId, { userLeave: exportUser(session.members.find(x => x.id === currentUserId)) });
        }
    }
}
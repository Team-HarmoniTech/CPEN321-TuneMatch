import { WebSocket } from "ws";
import { database, sessionService } from "..";
import { SessionMessage, UserWithSession, exportUser, exportUsers } from "../models/SessionModels";

export class SessionController {
    private sessionDB = database.session;

    // Websocket Routes
    async acceptRequest(ws: WebSocket, message: SessionMessage, currentUser: UserWithSession) {
        const func = (this)[message.action];
        if (!func) {
            ws.send(JSON.stringify({ Error: `Session endpoint ${message.action} does not exist.`}));
        } else {
            try {
                func(ws, message, currentUser);
            } catch (err) {
                ws.send(JSON.stringify({ success: false, Error: err }));
            }
        }
    }

    async message(ws: WebSocket, message: SessionMessage, currentUser: UserWithSession) {
        sessionService.messageSession(currentUser, message);
    }

    async queueAdd(ws: WebSocket, message: SessionMessage, currentUser: UserWithSession) {
        const { uri, duration, idx } = message.body;
        sessionService.queueAdd(currentUser.sessionId, uri, duration, idx);
    }

    async queueNext(ws: WebSocket, message: SessionMessage, currentUser: UserWithSession) {
        sessionService.queueNext(currentUser.sessionId);
    }

    async join(ws: WebSocket, message: SessionMessage, currentUser: UserWithSession) {
        const session = await sessionService.joinSession(currentUser.id, message.body['userId']);
        if (!session) {
            throw new Error(`User with id ${message.body['userId']} does not exist.`);
        }
        ws.send(JSON.stringify({ success: true, members: exportUsers(session.members.filter(x => x.id !== currentUser.id)) }));
        sessionService.messageSession(currentUser, { userJoin: exportUser(session.members.find(x => x.id === currentUser.id)) });
    }

    async leave(ws: WebSocket, message: SessionMessage, currentUser: UserWithSession) {
        const session = await sessionService.leaveSession(currentUser.id);
        if (session) {
            sessionService.messageSession(currentUser, { userLeave: exportUser(session.members.find(x => x.id === currentUser.id)) });
        }
    }
}
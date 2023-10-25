import { WebSocket } from "ws";
import { sessionService, userService } from "..";
import { SessionMessage } from "../models/SessionModels";
import { FriendsMessage, transformUser, transformUsers } from "../models/UserModels";

export class SessionController {

    // Websocket Route Dispatcher
    // ChatGPT Usage: No
    async acceptRequest(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const func = (this)[message.action];
        if (!func) {
            ws.send(JSON.stringify({ Error: `Session endpoint ${message.action} does not exist.`}));
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
        await sessionService.messageSession(currentUser.session.id, currentUserId, message);
    }

    // ChatGPT Usage: No
    async queueAdd(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const { uri, duration, idx } = message.body;
        const currentUser = await userService.getUserById(currentUserId);
        await sessionService.queueAdd(currentUser.session.id, uri, duration, idx);
        await sessionService.messageSession(currentUser.session.id, currentUserId, message);
    }

    // ChatGPT Usage: No
    async queueNext(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const currentUser = await userService.getUserById(currentUserId);
        await sessionService.queueNext(currentUser.session.id);
        await sessionService.messageSession(currentUser.session.id, currentUserId, message);
    }

    // ChatGPT Usage: No
    async join(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const session = await sessionService.joinSession(currentUserId, message?.body?.userId || undefined);
        ws.send(JSON.stringify(new SessionMessage("join", 
            transformUsers(session.members.filter(x => x.id !== currentUserId)))));
        
        await sessionService.messageSession(session.id, currentUserId, 
            new SessionMessage("join",  transformUser(session.members.find(x => x.id === currentUserId))));
        await userService.broadcastToFriends(currentUserId, 
            new FriendsMessage("update", transformUser(session.members.find(x => x.id === currentUserId), (user) => {
                return { 
                    currentlyPlaying: user.currently_playing, 
                    session: !!user.sessionId, 
                };
            }))
        );
    }

    // ChatGPT Usage: No
    async leave(ws: WebSocket, message: SessionMessage, currentUserId: number) {
        const session = await sessionService.leaveSession(currentUserId);
        if (session) {
            await sessionService.messageSession(session.id, currentUserId, 
                new SessionMessage("leave", transformUser(session.members.find(x => x.id === currentUserId))));
            await userService.broadcastToFriends(currentUserId, 
                new FriendsMessage("update", transformUser(session.members.find(x => x.id === currentUserId), (user) => {
                    return { 
                        currentlyPlaying: user.currently_playing, 
                        session: !!user.sessionId, 
                    };
                }))
            );
        }
    }
}
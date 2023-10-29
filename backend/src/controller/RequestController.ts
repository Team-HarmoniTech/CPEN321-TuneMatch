import { socketService, userService } from "..";
import { RequestsMessage, transformUser, transformUsers } from "../models/UserModels";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class RequestController {

    // Websocket Route Dispatcher
    // ChatGPT Usage: No
    async acceptRequest(ws: WebSocket, message: SocketMessage, currentUserId: number) {
        const func = (this)[message.action];
        if (!func) {
            ws.send(JSON.stringify({ Error: `Session endpoint ${message.action} does not exist.`}));
        } else {
            try {
                await func(ws, message, currentUserId);
            } catch (err) {
                ws.send(JSON.stringify(new RequestsMessage("error", err.message )));
            }
        }
    }

    // WebSocket Routes
    // ChatGPT Usage: No
    async refresh(ws: WebSocket, message: RequestsMessage, currentUserId: number) {
        const requests = await userService.getUserFriendsRequests(currentUserId);

	    ws.send(JSON.stringify(new RequestsMessage(
            "refresh", 
            { 
                requesting: await transformUsers(requests.requesting), 
                requested: await transformUsers(requests.requested) 
            }
	    )));
    }

    // ChatGPT Usage: No
    async add(ws: WebSocket, message: RequestsMessage, currentUserId: number) {
        const otherUser = await userService.getUserBySpotifyId(message.body.userId);
        if (!otherUser) {
            throw { message: "User to add does not exist" }; 
        }

        const user = await userService.addFriend(currentUserId, otherUser.id);
        const otherUserSocket = await socketService.retrieveById(otherUser.id);

        if (otherUserSocket) {
            /* Add currently playing and session here too incase the add makes the users friends */
            otherUserSocket.send(JSON.stringify(new RequestsMessage("add", await transformUser(user, async (user) => {
                    return { 
                        currentSong: user.current_song, 
                        currentSource: user.current_source
                    };
                }
            ))));
        }
    }

    // ChatGPT Usage: No
    async remove(ws: WebSocket, message: RequestsMessage, currentUserId: number) {
        const otherUser = await userService.getUserBySpotifyId(message.body.userId);
        if (!otherUser) {
            throw { message: "User to remove does not exist" }; 
        }

        const user = await userService.removeFriend(currentUserId, otherUser.id);
        const otherUserSocket = await socketService.retrieveById(otherUser.id);

        if (otherUserSocket) {
            otherUserSocket.send(JSON.stringify(new RequestsMessage("remove",  await transformUser(user))));
        }
    }
}
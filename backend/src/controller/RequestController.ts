import {
  FriendsMessage,
  RequestsMessage,
  transformFriend,
  transformUser,
  transformUsers
} from "@models/UserModels";
import { socketService, userService } from "@src/services";
import WebSocket = require("ws");

export class RequestController {
  // WebSocket Routes
  // ChatGPT Usage: No
  async refresh(
    ws: WebSocket,
    message: RequestsMessage,
    currentUserId: number,
  ) {
    const requests = await userService.getUserFriendsRequests(currentUserId);
    ws.send(
      JSON.stringify(
        new RequestsMessage("refresh", {
          requesting: await transformUsers(requests.requesting),
          requested: await transformUsers(requests.requested),
        }),
      ),
    );
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
      otherUserSocket.send(
        JSON.stringify(
          new RequestsMessage(
            "add",
            await transformFriend(user)
          ),
        ),
      );
    }
    if (
      (await userService.getUserFriends(currentUserId)).some(
        (u) => u.id === otherUser.id,
      )
    ) {
      ws.send(
        JSON.stringify(
          new FriendsMessage(
            "update",
            await transformFriend(user)
          ),
        ),
      );
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
      otherUserSocket.send(
        JSON.stringify(
          new RequestsMessage("remove", await transformUser(user)),
        ),
      );
    }
  }
}

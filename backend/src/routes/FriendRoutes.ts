import { header, param } from "express-validator";
import { FriendController } from "../controller/FriendController";

export const FriendRoutes = [{
    method: "get",
    route: "/me/friends",
    controller: FriendController,
    action: "getFriends",
    validation: [
        header('user-id').isAlphanumeric()
    ]
}, {
    method: "get",
    route: "/me/friends/requests",
    controller: FriendController,
    action: "getRequests",
    validation: [
        header('user-id').isAlphanumeric()
    ]
}, {
    method: "put",
    route: "/me/friends/add/:spotify_id",
    controller: FriendController,
    action: "add",
    validation: [
        header('user-id').isAlphanumeric(),
        param('spotify_id').isAlphanumeric(),
    ]
}, {
    method: "put",
    route: "/me/friends/remove/:spotify_id",
    controller: FriendController,
    action: "remove",
    validation: [
        header('user-id').isAlphanumeric(),
        param('spotify_id').isAlphanumeric(),
    ]
}]
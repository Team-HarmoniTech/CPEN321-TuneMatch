import { body, header, param, query } from "express-validator";
import { UserController } from "../controller/UserController";

export const UserRoutes = [{
    method: "get",
    route: "/users/:spotify_id",
    controller: UserController,
    action: "get",
    validation: [
        header('user-id').isAlphanumeric(),
        param('spotify_id').isAlphanumeric(),
        query('fullProfile').optional().isBoolean()
    ]
}, {
    method: "get",
    route: "/users/friends/:spotify_id",
    controller: UserController,
    action: "getFriends",
    validation: [
        header('user-id').isAlphanumeric(),
        param('spotify_id').isAlphanumeric(),
    ]
}, {
    method: "get",
    route: "/users/matches/:spotify_id",
    controller: UserController,
    action: "topMatches",
    validation: [
        header('user-id').isAlphanumeric(),
        param('spotify_id').isAlphanumeric(),
    ]
}, {
    method: "post",
    route: "/users/create",
    controller: UserController,
    action: "insert",
    validation: [
        body('userData.spotify_id').isAlphanumeric(),
        body('userData.username').isString(),
        body('userData.top_artists').isArray(),
        body('userData.top_genres').isArray(),
        body('userData.pfp_url').optional().isURL()
    ]
}, {
    method: "put",
    route: "/users/update/:spotify_id",
    controller: UserController,
    action: "update",
    validation: [
        header('user-id').isAlphanumeric(),
        param('spotify_id').isAlphanumeric(),
        body('username').optional().isString(),
        body('top_artists').optional().isArray(),
        body('top_genres').optional().isArray(),
        body('pfp_url').optional().isString(),
        body('bio').optional().isString(),
        body().custom((req) => {
            const fields = ['username', 'top_artists', 'top_genres', 'pfp_url', 'bio'];
            if (!fields.some(field => req[field] !== undefined && req[field] !== null)) {
                throw new Error(`At least one of ${fields.join(', ')} must be provided`);
            }
            return true;
        })
    ]
}, {
    method: "delete",
    route: "/users/delete/:spotify_id",
    controller: UserController,
    action: "remove",
    validation: [
        header('user-id').isAlphanumeric(),
        param('spotify_id').isAlphanumeric(),
    ]
}, {
    method: "get",
    route: "/users/search/:search_term",
    controller: UserController,
    action: "searchUsers",
    validation: [
        header('user-id').isAlphanumeric(),
        param('search_term').isString(),
        query('max').optional().isInt()
    ]
}]
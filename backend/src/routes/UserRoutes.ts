import { body, header, param, query } from "express-validator";
import { UserController } from "../controller/UserController";

export const UserRoutes = [{
    method: "get",
    route: "/users/search/:search_term",
    controller: UserController,
    action: "searchUsers",
    validation: [
        header('user-id').isAlphanumeric(),
        param('search_term').isString(),
        query('max').optional().isInt()
    ]
}, {
    method: "get",
    route: "/users/:spotify_id",
    controller: UserController,
    action: "getUser",
    validation: [
        param('spotify_id').isAlphanumeric(),
        query('fullProfile').optional().isBoolean()
    ]
}, {
    method: "get",
    route: "/me",
    controller: UserController,
    action: "getUser",
    validation: [
        header('user-id').isAlphanumeric(),
        query('fullProfile').optional().isBoolean()
    ]
}, {
    method: "get",
    route: "/me/matches",
    controller: UserController,
    action: "topMatches",
    validation: [
        header('user-id').isAlphanumeric()
    ]
}, {
    method: "get",
    route: "/me/match/:spotify_id",
    controller: UserController,
    action: "getMatch",
    validation: [
        header('user-id').isAlphanumeric(),
        param('spotify_id').isAlphanumeric()
    ]
}, {
    method: "post",
    route: "/users/create",
    controller: UserController,
    action: "insertUser",
    validation: [
        body('userData.spotify_id').isAlphanumeric(),
        body('userData.username').isString(),
        body('userData.top_artists').isArray(),
        body('userData.top_genres').isArray(),
        body('userData.pfp_url').optional().isURL()
    ]
}, {
    method: "put",
    route: "/me/update",
    controller: UserController,
    action: "updateUser",
    validation: [
        header('user-id').isAlphanumeric(),
        body('userData.username').optional().isString(),
        body('userData.top_artists').optional().isArray(),
        body('userData.top_genres').optional().isArray(),
        body('userData.pfp_url').optional().isString(),
        body('userData.bio').optional().isString(),
        body().custom((req) => {
            const fields = ['username', 'top_artists', 'top_genres', 'pfp_url', 'bio'];
            if (!fields.some(field => req?.userData[field] !== undefined && req?.userData[field] !== null)) {
                throw new Error(`At least one of ${fields.join(', ')} must be provided`);
            }
            return true;
        })
    ]
}, {
    method: "delete",
    route: "/me/delete",
    controller: UserController,
    action: "deleteUser",
    validation: [
        header('user-id').isAlphanumeric()
    ]
}]
import { body, param } from "express-validator";
import { UserController } from "../controller/UserController";

export const UserRoutes = [{
    method: "get",
    route: "/users/matches/:internal_id",
    controller: UserController,
    action: "topMatches",
    validation: [
        param('internal_id').isAlphanumeric(),
    ]
},{
    method: "get",
    route: "/users/:internal_id",
    controller: UserController,
    action: "insert",
    validation: [
        param('internal_id').isAlphanumeric(),
    ]
}, {
    method: "post",
    route: "/users/create",
    controller: UserController,
    action: "insert",
    validation: [
        body('userData.internal_id').isAlphanumeric(),
        body('userData.username').isString(),
        body('userData.top_artists').isJSON(),
        body('userData.top_genres').isJSON(),
        body('userData.pfp_url').optional().isString()
    ]
}, {
    method: "put",
    route: "/users/update/:internal_id",
    controller: UserController,
    action: "update",
    validation: [
        param('id').isAlphanumeric(),
        body('username').optional().isString(),
        body('top_artists').optional().isJSON(),
        body('top_genres').optional().isJSON(),
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
    route: "/users/delete/:internal_id",
    controller: UserController,
    action: "remove",
    validation: [
        param('id').isAlphanumeric(),
    ]
}]
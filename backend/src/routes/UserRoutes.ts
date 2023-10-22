import { body, param } from "express-validator";
import { UserController } from "../controller/UserController";

export const UserRoutes = [{
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
        body('internal_id').isAlphanumeric(),
        body('username').isString(),
        body('top_artists').isJSON(),
        body('top_genres').isJSON(),
        body('pfp_url').isString()
    ]
}, {
    method: "put",
    route: "/users/update/:id",
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
    route: "/users/delete/:id",
    controller: UserController,
    action: "remove",
    validation: [
        param('id').isAlphanumeric(),
    ]
}]
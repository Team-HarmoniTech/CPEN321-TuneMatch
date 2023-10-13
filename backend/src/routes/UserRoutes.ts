import { body, param } from "express-validator"
import { UserController } from "../controller/UserController"

export const UserRoutes = [{
    method: "post",
    route: "/users/create",
    controller: UserController,
    action: "insert",
    validation: [
        // body('internal_id').exists().isAlphanumeric,
        // body('username').exists().isstring(),
        // body('top_artists').exists().isJSON(),
        // body('top_genres').exists().isJSON(),
        // body('pfp_url').isstring()
    ]
}, {
    method: "post",
    route: "/users/update/:id",
    controller: UserController,
    action: "update",
    validation: [
        param('id').exists().isAlphanumeric(),
        body('username').isstring(),
        body('top_artists').isJSON(),
        body('top_genres').isJSON(),
        body('pfp_url').isstring(),
        body('bio').isstring()
    ]
}, {
    method: "delete",
    route: "/users/:id",
    controller: UserController,
    action: "remove",
    validation: [
        param('id').exists().isAlphanumeric(),
    ]
}]
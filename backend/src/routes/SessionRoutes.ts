import { body, header, oneOf, param } from "express-validator"
import { SessionController } from "../controller/SessionController"

export const SessionRoutes = [{
    method: "post",
    route: "/session/create",
    controller: SessionController,
    action: "create",
    validation: [
        header('user-id').exists().isAlphanumeric(),
    ]
}, {
    method: "post",
    route: "/session/:session-id/add",
    controller: SessionController,
    action: "addUser",
    validation: [
        header('user-id').exists().isAlphanumeric(),
        param('session-id').exists().isAlphanumeric(),
        oneOf([
            body('user-id').isAlphanumeric(),
            body('user-ids').isArray()
        ])
    ]
}, {
    method: "delete",
    route: "/session/:session-id/end",
    controller: SessionController,
    action: "end",
    validation: [
        header('user-id').exists().isAlphanumeric(),
        param('session-id').exists().isInt(),
    ]
}, {
    method: "get",
    route: "/session/join/:user-id",
    controller: SessionController,
    action: "join",
    validation: [
        header('user-id').exists().isAlphanumeric(),
        param('user-id').exists().isAlphanumeric(),
    ]
}]
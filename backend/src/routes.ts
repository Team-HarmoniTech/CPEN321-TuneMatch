import { param } from "express-validator"
import { UserController } from "./controller/UserController"

export const Routes = [{
    method: "post",
    route: "/users",
    controller: UserController,
    action: "insert",
    validation: [
        //body('id').isAlphanumeric(),
        //body('settings').isJSON(),
    ]
}, {
    method: "delete",
    route: "/users/:id",
    controller: UserController,
    action: "remove",
    validation: [
        param('id').isString(),
    ] 
},{
    method: "get",
    route: "/users/:id",
    controller: UserController,
    action: "list",
    validation: [
        param('id').isString(),
    ] 
}]
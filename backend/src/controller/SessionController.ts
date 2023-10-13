import { PrismaClient } from "@prisma/client";
import { NextFunction, Request, Response } from "express";
import { WebSocket } from "ws";
import { SessionMessage } from "../models/SessionModels";

export class SessionController {

    private database = new PrismaClient();
    private sessionDB = this.database;

    // REST Routes
    async start(req: Request, res: Response, next: NextFunction) {
        if (req.currentUser.session) {
            throw new Error("You cannot join a second session");
        }
        // const session: Session = await this.sessionDB.create({
        //     data: {
        //         members: {
        //             connect: {
        //                 id: req.currentUser.id
        //             }
        //         }
        //     }
        // });
        // res.send({
        //     'session-id': session.id
        // });
    }

    async end(req: Request, res: Response, next: NextFunction) {
        // const session: Session = await this.sessionDB.delete({
        //     where: {
        //         id: req.params['session-id']
        //     }
        // });
        // res.send({
        //     'session-id': session.id
        // });
    }

    // Websocket Routes
    async acceptRequest(ws: WebSocket, req: SessionMessage) {
        switch (req.action) {
            case "message":
                await this.message(ws, req);
                break;
            case "join":
                await this.join(ws, req);
                break;
            case "leave":
                await this.leave(ws, req);
                break;
            case "queue":
                await this.queue(ws, req);
                break;
            default:
                throw new Error(`Error: Session endpoint ${req.action} does not exist.`);
        }
    }

    async message(ws: WebSocket, req: SessionMessage) {

    }

    async queue(ws: WebSocket, req: SessionMessage) {

    }

    async join(ws: WebSocket, req: SessionMessage) {

    }

    async leave(ws: WebSocket, req: SessionMessage) {

    }
}
import { PrismaClient } from "@prisma/client";
import { NextFunction, Request, Response } from "express";
import { socketService } from "..";
import { SocketMessage } from "../models/WebsocketModels";
import WebSocket = require("ws");

export class UserController {

    private database = new PrismaClient();
    private userDB = this.database.user;

    async insert(req: Request, res: Response, next: NextFunction) {
        console.log(req.body);
        // let data = {
        //     internal_id: req.body.id,
        //     username: req.body.username,
        //     top_artists: req.body.top_artists,
        //     top_genres: req.body.top_genres
        // }

        // Optional fields
        // if (req.body.profile_url) {
        //     data['pfp_url'] = req.body.profile_url
        // }
        res.send(socketService.connections.get(1))
    }

    async update(req: Request, res: Response, next: NextFunction) {
        let data = {};
        console.log(req.body);
        res.send(await this.database.user.update({
            where: {
                internal_id: req.params.id
            },
            data: req.body
        }));
    }

    async delete(req: Request, res: Response, next: NextFunction) {

    }

    // Websocket Routes
    async updateCurrentlyPlaying(ws: WebSocket, req: SocketMessage) {

    }
}
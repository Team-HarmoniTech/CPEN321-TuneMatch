import 'module-alias/register';

import { handleConnection } from "@controller/WebsocketController";
import { findCurrentUser } from "@middleware/CurrentUser";
import { handleError } from "@middleware/ErrorHandler";
import { Prisma } from '@prisma/client';
import { Routes } from "@src/routes";
import express, { Request, Response } from "express";
import { validationResult } from "express-validator";
import * as http from "http";
import morgan from 'morgan';
import { WebSocketServer } from "ws";
import { ENVIRONMENT } from './config';
import { database } from './services';
import { startServer } from './startup';

export const app = express();

/* Middleware */

app.use(morgan("tiny", { /* API logger */
  skip: () => { 
    return ENVIRONMENT === "test"; 
  },
})); 
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(findCurrentUser);

/* Register express routes from defined application routes in routes.ts */
// ChatGPT Usage: No
Routes.forEach((route) => {
  (app as any)[route.method](
    route.route,
    route.validation,
    async (req: Request, res: Response, next: Function) => {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        /* If there are validation errors, send a response with the error messages */
        return res.status(400).json({ errors: errors.array() });
      }

      try {
        const result = await new (route.controller as any)()[route.action](
          req,
          res,
          next,
        );
        res.json(result);
      } catch (err) {
        next(err);
      }
    },
  );
});

app.use(handleError);

/* Create http websocket server */
export const server = http.createServer(app);
const wss = new WebSocketServer({ server: server, path: "/socket" });

/* Register websocket routes with WebsocketController */
wss.on("connection", handleConnection);

/* Reset sessions and current listening then start express server */
// ChatGPT Usage: No
Promise.all([
  database.session.deleteMany(),
  database.user.updateMany({
    data: {
      current_song: Prisma.DbNull,
      current_source: Prisma.DbNull,
    },
  }),
]).then(() => {
  startServer();
});
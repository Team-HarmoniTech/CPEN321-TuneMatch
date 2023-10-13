import * as bodyParser from "body-parser"
import * as express from "express"
import { Request, Response } from "express"
import * as http from "http"
import * as morgan from "morgan"
import { WebSocketServer } from "ws"
import { PORT } from "./config"
import { handleConnection } from "./controller/WebsocketController"
import { findCurrentUser } from "./middleware/CurrentUser"
import { handleError } from "./middleware/ErrorHandler"
import { Routes } from "./routes"
import { SessionService } from "./services/SessionService"
import { UserService } from "./services/UserService"
import { WebSocketService } from "./services/WebsocketService"

// Global services
export const socketService = new WebSocketService();
export const userService = new UserService();
export const sessionService = new SessionService();

const app = express();

// Middleware
app.use(morgan('tiny')); // API logger
app.use(bodyParser.json());
app.use(findCurrentUser);

// Register express routes from defined application routes in routes.ts
Routes.forEach(route => {
  (app as any)[route.method](route.route, async (req: Request, res: Response, next: Function) => {
    try {
      const result = await (new (route.controller as any))[route.action](req, res, next)
      res.json(result);
    } catch (err) {
      next(err);
    }
  })
})

app.use(handleError);

// Create http websocket server
const server = http.createServer(app);
const wss = new WebSocketServer({ server: server, path: "/socket" });

// Register websocket routes with WebsocketController
wss.on('connection', handleConnection);

// start express server
server.listen(PORT, () => {
  console.log(`Express server has started on port ${PORT}.`);
});
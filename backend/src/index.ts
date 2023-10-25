import { PrismaClient } from "@prisma/client"
import * as bodyParser from "body-parser"
import * as express from "express"
import { Request, Response } from "express"
import { validationResult } from "express-validator"
import * as http from "http"
import * as morgan from "morgan"
import { WebSocketServer } from "ws"
import { PORT } from "./config"
import { handleConnection } from "./controller/WebsocketController"
import { findCurrentUser } from "./middleware/CurrentUser"
import { handleError } from "./middleware/ErrorHandler"
import { Routes } from "./routes"
import { ReportService } from "./services/ReportService"
import { SessionService } from "./services/SessionService"
import { UserMatchingService } from "./services/UserMatchingService"
import { UserService } from "./services/UserService"
import { WebSocketService } from "./services/WebsocketService"

// Global services *ORDER MATTERS* its like crappy dependency injection 🤙
export const database = new PrismaClient();
export const socketService = new WebSocketService();
export const userService = new UserService();
export const sessionService = new SessionService();
export const reportService = new ReportService();
export const userMatchingService = new UserMatchingService();

const app = express();

// Middleware
app.use(morgan('tiny')); // API logger
app.use(bodyParser.json());
app.use(findCurrentUser);

// Register express routes from defined application routes in routes.ts
// ChatGPT Usage: No
Routes.forEach(route => {
  (app as any)[route.method](route.route, route.validation, async (req: Request, res: Response, next: Function) => {
    // Request Validation
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        // If there are validation errors, send a response with the error messages
        return res.status(400).json({ errors: errors.array() });
    }
    // Request Handling
    try {
      const result = await (new (route.controller as any))[route.action](req, res, next)
      res.json(result);
    } catch (err) {
      next(err);
    }
  })
})

app.use(handleError); // Express error handler

// Create http websocket server
const server = http.createServer(app);
const wss = new WebSocketServer({ server: server, path: "/socket" });

// Register websocket routes with WebsocketController
wss.on('connection', handleConnection);

// Reset sessions then start express server
// ChatGPT Usage: Partial
database.session.deleteMany().then(() => {
  server.listen(PORT, () => {
    console.log(`Express server has started on port ${PORT}.`);
  })
});
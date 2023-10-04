import * as bodyParser from "body-parser"
import * as express from "express"
import { Request, Response } from "express"
import * as http from "http"
import * as morgan from "morgan"
import { WebSocketServer } from "ws"
import { PORT } from "./config"
import { handleError } from "./middleware/errorHandler"
import { Routes } from "./routes"

const app = express();

// Middleware
app.use(morgan('tiny')); // API logger
app.use(bodyParser.json());

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
const wss = new WebSocketServer({server: server, path: "/socket"});

// Register websocket routes after connection
wss.on('connection', function connection(ws) {
  ws.on('error', console.error);

  ws.on('message', function message(data) {
    ws.send(`received: ${data}`);
  });

  wss.client
});

// Websocket auth functionality
server.on('upgrade', function upgrade(request, socket, head) {
  socket.on('error', console.error);

  const authenticate = (req, next) => {
    console.log(req.headers["user-id"]);
  }

  authenticate(request, function next(err, client) {
    if (err || !client) {
      socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n');
      socket.destroy();
      return;
    }

    socket.removeListener('error', console.error);

    wss.handleUpgrade(request, socket, head, function done(ws) {
      wss.emit('connection', ws, request, client);
    });
  });
});

// start express server
server.listen(PORT, () => {
    console.log(`Express server has started on port ${PORT}.`);
});
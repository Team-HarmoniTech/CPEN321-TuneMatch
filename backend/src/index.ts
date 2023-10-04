import * as bodyParser from "body-parser"
import * as express from "express"
import { Request, Response } from "express"
import * as morgan from "morgan"
import { WebSocket, WebSocketServer } from 'ws'
import { PORT } from "./config"
import { handleError } from "./middleware/errorHandler"
import { Routes } from "./routes"

// create express app
const app = express();
const cors = require('cors'); // prevent CORS errors

app.use(morgan('tiny')); // API logger
app.use(bodyParser.json());
app.use(cors())

// register express routes from defined application routes
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

// start express server
app.listen(PORT, () => {
    console.log(`Express server has started on port ${PORT}.`);
});

// create websocket server
const wss = new WebSocketServer({ port: 8080 });

wss.on('connection', function connection(ws) {
    ws.on('error', console.error);
  
    ws.on('message', function message(data, isBinary) {
      wss.clients.forEach(function each(client) {
        if (client !== ws && client.readyState === WebSocket.OPEN) {
          client.send(data, { binary: isBinary });
        }
      });
    });
  });
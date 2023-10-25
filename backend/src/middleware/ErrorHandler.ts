import { NextFunction, Request, Response } from "express";

// ChatGPT Usage: No
export function handleError(err, req: Request, res: Response, next: NextFunction) {
    res.status(err.statusCode || 500).send({ error: err.message });
}

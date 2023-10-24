import { NextFunction, Request, Response } from "express";

export function handleError(err, req: Request, res: Response, next: NextFunction) {
    res.status(err.statusCode || 500).send({ message: err.message });
}
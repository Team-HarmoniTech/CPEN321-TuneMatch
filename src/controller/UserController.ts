import { PrismaClient } from "@prisma/client";
import { NextFunction, Request, Response } from "express";

export class UserController {

    private database = new PrismaClient();

    async insert(request: Request, response: Response, next: NextFunction) {
        return await this.database.user.create({ 
            data: {
                email: "test"
            }
        });
    }

    async remove(request: Request, response: Response, next: NextFunction) {
        return await this.database.user.delete({ 
            where: {
                email: "test"
            }
        });
    }

    async list(request: Request, response: Response, next: NextFunction) {
        return await this.database.user.findMany({ 
            where: {
                email: "test"
            }
        });
    }
}
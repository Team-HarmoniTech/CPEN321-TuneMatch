import { Connection, Prisma, Session, User } from "@prisma/client";
import { database, socketService } from "..";
import { SocketMessage } from "../models/WebsocketModels";

export class UserService {
    private userDB = database.user;

    async getUserFriends(userId: number): Promise<User[]> {
        const user = await this.userDB.findUnique({
            where: { id: userId },
            include: {
                requested: true,
                requesting: true
            }
        });
        return user.requested.filter(requested => user.requesting.includes(requested));
    }

    async addFriend(requestingId: number, requestedId: number) {
        await this.userDB.update({
            where: { id: requestingId },
            data: { requesting: { connect: { id: requestedId } } }
        });
    }

    async removeFriend(userId: number, friendId: number) {
        await this.userDB.update({
            where: { id: userId },
            data: {
                requesting: { disconnect: { id: friendId } },
                requested: { disconnect: { id: friendId } }
            }
        });
    }

    async broadcastToFriends(userId: number, message: SocketMessage) {
        const user = await this.getUserById(userId);
        const recipients = (await this.getUserFriends(user.id)).map(user => user.id);
        message = { ...message, from: user.spotify_id };
        await socketService.broadcast(recipients, message);
    }

    async getUserBySpotifyId(spotify_id: string): Promise<User & { requesting: User[], requested: User[], session: Session }> {
        return await this.userDB.findUnique({
            where: { spotify_id: spotify_id },
            include: {
                requested: true,
                requesting: true,
                session: true
            }
        });
    }

    async getUserById(id: number): Promise<User & { requesting: User[], requested: User[], session: Session }> {
        return await this.userDB.findUnique({
            where: { id: id },
            include: {
                requested: true,
                requesting: true,
                session: true
            }
        });
    }

    async upsertUser(userData: object, userId?: number): Promise<User> {
        return await this.userDB.upsert({
            where: {
                id: userId ?? -1
            },
            create: <Prisma.UserCreateInput>userData,
            update: <Prisma.UserUpdateInput>userData
        });
    }

    async deleteUser(userId: number) {
        await this.userDB.delete({
            where: {
                id: userId
            }
        });
    }
    
    async getUserConnections(userId: number): Promise<(User & { match: number })[]> {
        const user = await this.userDB.findUnique({
            where: {
                id: userId
            },
            include: {
                connections1: {
                    include: {
                        user_1: true,
                        user_2: true
                    }
                },
                connections2: {
                    include: {
                        user_1: true,
                        user_2: true
                    }
                }
            }
        });
        return [...user.connections1, ...user.connections2].map(connection => {
            const otherUser = (userId === connection.user_id_1) ? connection.user_2 : connection.user_1;
            return { ...otherUser, match: connection.match_percent };
        }).sort((u1, u2) => {
            return u1.match - u2.match;
        });
    }

    async addUserConnection(userId1: number, userId2: number, match: number) {
        if (userId1 === userId2) return;
        await database.connection.create({
            data: {
                match_percent: match,
                user_1: { connect: { id: userId1 } },
                user_2: { connect: { id: userId2 } }
            }
        })
    }

    async getRandomUser(notIn: number[]): Promise<User> {
        return await this.userDB.findFirst({
            where: { id: { notIn: notIn, }, },
        });
    }

    async getConnection(userId1: number, userId2: number): Promise<Connection> {
        return await database.connection.findFirst({
            where: {
              user_id_1: userId1,
              user_id_2: userId2,
            },
        });
    }

    async searchUsers(search: string, max?: number): Promise<User[]> {
        if (max) {
            return await this.userDB.findMany({
                where: { username: { contains: search } },
                take: max
            });
        } else {
            return await this.userDB.findMany({
                where: { username: { contains: search } }
            });
        }
    }

    async connectionsComputed(userId: number, complete?: boolean): Promise<boolean> {
        if (complete) {
            await this.userDB.update({
                where: { id: userId },
                data: { connectionComputed: true }
            });
            return true;
        } else {
            const user = await this.userDB.findUnique({
                where: { id: userId }
            });
            return user.connectionComputed;
        }
    }
}
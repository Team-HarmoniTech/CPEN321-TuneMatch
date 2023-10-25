import { Connection, Prisma, Session, User } from "@prisma/client";
import { database, socketService } from "..";
import { SocketMessage } from "../models/WebsocketModels";

export class UserService {
    private userDB = database.user;
    private connectionDB = database.connection;

    // ChatGPT Usage: Partial
    async getUserFriends(userId: number): Promise<User[]> {
        const user = await this.userDB.findUnique({
            where: { id: userId },
            include: {
                requested: true,
                requesting: true
            }
        });
        // Only keep overlapping values
        return user.requested.filter(requested => user.requesting.some(requesting => requesting.id === requested.id));
    }

    // ChatGPT Usage: Partial
    async getUserFriendsRequests(userId: number): Promise<{ requesting: User[], requested: User[] }> {
        const user = await this.userDB.findUnique({
            where: { id: userId },
            include: {
                requested: true,
                requesting: true
            }
        });
        // Filter out overlapping users
        const requesting = user.requesting.filter(requesting => !user.requested.some(requested => requested.id === requesting.id));
        const requested = user.requested.filter(requested => !user.requesting.some(requesting => requesting.id === requested.id));

        return { requesting, requested };
    }

    // ChatGPT Usage: No
    async addFriend(userId: number, requestedId: number): Promise<User> {
        try {
            return await this.userDB.update({
                where: { id: userId },
                data: { requested: { connect: { id: requestedId } } }
            });
        } catch {
            throw { message: 'User does not exist', statusCode: 400 };
        }
    }

    // ChatGPT Usage: No
    async removeFriend(userId: number, requestedId: number): Promise<User> {
        try {
            return await this.userDB.update({
                where: { id: userId },
                data: {
                    requesting: { disconnect: { id: requestedId } },
                    requested: { disconnect: { id: requestedId } }
                }
            });
        } catch {
            throw { message: 'User does not exist', statusCode: 400 };
        }
    }

    async broadcastToFriends(userId: number, message: SocketMessage) {
        const user = await this.getUserById(userId);
        const recipients = (await this.getUserFriends(user.id)).map(user => user.id);
        
        message = { ...message, from: user.spotify_id };
        await socketService.broadcast(recipients, message);
    }

    // ChatGPT Usage: No
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

    // ChatGPT Usage: No
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

    // ChatGPT Usage: No
    async createUser(userData: object): Promise<User> {
        return await this.userDB.create({
            data: <Prisma.UserCreateInput>userData
        });
    }

    // ChatGPT Usage: No
    async updateUser(userData: object, userId: number): Promise<User> {
        return await this.userDB.update({
            where: {
                id: userId
            },
            data: <Prisma.UserUpdateInput>userData
        });
    }

    // ChatGPT Usage: No
    async deleteUser(userId: number) {
        if (await socketService.retrieveById(userId)) {
            throw { message: "Cannot delete a user with an active websocket", statusCode: 400 };
        }
        await this.userDB.delete({
            where: { id: userId }
        });
    }
    
    // ChatGPT Usage: Partial
    async getUserConnections(userId: number): Promise<(User & { match: number })[]> {
        const user = await this.userDB.findUnique({
            where: { id: userId },
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

    // ChatGPT Usage: No
    async addUserConnection(userId1: number, userId2: number, match: number) {
        if (userId1 === userId2) return;
        await this.connectionDB.create({
            data: {
                match_percent: match,
                user_1: { connect: { id: userId1 } },
                user_2: { connect: { id: userId2 } }
            }
        })
    }

    // ChatGPT Usage: No
    async getRandomUser(notIn: number[]): Promise<User> {
        return await this.userDB.findFirst({
            where: { id: { notIn: notIn, }, },
        });
    }

    // ChatGPT Usage: No
    async getConnection(userId1: number, userId2: number): Promise<Connection> {
        return await this.connectionDB.findFirst({
            where: {
              user_id_1: userId1,
              user_id_2: userId2,
            },
        });
    }

    // ChatGPT Usage: No
    async searchUsers(search: string, max?: number): Promise<User[]> {
        return await this.userDB.findMany({
            where: { username: { contains: search } },
            take: max ?? 50
        });
    }

    // ChatGPT Usage: No
    async updateUserStatus(userId: number, song?: string, source?: { type: string, uri: string }): Promise<User> {
        let user: any = await this.getUserById(userId);
        /* If they are in a session don't update the source */
        if (!user.session) {
            user = await this.updateUser({ current_song: song, current_source: source }, userId);
        } else {
            user = await this.updateUser({ current_song: song }, userId);
        }
        return user;
    }
}
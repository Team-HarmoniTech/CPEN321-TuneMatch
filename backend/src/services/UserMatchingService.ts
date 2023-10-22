import { Prisma, PrismaClient, User } from "@prisma/client";
import { UserService } from "./UserService";

export class UserMatchingService {
    private userService: UserService;

    constructor() {
      this.userService = new UserService();
    }

    private database = new PrismaClient();
    private userDB = this.database.user;
    private connectionDB = this.database.connection;

    calcPercentMatch(u1: User, u2: User): number {
        const arrayScore = (arr1, arr2): number => {
            // Create map from value to index
            const arr1map = new Map<string, number>();
            arr1.forEach((value, index) => {
                arr1map.set(value, index);
            });

            let score: number = 0;
            const maxLength = Math.max(arr1.length, arr2.length);

            // Score each index of array 2 against the created map
            arr2.forEach((value, index) => {
                if (arr1map.has(value)) {
                    score += (maxLength - Math.abs(index - arr1map.get(value))) / Math.max(arr1.length);
                }
            });

            return score / maxLength;
        }

        return (arrayScore(u1.top_artists, u2.top_artists) + arrayScore(u1.top_genres, u2.top_genres)) * 50;
    }

    async matchNewUser(userId) {
      const matchedUsers = [];
      const maxMatches = 80;
      const matchThreshold = 80;
      const matchQueue = [];

      const user = await this.userService.getUser(userId);
    
      if (!user) {
        throw new Error(`User with ID ${userId} not found.`);
      }

      while (matchedUsers.length < maxMatches) {
        let userToMatch;
        if (matchQueue.length > 0) {
          // Match with calculated high matches
          userToMatch = matchQueue.pop();
        } else if (matchedUsers.length === 0 || matchedUsers.length < maxMatches) {
          // Find a random user since no checked friends that are over 80
          userToMatch = await this.userDB.findFirst({
            where: {
              id: {
                not: userId,
                notIn: matchedUsers.map((matchedUser) => matchedUser.id),
              },
            },
          });
        }
    
        if (!userToMatch) {
          break; // No more potential matches
        }
    
        // If the user is from the queue, retrieve the match percentage from the database
        const connection = await this.connectionDB.findFirst({
          where: {
            user_id_1: userId,
            user_id_2: userToMatch.id,
          },
        });

        let matchPercent;
        if (connection) {
          matchPercent = connection.match_percent;
        } else {
          // Calculate the match percentage
          matchPercent = await this.calcPercentMatch(user, userToMatch);
          // Add the user connection to the database
          await this.userService.addUserConnection(userId, userToMatch.id, matchPercent);
        }

        matchedUsers.push(userToMatch);
    
        if (matchPercent >= matchThreshold) {
          // Find friends of the user to match
          const friends = await this.userService.getUserFriends(userToMatch.id);
    
          for (const friend of friends) {
            if (
              !matchedUsers.some((matchedUser) => matchedUser.id === friend.id) &&
              matchedUsers.length < maxMatches
            ) {
              const friendMatchPercent = await this.calcPercentMatch(user, friend);

              await this.userService.addUserConnection(userId, friend.id, friendMatchPercent);
    
              if (friendMatchPercent >= matchThreshold) {
                matchQueue.push(friend);
              }
            }
          }
        }
      }
    }

    async getTopMatches(userId) {
      // Fetch the user's connections and their match percentages
      const userConnections = await this.userService.getUserConnections(userId);
      
      // Sort the connections by match percentage in descending order
      userConnections.sort((a, b) => b.match - a.match);
      
      // Return the top 50 matches
      return userConnections.slice(0, 50);
    }
}
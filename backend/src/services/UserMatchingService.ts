import { User } from "@prisma/client";
import { database, userService } from "..";

export class UserMatchingService {
    private userDB = database.user;
    private connectionDB = database.connection;

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

    async matchNewUser(userId: number) {
      const matchedUsers = [];
      const maxMatches = 80;
      const matchThreshold = 80;
      const matchQueue = [];

      const user = await userService.getUserById(userId);
    
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
          
          await userService.addUserConnection(userId, userToMatch.id, matchPercent);
        }

        matchedUsers.push(userToMatch);
    
        if (matchPercent >= matchThreshold) {
          // Find friends of the user to match
          const friends = await userService.getUserConnections(userToMatch.id);
    
          for (const friend of friends) {
            if (
              !matchedUsers.some((matchedUser) => matchedUser.id === friend.id) &&
              matchedUsers.length < maxMatches
            ) {
              const friendMatchPercent = await this.calcPercentMatch(user, friend);

              await userService.addUserConnection(userId, friend.id, friendMatchPercent);
    
              if (friendMatchPercent >= matchThreshold) {
                matchQueue.push(friend);
              }
            }
          }
        }
      }
      await userService.connectionsComputed(userId, true);
    }

    async getTopMatches(userId: number) {
      const userConnections = await userService.getUserConnections(userId);

      userConnections.sort((a, b) => b.match - a.match);

      return userConnections.slice(0, 50);
    }
}
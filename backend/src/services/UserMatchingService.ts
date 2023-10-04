import { User } from "@prisma/client";

export class UserMatchingService {
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
}
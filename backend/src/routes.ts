import { SessionRoutes } from "./routes/SessionRoutes";
import { UserRoutes } from "./routes/UserRoutes";

export const Routes = [
    ...UserRoutes,
    ...SessionRoutes,
]

export function atLeastOne(fields: string[]) {
    return (req) => {
        const { body } = req;
        if (fields.some(field => body[field] !== undefined && body[field] !== null)) {
            throw new Error('At least one of the specified fields must be provided');
        }
        return true;
    };
}
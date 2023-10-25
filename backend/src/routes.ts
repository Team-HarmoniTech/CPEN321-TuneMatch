import { FriendRoutes } from "./routes/FriendRoutes";
import { UserRoutes } from "./routes/UserRoutes";

export const Routes = [
    ...UserRoutes,
    ...FriendRoutes
]
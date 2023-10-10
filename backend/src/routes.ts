import { SessionRoutes } from "./routes/SessionRoutes"
import { UserRoutes } from "./routes/UserRoutes"

export const Routes = [
    ...UserRoutes,
    ...SessionRoutes,
]
import { ReportRoutes } from "./routes/ReportRoutes";
import { UserRoutes } from "./routes/UserRoutes";

export const Routes = [
    ...UserRoutes,
    ...ReportRoutes
]
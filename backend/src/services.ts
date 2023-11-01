import { PrismaClient } from "@prisma/client";
import { ReportService } from "./services/ReportService";
import { SessionService } from "./services/SessionService";
import { UserMatchingService } from "./services/UserMatchingService";
import { UserService } from "./services/UserService";
import { WebSocketService } from "./services/WebsocketService";

// Global services *ORDER MATTERS* its like crappy dependency injection 🤙
export const database = new PrismaClient();
export const socketService = new WebSocketService();
export const userService = new UserService();
export const sessionService = new SessionService();
export const reportService = new ReportService();
export const userMatchingService = new UserMatchingService();
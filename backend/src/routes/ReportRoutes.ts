import { ReportController } from "@controller/ReportController";
import { body, param, query } from "express-validator";

export const ReportRoutes = [
  {
    method: "post",
    route: "/reports",
    controller: ReportController,
    action: "generateReport",
    validation: [
      body("offenderId").isAlphanumeric(),
      body("reporterId").isAlphanumeric(),
      body("reason").isString(),
      body("text").isString(),
      body("context").isJSON(),
    ],
  },
  {
    method: "get",
    route: "/reports",
    controller: ReportController,
    action: "viewReports",
    validation: [
      query("dateFrom").optional().isISO8601().toDate(),
      query("dateTo").optional().isISO8601().toDate(),
    ],
  },
  {
    method: "post",
    route: "/reports/ban/:userId",
    controller: ReportController,
    action: "banUser",
    validation: [param("userId").isAlphanumeric()],
  },
];

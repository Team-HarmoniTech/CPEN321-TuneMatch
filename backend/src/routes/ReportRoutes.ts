import { ReportController } from "@controller/ReportController";
import { body, header, param, query } from "express-validator";

export const ReportRoutes = [
  {
    method: "post",
    route: "/reports/create",
    controller: ReportController,
    action: "generateReport",
    validation: [
      header("user-id").isAlphanumeric(),
      body("offenderId").isAlphanumeric(),
      body("reason").isString(),
      body("text").isString(),
      body("context").exists(),
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
    method: "put",
    route: "/reports/ban/:userId",
    controller: ReportController,
    action: "banUser",
    validation: [param("userId").isAlphanumeric()],
  },
];

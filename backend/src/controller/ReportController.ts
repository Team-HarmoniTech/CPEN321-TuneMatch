import { reportService } from "@src/services";
import { NextFunction, Request, Response } from "express";

export class ReportController {
  // ChatGPT usage: No
  async generateReport(req: Request, res: Response, next: NextFunction) {
    const { offenderId, reason, text, context } = req.body;
    const report = await reportService.generateReport(offenderId, req.currentUserSpotifyId, {
      reason,
      text,
      context,
    });
    res.send(report);
  }

  // ChatGPT usage: No
  async viewReports(req: Request, res: Response, next: NextFunction) {
    const { dateFrom, dateTo } = req.query;
    const reports = await reportService.viewReports(dateFrom, dateTo);
    res.send(reports);
  }

  // ChatGPT usage: No
  async banUser(req: Request, res: Response, next: NextFunction) {
    const userId = await reportService.banUser(req.params.userId);
    res.send({ message: `User with ID ${userId} has been banned.` });
  }
}

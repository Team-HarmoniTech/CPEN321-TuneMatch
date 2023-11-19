import { reportService } from "@src/services";
import { NextFunction, Request, Response } from "express";

export class ReportController {
  // ChatGPT usage: No
  async generateReport(req: Request, res: Response, next: NextFunction) {
    const { offenderId, reason, text, context } = req.body;
    const report = await reportService.generateReport(offenderId, req.headers.currentUserSpotifyId as string, {
      reason,
      text,
      context,
    });
    res.send(report);
  }

  // ChatGPT usage: No
  async viewReports(req: Request, res: Response, next: NextFunction) {
    const { dateFrom, dateTo } = req.query;
    const reports = await reportService.viewReports(new Date(dateFrom as string), new Date(dateTo as string));
    res.send(reports);
  }

  // ChatGPT usage: No
  async banUser(req: Request, res: Response, next: NextFunction) {
    const user = await reportService.banUser(req.params.userId);
    res.send({ message: `${user.username} is banned: ${user.is_banned}.` });
  }
}

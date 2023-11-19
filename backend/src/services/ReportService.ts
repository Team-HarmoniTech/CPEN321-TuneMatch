import { Report } from "@prisma/client";
import { database } from "@src/services";

export class ReportService {
  private reportDB = database.report;

  // ChatGPT Usage: No
  async generateReport(
    offenderId: string,
    reporterId: string,
    report: any,
  ): Promise<Report> {
    return await this.reportDB.create({
      data: {
        offender: { connect: { spotify_id: offenderId } },
        reporter: { connect: { spotify_id: reporterId } },
        reason: report.reason,
        reason_text: report.text,
        report_context: report.context,
      },
    });
  }

  // ChatGPT Usage: No
  async viewReports(dateFrom?: Date, dateTo?: Date): Promise<Report[]> {
    return await this.reportDB.findMany({
      // MIGHT NOT WOrk
      where: {
        timestamp: {
          lte: dateTo,
          gte: dateFrom,
        },
      },
    });
  }

  // ChatGPT Usage: No
  async banUser(userId: string) {
    return await database.user.update({
      where: { spotify_id: userId },
      data: { is_banned: true },
    });
  }
}

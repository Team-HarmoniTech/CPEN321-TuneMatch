import { Report } from "@prisma/client";
import { database } from "../services";

export class ReportService {
    private reportDB = database.report;

    // ChatGPT Usage: No
    async generateReport(offenderId: number, reporterId: number, report: any): Promise<Report> {
        return await this.reportDB.create({
            data: {
                offender: { connect: { id: offenderId } },
                reporter: { connect: { id: reporterId } },
                reason: report.reason,
                reason_text: report.text,
                report_context: report.context
            }
        })
    }

    // ChatGPT Usage: No
    async viewReports(dateFrom?: Date, dateTo?: Date): Promise<Report[]> {
        return await this.reportDB.findMany({ // MIGHT NOT WOrk
            where: {
                timestamp: {
                    lte: dateTo,
                    gte: dateFrom
                }
            }
        })

    }

    // ChatGPT Usage: No
    async banUser(userId: number) {
        await database.user.update({
            where: { id: userId },
            data: { is_banned: true }
        });
    }
}

import { Report } from "@prisma/client";
import { database } from "..";

export class ReportService {
    async generateReport(offenderId: number, reporterId: number, report: any): Promise<Report> {
        return await database.report.create({
            data: {
                offender: { connect: { id: offenderId } },
                reporter: { connect: { id: reporterId } },
                reason: report.reason,
                reason_text: report.text,
                report_context: report.context
            }
        })
    }

    async viewReports(dateFrom?: Date, dateTo?: Date): Promise<Report[]> {
        return await database.report.findMany({ // MIGHT NOT WOrk
            where: {
                timestamp: {
                    lte: dateTo,
                    gte: dateFrom
                }
            }
        })

    }

    async banUser(userId: number) {
        await database.user.update({
            where: { id: userId },
            data: { is_banned: true }
        });
    }
}

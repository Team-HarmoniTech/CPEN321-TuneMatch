import { server } from "@src/index";
import { ENVIRONMENT, PORT } from "./config";
import logger from "./logger";

export const startServer = (): void => {
    if (ENVIRONMENT !== "test") {
        server.listen(PORT, () => {
          logger.log(`Express server has started on port ${PORT}.`);
        });
    }
}
import winston from "winston";

const logger = winston.createLogger({
  level: process.env.LOGGING ? "error" : "info", // Default to info level, but can be overridden with environment variable
  format: winston.format.simple(),
  transports: [new winston.transports.Console()],
});

export default logger;
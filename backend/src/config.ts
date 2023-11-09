require("dotenv").config();

export const PORT = process.env.PORT || 3000;
export const LOGGING = process.env.LOGGING || true;
export const ENVIRONMENT = process.env.ENVIRONMENT || "development";

import { ENVIRONMENT } from "./config";

const logger = {
  log: (message?: any, ...optionalParams: any[]) => { 
    if (ENVIRONMENT !== "test") console.log(message, optionalParams) 
  },
  dev: (message?: any, ...optionalParams: any[]) => { 
    if (ENVIRONMENT === "dev") console.debug(message, optionalParams) 
  },
  err: (message?: any, ...optionalParams: any[]) => {
    if (ENVIRONMENT !== "test") console.error(message, optionalParams) 
  }
}

export default logger;
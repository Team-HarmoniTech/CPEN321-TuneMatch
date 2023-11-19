jest.mock('@src/config', () => ({
  ...jest.requireActual('@src/config'),
  ENVIRONMENT: 'dev',
}));

import { ENVIRONMENT, PORT } from '@src/config';
import { server } from '@src/index';
import logger from '@src/logger';
import { startServer } from '@src/startup';

describe('Global Test', () => {
    it('should start the server', async () => {
        const logSpy = jest.spyOn(console, "log");
        server.close();
        expect(ENVIRONMENT).toBe('dev');

        startServer();

        expect(server.listening).toBeTruthy();
        await new Promise(f => setTimeout(f, 1000));
        expect(logSpy).toHaveBeenCalledWith(`Express server has started on port ${PORT}.`);
    });

    it('should log', async () => {
      const logSpy = jest.spyOn(console, "log");
      const debugSpy = jest.spyOn(console, "debug");
      const errSpy = jest.spyOn(console, "error");
      
      logger.log("test log");
      expect(logSpy).toHaveBeenCalledWith("test log");

      logger.dev("test debug");
      expect(debugSpy).toHaveBeenCalledWith("test debug");

      logger.err("test err");
      expect(errSpy).toHaveBeenCalledWith("test err");
  });
});
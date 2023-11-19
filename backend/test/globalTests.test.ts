jest.mock('@src/config', () => ({
  ...jest.requireActual('@src/config'),
  ENVIRONMENT: 'prod',
}));

import { ENVIRONMENT } from '@src/config';
import { server } from '@src/index';
import { startServer } from '@src/startup';

describe('Global Test', () => {
    it('should start the server', async () => {
        server.close();
        expect(ENVIRONMENT).toBe('prod');

        startServer();

        expect(server.listening).toBeTruthy();
        await new Promise(f => setTimeout(f, 1000));
    });
});
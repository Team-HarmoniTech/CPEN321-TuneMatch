/** @type {import('ts-jest').JestConfigWithTsJest} */
const sharedConfig = {
  preset: "ts-jest",
  testEnvironment: "node",
  setupFiles: ["dotenv/config"],
  moduleNameMapper: {
    "\\.(css|less|scss|sass)$": "identity-obj-proxy",
    "^@src/(.*)$": "<rootDir>/src/$1",
    "^@controller/(.*)$": "<rootDir>/src/controller/$1",
    "^@services/(.*)$": "<rootDir>/src/services/$1",
    "^@routes/(.*)$": "<rootDir>/src/routes/$1",
    "^@models/(.*)$": "<rootDir>/src/models/$1",
    "^@middleware/(.*)$": "<rootDir>/src/middleware/$1"
  },
  globalTeardown: '<rootDir>/test/globalTeardown.ts',
}

module.exports = {
  ...sharedConfig,
  projects: [
    // {
    //   displayName: 'Http Tests',
    //   testMatch: ['<rootDir>/test/http/**/*.test.ts'],
    //   setupFilesAfterEnv: [
    //     '<rootDir>/test/http/httpSetup.ts'
    //   ],
    //   ...sharedConfig
    // },
    {
      displayName: 'Websocket Tests',
      testMatch: ['<rootDir>/test/websocket/**/*.test.ts'],
      setupFilesAfterEnv: ['<rootDir>/test/websocket/websocketSetup.ts'],
      ...sharedConfig
    },
  ],
  testTimeout: 10000
};

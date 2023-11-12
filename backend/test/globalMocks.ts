import createPrismaMock from "prisma-mock";

jest.mock("../src/services", () => ({
    ...jest.requireActual('../src/services'),
    database: createPrismaMock()
}));
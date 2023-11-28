// Set a constant date for all tests
export const testConstantDate = new Date('2023-01-01T12:00:00Z');
export const originalDate = global.Date;

global.Date = jest.fn().mockImplementation(() => testConstantDate) as any;
global.Date.now = jest.fn().mockReturnValue(+testConstantDate);
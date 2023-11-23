import { server } from "@src/index";

beforeAll((done) => {
    server.listen(0, 'localhost', done);
});

afterAll((done) => {
    server.close(done);
});
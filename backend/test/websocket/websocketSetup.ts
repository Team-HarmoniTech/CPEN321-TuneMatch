import { server } from "@src/index";

beforeEach((done) => {
    server.listen(0, 'localhost', done);
});

afterEach((done) => {
    server.close(done);
});
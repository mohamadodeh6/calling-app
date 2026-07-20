const test = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');

process.env.JWT_SECRET = 'test-secret';
process.env.DATABASE_URL = '******localhost:5432/test';
process.env.CORS_ORIGIN = '*';

const { createApp } = require('../src/app');

function setupApp(overrides = {}) {
  const userService = {
    findUserByUsername: async () => null,
    createUser: async () => ({ id: 1, username: 'alice' }),
    validatePassword: async () => true,
    listUsers: async () => [],
    ...overrides,
  };

  return { app: createApp({ userService }), userService };
}

test('POST /register registers a new user', async () => {
  const { app } = setupApp();

  const res = await request(app)
    .post('/register')
    .send({ username: 'alice', password: 'password123' });

  assert.equal(res.statusCode, 201);
  assert.equal(res.body.user.username, 'alice');
  assert.ok(res.body.token);
});

test('POST /login logs in existing user', async () => {
  const { app } = setupApp({
    findUserByUsername: async () => ({ id: 2, username: 'bob', password_hash: 'hashed' }),
    validatePassword: async () => true,
  });

  const res = await request(app)
    .post('/login')
    .send({ username: 'bob', password: 'secret123' });

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.user.username, 'bob');
  assert.ok(res.body.token);
});

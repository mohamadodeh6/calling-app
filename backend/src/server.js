const http = require('http');
const env = require('./config/env');
const { createApp } = require('./app');
const userService = require('./services/userService');
const { createSignalServer } = require('./ws/signalServer');

const app = createApp({ userService });
const server = http.createServer(app);

createSignalServer({ server, userService });

server.listen(env.port, () => {
  // eslint-disable-next-line no-console
  console.log(`Backend listening on :${env.port}`);
});

const express = require('express');
const cors = require('cors');
const env = require('./config/env');
const authRoutes = require('./routes/authRoutes');
const userRoutes = require('./routes/userRoutes');
const userService = require('./services/userService');

function createApp(deps = { userService }) {
  const app = express();

  app.use(cors({ origin: env.corsOrigin }));
  app.use(express.json());

  app.use((req, _res, next) => {
    req.deps = deps;
    next();
  });

  app.get('/health', (_req, res) => {
    res.json({ ok: true });
  });

  app.use(authRoutes);
  app.use(userRoutes);

  return app;
}

module.exports = { createApp };

const express = require('express');
const rateLimit = require('express-rate-limit');
const { authenticateHttp } = require('../middleware/auth');

const router = express.Router();
const usersRateLimiter = rateLimit({
  windowMs: 60_000,
  limit: 60,
  standardHeaders: 'draft-8',
  legacyHeaders: false,
});

router.get('/users', usersRateLimiter, authenticateHttp, async (req, res) => {
  try {
    const users = await req.deps.userService.listUsers();
    return res.json({ users });
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('List users error', error);
    return res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;

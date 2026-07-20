const express = require('express');
const { authenticateHttp } = require('../middleware/auth');

const router = express.Router();

router.get('/users', authenticateHttp, async (req, res) => {
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

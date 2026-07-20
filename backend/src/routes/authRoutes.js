const express = require('express');
const { issueToken } = require('../middleware/auth');

const router = express.Router();

function isInvalidCredentials(username, password) {
  return !username || !password || username.length < 3 || password.length < 6;
}

router.post('/register', async (req, res) => {
  const { username, password } = req.body;

  if (isInvalidCredentials(username, password)) {
    return res.status(400).json({ error: 'Username/password do not meet minimum length' });
  }

  try {
    const existing = await req.deps.userService.findUserByUsername(username);
    if (existing) {
      return res.status(409).json({ error: 'Username is already taken' });
    }

    const user = await req.deps.userService.createUser(username, password);
    return res.status(201).json({
      token: issueToken(user),
      user: { id: user.id, username: user.username },
    });
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('Register error', error);
    return res.status(500).json({ error: 'Internal server error' });
  }
});

router.post('/login', async (req, res) => {
  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({ error: 'Missing username/password' });
  }

  try {
    const user = await req.deps.userService.findUserByUsername(username);
    if (!user) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const isValid = await req.deps.userService.validatePassword(user, password);
    if (!isValid) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    return res.json({
      token: issueToken(user),
      user: { id: user.id, username: user.username },
    });
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('Login error', error);
    return res.status(500).json({ error: 'Internal server error' });
  }
});

router.post('/logout', (_req, res) => {
  // Stateless JWT logout for MVP handled on client by dropping token
  return res.status(204).send();
});

module.exports = router;

const jwt = require('jsonwebtoken');
const env = require('../config/env');

function issueToken(user) {
  return jwt.sign({ sub: user.id, username: user.username }, env.jwtSecret, {
    expiresIn: env.jwtExpiresIn,
  });
}

function authenticateHttp(req, res, next) {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');

  if (scheme !== 'Bearer' || !token) {
    return res.status(401).json({ error: 'Missing or invalid Authorization header' });
  }

  try {
    req.user = jwt.verify(token, env.jwtSecret);
    return next();
  } catch (error) {
    return res.status(401).json({ error: 'Invalid or expired token' });
  }
}

function authenticateWsToken(token) {
  if (!token) {
    throw new Error('Missing token');
  }
  return jwt.verify(token, env.jwtSecret);
}

module.exports = {
  issueToken,
  authenticateHttp,
  authenticateWsToken,
};

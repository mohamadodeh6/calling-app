const bcrypt = require('bcryptjs');
const { pool } = require('../db');

const SALT_ROUNDS = 12;

async function findUserByUsername(username) {
  const result = await pool.query(
    'SELECT id, username, password_hash, online, created_at, last_seen FROM users WHERE username = $1',
    [username],
  );
  return result.rows[0] || null;
}

async function createUser(username, password) {
  const passwordHash = await bcrypt.hash(password, SALT_ROUNDS);
  const result = await pool.query(
    `INSERT INTO users (username, password_hash, online, last_seen)
     VALUES ($1, $2, FALSE, NOW())
     RETURNING id, username, online, created_at, last_seen`,
    [username, passwordHash],
  );
  return result.rows[0];
}

async function validatePassword(user, rawPassword) {
  return bcrypt.compare(rawPassword, user.password_hash);
}

async function setUserOnlineStatus(userId, isOnline) {
  await pool.query(
    'UPDATE users SET online = $1, last_seen = NOW() WHERE id = $2',
    [isOnline, userId],
  );
}

async function listUsers() {
  const result = await pool.query(
    'SELECT id, username, online, created_at, last_seen FROM users ORDER BY username ASC',
  );
  return result.rows;
}

module.exports = {
  findUserByUsername,
  createUser,
  validatePassword,
  setUserOnlineStatus,
  listUsers,
};

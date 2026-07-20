const WebSocket = require('ws');
const { authenticateWsToken } = require('../middleware/auth');

function safeSend(ws, payload) {
  if (ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(payload));
  }
}

function createSignalServer({ server, userService }) {
  const wss = new WebSocket.Server({ noServer: true });
  const clientsByUsername = new Map();

  function broadcast(payload, except) {
    for (const client of wss.clients) {
      if (client !== except && client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify(payload));
      }
    }
  }

  function handleDisconnect(ws) {
    if (!ws.user) return;
    clientsByUsername.delete(ws.user.username);
    userService.setUserOnlineStatus(ws.user.sub, false).catch((err) => {
      // eslint-disable-next-line no-console
      console.error('Failed to set user offline', err);
    });
    broadcast({ type: 'presence', username: ws.user.username, online: false }, ws);
  }

  wss.on('connection', async (ws) => {
    clientsByUsername.set(ws.user.username, ws);
    await userService.setUserOnlineStatus(ws.user.sub, true);

    safeSend(ws, {
      type: 'auth_ok',
      username: ws.user.username,
    });

    broadcast({ type: 'presence', username: ws.user.username, online: true }, ws);

    ws.on('message', (raw) => {
      try {
        const message = JSON.parse(raw.toString());
        const { type, to } = message;

        if (!type) {
          safeSend(ws, { type: 'error', message: 'Missing message type' });
          return;
        }

        if (['call_request', 'offer', 'answer', 'ice', 'hangup'].includes(type)) {
          const recipient = clientsByUsername.get(to);
          if (!recipient) {
            safeSend(ws, { type: 'error', message: 'User is offline', to });
            return;
          }

          safeSend(recipient, {
            ...message,
            from: ws.user.username,
          });
          return;
        }

        safeSend(ws, { type: 'error', message: `Unsupported type: ${type}` });
      } catch (error) {
        safeSend(ws, { type: 'error', message: 'Invalid JSON payload' });
      }
    });

    ws.on('close', () => handleDisconnect(ws));
    ws.on('error', () => handleDisconnect(ws));
  });

  server.on('upgrade', (req, socket, head) => {
    const url = new URL(req.url, `http://${req.headers.host}`);
    if (url.pathname !== '/signal') {
      socket.destroy();
      return;
    }

    const token = url.searchParams.get('token');

    try {
      const user = authenticateWsToken(token);
      wss.handleUpgrade(req, socket, head, (ws) => {
        ws.user = user;
        wss.emit('connection', ws, req);
      });
    } catch (_error) {
      socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n');
      socket.destroy();
    }
  });

  return wss;
}

module.exports = { createSignalServer };

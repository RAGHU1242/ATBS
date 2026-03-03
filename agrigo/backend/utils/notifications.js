const { getMessaging } = require('../config/firebase');

const sendPushNotification = async ({ token, title, body, data = {} }) => {
  const messaging = getMessaging();

  if (!messaging || !token) return;

  await messaging.send({
    token,
    notification: { title, body },
    data,
  });
};

module.exports = { sendPushNotification };

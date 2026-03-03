const admin = require('firebase-admin');

let initialized = false;

const initFirebase = () => {
  if (initialized) return;

  if (!process.env.FIREBASE_PROJECT_ID || !process.env.FIREBASE_CLIENT_EMAIL || !process.env.FIREBASE_PRIVATE_KEY) {
    console.warn('⚠️ Firebase is not configured. Push notifications are disabled.');
    return;
  }

  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
    }),
  });

  initialized = true;
  console.log('✅ Firebase admin initialized');
};

const getMessaging = () => (initialized ? admin.messaging() : null);

module.exports = { initFirebase, getMessaging };

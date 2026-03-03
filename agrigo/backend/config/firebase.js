const admin = require('firebase-admin');

let initialized = false;

const initFirebase = () => {
  if (initialized) return;

  if (!process.env.FIREBASE_SERVICE_ACCOUNT) {
    console.warn('⚠️ Firebase not configured');
    return;
  }

  try {
    const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);

    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
    });

    initialized = true;
    console.log('✅ Firebase admin initialized');
  } catch (error) {
    console.error('❌ Failed to initialize Firebase:', error.message);
  }
};

const getMessaging = () =>
  initialized ? admin.messaging() : null;

module.exports = { initFirebase, getMessaging };

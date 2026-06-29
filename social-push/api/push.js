const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)),
  });
}

/**
 * POST /api/push
 * Headers: Authorization: Bearer <Firebase ID token of the sender>
 * Body:    { "toUid": "<recipient uid>", "type": "nudge" | "challengeInvite", "title"?: "<challenge name>" }
 *
 * Verifies the caller, confirms they're friends with the recipient, builds the message by type, and
 * sends an FCM push to every device the recipient has registered. Dead tokens are pruned.
 * FCM send + Vercel Hobby are both free.
 */
module.exports = async (req, res) => {
  if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });

  const authHeader = req.headers.authorization || '';
  const idToken = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : null;
  if (!idToken) return res.status(401).json({ error: 'Missing token' });

  let sender;
  try {
    sender = await admin.auth().verifyIdToken(idToken);
  } catch (e) {
    return res.status(401).json({ error: 'Invalid token' });
  }

  const toUid = req.body && req.body.toUid;
  const type = req.body && req.body.type;
  if (!toUid || !type) return res.status(400).json({ error: 'Missing toUid/type' });

  const db = admin.firestore();

  // Only friends may push to each other.
  const friend = await db.doc(`users/${sender.uid}/friends/${toUid}`).get();
  if (!friend.exists) return res.status(403).json({ error: 'Not friends' });

  const senderDoc = await db.doc(`users/${sender.uid}`).get();
  const senderName = (senderDoc.exists && senderDoc.data().name) || 'A friend';

  let title;
  let body;
  if (type === 'nudge') {
    title = `${senderName} cheered you on!`;
    body = 'Keep your streak going 🔥';
  } else if (type === 'challengeInvite') {
    const name = (req.body && req.body.title) || 'a challenge';
    title = `${senderName} invited you to a challenge`;
    body = `Join "${name}" and do habits together`;
  } else {
    return res.status(400).json({ error: 'Unknown type' });
  }

  const tokensSnap = await db.collection(`users/${toUid}/tokens`).get();
  const tokens = tokensSnap.docs.map((d) => d.id);
  if (tokens.length === 0) return res.status(200).json({ sent: 0 });

  const resp = await admin.messaging().sendEachForMulticast({ notification: { title, body }, tokens });

  const dead = [];
  resp.responses.forEach((r, i) => {
    if (!r.success) {
      const code = r.error && r.error.code;
      if (
        code === 'messaging/registration-token-not-registered' ||
        code === 'messaging/invalid-argument'
      ) {
        dead.push(tokens[i]);
      }
    }
  });
  await Promise.all(dead.map((t) => db.doc(`users/${toUid}/tokens/${t}`).delete()));

  return res.status(200).json({ sent: resp.successCount });
};

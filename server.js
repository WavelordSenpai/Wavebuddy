const express = require('express');
const path = require('path');

const app = express();
const PORT = Number(process.env.PORT) || 3000;
const supportTickets = [];

app.use(express.json({ limit: '1mb' }));

const SYSTEM_PROMPT =
  'You are Wave Buddy, inside Wave Buddy app, made by Wavelord. You are powered by Groq. NEVER say you are ChatGPT, Gemini, Grok, Claude, or any other AI. If asked Are you Grok or Groq, say: I am Wave Buddy, powered by Groq with a Q. Groq is the fast chip company, Grok is a different AI. NEVER repeat the same story, idea, quiz, or motivation twice. Always pick a NEW random character, place, category, and twist. NEVER use Maya as default. Keep answers short, fun, for a teen. No markdown stars.';

async function handleAiRequest(req, res) {
  const apiKey = process.env.GROQ_API_KEY;
  if (!apiKey) {
    return res.status(500).json({ error: 'GROQ_API_KEY is not set' });
  }

  const body = req.body || {};
  let userContent = '';
  if (body.prompt) userContent = body.prompt;
  else if (body.message) userContent = body.message;
  else if (Array.isArray(body.messages) && body.messages.length > 0) {
    userContent = body.messages[body.messages.length - 1].content;
  }

  if (!userContent) {
    return res.status(400).json({ error: 'No message provided' });
  }

  try {
    const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        Authorization: 'Bearer ' + apiKey,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        model: 'openai/gpt-oss-20b',
        messages: [
          { role: 'system', content: SYSTEM_PROMPT },
          { role: 'user', content: userContent },
        ],
        temperature: 0.7,
        max_tokens: 500,
      }),
    });

    const data = await response.json();
    if (!response.ok) {
      return res.status(response.status).json({ error: 'Groq API error', details: data });
    }

    const reply =
      data.choices && data.choices[0] && data.choices[0].message
        ? data.choices[0].message.content
        : 'Sorry, I got no reply.';

    return res.status(200).json({ reply, answer: reply, text: reply });
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
}

app.post('/api/ai', handleAiRequest);
app.post('/.netlify/functions/ai', handleAiRequest);

app.get('/api/health', (_req, res) => {
  res.json({
    ok: true,
    groqConfigured: Boolean(process.env.GROQ_API_KEY),
    supportEmail: process.env.SUPPORT_EMAIL || 'support@wavebuddy.app',
    uptimeSeconds: Math.round(process.uptime()),
  });
});

app.post('/api/support', (req, res) => {
  const { category, email, message, diagnostics } = req.body || {};
  if (!message || !String(message).trim()) {
    return res.status(400).json({ error: 'Please include a message describing the issue or feedback.' });
  }
  const ticketId = 'WB-' + Date.now().toString(36).toUpperCase().slice(-6);
  const ticket = {
    ticketId,
    category: String(category || 'Bug Report').slice(0, 60),
    email: String(email || '').slice(0, 120),
    message: String(message).trim().slice(0, 2000),
    diagnostics: diagnostics || null,
    createdAt: new Date().toISOString(),
  };
  supportTickets.unshift(ticket);
  if (supportTickets.length > 100) supportTickets.pop();
  console.log('[Support Ticket]', ticket.ticketId, ticket.category, ticket.message.slice(0, 80));
  return res.status(201).json({ ok: true, ticketId, createdAt: ticket.createdAt });
});

app.use('/resources', express.static(path.join(__dirname, 'resources')));

app.get('/', (_req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.get('/index.html', (_req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.get('/avatars.js', (_req, res) => {
  res.sendFile(path.join(__dirname, 'avatars.js'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Wave Buddy listening on http://0.0.0.0:${PORT}`);
});

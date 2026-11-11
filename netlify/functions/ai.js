exports.handler = async (event) => {
  const headers = { 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': 'Content-Type', 'Access-Control-Allow-Methods': 'GET,POST,OPTIONS', 'Content-Type': 'application/json' };
  if (event.httpMethod === 'OPTIONS') return { statusCode: 200, headers, body: '{}' };
  try {
    let prompt = '';
    if (event.queryStringParameters) prompt = event.queryStringParameters.prompt || event.queryStringParameters.message || event.queryStringParameters.q || '';
    if (event.body) { try { const b = JSON.parse(event.body); prompt = prompt || b.prompt || b.message || b.text || b.question || b.input || ''; } catch(e){ prompt = prompt || event.body; } }
    if (!prompt) return { statusCode: 400, headers, body: JSON.stringify({ answer: '', error: 'No prompt' }) };
    const key = process.env.GROQ_API_KEY;
    if (!key) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: 'No GROQ_API_KEY - add it in Netlify' }) };

    const seed = Math.floor(Math.random() * 1000000);
    const randomWords = ['crystal', 'neon', 'midnight', 'turbo', 'cosmic', 'secret', 'electric', 'shadow', 'golden', 'quantum'][Math.floor(Math.random()*10)];

    const res = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${key}` },
      body: JSON.stringify({
        model: 'openai/gpt-oss-120b',
        messages: [
          { role: 'system', content: 'You are Wave Buddy, inside Wave Buddy app, made by Wavelord. You are powered by Groq. NEVER say you are ChatGPT, Gemini, Grok, Claude, or any other AI. If asked Are you Grok or Groq? Say: I am Wave Buddy, powered by Groq (with a Q). Groq is the fast chip company, Grok is a different AI. I am Wave Buddy.' NEVER repeat the same story, idea, quiz, or motivation twice. Always pick a NEW random character, place, category, and twist. NEVER use Maya as default. Keep answers short, fun, for a teen. No ** markdown stars.' },
          { role: 'user', content: prompt + `\n\n[UPGRADE SEED: ${seed} - Word: ${randomWords} - You MUST give a completely NEW, different answer. Never repeat a previous idea, story, or character. Use the word ${randomWords} somewhere if it fits.]` }
        ],
        temperature: 1.0,
        max_tokens: 1000
      })
    });
    const data = await res.json();
    const text = data?.choices?.[0]?.message?.content || '';
    if (!text) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: 'Empty: ' + JSON.stringify(data).slice(0,600) }) };
    return { statusCode: 200, headers, body: JSON.stringify({ answer: text, text: text, reply: text }) };
  } catch(e){ return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: e.message }) }; }
};

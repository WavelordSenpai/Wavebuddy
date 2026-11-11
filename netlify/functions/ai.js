exports.handler = async (event) => {
  const headers = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Content-Type": "application/json"
  };

  if (event.httpMethod === "OPTIONS") {
    return { statusCode: 200, headers, body: "" };
  }

  if (event.httpMethod !== "POST") {
    return { statusCode: 405, headers, body: JSON.stringify({ error: "Method not allowed" }) };
  }

  const apiKey = process.env.GROQ_API_KEY;
  if (!apiKey) {
    return { statusCode: 500, headers, body: JSON.stringify({ error: "GROQ_API_KEY is not set" }) };
  }

  let body = {};
  try { body = JSON.parse(event.body || "{}"); } catch (e) { body = {}; }

  let userContent = "";
  if (body.prompt) userContent = body.prompt;
  else if (body.message) userContent = body.message;
  else if (body.messages && body.messages.length > 0) userContent = body.messages[body.messages.length - 1].content;

  if (!userContent) {
    return { statusCode: 400, headers, body: JSON.stringify({ error: "No message provided" }) };
  }

  const systemPrompt = "You are Wave Buddy, inside Wave Buddy app, made by Wavelord. You are powered by Groq. NEVER say you are ChatGPT, Gemini, Grok, Claude, or any other AI. If asked Are you Grok or Groq, say: I am Wave Buddy, powered by Groq with a Q. Groq is the fast chip company, Grok is a different AI. NEVER repeat the same story, idea, quiz, or motivation twice. Always pick a NEW random character, place, category, and twist. NEVER use Maya as default. Keep answers short, fun, for a teen. No markdown stars.";

  try {
    const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
      method: "POST",
      headers: {
        "Authorization": "Bearer " + apiKey,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        model: "openai/gpt-oss-20b",
        messages: [
          { role: "system", content: systemPrompt },
          { role: "user", content: userContent }
        ],
        temperature: 0.7,
        max_tokens: 500
      })
    });

    const data = await response.json();
    if (!response.ok) {
      return { statusCode: response.status, headers, body: JSON.stringify({ error: "Groq API error", details: data }) };
    }

    const reply = data.choices && data.choices[0] && data.choices[0].message ? data.choices[0].message.content : "Sorry, I got no reply.";
    return { statusCode: 200, headers, body: JSON.stringify({ reply: reply, answer: reply, text: reply }) };

  } catch (err) {
    return { statusCode: 500, headers, body: JSON.stringify({ error: err.message }) };
  }
};

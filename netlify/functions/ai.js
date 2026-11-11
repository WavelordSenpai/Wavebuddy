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
  try {
    body = JSON.parse(event.body || "{}");
  } catch (e) {
    body = {};
  }

  const messages = body.messages || [];
  const userMessage = body.message || "";

  let finalMessages = messages;
  if (messages.length === 0 && userMessage) {
    finalMessages = [{ role: "user", content: userMessage }];
  }

  if (finalMessages.length === 0) {
    return { statusCode: 400, headers, body: JSON.stringify({ error: "No message provided" }) };
  }

  const systemPrompt = {
    role: "system",
    content: "You are Wave Buddy, a friendly AI assistant for friends. Be fun, helpful, and concise. Keep answers short and warm."
  };

  try {
    const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
      method: "POST",
      headers: {
        "Authorization": "Bearer " + apiKey,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        model: "llama-3.3-70b-versatile",
        messages: [systemPrompt, ...finalMessages],
        temperature: 0.7,
        max_tokens: 500
      })
    });

    const data = await response.json();

    if (!response.ok) {
      return { statusCode: response.status, headers, body: JSON.stringify({ error: "Groq API error", details: data }) };
    }

    const reply = data.choices && data.choices[0] && data.choices[0].message ? data.choices[0].message.content : "Sorry, I got no reply.";

    return { statusCode: 200, headers, body: JSON.stringify({ reply: reply }) };

  } catch (err) {
    return { statusCode: 500, headers, body: JSON.stringify({ error: err.message }) };
  }
};

/**
 * Wave Buddy — Modular Avatar & Animation Registry
 * ------------------------------------------------------------
 * How to tweak or add a new companion:
 * 1. Add a unique key to AVATAR_ORDER and a matching object in WAVE_AVATARS.
 * 2. Define colors, role, personality, tips, and `signatureMoves` (unique animations).
 * 3. Build the SVG inside `buildSvg()` using makeShadow(), makeFace(), makeArms(), makeLegs().
 * 4. Add any custom CSS @keyframes for your signature animation classes in index.html.
 */
(function (global) {
  'use strict';

  var AVATAR_ORDER = [
    'clippy',
    'crystal',
    'tetra',
    'cube',
    'dodeca',
    'icosa',
    'robot',
    'sticky',
    'owl',
    'sprout',
    'cassette',
    'comet'
  ];

  function esc(str) {
    return String(str || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function makeShadow() {
    return '<ellipse class="buddy-shadow" cx="100" cy="214" rx="46" ry="9" fill="rgba(0,0,0,0.38)"/>';
  }

  function makeFace(opts) {
    var cx = opts.cx || 100;
    var cy = opts.cy || 96;
    var sc = opts.sc || 1;
    var browColor = opts.browColor || '#14171F';
    var blushColor = opts.blushColor || '#FF7B89';
    var eyeRx = 14.5 * sc;
    var eyeRy = 16.5 * sc;
    var eyeGap = 27 * sc;
    var eyeY = cy - 7 * sc;
    var pupilR = Math.max(5.5, 7 * sc);

    var glasses = opts.glasses
      ? '<g class="glasses" opacity="0.94">' +
          '<circle cx="' + (cx - eyeGap) + '" cy="' + eyeY + '" r="' + (19 * sc) + '" fill="rgba(255,255,255,0.08)" stroke="#1E2433" stroke-width="3.2"/>' +
          '<circle cx="' + (cx + eyeGap) + '" cy="' + eyeY + '" r="' + (19 * sc) + '" fill="rgba(255,255,255,0.08)" stroke="#1E2433" stroke-width="3.2"/>' +
          '<path d="M' + (cx - eyeGap + 19 * sc) + ' ' + eyeY + ' Q' + cx + ' ' + (eyeY - 5 * sc) + ' ' + (cx + eyeGap - 19 * sc) + ' ' + eyeY + '" fill="none" stroke="#1E2433" stroke-width="3"/>' +
        '</g>'
      : '';

    var mouth = opts.smilePath
      ? opts.smilePath
      : '<path class="mouth" d="M' + (cx - 15 * sc) + ' ' + (cy + 21 * sc) + ' Q' + cx + ' ' + (cy + 32 * sc) + ' ' + (cx + 15 * sc) + ' ' + (cy + 21 * sc) + '" fill="none" stroke="' + browColor + '" stroke-width="3.4" stroke-linecap="round"/>';

    return (
      '<g class="face">' +
        '<g class="eye">' +
          '<ellipse cx="' + (cx - eyeGap) + '" cy="' + eyeY + '" rx="' + eyeRx + '" ry="' + eyeRy + '" fill="#FFFFFF" stroke="#1B2030" stroke-width="2.2"/>' +
          '<g class="pupil">' +
            '<circle cx="' + (cx - eyeGap + 2.5 * sc) + '" cy="' + (eyeY + 2 * sc) + '" r="' + pupilR + '" fill="#10131C"/>' +
            '<circle cx="' + (cx - eyeGap + 5 * sc) + '" cy="' + (eyeY - 1.5 * sc) + '" r="' + (2.6 * sc) + '" fill="#FFFFFF"/>' +
            '<circle cx="' + (cx - eyeGap - 1 * sc) + '" cy="' + (eyeY + 4.5 * sc) + '" r="' + (1.3 * sc) + '" fill="#FFFFFF" opacity="0.75"/>' +
          '</g>' +
        '</g>' +
        '<g class="eye">' +
          '<ellipse cx="' + (cx + eyeGap) + '" cy="' + eyeY + '" rx="' + eyeRx + '" ry="' + eyeRy + '" fill="#FFFFFF" stroke="#1B2030" stroke-width="2.2"/>' +
          '<g class="pupil">' +
            '<circle cx="' + (cx + eyeGap - 2.5 * sc) + '" cy="' + (eyeY + 2 * sc) + '" r="' + pupilR + '" fill="#10131C"/>' +
            '<circle cx="' + (cx + eyeGap + 0.5 * sc) + '" cy="' + (eyeY - 1.5 * sc) + '" r="' + (2.6 * sc) + '" fill="#FFFFFF"/>' +
            '<circle cx="' + (cx + eyeGap - 4.5 * sc) + '" cy="' + (eyeY + 4.5 * sc) + '" r="' + (1.3 * sc) + '" fill="#FFFFFF" opacity="0.75"/>' +
          '</g>' +
        '</g>' +
        glasses +
        '<path class="eyebrow" d="M' + (cx - eyeGap - 11 * sc) + ' ' + (cy - 26 * sc) + ' Q' + (cx - eyeGap) + ' ' + (cy - 31 * sc) + ' ' + (cx - eyeGap + 11 * sc) + ' ' + (cy - 26 * sc) + '" fill="none" stroke="' + browColor + '" stroke-width="3.6" stroke-linecap="round"/>' +
        '<path class="eyebrow" d="M' + (cx + eyeGap - 11 * sc) + ' ' + (cy - 26 * sc) + ' Q' + (cx + eyeGap) + ' ' + (cy - 31 * sc) + ' ' + (cx + eyeGap + 11 * sc) + ' ' + (cy - 26 * sc) + '" fill="none" stroke="' + browColor + '" stroke-width="3.6" stroke-linecap="round"/>' +
        '<ellipse cx="' + (cx - eyeGap - 12 * sc) + '" cy="' + (cy + 14 * sc) + '" rx="' + (8.5 * sc) + '" ry="' + (4.5 * sc) + '" fill="' + blushColor + '" opacity="0.55"/>' +
        '<ellipse cx="' + (cx + eyeGap + 12 * sc) + '" cy="' + (cy + 14 * sc) + '" rx="' + (8.5 * sc) + '" ry="' + (4.5 * sc) + '" fill="' + blushColor + '" opacity="0.55"/>' +
        mouth +
      '</g>'
    );
  }

  function makeArms(opts) {
    var lx = opts.lx || 42;
    var rx = opts.rx || 158;
    var y = opts.y || 110;
    var c = opts.color || '#2E3440';
    var hi = opts.highlight || '#4C566A';
    var handC = opts.handColor || '#F4F6FB';
    var propR = opts.propR || '';
    var propL = opts.propL || '';

    return (
      '<g class="armL">' +
        '<circle cx="' + lx + '" cy="' + y + '" r="6.5" fill="' + c + '"/>' +
        '<path d="M' + lx + ' ' + y + ' L' + (lx - 4) + ' ' + (y + 22) + '" stroke="' + c + '" stroke-width="10" stroke-linecap="round"/>' +
        '<path d="M' + lx + ' ' + y + ' L' + (lx - 4) + ' ' + (y + 22) + '" stroke="' + hi + '" stroke-width="3" stroke-linecap="round" opacity="0.55"/>' +
        '<g class="forearmL">' +
          '<path d="M' + (lx - 4) + ' ' + (y + 22) + ' L' + (lx - 7) + ' ' + (y + 42) + '" stroke="' + c + '" stroke-width="9.5" stroke-linecap="round"/>' +
          '<circle class="handL" cx="' + (lx - 8) + '" cy="' + (y + 49) + '" r="8.5" fill="' + handC + '" stroke="' + c + '" stroke-width="2.2"/>' +
          propL +
        '</g>' +
      '</g>' +
      '<g class="armR">' +
        '<circle cx="' + rx + '" cy="' + y + '" r="6.5" fill="' + c + '"/>' +
        '<path d="M' + rx + ' ' + y + ' L' + (rx + 4) + ' ' + (y + 22) + '" stroke="' + c + '" stroke-width="10" stroke-linecap="round"/>' +
        '<path d="M' + rx + ' ' + y + ' L' + (rx + 4) + ' ' + (y + 22) + '" stroke="' + hi + '" stroke-width="3" stroke-linecap="round" opacity="0.55"/>' +
        '<g class="forearmR">' +
          '<path d="M' + (rx + 4) + ' ' + (y + 22) + ' L' + (rx + 7) + ' ' + (y + 42) + '" stroke="' + c + '" stroke-width="9.5" stroke-linecap="round"/>' +
          '<circle class="handR" cx="' + (rx + 8) + '" cy="' + (y + 49) + '" r="8.5" fill="' + handC + '" stroke="' + c + '" stroke-width="2.2"/>' +
          propR +
        '</g>' +
      '</g>'
    );
  }

  function makeLegs(opts) {
    var o = opts || {};
    var y = o.y || 152;
    var legC = o.color || '#252A36';
    var shoeC = o.shoeColor || '#181B24';
    var stripe = o.stripe || '#FFD700';
    return (
      '<g class="legL">' +
        '<rect x="78" y="' + y + '" width="16" height="22" rx="7" fill="' + legC + '"/>' +
        '<g class="shinL">' +
          '<rect x="78" y="' + (y + 18) + '" width="16" height="17" rx="6" fill="' + legC + '"/>' +
          '<ellipse cx="84" cy="' + (y + 39) + '" rx="14" ry="7" fill="' + shoeC + '"/>' +
          '<path d="M74 ' + (y + 39) + ' Q84 ' + (y + 36) + ' 94 ' + (y + 39) + '" stroke="' + stripe + '" stroke-width="2" fill="none" opacity="0.8"/>' +
        '</g>' +
      '</g>' +
      '<g class="legR">' +
        '<rect x="106" y="' + y + '" width="16" height="22" rx="7" fill="' + legC + '"/>' +
        '<g class="shinR">' +
          '<rect x="106" y="' + (y + 18) + '" width="16" height="17" rx="6" fill="' + legC + '"/>' +
          '<ellipse cx="116" cy="' + (y + 39) + '" rx="14" ry="7" fill="' + shoeC + '"/>' +
          '<path d="M106 ' + (y + 39) + ' Q116 ' + (y + 36) + ' 126 ' + (y + 39) + '" stroke="' + stripe + '" stroke-width="2" fill="none" opacity="0.8"/>' +
        '</g>' +
      '</g>'
    );
  }

  function makeJuggleBalls() {
    return (
      '<circle class="ball ball-1" cx="56" cy="18" r="6" fill="#FFD700" opacity="0"/>' +
      '<circle class="ball ball-2" cx="100" cy="8" r="5" fill="#FF6B6B" opacity="0"/>' +
      '<circle class="ball ball-3" cx="144" cy="18" r="6" fill="#4CC9FF" opacity="0"/>'
    );
  }

  var WAVE_AVATARS = {
    clippy: {
      key: 'clippy',
      name: 'Clippy Jr.',
      short: 'CLIPPY',
      emoji: '📎',
      role: 'Writing & Edits',
      color: '#D8E2F2',
      accent: '#5B8DEF',
      text: '#0E172A',
      personality: 'Clippy Jr., classic helpful paperclip assistant, witty, eager to polish writing, brainstorm, and organize notes.',
      workActionLabel: 'Polish My Draft',
      workPromptPrefix: 'Polish and tighten this draft in 2-3 clear sentences or bullets:\n\n',
      tips: [
        'It looks like you are writing! Drop text in Work Draft and tap Polish.',
        'Tell me "remember ..." and I will lock it into your Notes Vault.',
        'Need a 25-min focus sprint? Start the Focus Timer anytime!'
      ],
      signatureMoves: [
        { id: 'wire-twist', label: '📎 Wire Twist', cls: 'sig-wireTwist', dur: 2200, em: 'happy', thought: '📎 Boing!' },
        { id: 'scribble', label: '✏️ Scribble', cls: 'sig-scribble', dur: 2400, em: 'curious', thought: '✏️ Taking notes...' }
      ],
      buildSvg: function () {
        var pencil =
          '<g class="prop-pencil" transform="translate(160,154) rotate(-25)">' +
            '<rect x="-4" y="-22" width="8" height="32" rx="2" fill="#FBBF24" stroke="#92400E" stroke-width="1.5"/>' +
            '<polygon points="-4,-22 4,-22 0,-30" fill="#FDE68A" stroke="#92400E" stroke-width="1.2"/>' +
            '<polygon points="-1.5,-27 1.5,-27 0,-30" fill="#1F2937"/>' +
            '<rect x="-4" y="8" width="8" height="6" rx="1.5" fill="#FB7185"/>' +
          '</g>';
        var notepad =
          '<g class="fx-clippy-pad" opacity="0">' +
            '<rect x="10" y="126" width="44" height="52" rx="6" fill="#FEF9C3" stroke="#CA8A04" stroke-width="2"/>' +
            '<line x1="18" y1="140" x2="46" y2="140" stroke="#94A3B8" stroke-width="2.5" stroke-linecap="round"/>' +
            '<line x1="18" y1="150" x2="42" y2="150" stroke="#94A3B8" stroke-width="2.5" stroke-linecap="round"/>' +
            '<line x1="18" y1="160" x2="36" y2="160" stroke="#5B8DEF" stroke-width="2.5" stroke-linecap="round"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<linearGradient id="clippyWire" x1="0" y1="0" x2="1" y2="1">' +
                '<stop offset="0%" stop-color="#F8FAFC"/>' +
                '<stop offset="45%" stop-color="#94A3B8"/>' +
                '<stop offset="100%" stop-color="#475569"/>' +
              '</linearGradient>' +
            '</defs>' +
            makeShadow() +
            '<g class="body">' +
              '<path d="M72 152 L72 52 C72 22 128 22 128 52 L128 152 C128 184 82 184 82 152 L82 68 C82 48 116 48 116 68 L116 140" fill="none" stroke="#0F172A" stroke-width="18" stroke-linecap="round" stroke-linejoin="round"/>' +
              '<path d="M72 152 L72 52 C72 22 128 22 128 52 L128 152 C128 184 82 184 82 152 L82 68 C82 48 116 48 116 68 L116 140" fill="none" stroke="url(#clippyWire)" stroke-width="13" stroke-linecap="round" stroke-linejoin="round"/>' +
              '<path d="M70 144 L70 54 C70 28 124 28 124 54" fill="none" stroke="#FFFFFF" stroke-width="3" stroke-linecap="round" opacity="0.75"/>' +
            '</g>' +
            '<g class="head">' +
              makeFace({ cx: 100, cy: 86, sc: 0.96, browColor: '#0F172A', blushColor: '#60A5FA' }) +
            '</g>' +
            notepad +
            makeArms({ lx: 56, rx: 144, y: 118, color: '#475569', highlight: '#CBD5E1', handColor: '#F8FAFC', propR: pencil }) +
            makeLegs({ y: 162, color: '#334155', shoeColor: '#0F172A', stripe: '#60A5FA' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    crystal: {
      key: 'crystal',
      name: 'Crystal',
      short: 'CRYSTAL',
      emoji: '💎',
      role: 'Tasks & Reminders',
      color: '#FFD700',
      accent: '#F59E0B',
      text: '#111827',
      personality: 'Crystal, shiny upbeat organizer who keeps your day sparkling, breaks down tasks, and sets reminders.',
      workActionLabel: 'Turn Into Checklist',
      workPromptPrefix: 'Turn this into a crisp 3-step action checklist:\n\n',
      tips: [
        'Stay sharp! Want me to turn your notes into a 3-step checklist?',
        'Long-press me in the room to cycle through all 12 companions!'
      ],
      signatureMoves: [
        { id: 'prism-spark', label: '✨ Prism Glow', cls: 'sig-prismGlow', dur: 2400, em: 'happy', thought: '✨ Sparkling!' },
        { id: 'levitate', label: '💎 Levitate', cls: 'sig-levitate', dur: 2500, em: 'happy', thought: '💎 Ascending!' }
      ],
      buildSvg: function () {
        var sparkles =
          '<g class="fx-sparkles" opacity="0">' +
            '<path class="sp1" d="M32 34 L35 26 L38 34 L46 37 L38 40 L35 48 L32 40 L24 37 Z" fill="#FFFBEB"/>' +
            '<path class="sp2" d="M164 38 L167 30 L170 38 L178 41 L170 44 L167 52 L164 44 L156 41 Z" fill="#FDE047"/>' +
            '<path class="sp3" d="M100 2 L102 -4 L104 2 L110 4 L104 6 L102 12 L100 6 L94 4 Z" fill="#FFFFFF"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<linearGradient id="crysGrad" x1="0" y1="0" x2="1" y2="1">' +
                '<stop offset="0%" stop-color="#FFF3A1"/>' +
                '<stop offset="45%" stop-color="#FFD700"/>' +
                '<stop offset="100%" stop-color="#D97706"/>' +
              '</linearGradient>' +
            '</defs>' +
            makeShadow() +
            '<g class="head"><g class="body">' +
              '<polygon points="100,12 168,66 142,158 58,158 32,66" fill="url(#crysGrad)" stroke="#78350F" stroke-width="3"/>' +
              '<polygon points="100,12 168,66 100,66" fill="#FFFBEB" opacity="0.45"/>' +
              '<polygon points="32,66 100,12 100,66" fill="#FEF08A" opacity="0.35"/>' +
              '<polygon points="100,158 142,158 168,66 100,66" fill="#B45309" opacity="0.22"/>' +
              '<path d="M52 40 L56 32 L60 40 L68 44 L60 48 L56 56 L52 48 L44 44 Z" fill="#FFFBEB" opacity="0.9"/>' +
              makeFace({ cx: 100, cy: 102, sc: 0.94 }) +
            '</g></g>' +
            sparkles +
            makeArms({ lx: 40, rx: 160, y: 112, color: '#334155', highlight: '#64748B' }) +
            makeLegs({ y: 156, stripe: '#FFD700' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    tetra: {
      key: 'tetra',
      name: 'Tetra',
      short: 'TETRA',
      emoji: '🔺',
      role: 'Hype & Summaries',
      color: '#FF6B6B',
      accent: '#EF4444',
      text: '#FFFFFF',
      personality: 'Tetra, spicy, fast, funny motivator who turns walls of text into punchy 3-bullet TL;DRs.',
      workActionLabel: 'Give Me TL;DR',
      workPromptPrefix: 'Summarize this into 3 short punchy bullet points:\n\n',
      tips: [
        'Paste anything long in Work Draft and hit Give Me TL;DR!',
        'Momentum beats perfection—ship the first version!'
      ],
      signatureMoves: [
        { id: 'rocket-boost', label: '🚀 Rocket Boost', cls: 'sig-rocketBoost', dur: 2000, em: 'surprised', thought: '🚀 Blast off!' },
        { id: 'hype-shout', label: '📣 Hype Wave', cls: 'sig-hypeWave', dur: 2200, em: 'happy', thought: '📣 Let’s go!' }
      ],
      buildSvg: function () {
        var flame =
          '<g class="fx-flame" opacity="0">' +
            '<path d="M84 162 Q100 202 116 162 Z" fill="#F97316"/>' +
            '<path d="M90 162 Q100 188 110 162 Z" fill="#FDE047"/>' +
          '</g>';
        var waves =
          '<g class="fx-soundwaves" opacity="0">' +
            '<path d="M174 88 Q186 100 174 112" fill="none" stroke="#FDE047" stroke-width="3" stroke-linecap="round"/>' +
            '<path d="M184 80 Q200 100 184 120" fill="none" stroke="#FF6B6B" stroke-width="3" stroke-linecap="round"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<linearGradient id="tetraL" x1="0" y1="0" x2="1" y2="1">' +
                '<stop offset="0%" stop-color="#FFA5A5"/>' +
                '<stop offset="100%" stop-color="#FF5252"/>' +
              '</linearGradient>' +
              '<linearGradient id="tetraR" x1="0" y1="0" x2="1" y2="1">' +
                '<stop offset="0%" stop-color="#E11D48"/>' +
                '<stop offset="100%" stop-color="#9F1239"/>' +
              '</linearGradient>' +
            '</defs>' +
            makeShadow() +
            flame +
            '<g class="head"><g class="body">' +
              '<path d="M100 12 L170 156 L30 156 Z" fill="url(#tetraL)" stroke="#4C0519" stroke-width="3" stroke-linejoin="round"/>' +
              '<path d="M100 12 L170 156 L112 156 Z" fill="url(#tetraR)" opacity="0.45"/>' +
              '<path d="M100 18 L50 144" stroke="#FFE4E6" stroke-width="3" stroke-linecap="round" opacity="0.5"/>' +
              makeFace({ cx: 100, cy: 112, sc: 0.86, blushColor: '#FDE047' }) +
            '</g></g>' +
            waves +
            makeArms({ lx: 50, rx: 150, y: 116, color: '#27272A', highlight: '#52525B' }) +
            makeLegs({ y: 156, stripe: '#FF6B6B' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    cube: {
      key: 'cube',
      name: 'Cube',
      short: 'CUBE',
      emoji: '🧊',
      role: 'Focus Timer',
      color: '#4CC9FF',
      accent: '#0EA5E9',
      text: '#082F49',
      personality: 'Cube, chill, steady focus coach who helps you lock into Pomodoro sprints and keep calm.',
      workActionLabel: 'Break Down Step 1',
      workPromptPrefix: 'Give me just the smallest 5-minute first step for this:\n\n',
      tips: [
        'Start a 25m Focus sprint—I will watch the clock for you.',
        'One task at a time keeps stress low.'
      ],
      signatureMoves: [
        { id: 'beat-nod', label: '🎧 Beat Nod', cls: 'sig-beatNod', dur: 2400, em: 'happy', thought: '🎧 Locked in' },
        { id: 'zen-float', label: '🧊 Zen Hover', cls: 'sig-levitate', dur: 2500, em: 'curious', thought: '🧊 Deep focus' }
      ],
      buildSvg: function () {
        var pulses =
          '<g class="fx-beats" opacity="0">' +
            '<path d="M12 76 Q4 92 12 108" fill="none" stroke="#7DD3FC" stroke-width="3" stroke-linecap="round"/>' +
            '<path d="M188 76 Q196 92 188 108" fill="none" stroke="#7DD3FC" stroke-width="3" stroke-linecap="round"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<linearGradient id="cubeFront" x1="0" y1="0" x2="1" y2="1">' +
                '<stop offset="0%" stop-color="#7DD3FC"/>' +
                '<stop offset="100%" stop-color="#0284C7"/>' +
              '</linearGradient>' +
            '</defs>' +
            makeShadow() +
            pulses +
            '<g class="head"><g class="body">' +
              '<polygon points="44,30 156,30 170,44 30,44" fill="#BAE6FD" stroke="#0C4A6E" stroke-width="2.5" stroke-linejoin="round"/>' +
              '<rect x="30" y="44" width="140" height="114" rx="16" fill="url(#cubeFront)" stroke="#0C4A6E" stroke-width="3"/>' +
              '<path d="M42 56 L158 56" stroke="#E0F2FE" stroke-width="3" stroke-linecap="round" opacity="0.6"/>' +
              '<rect x="22" y="74" width="10" height="36" rx="5" fill="#0F172A"/>' +
              '<rect x="168" y="74" width="10" height="36" rx="5" fill="#0F172A"/>' +
              '<path d="M27 74 C27 16 173 16 173 74" fill="none" stroke="#1E293B" stroke-width="6" stroke-linecap="round"/>' +
              makeFace({ cx: 100, cy: 102, sc: 0.96 }) +
            '</g></g>' +
            makeArms({ lx: 30, rx: 170, y: 112, color: '#1E293B', highlight: '#475569' }) +
            makeLegs({ y: 158, stripe: '#4CC9FF' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    dodeca: {
      key: 'dodeca',
      name: 'Dodeca',
      short: 'DODECA',
      emoji: '🔮',
      role: 'Creative Ideas',
      color: '#A78BFA',
      accent: '#8B5CF6',
      text: '#FFFFFF',
      personality: 'Dodeca, imaginative brainstormer who invents fresh stories, metaphors, and unexpected angles.',
      workActionLabel: '3 Creative Angles',
      workPromptPrefix: 'Give me 3 creative or unexpected angles on this idea:\n\n',
      tips: [
        'Stuck? Ask me for 3 wild twists on your draft!',
        'Combine two unrelated things for instant originality.'
      ],
      signatureMoves: [
        { id: 'mystic-halo', label: '🔮 Mystic Halo', cls: 'sig-mysticHalo', dur: 2400, em: 'curious', thought: '🔮 Vision...' },
        { id: 'idea-bulb', label: '💡 Idea Flash', cls: 'sig-ideaFlash', dur: 2200, em: 'surprised', thought: '💡 Idea!' }
      ],
      buildSvg: function () {
        var halo =
          '<g class="fx-halo" opacity="0">' +
            '<ellipse cx="100" cy="14" rx="44" ry="10" fill="none" stroke="#FDE047" stroke-width="3" stroke-dasharray="8 6"/>' +
          '</g>';
        var bulb =
          '<g class="fx-bulb" opacity="0" transform="translate(100,6)">' +
            '<circle cx="0" cy="0" r="11" fill="#FDE047" stroke="#854D0E" stroke-width="2"/>' +
            '<line x1="0" y1="-16" x2="0" y2="-22" stroke="#FDE047" stroke-width="2.5" stroke-linecap="round"/>' +
            '<line x1="-15" y1="-10" x2="-20" y2="-14" stroke="#FDE047" stroke-width="2.5" stroke-linecap="round"/>' +
            '<line x1="15" y1="-10" x2="20" y2="-14" stroke="#FDE047" stroke-width="2.5" stroke-linecap="round"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<radialGradient id="dodGrad" cx="40%" cy="35%" r="65%">' +
                '<stop offset="0%" stop-color="#DDD6FE"/>' +
                '<stop offset="55%" stop-color="#8B5CF6"/>' +
                '<stop offset="100%" stop-color="#4C1D95"/>' +
              '</radialGradient>' +
            '</defs>' +
            makeShadow() +
            halo + bulb +
            '<g class="head"><g class="body">' +
              '<polygon points="100,12 168,58 144,154 56,154 32,58" fill="url(#dodGrad)" stroke="#2E1065" stroke-width="3" stroke-linejoin="round"/>' +
              '<polygon points="100,32 146,66 128,136 72,136 54,66" fill="none" stroke="#EDE9FE" stroke-width="1.8" opacity="0.45"/>' +
              '<circle cx="100" cy="24" r="4" fill="#FDE047"/>' +
              makeFace({ cx: 100, cy: 96, sc: 0.94, blushColor: '#F472B6' }) +
            '</g></g>' +
            makeArms({ lx: 40, rx: 160, y: 112, color: '#312E81', highlight: '#6366F1' }) +
            makeLegs({ y: 154, stripe: '#A78BFA' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    icosa: {
      key: 'icosa',
      name: 'Icosa',
      short: 'ICOSA',
      emoji: '🧠',
      role: 'Science & Facts',
      color: '#34D399',
      accent: '#10B981',
      text: '#022C22',
      personality: 'Icosa, big-brain science & math buddy who explains complex things simply with clear analogies.',
      workActionLabel: 'Explain Simply',
      workPromptPrefix: 'Explain this clearly using one simple real-world analogy:\n\n',
      tips: [
        'Paste confusing jargon in Work Draft and tap Explain Simply!',
        'Ask me quick math, conversions, or science trivia.'
      ],
      signatureMoves: [
        { id: 'atom-orbit', label: '⚛️ Atom Spin', cls: 'sig-atomOrbit', dur: 2400, em: 'curious', thought: '⚛️ Calculating...' },
        { id: 'eureka', label: '🤓 Specs Adjust', cls: 'sig-eureka', dur: 2000, em: 'surprised', thought: '🤓 Eureka!' }
      ],
      buildSvg: function () {
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<linearGradient id="icoGrad" x1="0" y1="0" x2="1" y2="1">' +
                '<stop offset="0%" stop-color="#A7F3D0"/>' +
                '<stop offset="50%" stop-color="#10B981"/>' +
                '<stop offset="100%" stop-color="#065F46"/>' +
              '</linearGradient>' +
            '</defs>' +
            makeShadow() +
            '<g class="head"><g class="body">' +
              '<g class="fx-orbit">' +
                '<ellipse cx="100" cy="84" rx="84" ry="22" fill="none" stroke="#6EE7B7" stroke-width="2" stroke-dasharray="6 5" transform="rotate(-16 100 84)" opacity="0.72"/>' +
                '<circle cx="24" cy="102" r="4.5" fill="#FDE047"/>' +
                '<circle cx="176" cy="66" r="4.5" fill="#38BDF8"/>' +
              '</g>' +
              '<polygon points="100,10 162,48 176,104 134,156 66,156 24,104 38,48" fill="url(#icoGrad)" stroke="#064E3B" stroke-width="3" stroke-linejoin="round"/>' +
              '<polyline points="38,48 100,56 162,48" fill="none" stroke="#D1FAE5" stroke-width="1.6" opacity="0.5"/>' +
              '<line x1="100" y1="10" x2="100" y2="56" stroke="#D1FAE5" stroke-width="1.6" opacity="0.5"/>' +
              makeFace({ cx: 100, cy: 98, sc: 0.92, glasses: true }) +
            '</g></g>' +
            makeArms({ lx: 30, rx: 170, y: 112, color: '#1F2937', highlight: '#4B5563' }) +
            makeLegs({ y: 156, stripe: '#34D399' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    robot: {
      key: 'robot',
      name: 'Robo',
      short: 'ROBO',
      emoji: '🤖',
      role: 'Code & Tech',
      color: '#38BDF8',
      accent: '#0284C7',
      text: '#082F49',
      personality: 'Robo, friendly retro-tech nerd who helps with code, shortcuts, and debugging errors.',
      workActionLabel: 'Debug / Fix Code',
      workPromptPrefix: 'Spot the bug or suggest a clean improvement for this:\n\n',
      tips: [
        'Paste a snippet or error in Work Draft and hit Debug!',
        'Check the Support & Debug tab anytime to inspect app logs.'
      ],
      signatureMoves: [
        { id: 'radar-ping', label: '📡 Radar Ping', cls: 'sig-radarPing', dur: 2300, em: 'curious', thought: '📡 Scanning...' },
        { id: 'jet-hover', label: '⚡ Jet Hover', cls: 'sig-jetHover', dur: 2200, em: 'happy', thought: '⚡ Overclocked!' }
      ],
      buildSvg: function () {
        var radar =
          '<g class="fx-radar" opacity="0">' +
            '<circle cx="100" cy="11" r="14" fill="none" stroke="#FFD700" stroke-width="2.5"/>' +
            '<circle cx="100" cy="11" r="24" fill="none" stroke="#38BDF8" stroke-width="2"/>' +
          '</g>';
        var jets =
          '<g class="fx-jets" opacity="0">' +
            '<path d="M78 218 L84 234 L90 218 Z" fill="#38BDF8"/>' +
            '<path d="M110 218 L116 234 L122 218 Z" fill="#38BDF8"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<linearGradient id="roboBody" x1="0" y1="0" x2="0" y2="1">' +
                '<stop offset="0%" stop-color="#38BDF8"/>' +
                '<stop offset="100%" stop-color="#0369A1"/>' +
              '</linearGradient>' +
              '<linearGradient id="roboHead" x1="0" y1="0" x2="1" y2="1">' +
                '<stop offset="0%" stop-color="#F8FAFC"/>' +
                '<stop offset="100%" stop-color="#CBD5E1"/>' +
              '</linearGradient>' +
            '</defs>' +
            makeShadow() +
            radar +
            '<g class="body">' +
              '<rect x="64" y="138" width="72" height="50" rx="16" fill="url(#roboBody)" stroke="#0C4A6E" stroke-width="3"/>' +
              '<rect x="78" y="150" width="44" height="24" rx="7" fill="#0F172A"/>' +
              '<circle class="led-1" cx="90" cy="162" r="4" fill="#34D399"/>' +
              '<circle class="led-2" cx="102" cy="162" r="4" fill="#FFD700"/>' +
              '<circle class="led-3" cx="114" cy="162" r="4" fill="#38BDF8"/>' +
            '</g>' +
            '<g class="head">' +
              '<line x1="100" y1="12" x2="100" y2="34" stroke="#334155" stroke-width="5" stroke-linecap="round"/>' +
              '<circle class="antenna-tip" cx="100" cy="11" r="8" fill="#FFD700" stroke="#92400E" stroke-width="2"/>' +
              '<rect x="34" y="34" width="132" height="100" rx="26" fill="url(#roboHead)" stroke="#0284C7" stroke-width="3.5"/>' +
              '<rect x="44" y="44" width="112" height="80" rx="18" fill="#0F172A" opacity="0.92"/>' +
              makeFace({ cx: 100, cy: 86, sc: 0.9, browColor: '#38BDF8', blushColor: '#38BDF8', smilePath: '<rect class="mouth" x="80" y="106" width="40" height="9" rx="4.5" fill="#38BDF8"/>' }) +
            '</g>' +
            makeArms({ lx: 56, rx: 144, y: 144, color: '#475569', highlight: '#94A3B8', handColor: '#38BDF8' }) +
            makeLegs({ y: 178, color: '#334155', shoeColor: '#0F172A', stripe: '#38BDF8' }) +
            jets +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    sticky: {
      key: 'sticky',
      name: 'Sticky',
      short: 'STICKY',
      emoji: '📝',
      role: 'Quick Notes',
      color: '#FDE047',
      accent: '#EAB308',
      text: '#422006',
      personality: 'Sticky, cheerful sticky-note buddy who captures key takeaways and saves facts cleanly.',
      workActionLabel: 'Extract Key Points',
      workPromptPrefix: 'Extract the 3 most important takeaways to remember from this:\n\n',
      tips: [
        'Use the Notes & Reminders tab to pin facts while you work!',
        'Tap Extract Key Points on your draft to distill main ideas.'
      ],
      signatureMoves: [
        { id: 'paper-flutter', label: '📄 Flutter', cls: 'sig-flutter', dur: 2200, em: 'happy', thought: '📄 Breezy!' },
        { id: 'check-pop', label: '✅ Checkmark', cls: 'sig-checkPop', dur: 2000, em: 'happy', thought: '✅ Noted!' }
      ],
      buildSvg: function () {
        var check =
          '<g class="fx-check" opacity="0">' +
            '<circle cx="156" cy="36" r="14" fill="#22C55E" stroke="#14532D" stroke-width="2"/>' +
            '<path d="M149 36 L154 41 L164 31" fill="none" stroke="#FFFFFF" stroke-width="3.2" stroke-linecap="round" stroke-linejoin="round"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<linearGradient id="stickyGrad" x1="0" y1="0" x2="1" y2="1">' +
                '<stop offset="0%" stop-color="#FEF08A"/>' +
                '<stop offset="100%" stop-color="#FACC15"/>' +
              '</linearGradient>' +
            '</defs>' +
            makeShadow() +
            '<g class="head"><g class="body">' +
              '<path d="M36 32 L164 32 L164 134 L138 158 L36 158 Z" fill="url(#stickyGrad)" stroke="#854D0E" stroke-width="3" stroke-linejoin="round"/>' +
              '<polygon points="138,158 138,134 164,134" fill="#FEF9C3" stroke="#854D0E" stroke-width="2.5" stroke-linejoin="round"/>' +
              '<rect x="74" y="20" width="52" height="16" rx="3" fill="#FCA5A5" opacity="0.85" stroke="#B91C1C" stroke-width="1.5"/>' +
              '<line x1="48" y1="54" x2="152" y2="54" stroke="#EAB308" stroke-width="1.5" opacity="0.55"/>' +
              makeFace({ cx: 100, cy: 98, sc: 0.94, browColor: '#422006' }) +
            '</g></g>' +
            check +
            makeArms({ lx: 34, rx: 166, y: 112, color: '#713F12', highlight: '#A16207' }) +
            makeLegs({ y: 158, color: '#713F12', shoeColor: '#422006', stripe: '#FDE047' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    owl: {
      key: 'owl',
      name: 'Owlbert',
      short: 'OWL',
      emoji: '🦉',
      role: 'Proofreading',
      color: '#D97706',
      accent: '#B45309',
      text: '#FFFBEB',
      personality: 'Owlbert, wise night-owl editor who spots typos, sharpens tone, and keeps late study sessions cozy.',
      workActionLabel: 'Proofread Draft',
      workPromptPrefix: 'Proofread this for grammar, clarity, and tone, then list any fixes:\n\n',
      tips: [
        'Paste a paragraph in Work Draft and tap Proofread!',
        'Working late? Remember to blink and rest your eyes.'
      ],
      signatureMoves: [
        { id: 'owl-tilt', label: '🦉 Owl Tilt', cls: 'sig-owlTilt', dur: 2200, em: 'curious', thought: '🦉 Hoot?' },
        { id: 'wing-flap', label: '📖 Night Read', cls: 'sig-wingFlap', dur: 2400, em: 'happy', thought: '📖 Reading...' }
      ],
      buildSvg: function () {
        var book =
          '<g class="fx-book" opacity="0" transform="translate(68,140)">' +
            '<path d="M0 10 Q16 2 32 10 L32 34 Q16 26 0 34 Z" fill="#FFFBEB" stroke="#78350F" stroke-width="2"/>' +
            '<path d="M32 10 Q48 2 64 10 L64 34 Q48 26 32 34 Z" fill="#FEF3C7" stroke="#78350F" stroke-width="2"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<linearGradient id="owlGrad" x1="0" y1="0" x2="0" y2="1">' +
                '<stop offset="0%" stop-color="#B45309"/>' +
                '<stop offset="100%" stop-color="#78350F"/>' +
              '</linearGradient>' +
            '</defs>' +
            makeShadow() +
            '<g class="head"><g class="body">' +
              '<polygon points="44,48 64,20 78,44" fill="#92400E" stroke="#451A03" stroke-width="2.5" stroke-linejoin="round"/>' +
              '<polygon points="156,48 136,20 122,44" fill="#92400E" stroke="#451A03" stroke-width="2.5" stroke-linejoin="round"/>' +
              '<ellipse cx="100" cy="96" rx="64" ry="62" fill="url(#owlGrad)" stroke="#451A03" stroke-width="3"/>' +
              '<ellipse cx="100" cy="122" rx="40" ry="34" fill="#FEF3C7" opacity="0.9"/>' +
              '<path d="M84 120 Q90 125 96 120 M104 120 Q110 125 116 120 M94 132 Q100 137 106 132" stroke="#D97706" stroke-width="2.4" fill="none" stroke-linecap="round"/>' +
              makeFace({
                cx: 100,
                cy: 84,
                sc: 0.94,
                glasses: true,
                browColor: '#FEF3C7',
                smilePath: '<polygon class="mouth" points="93,97 107,97 100,108" fill="#F59E0B" stroke="#78350F" stroke-width="1.8"/>'
              }) +
            '</g></g>' +
            makeArms({ lx: 38, rx: 162, y: 110, color: '#78350F', highlight: '#B45309', handColor: '#FEF3C7' }) +
            book +
            makeLegs({ y: 156, color: '#D97706', shoeColor: '#F59E0B', stripe: '#FFFBEB' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    sprout: {
      key: 'sprout',
      name: 'Sprout',
      short: 'SPROUT',
      emoji: '🌱',
      role: 'Breaks & Hydration',
      color: '#86EFAC',
      accent: '#22C55E',
      text: '#052E16',
      personality: 'Sprout, gentle desk-plant companion who reminds you to hydrate, stretch, and avoid burnout.',
      workActionLabel: 'Quick Break Idea',
      workPromptPrefix: 'Suggest a refreshing 2-minute desk break or reset based on what I am doing:\n\n',
      tips: [
        'Water check! Take a sip of water and relax your jaw.',
        'Roll your shoulders back and look away from the screen for 20s.'
      ],
      signatureMoves: [
        { id: 'water-drop', label: '💧 Hydrate', cls: 'sig-waterDrop', dur: 2400, em: 'happy', thought: '💧 Hydrate!' },
        { id: 'leaf-bloom', label: '☀️ Sun Bask', cls: 'sig-sunBask', dur: 2400, em: 'happy', thought: '☀️ Growing!' }
      ],
      buildSvg: function () {
        var drop =
          '<g class="fx-water" opacity="0">' +
            '<path d="M100 0 C100 0 110 14 110 20 A10 10 0 1 1 90 20 C90 14 100 0 100 0 Z" fill="#38BDF8" stroke="#0284C7" stroke-width="1.8"/>' +
          '</g>';
        var sun =
          '<g class="fx-sun" opacity="0">' +
            '<line x1="64" y1="8" x2="72" y2="20" stroke="#FDE047" stroke-width="3" stroke-linecap="round"/>' +
            '<line x1="100" y1="2" x2="100" y2="16" stroke="#FDE047" stroke-width="3" stroke-linecap="round"/>' +
            '<line x1="136" y1="8" x2="128" y2="20" stroke="#FDE047" stroke-width="3" stroke-linecap="round"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<linearGradient id="potGrad" x1="0" y1="0" x2="1" y2="1">' +
                '<stop offset="0%" stop-color="#FB923C"/>' +
                '<stop offset="100%" stop-color="#C2410C"/>' +
              '</linearGradient>' +
            '</defs>' +
            makeShadow() +
            drop + sun +
            '<g class="head"><g class="body">' +
              '<g class="fx-leaves">' +
                '<path d="M100 66 C100 38 76 18 54 26 C64 46 82 54 100 52 Z" fill="#4ADE80" stroke="#14532D" stroke-width="2.5"/>' +
                '<path d="M100 66 C100 32 126 14 150 24 C138 46 120 54 100 52 Z" fill="#22C55E" stroke="#14532D" stroke-width="2.5"/>' +
              '</g>' +
              '<rect x="96" y="48" width="8" height="22" rx="4" fill="#16A34A"/>' +
              '<rect x="38" y="64" width="124" height="22" rx="9" fill="#EA580C" stroke="#7C2D12" stroke-width="3"/>' +
              '<path d="M46 86 L154 86 L142 158 L58 158 Z" fill="url(#potGrad)" stroke="#7C2D12" stroke-width="3" stroke-linejoin="round"/>' +
              makeFace({ cx: 100, cy: 118, sc: 0.88, browColor: '#431407' }) +
            '</g></g>' +
            makeArms({ lx: 48, rx: 152, y: 118, color: '#15803D', highlight: '#4ADE80', handColor: '#DCFCE7' }) +
            makeLegs({ y: 158, color: '#9A3412', shoeColor: '#431407', stripe: '#4ADE80' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    cassette: {
      key: 'cassette',
      name: 'Cassette',
      short: 'BEATS',
      emoji: '📼',
      role: 'Deep-Work Vibes',
      color: '#FB7185',
      accent: '#F43F5E',
      text: '#FFFFFF',
      personality: 'Cassette, upbeat lo-fi mixtape companion who keeps your study groove steady and drops fun trivia.',
      workActionLabel: 'Study Vibe Check',
      workPromptPrefix: 'Give me an encouraging study boost and a quick fun music/focus tip for:\n\n',
      tips: [
        'Queue up an instrumental playlist and lock in for one sprint!',
        'Ask me for a quick riddle when you need a mental reset.'
      ],
      signatureMoves: [
        { id: 'reel-spin', label: '📼 Spin Reels', cls: 'sig-reelSpin', dur: 2400, em: 'happy', thought: '🎵 Playing...' },
        { id: 'bass-bounce', label: '🔊 Bass Bounce', cls: 'sig-bassBounce', dur: 2200, em: 'happy', thought: '🔊 Groovy!' }
      ],
      buildSvg: function () {
        var notes =
          '<g class="fx-notes" opacity="0">' +
            '<text x="34" y="34" font-size="20" fill="#FB7185" font-weight="bold">♪</text>' +
            '<text x="152" y="28" font-size="22" fill="#38BDF8" font-weight="bold">♫</text>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            makeShadow() +
            notes +
            '<g class="head"><g class="body">' +
              '<rect x="26" y="46" width="148" height="106" rx="14" fill="#1E293B" stroke="#0F172A" stroke-width="3"/>' +
              '<rect x="38" y="56" width="124" height="68" rx="8" fill="#FFF1F2"/>' +
              '<rect x="38" y="56" width="124" height="18" rx="6" fill="#FB7185"/>' +
              '<circle class="reel-l" cx="56" cy="65" r="5" fill="#0F172A"/>' +
              '<circle class="reel-r" cx="144" cy="65" r="5" fill="#0F172A"/>' +
              '<polygon points="56,152 144,152 136,134 64,134" fill="#334155" stroke="#0F172A" stroke-width="2"/>' +
              makeFace({ cx: 100, cy: 98, sc: 0.88, browColor: '#1E293B', blushColor: '#FB7185' }) +
            '</g></g>' +
            makeArms({ lx: 26, rx: 174, y: 112, color: '#334155', highlight: '#64748B', handColor: '#FFF1F2' }) +
            makeLegs({ y: 152, color: '#1E293B', shoeColor: '#0F172A', stripe: '#FB7185' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    },

    comet: {
      key: 'comet',
      name: 'Comet',
      short: 'COMET',
      emoji: '☄️',
      role: 'Quick Answers',
      color: '#818CF8',
      accent: '#6366F1',
      text: '#FFFFFF',
      personality: 'Comet, zippy starlight companion built for instant answers, rapid Q&A, and snappy rewrites.',
      workActionLabel: 'Shorten & Punch Up',
      workPromptPrefix: 'Make this shorter, punchier, and easier to scan:\n\n',
      tips: [
        'Got a fast question? Fire away!',
        'Drag me anywhere on screen—I travel light.'
      ],
      signatureMoves: [
        { id: 'warp-dash', label: '☄️ Warp Dash', cls: 'sig-warpDash', dur: 1900, em: 'surprised', thought: '☄️ Zoom!' },
        { id: 'star-burst', label: '🌟 Star Burst', cls: 'sig-prismGlow', dur: 2200, em: 'happy', thought: '🌟 Starlight!' }
      ],
      buildSvg: function () {
        var trail =
          '<g class="fx-trail" opacity="0">' +
            '<line x1="12" y1="68" x2="48" y2="68" stroke="#A5B4FC" stroke-width="4" stroke-linecap="round"/>' +
            '<line x1="6" y1="92" x2="44" y2="92" stroke="#FDE047" stroke-width="3.5" stroke-linecap="round"/>' +
            '<line x1="14" y1="116" x2="50" y2="116" stroke="#818CF8" stroke-width="4" stroke-linecap="round"/>' +
          '</g>';
        return (
          '<svg viewBox="0 0 200 240" width="142" height="172">' +
            '<defs>' +
              '<radialGradient id="cometGrad" cx="40%" cy="40%" r="65%">' +
                '<stop offset="0%" stop-color="#E0E7FF"/>' +
                '<stop offset="55%" stop-color="#818CF8"/>' +
                '<stop offset="100%" stop-color="#3730A3"/>' +
              '</radialGradient>' +
            '</defs>' +
            makeShadow() +
            trail +
            '<g class="head"><g class="body">' +
              '<path d="M100 10 L122 44 L158 34 L142 74 L168 106 L142 152 L58 152 L32 106 L58 74 L42 34 L78 44 Z" fill="url(#cometGrad)" stroke="#1E1B4B" stroke-width="3" stroke-linejoin="round"/>' +
              '<circle cx="100" cy="30" r="4" fill="#FDE047"/>' +
              makeFace({ cx: 100, cy: 104, sc: 0.9, browColor: '#1E1B4B', blushColor: '#FDE047' }) +
            '</g></g>' +
            makeArms({ lx: 38, rx: 162, y: 114, color: '#312E81', highlight: '#818CF8' }) +
            makeLegs({ y: 154, color: '#312E81', shoeColor: '#1E1B4B', stripe: '#FDE047' }) +
            makeJuggleBalls() +
          '</svg>'
        );
      }
    }
  };

  global.WAVE_AVATAR_ORDER = AVATAR_ORDER;
  global.WAVE_AVATARS = WAVE_AVATARS;
  global.escapeWaveHtml = esc;
})(window);

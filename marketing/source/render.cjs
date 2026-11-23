// Renders the listing artwork in marketing/images/ from real in-game screenshots (marketing/images/screenshots/), the
// mod's own HUD pixel art (marketing/source/renders/, painted by HudRenders.java), the mod's particle and menu sprites
// (1.20.1/common/src/main/resources, the same art in every workspace), the mod logo and the bundled OFL fonts. Usage (from the repository root):
//   NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available. Set CHROMIUM to a Chromium executable to use
// that one instead of Playwright's own download.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the compatibility tables of curseforge/description.md and modrinth/description.md).
const RELEASE = { version: '1.0' };
const VERSIONS = [
  { minecraft: '1.20.1', loaders: ['Fabric', 'Forge'] },
  { minecraft: '1.21.1', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.2', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.3', loaders: ['Fabric', 'NeoForge'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
const renders = path.join(__dirname, 'renders');
const textures = path.join(root, '1.20.1', 'common', 'src', 'main', 'resources', 'assets', 'pinkified', 'textures');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));

// The themes, their names and colours, copied from theme/ThemeId.java, theme/Palette.java and the en_us lang file.
// [id, name, title colour, swatches (the palette's light, base, accent and heart colours), treat]
const THEMES = [
  ['strawberry_milk', 'Strawberry Milk', '#D2456F', ['#FFF4F6', '#F8C6D6', '#E38FAD', '#F2557E'], 'Strawberries'],
  ['lavender_dreams', 'Lavender Dreams', '#7A62AE', ['#F8F4FF', '#DCCFF6', '#AC95DC', '#B583F0'], 'Macarons'],
  ['pink_coquette', 'Pink Coquette', '#C23A5C', ['#FFF9F3', '#FBD7E0', '#EBA4B8', '#E8456B'], 'Donuts'],
  ['cherry_blossom', 'Cherry Blossom', '#C85A86', ['#FFF8FA', '#FCDEE8', '#E8ABC0', '#A6D98C'], 'Dango'],
  ['angelcore', 'Angelcore', '#A8802E', ['#FFFFFF', '#F2ECE0', '#CDBFA2', '#8FB1D8'], 'Cupcakes'],
  ['pastel_princess', 'Pastel Princess', '#A9528F', ['#FFF5FC', '#F7CBE7', '#E093C7', '#FFD66E'], 'Bonbons'],
  ['custom', 'Custom', '#9C6FC0', ['#F48FB1', '#B39DDB', '#F8C6D6', '#E6DDF5'], 'Your colours'],
];
const TITLE_SHOT = { strawberry_milk: 'title.jpg', lavender_dreams: 'title-lavender.jpg', pink_coquette: 'title-coquette.jpg',
  cherry_blossom: 'title-cherry.jpg', angelcore: 'title-angelcore.jpg', pastel_princess: 'title-princess.jpg', custom: 'title-custom.jpg' };

const css = `
@font-face { font-family: PkDisplay; src: url('${url(path.join(fonts, 'fredoka.ttf'))}'); }
@font-face { font-family: PkText; src: url('${url(path.join(fonts, 'nunito.ttf'))}'); font-weight: 400; }
@font-face { font-family: PkText; src: url('${url(path.join(fonts, 'nunito-extrabold.ttf'))}'); font-weight: 800; }
:root { --text:#5a3a4a; --dim:#a07f8f; --plum:#6e2f4f; --rose:#E2557A; --rose-deep:#c23a5c; --blush:#F8C6D6; --lav:#9F86DB;
        --lav-deep:#7A62AE; --gold:#F3C25A; --cream:#fff6f8; --line:rgba(194,58,92,.28); }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--cream); color: var(--text); font-family: PkText, sans-serif; }
.stage { position: relative; overflow: hidden; background: linear-gradient(160deg, #fff7f9 0%, #fdeef3 55%, #f3edfb 100%); }
.display { font-family: PkDisplay, sans-serif; }
.pinkgrad { background: linear-gradient(100deg, #e2557a 0%, #ef7fa2 38%, #c98ad6 72%, #8f79d9 100%);
            -webkit-background-clip: text; background-clip: text; color: transparent; }
.soft { filter: drop-shadow(0 3px 0 rgba(255,255,255,.95)) drop-shadow(0 10px 24px rgba(194,58,92,.25)); }
.kicker { font-family: PkDisplay, sans-serif; color: var(--lav-deep); letter-spacing: .14em; font-size: 24px; text-transform: uppercase; }
.heart { display: inline-block; vertical-align: -4px; margin-right: 12px; }
.candy { background: repeating-linear-gradient(135deg, #f48fb1 0 12px, #fff 12px 24px, #c9b2f2 24px 36px, #fff 36px 48px); }
.rule { height: 6px; border-radius: 3px; background: repeating-linear-gradient(90deg, var(--rose) 0 10px, transparent 10px 16px, var(--lav) 16px 26px, transparent 26px 32px); opacity: .8; }
.frame { position: relative; border: 7px solid #fff; border-radius: 22px; overflow: hidden; background: #fff;
         box-shadow: 0 0 0 2px rgba(226,85,122,.5), 0 26px 60px rgba(110,47,79,.25), 0 6px 14px rgba(110,47,79,.12); }
.frame > img { display: block; width: 100%; }
canvas.motes { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
.wash { position: absolute; inset: 0; background: radial-gradient(ellipse at 70% 45%, rgba(255,255,255,0) 30%, rgba(255,244,247,.6) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(18px) brightness(1.1) saturate(1.05); }
.pill { display: inline-block; border-radius: 999px; padding: 9px 22px 7px; margin-right: 12px; font-family: PkDisplay, sans-serif; font-size: 22px;
        color: #fff; background: linear-gradient(100deg, #e2557a, #c98ad6); box-shadow: 0 6px 16px rgba(194,58,92,.28); }
.pill.ghost { color: var(--lav-deep); background: rgba(255,255,255,.9); box-shadow: 0 0 0 2px rgba(122,98,174,.55), 0 6px 16px rgba(110,47,79,.12); }
.label { position: absolute; font-family: PkDisplay, sans-serif; font-size: 23px; color: var(--plum); background: rgba(255,255,255,.95);
         border-radius: 999px; padding: 8px 20px 6px; white-space: nowrap; box-shadow: 0 0 0 2px rgba(226,85,122,.6), 0 10px 26px rgba(110,47,79,.25); z-index: 2; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 38px; margin: 0 0 16px; font-size: 27px; line-height: 1.3; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 0; top: 8px; width: 22px; height: 20px; background: var(--rose);
  -webkit-mask: var(--heart) center / contain no-repeat; mask: var(--heart) center / contain no-repeat; }
ul.points li:nth-child(3n+2)::before { background: var(--lav); }
ul.points li:nth-child(3n)::before { background: #f08fb6; }
:root { --heart: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 22'><path d='M12 21 C5 15.5 1 11.6 1 7 C1 3.6 3.6 1 6.8 1 C9 1 10.9 2.2 12 4 C13.1 2.2 15 1 17.2 1 C20.4 1 23 3.6 23 7 C23 11.6 19 15.5 12 21 Z'/></svg>"); }
b.hi { color: var(--plum); font-weight: 800; }
.stats { display: flex; gap: 14px; margin-top: 22px; }
.stats > div { flex: 1; padding: 14px 20px 12px; border-radius: 18px; background: rgba(255,255,255,.93);
               box-shadow: 0 0 0 2px rgba(226,85,122,.4), 0 14px 34px rgba(110,47,79,.16); text-align: center; }
.stats .t { font-family: PkText, sans-serif; font-weight: 800; font-size: 16px; letter-spacing: .1em; text-transform: uppercase; color: var(--dim); }
.stats .v { font-family: PkDisplay, sans-serif; font-size: 34px; color: var(--plum); margin-top: 2px; white-space: nowrap; }
.tile { position: relative; border-radius: 18px; background: rgba(255,255,255,.93); text-align: center;
        box-shadow: 0 0 0 2px rgba(226,85,122,.4), 0 14px 30px rgba(110,47,79,.16); }
.tile .art { display: flex; align-items: center; justify-content: center; gap: 10px; }
.tile .n { font-family: PkDisplay, sans-serif; color: var(--plum); line-height: 1.1; }
.tile .s { font-family: PkText, sans-serif; font-weight: 800; color: var(--dim); font-size: 15px; letter-spacing: .04em; }
img.px { image-rendering: pixelated; }
pre.code { margin: 0; font-family: 'DejaVu Sans Mono', monospace; font-size: 21px; line-height: 1.48; color: var(--text); }
`;

// A pixel heart, the Pinkified counterpart of the sibling kits' flowers and stars.
const heartSvg = (fill, size, shine = '#fff') => `<svg class="heart" width="${size}" height="${Math.round(size * 22 / 24)}" viewBox="0 0 24 22" style="display:inline-block">
  <path d="M12 21 C5 15.5 1 11.6 1 7 C1 3.6 3.6 1 6.8 1 C9 1 10.9 2.2 12 4 C13.1 2.2 15 1 17.2 1 C20.4 1 23 3.6 23 7 C23 11.6 19 15.5 12 21 Z"
        fill="${fill}" stroke="#fff" stroke-width="1.6"/><ellipse cx="7" cy="6.5" rx="2.2" ry="1.6" fill="${shine}" opacity=".75"/></svg>`;

// Drifting petals, little hearts and four-point sparkles. Deterministic so renders are reproducible.
const motes = (count = 44, seed = 7) => `<canvas class="motes"></canvas><script>
(() => { const c = document.currentScript.previousElementSibling; const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = ${seed};
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  const sparkle = (x, y, z, a) => { g.save(); g.translate(x, y); g.fillStyle = 'rgba(255,255,255,' + a + ')'; g.beginPath();
    g.moveTo(0, -z * 3); g.quadraticCurveTo(0, 0, z * 3, 0); g.quadraticCurveTo(0, 0, 0, z * 3); g.quadraticCurveTo(0, 0, -z * 3, 0); g.quadraticCurveTo(0, 0, 0, -z * 3); g.fill(); g.restore(); };
  const heart = (x, y, z, a) => { g.save(); g.translate(x, y); g.scale(z / 6, z / 6); g.fillStyle = 'rgba(240,120,160,' + a + ')'; g.beginPath();
    g.moveTo(0, 6); g.bezierCurveTo(-8, 0, -6, -6, 0, -2.5); g.bezierCurveTo(6, -6, 8, 0, 0, 6); g.fill(); g.restore(); };
  const petal = (x, y, z, a, rot, tint) => { g.save(); g.translate(x, y); g.rotate(rot);
    const h = g.createLinearGradient(-z * 4, 0, z * 4, 0); h.addColorStop(0, 'rgba(' + tint + ',' + a + ')'); h.addColorStop(1, 'rgba(255,246,250,' + a + ')');
    g.fillStyle = h; g.beginPath(); g.ellipse(0, 0, z * 4.2, z * 2.4, 0, 0, Math.PI * 2); g.fill(); g.restore(); };
  for (let i = 0; i < ${count}; i++) { const x = rnd() * c.width, y = rnd() * c.height, a = .4 + rnd() * .45, z = 1.4 + rnd() * 2.2, k = rnd();
    if (k < .25) sparkle(x, y, z * 1.2, a); else if (k < .42) heart(x, y, z * 2.2, a * .8); else petal(x, y, z, a, rnd() * Math.PI, rnd() < .6 ? '244,160,190' : '201,178,242'); } })();
</script>`;

const loadScript = `<script>
(() => { const imgs = [...document.images]; let left = imgs.length;
  const done = () => { if (--left <= 0) document.body.dataset.items = 'ready'; };
  if (!left) document.body.dataset.items = 'ready';
  for (const i of imgs) { if (i.complete) done(); else { i.onload = done; i.onerror = done; } } })();
</script>`;
// The mod's own HUD pixel art (HudRenders.java), drawn pixel-sharp at a whole-number scale.
const sprite = (name, scale = 6) => `<img class="px" src="${url(path.join(renders, `${name}.png`))}" style="zoom:${scale}">`;
// One of the mod's texture files, drawn pixel-sharp.
const tex = (rel, scale = 6) => `<img class="px" src="${url(path.join(textures, rel))}" style="zoom:${scale}">`;
const tile = (art, title, { art: h = 110, size = 20, sub = '' } = {}) => `<div class="tile" style="padding:12px 8px 12px">
    <div class="art" style="height:${h}px">${art}</div>
    <div class="n" style="font-size:${size}px;margin-top:6px">${title}</div>${sub ? `<div class="s">${sub}</div>` : ''}</div>`;
const grid = (cols, items, gap = 14) => `<div style="display:grid;grid-template-columns:repeat(${cols},1fr);gap:${gap}px">${items.join('')}</div>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div>${loadScript}</body></html>`;

// A section heading: a candy-edged ribbon with the title, readable on CurseForge's and Modrinth's light and dark themes.
const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:22px">
    <div class="rule" style="width:230px;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
    ${heartSvg('#9F86DB', 34)}
    <div style="position:relative;padding:14px 54px 12px;border-radius:999px;background:linear-gradient(100deg,#e2557a 0%,#ee7fa1 50%,#c98ad6 100%);
                box-shadow:0 0 0 4px #fff,0 0 0 6px rgba(226,85,122,.6),0 12px 28px rgba(110,47,79,.28)">
      <div class="display" style="font-size:46px;line-height:1;color:#fff;white-space:nowrap;text-shadow:0 3px 0 rgba(150,40,80,.4)">${title}</div></div>
    ${heartSvg('#F48FB1', 34)}
    <div class="rule" style="width:230px;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right. Bullets never end on a one-word line.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 980 }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${shot('title.jpg')}');opacity:.5"></div>
  <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,247,249,.96) 0%,rgba(255,247,249,.9) 34%,rgba(253,238,243,.62) 62%,rgba(243,237,251,.45) 100%)"></div>
  <div class="wash"></div>${motes()}
  <div class="candy" style="position:absolute;left:0;right:0;top:0;height:14px;opacity:.85"></div>
  <div class="candy" style="position:absolute;left:0;right:0;bottom:0;height:14px;opacity:.85"></div>
  <div style="position:absolute;left:76px;top:0;bottom:0;width:${width - artWidth - 176}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"${kicker.length > 22 ? ' style="font-size:20px;letter-spacing:.1em"' : ''}>${heartSvg('#E2557A', 26)}${kicker}</div>
    <div class="display pinkgrad soft" style="font-size:66px;line-height:1.02;margin:16px 0 22px">${title}</div>
    <div class="rule" style="width:70%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:56px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

// An exact region (x, y, w, h in source pixels, 1920x1080 captures) of a capture, scaled to W pixels wide.
const region = (file, x, y, w, h, W, full = 1920) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * full / w}px;left:${-x * W / w}px;top:${-y * W / w}px"></div>`;
const framed = (inner, style = '') => `<div class="frame" style="${style}">${inner}</div>`;
const label = (text, style) => `<div class="label" style="${style}">${text}</div>`;
const stats = (items) => `<div class="stats">${items.map(([t, v, flex]) =>
  `<div${flex ? ` style="flex:${flex}"` : ''}><div class="v">${v}</div><div class="t">${t}</div></div>`).join('')}</div>`;
// Two overlapping captures, the first top left and the second bottom right.
const pair = (first, second, firstLabel, secondLabel, height = 640, width = '73%') => `<div style="position:relative;height:${height}px">
    <div class="frame" style="position:absolute;left:0;top:0;width:${width}">${first}</div>
    <div class="frame" style="position:absolute;right:0;bottom:0;width:${width}">${second}</div>
    ${firstLabel ? label(firstLabel, 'left:24px;top:22px') : ''}${secondLabel ? label(secondLabel, 'right:24px;bottom:22px') : ''}</div>`;
const code = (file, body) => `<div class="frame" style="background:rgba(255,255,255,.96);padding:22px 32px">
  <div style="font-family:PkText;font-weight:800;font-size:18px;letter-spacing:.08em;color:var(--dim);margin-bottom:10px">${file}</div><pre class="code">${body}</pre></div>`;
const k = (t) => `<span style="color:#c23a5c">${t}</span>`;
const v = (t) => `<span style="color:#7A62AE">${t}</span>`;
const cm = (t) => `<span style="color:#c4a9b6">${t}</span>`;

const versionText = VERSIONS.map((ver) => ver.minecraft).join(' · ');

// The middle of a title-screen capture: the logo, the Pinkified ribbon and the buttons.
const titleCrop = (file, W) => region(file, 420, 20, 1080, 860, W);

const assets = {
  banner: () => page(1800, 600, `
    <img src="${shot('title.jpg')}" style="position:absolute;right:-20px;top:0px;width:1070px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 32%);mask-image:linear-gradient(90deg,transparent 0%,#000 32%)">
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,247,249,.98) 0%,rgba(255,247,249,.95) 42%,rgba(255,247,249,.45) 60%,rgba(255,247,249,0) 74%)"></div>
    ${motes(50, 11)}
    <div class="candy" style="position:absolute;left:0;right:0;top:0;height:16px"></div>
    <div class="candy" style="position:absolute;left:0;right:0;bottom:0;height:16px"></div>
    <div style="position:absolute;left:86px;top:56px;width:1000px">
      <div class="kicker">${heartSvg('#E2557A', 26)}Version ${RELEASE.version} · Sugarplum Studios</div>
      <div class="display pinkgrad soft" style="font-size:100px;line-height:1;white-space:nowrap;margin:16px 0 4px">Pinkified Minecraft</div>
      <div class="rule" style="width:640px;margin:18px 0 24px"></div>
      <div style="font-family:PkDisplay;font-size:42px;color:var(--plum);line-height:1.2">The Complete Pink Overhaul.</div>
      <div style="font-size:27px;color:var(--text);line-height:1.35;margin-top:8px">Six pastel themes for menus, inventories, the HUD<br>and particles. Gameplay stays exactly the same.</div>
      <div style="margin-top:30px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}<span class="pill ghost">Client-side only</span></div>
      <div style="margin-top:16px;font-family:PkDisplay;font-size:26px;color:var(--dim)">Minecraft ${versionText}</div>
    </div>
`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;background:radial-gradient(circle at 50% 40%,#fff7f9 0%,#fbe3ec 55%,#eadff8 100%)"></div>${motes(22, 5)}
    <div class="candy" style="position:absolute;left:46px;top:46px;width:420px;height:420px;border-radius:50%;
                box-shadow:0 0 0 6px #fff,0 0 0 10px #e2557a,0 24px 50px rgba(110,47,79,.3)"></div>
    <div style="position:absolute;left:74px;top:74px;width:364px;height:364px;border-radius:50%;background:radial-gradient(circle at 50% 42%,#fff,#fdeef3)"></div>
    <div style="position:absolute;left:74px;top:74px;width:364px;height:364px;display:flex;align-items:center;justify-content:center">
      ${sprite('strawberry_milk_heart', 26)}</div>
    ${[[256, 34], [478, 256], [256, 478], [34, 256]].map(([x, y], i) => `<div style="position:absolute;left:${x - 22}px;top:${y - 22}px;filter:drop-shadow(0 0 3px #fff) drop-shadow(0 3px 6px rgba(110,47,79,.35))">${tex(i % 2 ? 'particle/flower_0.png' : 'particle/heart.png', 5)}</div>`).join('')}`),

  'card-themes': () => card({
    kicker: 'Six themes and your own', title: 'Pick your<br>pink',
    points: ['Strawberry Milk, Lavender Dreams, Pink Coquette', 'Cherry Blossom, Angelcore, Pastel Princess', 'Custom: any two colours with hue and softness sliders',
      'One theme recolours menus, HUD, particles and tooltips', 'Switching is instant, with no resource reload'],
    art: grid(4, THEMES.map(([id, name, color, swatch, treat]) => `<div class="tile" style="padding:8px 8px 10px;box-shadow:0 0 0 3px ${color}55,0 12px 26px rgba(110,47,79,.15)">
        <div style="border-radius:12px;overflow:hidden">${titleCrop(TITLE_SHOT[id], 222)}</div>
        <div class="display" style="font-size:22px;color:${color};margin-top:8px;white-space:nowrap">${name}</div>
        <div class="art" style="height:46px;gap:8px">${sprite(`${id}_heart`, 4)}${sprite(`${id}_food`, 4)}${sprite(`${id}_armor`, 4)}</div>
        <div style="display:flex;justify-content:center;gap:6px;margin-top:4px">${swatch.map((c) => `<span style="width:22px;height:14px;border-radius:7px;background:${c};box-shadow:0 0 0 1.5px rgba(110,47,79,.25)"></span>`).join('')}</div>
        <div class="s" style="margin-top:4px">${treat}</div></div>`).concat([`<div class="tile" style="display:flex;flex-direction:column;justify-content:center;padding:14px">
        <div class="display" style="font-size:58px;color:var(--plum);line-height:1">0</div><div class="s" style="font-size:16px">gameplay changes</div>
        <div class="display" style="font-size:58px;color:var(--plum);line-height:1;margin-top:16px">7</div><div class="s" style="font-size:16px">themes to pick from</div></div>`]), 14) }),

  'card-menu': () => card({
    kicker: 'Main menu', title: 'A dreamy<br>first screen',
    points: ['Animated pastel sky with clouds, bokeh and petals', 'The Minecraft logo in theme colours, with a Pinkified ribbon',
      'Rounded buttons that lift, glow and squish', 'Sweet splash texts and an original music-box lullaby', 'A heart button opens the Pinkified settings'],
    art: framed(region('title.jpg', 0, 0, 1920, 1080, 966)) + `<div style="margin-top:16px">${grid(4, ['title-lavender.jpg', 'title-cherry.jpg', 'title-angelcore.jpg', 'title-princess.jpg']
      .map((f) => framed(titleCrop(f, 226), 'border-width:5px;border-radius:16px')), 12)}</div>` }),

  'card-screens': () => card({
    kicker: 'Inventories & screens', title: 'Every window,<br>pastel',
    points: ['Inventory, crafting, furnaces, chests, anvil and enchanting', 'Brewing, beacon, trading, creative and the recipe book',
      'A little bow and heart tied onto each window', 'Buttons, sliders and lists, in other mods’ screens too', 'Built from your resource pack’s own art'],
    art: pair(region('inventory.jpg', 520, 140, 1260, 760, 715), region('furnace-angelcore.jpg', 520, 120, 1260, 760, 715), 'Strawberry Milk', 'Angelcore', 680) }),

  'card-hud': () => card({
    kicker: 'Heads-up display', title: 'A sweeter<br>HUD',
    points: ['Hearts for every state: hardcore, poison, frozen', 'Six hunger treats, or keep the drumsticks', 'Heart-shield armor and pastel air bubbles',
      'A sparkly gradient XP bar and a bow on the hotbar', 'Heart, sparkle, flower, bow, dot or cross crosshair'],
    art: framed(region('hud.jpg', 560, 860, 800, 220, 966)) + `<div style="margin-top:16px">${grid(6, [
      tile(sprite('strawberry_milk_heart', 7), 'Hearts'), tile(sprite('strawberry_milk_heart_hardcore', 7), 'Hardcore'),
      tile(sprite('strawberry_milk_heart_absorption', 7), 'Absorption'), tile(sprite('strawberry_milk_heart_frozen', 7), 'Frozen'),
      tile(sprite('strawberry_milk_armor', 7), 'Armor'), tile(sprite('strawberry_milk_bubble', 7), 'Air')]
      .map((t) => t.replace('height:110px', 'height:80px')), 10)}</div>
      <div style="margin-top:12px">${grid(2, [
      tile(['strawberry', 'macaron', 'donut', 'dango', 'cupcake', 'bonbon'].map((f) => sprite(`strawberry_milk_treat_${f}`, 6)).join(''), 'Six treats', { art: 70 }),
      tile(['heart', 'sparkle', 'flower', 'bow', 'dot', 'cross'].map((c) => sprite(`strawberry_milk_crosshair_${c}`, 4)).join(''), 'Six crosshairs', { art: 70 })], 10)}</div>` }),

  'card-effects': () => card({
    kicker: 'Cosmetic effects', title: 'Sparkles only<br>you see',
    points: ['Ambient sparkles and blossom petals outdoors', 'Hearts float up when you heal', 'Flowers pop up as you walk on grass and dirt',
      'A sparkle burst on level-up, pink enchanting glyphs', 'Follows your Particles setting, with its own slider'],
    art: framed(region('third-person.jpg', 280, 120, 1360, 860, 966)) + `<div style="margin-top:16px">${grid(6, [
      ['particle/heart.png', 'Hearts'], ['particle/sparkle_0.png', 'Sparkles'], ['particle/petal_0.png', 'Petals'],
      ['particle/flower_0.png', 'Flowers'], ['particle/sparkle_2.png', 'Level-up'], ['particle/heart_small.png', 'Healing']]
      .map(([t, n]) => tile(`<div style="width:96px;height:76px;border-radius:14px;display:flex;align-items:center;justify-content:center;background:linear-gradient(180deg,#EE9DBC,#F9CCDB)">${tex(t, 6)}</div>`, n, { art: 76 })), 10)}</div>` }),

  'card-touches': () => card({
    kicker: 'Gentle touches', title: 'Soft in<br>every corner',
    points: ['A tinted pause menu with a heart button', 'A gentle game-over screen with drifting hearts', 'Menus fade in softly when they open',
      'Pastel tooltips and a pink enchantment glint', 'Sky backgrounds replace dirt outside worlds'],
    art: `<div style="display:grid;grid-template-columns:1fr 1fr;gap:20px">
      <div style="position:relative">${framed(region('pause.jpg', 440, 130, 1000, 750, 466))}${label('Pause', 'left:18px;top:16px')}</div>
      <div style="position:relative">${framed(region('death.jpg', 460, 130, 1000, 750, 466))}${label('Game over', 'left:18px;top:16px')}</div></div>
      <div style="margin-top:20px;position:relative">${framed(region('multiplayer.jpg', 0, 60, 1920, 600, 966))}${label('Sky instead of dirt', 'right:18px;bottom:16px')}</div>` }),

  'card-settings': () => card({
    kicker: 'Pinkified Settings', title: 'Change it<br>live',
    points: ['Five tabs: Theme, Interface, HUD, Effects, Motion & Sound', 'Every change previews straight away', 'Open it from a heart button, Mods or a key',
      'Turn off any part to keep the vanilla look', 'One click resets everything to the defaults'],
    art: pair(region('settings-effects.jpg', 0, 0, 1920, 1080, 715), region('settings-hud.jpg', 0, 0, 1920, 1080, 715), null, null, 640) }),

  'card-config': () => card({
    kicker: 'Make it yours', title: 'Every switch<br>in one file',
    points: ['Saved to pinkified-client.toml', 'Theme and your own two colours', 'Each menu, HUD and effect part on or off', 'Particle amount and animation liveliness',
      'Edit by hand: changes apply while you play'],
    art: `<div style="display:flex;gap:20px">
      ${code('config/pinkified-client.toml', `${k('[theme]')}
  ${k('theme')} = ${v('"STRAWBERRY_MILK"')}
  ${k('customPrimary')} = ${v('"#F48FB1"')}
  ${k('customAccent')} = ${v('"#B39DDB"')}
${k('[interface]')}
  ${k('customMainMenu')} = ${v('true')}
  ${k('themeContainers')} = ${v('true')}
  ${k('pinkGlint')} = ${v('true')}
${k('[hud]')}
  ${k('foodStyle')} = ${v('"AUTO"')}
  ${k('crosshair')} = ${v('"HEART"')}`)}
      ${code('&nbsp;', `${k('[effects]')}
  ${k('particles')} = ${v('true')}
  ${k('cherryBlossoms')} = ${v('true')}
  ${k('flowerSteps')} = ${v('true')}
  ${k('particleDensity')} = ${v('1.0')}
${k('[motion]')}
  ${k('animations')} = ${v('true')}
  ${k('animationIntensity')} = ${v('1.0')}
  ${k('menuMusic')} = ${v('true')}
  ${k('uiSounds')} = ${v('true')}
${cm('# 34 settings in all')}`)}</div>` }),

  'card-clientside': () => card({
    kicker: 'Client-side only', title: 'Join any<br>server',
    points: ['Install it on your own game, servers never need it', 'A server with it installed by mistake still starts cleanly', 'No new blocks, items or registry entries',
      'HUD mods like AppleSkin keep working', 'Prefer the vanilla title screen? It gets pink too'],
    art: framed(region('title-vanilla.jpg', 0, 0, 1920, 1080, 966)) + label('Pinkified main menu off', 'right:24px;top:22px') +
      stats([['Server install', 'None'], ['Registry entries', '0'], ['Gameplay changes', '0'], ['Loaders', '3']]) }),
};

const headers = { 'header-themes': 'The Themes', 'header-menu': 'Main Menu', 'header-screens': 'Inventories & Screens',
  'header-hud': 'The HUD', 'header-effects': 'Cosmetic Effects', 'header-sound': 'Music & Sound', 'header-touches': 'Gentle Touches',
  'header-settings': 'Settings', 'header-config': 'Configuration', 'header-how': 'How It Works',
  'header-compatibility': 'Downloads & Compatibility', 'header-faq': 'Questions', 'header-gallery': 'Gallery' };
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);
assets.divider = () => page(1400, 40, `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:14px">
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
  ${heartSvg('#9F86DB', 22)}${heartSvg('#E2557A', 28)}${heartSvg('#F48FB1', 22)}
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div></div>`, true);

(async () => {
  const wanted = process.argv.slice(2);
  const browser = await chromium.launch(process.env.CHROMIUM
    ? { executablePath: process.env.CHROMIUM, args: ['--no-sandbox', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] }
    : {});
  fs.mkdirSync(out, { recursive: true });
  for (const [name, build] of Object.entries(assets)) {
    if (wanted.length && !wanted.includes(name)) continue;
    const html = build();
    const size = html.match(/body \{ width: (\d+)px; height: (\d+)px; \}/);
    const tab = await browser.newPage({ viewport: { width: +size[1], height: +size[2] }, deviceScaleFactor: 1 });
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    await tab.goto(url(file));
    await tab.evaluate(() => document.fonts.ready);
    await tab.waitForLoadState('networkidle');
    await tab.waitForFunction(() => document.body.dataset.items === 'ready');
    await tab.screenshot({ path: path.join(out, `${name}.png`), omitBackground: name === 'divider' || name.startsWith('header-') });
    fs.unlinkSync(file);
    await tab.close();
    console.log('rendered', name);
  }
  await browser.close();
})().catch((error) => { console.error(error); process.exit(1); });

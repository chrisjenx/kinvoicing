const windowStub = {
  addEventListener: () => {}, removeEventListener: () => {},
  postMessage: () => {},
  setTimeout: (fn, ms) => setTimeout(fn, ms),
  clearTimeout: (id) => clearTimeout(id),
  setInterval: (fn, ms) => setInterval(fn, ms),
  clearInterval: (id) => clearInterval(id),
  requestAnimationFrame: (fn) => setTimeout(() => fn(Date.now()), 16),
  cancelAnimationFrame: (id) => clearTimeout(id),
  location: { href: 'http://localhost/', origin: 'http://localhost' },
};
globalThis.window = windowStub;
globalThis.self = windowStub;
globalThis.document = {
  createElement: () => ({ getContext: () => null, style: {}, addEventListener: () => {} }),
  documentElement: { style: {} },
  body: { appendChild: () => {}, style: {} },
  addEventListener: () => {}, removeEventListener: () => {},
};
if (!globalThis.navigator) {
  globalThis.navigator = { userAgent: 'node', language: 'en-US', languages: ['en-US'] };
}
const skiko = await import('./kotlin/skiko.mjs');
await skiko.awaitSkiko;

const apiOrigin = process.env.RENDER_API_ORIGIN;

if (!apiOrigin) {
  throw new Error("Configura RENDER_API_ORIGIN con la URL HTTPS del backend en Render.");
}

const origin = new URL(apiOrigin);

if (origin.protocol !== "https:") {
  throw new Error("RENDER_API_ORIGIN debe usar HTTPS.");
}

export const config = {
  framework: "vite",
  buildCommand: "npm run build",
  outputDirectory: "dist",
  rewrites: [
    { source: "/api/:path*", destination: `${origin.origin}/api/:path*` },
    { source: "/:path*", destination: "/index.html" },
  ],
};

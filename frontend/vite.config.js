import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": "http://localhost:8099",
      "/images": "http://localhost:8099",
      "/oauth2": "http://localhost:8099",
      "/login/oauth2": "http://localhost:8099",
    },
  },
});

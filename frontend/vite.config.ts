import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";

export default defineConfig({
  appType: "spa", // fallback de SPA ligado
  plugins: [react()],
  server: {
    host: true,   // aceita localhost/127.0.0.1/rede
    port: 5173,
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});
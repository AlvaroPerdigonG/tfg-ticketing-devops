import { fileURLToPath, URL } from "node:url";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      antd: fileURLToPath(new URL("./src/vendor/antd/index.tsx", import.meta.url)),
      "@ant-design/icons": fileURLToPath(
        new URL("./src/vendor/ant-design-icons/index.tsx", import.meta.url),
      ),
    },
  },
  test: {
    environment: "jsdom",
    setupFiles: "./src/tests/setupTests.ts",
    globals: true,
  },
});

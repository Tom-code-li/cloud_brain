import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'node',
    setupFiles: ['./tests/setup/vitest.setup.js']
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/registration-api': {
        target: 'http://127.0.0.1:9200',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/registration-api/, '')
      },
      '/registration-ai': {
        target: 'http://127.0.0.1:9600',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/registration-ai/, '')
      },
      '/medical-exam': {
        target: 'http://127.0.0.1:9400',
        changeOrigin: true
      }
    }
  }
});

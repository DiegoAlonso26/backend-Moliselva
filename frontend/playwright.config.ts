import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  workers: 1,
  fullyParallel: false,
  timeout: 90000,
  expect: { timeout: 12000 },
  reporter: 'list',
  use: { baseURL: 'http://localhost:5173', browserName: 'chromium', viewport: { width: 1440, height: 1000 }, trace: 'off', screenshot: 'off', video: 'off' },
  webServer: { command: 'npm run dev -- --host 127.0.0.1 --port 5173 --strictPort', url: 'http://localhost:5173', reuseExistingServer: true },
})

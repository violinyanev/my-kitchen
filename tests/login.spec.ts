import { test, expect } from '@playwright/test';

test.describe('Login Functionality', () => {
  test.beforeEach(async ({ page }) => {
    // Clear localStorage before each test
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.clear();
    });
    await page.reload();
  });

  test('should show login button when not logged in', async ({ page }) => {
    await page.goto('/');
    
    // Check that login button is visible and logout button is hidden
    await expect(page.locator('#login-btn')).toBeVisible();
    await expect(page.locator('#logout-btn')).not.toBeVisible();
    
    // Check that no login status is shown
    await expect(page.locator('#login-status')).toBeEmpty();
  });

  test('should open login modal when clicking login button', async ({ page }) => {
    await page.goto('/');
    
    // Click login button
    await page.click('#login-btn');
    
    // Check that modal opens
    await expect(page.locator('#login-modal')).toBeVisible();
    await expect(page.locator('#login-modal h3')).toContainText('Login to Backend');
    
    // Check that form fields are present
    await expect(page.locator('#server-input')).toBeVisible();
    await expect(page.locator('#email-input')).toBeVisible();
    await expect(page.locator('#password-input')).toBeVisible();
    
    // Check default server value
    await expect(page.locator('#server-input')).toHaveValue('http://localhost:5000');
  });

  test('should close login modal when clicking close button', async ({ page }) => {
    await page.goto('/');
    
    // Open login modal
    await page.click('#login-btn');
    await expect(page.locator('#login-modal')).toBeVisible();
    
    // Click close button
    await page.click('.close-btn');
    
    // Check that modal closes
    await expect(page.locator('#login-modal')).not.toBeVisible();
  });

  test('should close login modal when clicking cancel', async ({ page }) => {
    await page.goto('/');
    
    // Open login modal
    await page.click('#login-btn');
    await expect(page.locator('#login-modal')).toBeVisible();
    
    // Click cancel button
    await page.click('.form-actions .btn-secondary:has-text("Cancel")');
    
    // Check that modal closes
    await expect(page.locator('#login-modal')).not.toBeVisible();
  });

  test('should validate required fields when logging in', async ({ page }) => {
    await page.goto('/');
    
    // Open login modal
    await page.click('#login-btn');
    
    // Try to submit without filling fields
    await page.click('#login-submit-btn');
    
    // Check that alert appears
    page.on('dialog', dialog => {
      expect(dialog.message()).toContain('Please fill in all fields');
      dialog.accept();
    });
    
    // Modal should still be open
    await expect(page.locator('#login-modal')).toBeVisible();
  });

  test('should show loading state during login', async ({ page }) => {
    await page.goto('/');
    
    // Open login modal
    await page.click('#login-btn');
    
    // Fill in login form
    await page.fill('#server-input', 'http://localhost:5000');
    await page.fill('#email-input', 'test@example.com');
    await page.fill('#password-input', 'password');
    
    // Submit login (this will fail but we can check the loading state)
    await page.click('#login-submit-btn');
    
    // Check that login status shows pending state
    await expect(page.locator('.status-info')).toContainText('Logging in...');
    
    // Login button should be hidden during login
    await expect(page.locator('#login-btn')).not.toBeVisible();
  });

  test('should handle login failure gracefully', async ({ page }) => {
    await page.goto('/');
    
    // Open login modal
    await page.click('#login-btn');
    
    // Fill in login form with invalid credentials
    await page.fill('#server-input', 'http://localhost:5000');
    await page.fill('#email-input', 'invalid@example.com');
    await page.fill('#password-input', 'wrongpassword');
    
    // Submit login
    await page.click('#login-submit-btn');
    
    // Wait for login to fail
    await page.waitForTimeout(2000);
    
    // Check that error status is shown
    await expect(page.locator('.status-error')).toContainText('Login failed');
    
    // Login button should be visible again
    await expect(page.locator('#login-btn')).toBeVisible();
    await expect(page.locator('#logout-btn')).not.toBeVisible();
  });

  test('should handle successful login', async ({ page }) => {
    // This test assumes the backend is running with valid credentials
    // You may need to adjust the credentials based on your test setup
    test.skip(true, 'Requires running backend server with test credentials');
    
    await page.goto('/');
    
    // Open login modal
    await page.click('#login-btn');
    
    // Fill in login form with valid credentials
    await page.fill('#server-input', 'http://localhost:5000');
    await page.fill('#email-input', 'test@example.com');
    await page.fill('#password-input', 'password');
    
    // Submit login
    await page.click('#login-submit-btn');
    
    // Wait for login to succeed
    await page.waitForTimeout(3000);
    
    // Check that success status is shown
    await expect(page.locator('.status-success')).toContainText('Connected to backend');
    
    // Logout button should be visible
    await expect(page.locator('#logout-btn')).toBeVisible();
    await expect(page.locator('#login-btn')).not.toBeVisible();
  });

  test('should logout successfully', async ({ page }) => {
    // First, simulate being logged in by setting localStorage
    await page.evaluate(() => {
      localStorage.setItem('mykitchen_server', 'http://localhost:5000');
      localStorage.setItem('mykitchen_token', 'fake-token');
    });
    
    await page.reload();
    
    // Check that logout button is visible
    await expect(page.locator('#logout-btn')).toBeVisible();
    
    // Click logout
    await page.click('#logout-btn');
    
    // Check that login button is visible again
    await expect(page.locator('#login-btn')).toBeVisible();
    await expect(page.locator('#logout-btn')).not.toBeVisible();
    
    // Check that login status is cleared
    await expect(page.locator('#login-status')).toBeEmpty();
  });

  test('should persist login state across page reloads', async ({ page }) => {
    // Simulate being logged in
    await page.evaluate(() => {
      localStorage.setItem('mykitchen_server', 'http://localhost:5000');
      localStorage.setItem('mykitchen_token', 'fake-token');
    });
    
    await page.reload();
    
    // Check that logout button is visible (indicating logged in state)
    await expect(page.locator('#logout-btn')).toBeVisible();
    await expect(page.locator('#login-btn')).not.toBeVisible();
  });

  test('should clear login state when localStorage is cleared', async ({ page }) => {
    // First, simulate being logged in
    await page.evaluate(() => {
      localStorage.setItem('mykitchen_server', 'http://localhost:5000');
      localStorage.setItem('mykitchen_token', 'fake-token');
    });
    
    await page.reload();
    
    // Verify logged in state
    await expect(page.locator('#logout-btn')).toBeVisible();
    
    // Clear localStorage
    await page.evaluate(() => {
      localStorage.clear();
    });
    
    await page.reload();
    
    // Check that login button is visible again
    await expect(page.locator('#login-btn')).toBeVisible();
    await expect(page.locator('#logout-btn')).not.toBeVisible();
  });
});
import { test, expect } from '@playwright/test';

test.describe('App Navigation and UI', () => {
  test.beforeEach(async ({ page }) => {
    // Clear localStorage before each test
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.clear();
    });
    await page.reload();
  });

  test('should display correct app title and header', async ({ page }) => {
    await page.goto('/');
    
    // Check main title
    await expect(page.locator('h1')).toContainText('My Kitchen');
    
    // Check section title
    await expect(page.locator('h2')).toContainText('Your Recipes');
  });

  test('should be responsive on mobile devices', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');
    
    // Check that header is responsive
    await expect(page.locator('header')).toBeVisible();
    
    // Check that main content is visible
    await expect(page.locator('main')).toBeVisible();
    
    // Check that add recipe button is visible
    await expect(page.locator('#add-recipe-btn')).toBeVisible();
  });

  test('should handle keyboard navigation', async ({ page }) => {
    await page.goto('/');
    
    // Tab through interactive elements
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    
    // Check that focus is on the add recipe button
    await expect(page.locator('#add-recipe-btn')).toBeFocused();
    
    // Press Enter to open modal
    await page.keyboard.press('Enter');
    
    // Check that modal opens
    await expect(page.locator('#recipe-modal')).toBeVisible();
    
    // Tab to close button and press Enter
    await page.keyboard.press('Tab');
    await page.keyboard.press('Enter');
    
    // Check that modal closes
    await expect(page.locator('#recipe-modal')).not.toBeVisible();
  });

  test('should handle escape key to close modals', async ({ page }) => {
    await page.goto('/');
    
    // Open recipe modal
    await page.click('#add-recipe-btn');
    await expect(page.locator('#recipe-modal')).toBeVisible();
    
    // Press Escape
    await page.keyboard.press('Escape');
    
    // Check that modal closes
    await expect(page.locator('#recipe-modal')).not.toBeVisible();
    
    // Open login modal
    await page.click('#login-btn');
    await expect(page.locator('#login-modal')).toBeVisible();
    
    // Press Escape
    await page.keyboard.press('Escape');
    
    // Check that modal closes
    await expect(page.locator('#login-modal')).not.toBeVisible();
  });

  test('should display proper loading state', async ({ page }) => {
    // Navigate to the page
    await page.goto('/');
    
    // The loading state should be replaced by the actual app content
    await expect(page.locator('.loading')).not.toBeVisible();
    await expect(page.locator('h1')).toContainText('My Kitchen');
  });

  test('should handle form validation properly', async ({ page }) => {
    await page.goto('/');
    
    // Test recipe form validation
    await page.click('#add-recipe-btn');
    
    // Try to submit empty form
    await page.click('#recipe-submit-btn');
    
    // Should show validation error
    page.on('dialog', dialog => {
      expect(dialog.message()).toContain('Please fill in all fields');
      dialog.accept();
    });
    
    // Test login form validation
    await page.click('.close-btn'); // Close recipe modal
    await page.click('#login-btn');
    
    // Try to submit empty login form
    await page.click('#login-submit-btn');
    
    // Should show validation error
    page.on('dialog', dialog => {
      expect(dialog.message()).toContain('Please fill in all fields');
      dialog.accept();
    });
  });

  test('should maintain state when switching between modals', async ({ page }) => {
    await page.goto('/');
    
    // Open recipe modal and fill some data
    await page.click('#add-recipe-btn');
    await page.fill('#recipe-title-input', 'Test Recipe');
    await page.fill('#recipe-content-input', 'Test content');
    
    // Close modal
    await page.click('.close-btn');
    
    // Open login modal
    await page.click('#login-btn');
    await page.fill('#email-input', 'test@example.com');
    
    // Close login modal
    await page.click('.close-btn');
    
    // Open recipe modal again
    await page.click('#add-recipe-btn');
    
    // Check that form is empty (state should be reset)
    await expect(page.locator('#recipe-title-input')).toHaveValue('');
    await expect(page.locator('#recipe-content-input')).toHaveValue('');
  });

  test('should handle multiple rapid clicks gracefully', async ({ page }) => {
    await page.goto('/');
    
    // Rapidly click add recipe button multiple times
    await page.click('#add-recipe-btn');
    await page.click('#add-recipe-btn');
    await page.click('#add-recipe-btn');
    
    // Should only have one modal open
    await expect(page.locator('#recipe-modal')).toHaveCount(1);
    await expect(page.locator('#recipe-modal')).toBeVisible();
  });

  test('should display proper button states', async ({ page }) => {
    await page.goto('/');
    
    // Check initial button states
    await expect(page.locator('#login-btn')).toBeVisible();
    await expect(page.locator('#logout-btn')).not.toBeVisible();
    await expect(page.locator('#add-recipe-btn')).toBeVisible();
    
    // Check button hover effects
    await page.hover('#add-recipe-btn');
    // Button should still be visible and functional
    await expect(page.locator('#add-recipe-btn')).toBeVisible();
  });

  test('should handle window resize gracefully', async ({ page }) => {
    await page.goto('/');
    
    // Start with desktop size
    await page.setViewportSize({ width: 1200, height: 800 });
    await expect(page.locator('.app-container')).toBeVisible();
    
    // Resize to mobile
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.locator('.app-container')).toBeVisible();
    
    // Resize back to desktop
    await page.setViewportSize({ width: 1200, height: 800 });
    await expect(page.locator('.app-container')).toBeVisible();
  });

  test('should handle page refresh gracefully', async ({ page }) => {
    await page.goto('/');
    
    // Add a recipe
    await page.click('#add-recipe-btn');
    await page.fill('#recipe-title-input', 'Refresh Test');
    await page.fill('#recipe-content-input', 'This recipe should survive refresh');
    await page.click('#recipe-submit-btn');
    
    // Verify recipe is there
    await expect(page.locator('.recipe-item h3')).toContainText('Refresh Test');
    
    // Refresh the page
    await page.reload();
    
    // Recipe should still be there
    await expect(page.locator('.recipe-item h3')).toContainText('Refresh Test');
  });
});
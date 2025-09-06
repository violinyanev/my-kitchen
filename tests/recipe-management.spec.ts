import { test, expect } from '@playwright/test';

test.describe('Recipe Management', () => {
  test.beforeEach(async ({ page }) => {
    // Clear localStorage before each test
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.clear();
    });
    await page.reload();
  });

  test('should display empty state when no recipes exist', async ({ page }) => {
    await page.goto('/');
    
    // Wait for the app to load
    await expect(page.locator('h1')).toContainText('My Kitchen');
    
    // Check empty state
    await expect(page.locator('.empty-state')).toBeVisible();
    await expect(page.locator('.empty-state p')).toContainText('No recipes yet. Add your first recipe!');
  });

  test('should add a new recipe', async ({ page }) => {
    await page.goto('/');
    
    // Click add recipe button
    await page.click('#add-recipe-btn');
    
    // Fill in recipe form
    await page.fill('#recipe-title-input', 'Test Recipe');
    await page.fill('#recipe-content-input', 'This is a test recipe content with multiple lines.\nIt should work properly.');
    
    // Submit the form
    await page.click('#recipe-submit-btn');
    
    // Wait for modal to close
    await expect(page.locator('#recipe-modal')).not.toBeVisible();
    
    // Check that recipe appears in the list
    await expect(page.locator('.recipe-item')).toHaveCount(1);
    await expect(page.locator('.recipe-item h3')).toContainText('Test Recipe');
    await expect(page.locator('.recipe-preview')).toContainText('This is a test recipe content with multiple lines.');
  });

  test('should edit an existing recipe', async ({ page }) => {
    await page.goto('/');
    
    // First, add a recipe
    await page.click('#add-recipe-btn');
    await page.fill('#recipe-title-input', 'Original Recipe');
    await page.fill('#recipe-content-input', 'Original content');
    await page.click('#recipe-submit-btn');
    
    // Wait for the recipe to appear
    await expect(page.locator('.recipe-item')).toHaveCount(1);
    
    // Click edit button
    await page.click('.recipe-actions .btn-secondary:has-text("Edit")');
    
    // Check that modal opens with existing data
    await expect(page.locator('#recipe-modal')).toBeVisible();
    await expect(page.locator('#recipe-modal-title')).toContainText('Edit Recipe');
    await expect(page.locator('#recipe-title-input')).toHaveValue('Original Recipe');
    await expect(page.locator('#recipe-content-input')).toHaveValue('Original content');
    
    // Modify the recipe
    await page.fill('#recipe-title-input', 'Updated Recipe');
    await page.fill('#recipe-content-input', 'Updated content');
    await page.click('#recipe-submit-btn');
    
    // Check that recipe is updated
    await expect(page.locator('.recipe-item h3')).toContainText('Updated Recipe');
    await expect(page.locator('.recipe-preview')).toContainText('Updated content');
  });

  test('should delete a recipe', async ({ page }) => {
    await page.goto('/');
    
    // First, add a recipe
    await page.click('#add-recipe-btn');
    await page.fill('#recipe-title-input', 'Recipe to Delete');
    await page.fill('#recipe-content-input', 'This recipe will be deleted');
    await page.click('#recipe-submit-btn');
    
    // Wait for the recipe to appear
    await expect(page.locator('.recipe-item')).toHaveCount(1);
    
    // Click delete button and confirm
    page.on('dialog', dialog => dialog.accept());
    await page.click('.recipe-actions .btn-danger:has-text("Delete")');
    
    // Check that recipe is removed
    await expect(page.locator('.recipe-item')).toHaveCount(0);
    await expect(page.locator('.empty-state')).toBeVisible();
  });

  test('should cancel delete when user declines', async ({ page }) => {
    await page.goto('/');
    
    // First, add a recipe
    await page.click('#add-recipe-btn');
    await page.fill('#recipe-title-input', 'Recipe to Keep');
    await page.fill('#recipe-content-input', 'This recipe should remain');
    await page.click('#recipe-submit-btn');
    
    // Wait for the recipe to appear
    await expect(page.locator('.recipe-item')).toHaveCount(1);
    
    // Click delete button but cancel
    page.on('dialog', dialog => dialog.dismiss());
    await page.click('.recipe-actions .btn-danger:has-text("Delete")');
    
    // Check that recipe is still there
    await expect(page.locator('.recipe-item')).toHaveCount(1);
    await expect(page.locator('.recipe-item h3')).toContainText('Recipe to Keep');
  });

  test('should validate required fields when adding recipe', async ({ page }) => {
    await page.goto('/');
    
    // Click add recipe button
    await page.click('#add-recipe-btn');
    
    // Try to submit without filling fields
    await page.click('#recipe-submit-btn');
    
    // Check that alert appears
    page.on('dialog', dialog => {
      expect(dialog.message()).toContain('Please fill in all fields');
      dialog.accept();
    });
    
    // Modal should still be open
    await expect(page.locator('#recipe-modal')).toBeVisible();
  });

  test('should cancel adding recipe', async ({ page }) => {
    await page.goto('/');
    
    // Click add recipe button
    await page.click('#add-recipe-btn');
    
    // Fill in some data
    await page.fill('#recipe-title-input', 'Test Recipe');
    await page.fill('#recipe-content-input', 'Test content');
    
    // Click cancel
    await page.click('.form-actions .btn-secondary:has-text("Cancel")');
    
    // Check that modal closes and no recipe is added
    await expect(page.locator('#recipe-modal')).not.toBeVisible();
    await expect(page.locator('.empty-state')).toBeVisible();
  });

  test('should close modal when clicking close button', async ({ page }) => {
    await page.goto('/');
    
    // Click add recipe button
    await page.click('#add-recipe-btn');
    
    // Click close button
    await page.click('.close-btn');
    
    // Check that modal closes
    await expect(page.locator('#recipe-modal')).not.toBeVisible();
  });

  test('should persist recipes in localStorage', async ({ page }) => {
    await page.goto('/');
    
    // Add a recipe
    await page.click('#add-recipe-btn');
    await page.fill('#recipe-title-input', 'Persistent Recipe');
    await page.fill('#recipe-content-input', 'This recipe should persist');
    await page.click('#recipe-submit-btn');
    
    // Verify recipe is there
    await expect(page.locator('.recipe-item h3')).toContainText('Persistent Recipe');
    
    // Reload the page
    await page.reload();
    
    // Check that recipe is still there
    await expect(page.locator('.recipe-item h3')).toContainText('Persistent Recipe');
  });

  test('should display multiple recipes', async ({ page }) => {
    await page.goto('/');
    
    // Add first recipe
    await page.click('#add-recipe-btn');
    await page.fill('#recipe-title-input', 'First Recipe');
    await page.fill('#recipe-content-input', 'First recipe content');
    await page.click('#recipe-submit-btn');
    
    // Add second recipe
    await page.click('#add-recipe-btn');
    await page.fill('#recipe-title-input', 'Second Recipe');
    await page.fill('#recipe-content-input', 'Second recipe content');
    await page.click('#recipe-submit-btn');
    
    // Check that both recipes are displayed
    await expect(page.locator('.recipe-item')).toHaveCount(2);
    await expect(page.locator('.recipe-item h3').first()).toContainText('First Recipe');
    await expect(page.locator('.recipe-item h3').last()).toContainText('Second Recipe');
  });
});
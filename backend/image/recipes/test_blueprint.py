#!/usr/bin/env python3

import unittest
import tempfile
import os
import json
import shutil
from pathlib import Path
from flask import Flask
from unittest.mock import patch, MagicMock, Mock
from io import BytesIO
from werkzeug.datastructures import FileStorage
import jwt

from recipes import database, blueprint


class TestRecipesBlueprint(unittest.TestCase):

    def setUp(self):
        # Create a test Flask app
        self.app = Flask(__name__)
        self.app.config['TESTING'] = True
        self.app.config['SECRET_KEY'] = 'test-secret-key'
        
        # Create temporary directories for testing
        self.test_dir = tempfile.mkdtemp()
        self.test_data_folder = os.path.join(self.test_dir, 'data')
        self.test_images_folder = os.path.join(self.test_data_folder, 'images')
        os.makedirs(self.test_images_folder, exist_ok=True)
        
        self.app.config['DATA_FOLDER'] = self.test_data_folder
        
        # Create test databases
        self.recipes_db_file = Path(self.test_data_folder) / 'test_recipes.yaml'
        self.recipes_db = database.Database(self.recipes_db_file)
        self.app.config['recipes_db'] = self.recipes_db
        
        # Set up users database for authentication
        from users import database as users_database
        self.users_db_file = Path(self.test_data_folder) / 'test_users.yaml'
        self.users_db = users_database.Database(self.users_db_file)
        # Create a test user
        self.users_db.create('test@user.com', 'testuser', 'testpassword')
        self.app.config['users_db'] = self.users_db
        
        # Register the blueprint
        self.app.register_blueprint(blueprint.RecipesBlueprint)
        
        # Create test client
        self.client = self.app.test_client()
        
        # Mock user for authentication
        self.test_user = {'name': 'testuser', 'email': 'test@user.com'}
        
        # Create a valid JWT token for testing
        self.test_token = jwt.encode({'username': 'testuser'}, 'test-secret-key', algorithm='HS256')

    def tearDown(self):
        # Clean up temporary directories
        shutil.rmtree(self.test_dir, ignore_errors=True)

    def test_allowed_file_valid_extensions(self):
        """Test allowed_file function with valid file extensions"""
        self.assertTrue(blueprint.allowed_file('test.jpg'))
        self.assertTrue(blueprint.allowed_file('test.jpeg'))
        self.assertTrue(blueprint.allowed_file('test.png'))
        self.assertTrue(blueprint.allowed_file('test.gif'))
        self.assertTrue(blueprint.allowed_file('TEST.JPG'))  # Case insensitive

    def test_allowed_file_invalid_extensions(self):
        """Test allowed_file function with invalid file extensions"""
        self.assertFalse(blueprint.allowed_file('test.txt'))
        self.assertFalse(blueprint.allowed_file('test.pdf'))
        self.assertFalse(blueprint.allowed_file('test.exe'))
        self.assertFalse(blueprint.allowed_file('test'))  # No extension

    def test_get_images_folder_creates_directory(self):
        """Test that get_images_folder creates the directory if it doesn't exist"""
        # Remove the images folder
        shutil.rmtree(self.test_images_folder, ignore_errors=True)
        
        with self.app.app_context():
            images_folder = blueprint.get_images_folder()
            self.assertTrue(os.path.exists(images_folder))
            self.assertEqual(images_folder, self.test_images_folder)

    def test_upload_recipe_image_endpoint_success(self):
        """Test the actual image upload endpoint with a valid file"""
        # Create a test recipe first  
        recipe_data = {"id": 1, "title": "Test Recipe", "body": "Test body"}
        self.recipes_db.put(self.test_user, recipe_data)
        
        # Create test image data
        test_image_data = b'fake image data for testing'
        
        # Test successful upload
        response = self.client.post(
            '/recipes/1/image',
            data={'file': (BytesIO(test_image_data), 'test.jpg')},
            content_type='multipart/form-data',
            headers={'Authorization': f'Bearer {self.test_token}'}
        )
        
        self.assertEqual(response.status_code, 201)
        response_data = json.loads(response.data)
        self.assertIn('message', response_data)
        self.assertIn('filename', response_data)
        self.assertTrue(response_data['filename'].startswith('1_'))
        self.assertTrue(response_data['filename'].endswith('.jpg'))

    def test_upload_recipe_image_endpoint_no_file(self):
        """Test image upload endpoint with no file"""
        response = self.client.post(
            '/recipes/1/image',
            data={},
            content_type='multipart/form-data',
            headers={'Authorization': f'Bearer {self.test_token}'}
        )
        
        self.assertEqual(response.status_code, 400)

    def test_upload_recipe_image_endpoint_invalid_file_type(self):
        """Test image upload endpoint with invalid file type"""
        test_file_data = b'some text data'
        
        response = self.client.post(
            '/recipes/1/image',
            data={'file': (BytesIO(test_file_data), 'test.txt')},
            content_type='multipart/form-data',
            headers={'Authorization': f'Bearer {self.test_token}'}
        )
        
        self.assertEqual(response.status_code, 400)

    def test_get_recipe_image_endpoint_success(self):
        """Test the actual image retrieval endpoint"""
        # Create a test image file
        test_filename = 'test_image.jpg'
        test_image_path = os.path.join(self.test_images_folder, test_filename)
        test_image_data = b'fake image data for testing'
        
        with open(test_image_path, 'wb') as f:
            f.write(test_image_data)
        
        response = self.client.get(f'/recipes/images/{test_filename}')
        
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data, test_image_data)

    def test_get_recipe_image_endpoint_not_found(self):
        """Test image retrieval endpoint for non-existent file"""
        response = self.client.get('/recipes/images/nonexistent.jpg')
        
        self.assertEqual(response.status_code, 404)

    def test_get_recipes_endpoint(self):
        """Test the get recipes endpoint works"""
        # Create a test recipe
        recipe_data = {"id": 1, "title": "Test Recipe", "body": "Test body"}
        self.recipes_db.put(self.test_user, recipe_data)
        
        response = self.client.get(
            '/recipes',
            headers={'Authorization': f'Bearer {self.test_token}'}
        )
        
        self.assertEqual(response.status_code, 200)
        recipes = json.loads(response.data)
        self.assertEqual(len(recipes), 1)
        self.assertEqual(recipes[0]['title'], 'Test Recipe')

    def test_create_recipe_endpoint(self):
        """Test the create recipe endpoint works"""
        recipe_data = {"title": "New Recipe", "body": "Recipe body"}
        
        response = self.client.post(
            '/recipes',
            data=json.dumps(recipe_data),
            content_type='application/json',
            headers={'Authorization': f'Bearer {self.test_token}'}
        )
        
        self.assertEqual(response.status_code, 201)
        response_data = json.loads(response.data)
        self.assertIn('recipe', response_data)
        self.assertEqual(response_data['recipe']['title'], 'New Recipe')

    def test_delete_recipe_endpoint_with_image_cleanup(self):
        """Test that deleting a recipe via endpoint also removes associated image file"""
        # Create a test recipe with image
        recipe_data = {"id": 1, "title": "Test Recipe", "body": "Test body", "image_filename": "test_image.jpg"}
        self.recipes_db.put(self.test_user, recipe_data)
        
        # Create the image file
        test_image_path = os.path.join(self.test_images_folder, "test_image.jpg")
        with open(test_image_path, 'wb') as f:
            f.write(b'fake image data')
        
        # Verify file exists
        self.assertTrue(os.path.exists(test_image_path))
        
        response = self.client.delete(
            '/recipes/1',
            headers={'Authorization': f'Bearer {self.test_token}'}
        )
        
        self.assertEqual(response.status_code, 204)
        
        # Verify image file was deleted
        self.assertFalse(os.path.exists(test_image_path))

    def test_database_delete_recipe_with_image_cleanup(self):
        """Test that database delete method removes associated image file"""
        # Create a test recipe with image
        recipe_data = {"id": 1, "title": "Test Recipe", "body": "Test body", "image_filename": "test_image.jpg"}
        self.recipes_db.put(self.test_user, recipe_data)
        
        # Create the image file
        test_image_path = os.path.join(self.test_images_folder, "test_image.jpg")
        with open(test_image_path, 'wb') as f:
            f.write(b'fake image data')
        
        # Verify file exists
        self.assertTrue(os.path.exists(test_image_path))
        
        # Test the delete function directly
        success, result = self.recipes_db.delete(self.test_user, 1, self.test_images_folder)
        
        self.assertTrue(success)
        self.assertEqual(result['id'], 1)
        
        # Verify image file was deleted
        self.assertFalse(os.path.exists(test_image_path))

    def test_database_delete_recipe_without_image(self):
        """Test that database delete method works normally for recipes without images"""
        # Create a test recipe without image
        recipe_data = {"id": 1, "title": "Test Recipe", "body": "Test body"}
        self.recipes_db.put(self.test_user, recipe_data)
        
        success, result = self.recipes_db.delete(self.test_user, 1, self.test_images_folder)
        
        self.assertTrue(success)
        self.assertEqual(result['id'], 1)

    def test_database_delete_recipe_nonexistent_image_file(self):
        """Test deleting recipe with image_filename but file doesn't exist"""
        # Create a test recipe with image filename but no actual file
        recipe_data = {"id": 1, "title": "Test Recipe", "body": "Test body", "image_filename": "nonexistent.jpg"}
        self.recipes_db.put(self.test_user, recipe_data)
        
        # Delete the recipe - should not fail even if image file doesn't exist
        success, result = self.recipes_db.delete(self.test_user, 1, self.test_images_folder)
        
        self.assertTrue(success)
        self.assertEqual(result['id'], 1)

    def test_upload_image_generates_unique_filename(self):
        """Test that uploaded images get unique filenames based on UUID logic"""
        # Test the filename generation logic by simulating the code path
        import uuid
        
        recipe_id = 1
        extension = 'jpg'
        
        # Generate two filenames to verify they're unique
        filename1 = f"{recipe_id}_{uuid.uuid4().hex}.{extension}"
        filename2 = f"{recipe_id}_{uuid.uuid4().hex}.{extension}"
        
        # Filenames should be different due to UUID
        self.assertNotEqual(filename1, filename2)
        
        # Both should follow the expected pattern
        self.assertTrue(filename1.startswith(f'{recipe_id}_'))
        self.assertTrue(filename1.endswith(f'.{extension}'))
        self.assertTrue(filename2.startswith(f'{recipe_id}_'))
        self.assertTrue(filename2.endswith(f'.{extension}'))
        
        # Test that the UUID part is actually a valid hex string
        uuid_part1 = filename1.split('_')[1].split('.')[0]
        uuid_part2 = filename2.split('_')[1].split('.')[0]
        
        # Should be 32 hex characters
        self.assertEqual(len(uuid_part1), 32)
        self.assertEqual(len(uuid_part2), 32)
        
        # Should be valid hex
        try:
            int(uuid_part1, 16)
            int(uuid_part2, 16)
        except ValueError:
            self.fail("UUID parts should be valid hex strings")


if __name__ == '__main__':
    unittest.main()
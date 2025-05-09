#!/usr/bin/env python3

import unittest
from pathlib import Path
import yaml
from recipes import database

class TestDatabase(unittest.TestCase):

    def setUp(self):
        self.test_file = Path("test_database.yaml")
        self.test_user = {'name': 'Joe'}

    def tearDown(self):
        if self.test_file.exists():
            self.test_file.unlink()

    def test_init(self):
        db = database.Database(self.test_file)
        self.assertTrue(self.test_file.exists())
        self.assertEqual(db.data, {'recipes': []})
        self.assertEqual(db.next_id, 1)
        self.assertEqual(db.get(self.test_user, all=False), [])

    def test_put_valid_recipe(self):
        db = database.Database(self.test_file)
        recipe = {"id": 1, "title": "Test Recipe", "body": "Recipe body", "timestamp": 12}
        new_recipe, error = db.put(self.test_user, recipe)
        self.assertIsNone(error)
        self.assertIn(new_recipe, db.data['recipes'])
        self.assertEqual(new_recipe['timestamp'], 12)
        self.assertEqual(new_recipe['user'], self.test_user['name'])

    def test_get_only_my_recipes(self):
        db = database.Database(self.test_file)
        user2 = {'name': 'Mickey'}
        r1, _ = db.put(self.test_user, {"id": 1, "title": "Test Recipe1", "body": "Recipe body", "timestamp": 12})
        r2, _ = db.put(user2    , {"id": 2, "title": "Test Recipe2", "body": "Recipe body", "timestamp": 12})

        mine = db.get(self.test_user, all=False)
        self.assertEqual(mine, [r1])
        all = db.get(self.test_user, all=True)
        self.assertEqual(all, [r1, r2])
        theirs = db.get(user2, all=False)
        self.assertEqual(theirs, [r2])

    def test_put_id_not_int(self):
        db = database.Database(self.test_file)
        recipe = {"id": "not an integer", "title": "Test Recipe", "body": "Recipe body"}
        new_recipe, error = db.put(self.test_user, recipe)
        self.assertEqual(error, "id must be of type integer, found 'not an integer' instead!")
        self.assertNotIn(new_recipe, db.data['recipes'])

    def test_put_id_null(self):
        db = database.Database(self.test_file)
        recipe = {"id": None, "title": "Test Recipe", "body": "Recipe body"}
        new_recipe, error = db.put(self.test_user, recipe)
        self.assertEqual(error, "id must be of type integer, found 'None' instead!")
        self.assertNotIn(new_recipe, db.data['recipes'])

    def test_put_existing_id(self):
        db = database.Database(self.test_file)
        recipe = {"id": 1, "title": "Test Recipe", "body": "Recipe body"}
        db.put(self.test_user, recipe)
        result, error = db.put(self.test_user, recipe)
        self.assertEqual(error, "Recipe with id 1 exists!")
        self.assertIsNone(result)

    def test_put_timestamp_not_int(self):
        db = database.Database(self.test_file)
        recipe = {"id": 1, "title": "Test Recipe", "body": "Recipe body", "timestamp": "not int"}
        db.put(self.test_user, recipe)
        result, error = db.put(self.test_user, recipe)
        self.assertEqual(error, "timestamp must be of type integer, found 'not int' instead!")
        self.assertIsNone(result)

    def test_put_empty_title(self):
        db = database.Database(self.test_file)
        recipe = {"id": 1, "title": "", "body": "Recipe body"}
        result, error = db.put(self.test_user, recipe)
        self.assertEqual(error, "Recipe title can't be empty")
        self.assertIsNone(result)

    def test_delete_existing_recipe(self):
        db = database.Database(self.test_file)
        recipe = {"id": 1, "title": "Test Recipe", "body": "Recipe body"}
        recipe,_ = db.put(self.test_user, recipe)
        success, deleted_recipe = db.delete(self.test_user, 1)
        self.assertTrue(success)
        self.assertEqual(deleted_recipe, recipe)
        self.assertNotIn(recipe, db.data['recipes'])

    def test_delete_nonexistent_recipe(self):
        db = database.Database(self.test_file)
        success, error = db.delete(self.test_user, 1)
        self.assertFalse(success)
        self.assertEqual(error, "There is no recipe with id 1")

    def test_save(self):
        db = database.Database(self.test_file)
        recipe = {"id": 1, "title": "Test Recipe", "body": "Recipe body"}
        db.put(self.test_user, recipe)
        db.save()
        with self.test_file.open('r') as f:
            saved_data = yaml.safe_load(f)
        self.assertEqual(saved_data, db.data)


if __name__ == '__main__':
    unittest.main()

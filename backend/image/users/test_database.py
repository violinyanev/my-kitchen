#!/usr/bin/env python3

import unittest
from pathlib import Path
import jsonschema
import yaml
from users import database

class TestDatabase(unittest.TestCase):

    def setUp(self):
        self.test_file = Path("test_database.yaml")

    def tearDown(self):
        if self.test_file.exists():
            self.test_file.unlink()

    def test_init_empty(self):
        db = database.Database(self.test_file)
        self.assertTrue(self.test_file.exists())
        self.assertEqual(db.data, {'users': []})
        self.assertEqual(db.get(), [])

    def test_init_existing_users(self):
        test_file = Path("some_users.yaml")

        fake_user = {
            'name': "Max",
            'password': "123456",
            'email': "max@theguy.com",
        }

        with open(test_file, 'w') as f:
            f.write(yaml.safe_dump({'users': [fake_user]}))
        db = database.Database(test_file)
        self.assertEqual(db.data, {'users': [fake_user]})
        self.assertEqual(db.get(), [fake_user])
        test_file.unlink()



if __name__ == '__main__':
    unittest.main()

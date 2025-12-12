-- Seed roles
INSERT IGNORE INTO role(name) VALUES ('ROLE_ADMIN');
INSERT IGNORE INTO role(name) VALUES ('ROLE_WAITER');
INSERT IGNORE INTO role(name) VALUES ('ROLE_CHEF');

-- Sample tables
INSERT IGNORE INTO dining_table(name, capacity, status) VALUES ('Bàn 1', 4, 'AVAILABLE');
INSERT IGNORE INTO dining_table(name, capacity, status) VALUES ('Bàn 2', 2, 'AVAILABLE');

-- Sample menu items
INSERT IGNORE INTO menu_item(name, description, price, available, category) VALUES ('Phở bò', 'Phở bò tái', 45000.00, true, 'Món chính');
INSERT IGNORE INTO menu_item(name, description, price, available, category) VALUES ('Gỏi cuốn', 'Gỏi cuốn tươi', 30000.00, true, 'Khai vị');

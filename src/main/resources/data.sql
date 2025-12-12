-- Seed roles
INSERT INTO role(name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO role(name) VALUES ('ROLE_WAITER') ON CONFLICT (name) DO NOTHING;
INSERT INTO role(name) VALUES ('ROLE_CHEF') ON CONFLICT (name) DO NOTHING;

-- Sample tables
INSERT INTO dining_table(name, capacity, status) VALUES ('Bàn 1', 4, 'AVAILABLE') ON CONFLICT (name) DO NOTHING;
INSERT INTO dining_table(name, capacity, status) VALUES ('Bàn 2', 2, 'AVAILABLE') ON CONFLICT (name) DO NOTHING;

-- Sample menu items
INSERT INTO menu_item(name, description, price, available, category) VALUES ('Phở bò', 'Phở bò tái', 45000.00, true, 'Món chính') ON CONFLICT (name) DO NOTHING;
INSERT INTO menu_item(name, description, price, available, category) VALUES ('Gỏi cuốn', 'Gỏi cuốn tươi', 30000.00, true, 'Khai vị') ON CONFLICT (name) DO NOTHING;

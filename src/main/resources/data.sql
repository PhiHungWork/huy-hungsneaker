-- Admin User (Password: '123456')
INSERT INTO users (username, password, full_name, role, enabled) 
VALUES ('admin', '$2a$10$7Q.y5aH1OQ8C7m6d9E0n9u5K9i8H4L/1tO7vJ4N2G1xR2.yM7N4zG', 'Quản Trị Viên', 'ROLE_ADMIN', true)
ON CONFLICT (username) DO NOTHING;

-- Categories
INSERT INTO categories (name, slug, description, status) VALUES ('Nike Basketball', 'nike', 'Giày Nike Basketball', 'ACTIVE') ON CONFLICT (slug) DO NOTHING;
INSERT INTO categories (name, slug, description, status) VALUES ('Adidas Originals', 'adidas', 'Giày Adidas Originals', 'ACTIVE') ON CONFLICT (slug) DO NOTHING;
INSERT INTO categories (name, slug, description, status) VALUES ('Air Jordan', 'jordan', 'Giày Air Jordan Hot', 'ACTIVE') ON CONFLICT (slug) DO NOTHING;

-- Products
INSERT INTO products (code, name, category_id, brand, price, image_url, status) 
VALUES ('NK-AF1', 'Nike Air Force 1 Low', 1, 'Nike', 2500000, 'https://res.cloudinary.com/dxjwa3jdl/image/upload/v1780427736/HuyAndHungStore/sneaker_12a961cf-8966-491d-991f-8712865213a6.png', 'ACTIVE') ON CONFLICT (code) DO NOTHING;

INSERT INTO products (code, name, category_id, brand, price, image_url, status) 
VALUES ('NK-DUNK', 'Nike Dunk Low Panda', 1, 'Nike', 3200000, 'https://res.cloudinary.com/dxjwa3jdl/image/upload/v1780427884/HuyAndHungStore/sneaker_a7f8372e-e406-4003-a88a-0fe5792990ca.png', 'ACTIVE') ON CONFLICT (code) DO NOTHING;

INSERT INTO products (code, name, category_id, brand, price, image_url, status) 
VALUES ('AD-SAMBA', 'Adidas Samba OG Black', 2, 'Adidas', 2800000, 'https://res.cloudinary.com/dxjwa3jdl/image/upload/v1780428048/HuyAndHungStore/sneaker_7f8f726b-3b05-4339-9120-b87192a1be18.png', 'ACTIVE') ON CONFLICT (code) DO NOTHING;

-- Product Variants
INSERT INTO product_variants (product_id, size, stock_quantity) VALUES (1, '40', 10) ON CONFLICT (product_id, size) DO NOTHING;
INSERT INTO product_variants (product_id, size, stock_quantity) VALUES (1, '41', 15) ON CONFLICT (product_id, size) DO NOTHING;
INSERT INTO product_variants (product_id, size, stock_quantity) VALUES (1, '42', 20) ON CONFLICT (product_id, size) DO NOTHING;

INSERT INTO product_variants (product_id, size, stock_quantity) VALUES (2, '39', 5) ON CONFLICT (product_id, size) DO NOTHING;
INSERT INTO product_variants (product_id, size, stock_quantity) VALUES (2, '40', 8) ON CONFLICT (product_id, size) DO NOTHING;

INSERT INTO product_variants (product_id, size, stock_quantity) VALUES (3, '41', 12) ON CONFLICT (product_id, size) DO NOTHING;
INSERT INTO product_variants (product_id, size, stock_quantity) VALUES (3, '42', 15) ON CONFLICT (product_id, size) DO NOTHING;

-- Spoonful sample data. Load schema-demo.sql first, then this file:
--   mysql -u <your_user> -p < src/main/java/com/CS157A/FinalProject/database/demo/sample-data-demo.sql
--
-- Every demo account uses the password: password123
--
--   Email                    Type      Notes
--   demo@example.com         CUSTOMER  has a special request
--   maya.patel@example.com   CUSTOMER  no special request
--   staff@example.com        STAFF     manages Harbor & Vine
--   koji.staff@example.com   STAFF     manages Koji Ramen Bar, no phone

USE SpoonfulDB;

SET @pw = '$2a$10$rNx0/MQMcrHV5UFfbruWn.gQ2V8AHdABfx5cCo2W9KxnpkvfSlkA.';  -- hashed global password for demo purposes. non hashed password is: "password123", use it to login

INSERT INTO users (user_id, user_type, name, email, password_hash, phone_number) VALUES
    (1, 'CUSTOMER', 'Demo Customer', 'demo@example.com',       @pw, '408-555-0101'),
    (2, 'CUSTOMER', 'Maya Patel',    'maya.patel@example.com', @pw, '408-555-0102'),
    (3, 'STAFF',    'Sam Rivera',    'staff@example.com',      @pw, '408-555-0103'),
    (4, 'STAFF',    'Kenji Mori',    'koji.staff@example.com', @pw, NULL);

INSERT INTO customer (user_id, profile_special_request) VALUES
    (1, 'Window seat if possible'),
    (2, NULL);

INSERT INTO restaurant_staff (user_id) VALUES
    (3),
    (4);

INSERT INTO zip_code (zip_code, city, state) VALUES
    ('95112', 'San Jose', 'CA'),
    ('95113', 'San Jose', 'CA');

INSERT INTO restaurant (restaurant_id, name, description, dress_code, dining_style, street, zip_code, manager_staff_id) VALUES
    (1, 'Harbor & Vine',  'Seafood and California wines in a relaxed dining room.', 'Smart casual', 'Casual elegant', '120 S First St',   '95113', 3),
    (2, 'Koji Ramen Bar', 'Rich tonkotsu and miso ramen made fresh daily.',        'Casual',       'Casual dining',  '45 E Santa Clara St', '95112', 4);

INSERT INTO restaurant_phone (restaurant_id, phone_number) VALUES
    (1, '408-555-0200'),
    (1, '408-555-0201'),
    (2, '408-555-0300');

-- New sign-ups through the app get ids after these.
ALTER TABLE users AUTO_INCREMENT = 100;
ALTER TABLE restaurant AUTO_INCREMENT = 100;

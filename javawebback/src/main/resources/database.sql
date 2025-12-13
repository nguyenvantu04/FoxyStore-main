CREATE DATABASE IF NOT EXISTS foxystore;
USE foxystore;

CREATE TABLE role (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    email VARCHAR(255),
    auth_provider VARCHAR(50),
    email_verified BOOLEAN DEFAULT FALSE,
    provider_id VARCHAR(255),
    status VARCHAR(50),
    gender VARCHAR(10),
    dob DATE
);

CREATE TABLE user_role (
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(role_id)
);

CREATE TABLE address (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(255),
    street VARCHAR(255),
    detailed_address VARCHAR(255),
    name VARCHAR(255),
    phone_number INT,
    is_default BOOLEAN,
    user_id INT,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE catalog (
    catalog_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    catalog_id INT,
    CONSTRAINT fk_category_catalog FOREIGN KEY (catalog_id) REFERENCES catalog(catalog_id)
);

CREATE TABLE sale (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_name VARCHAR(255),
    start_date DATETIME,
    end_date DATETIME,
    discount_percent INT,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE product (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    price DECIMAL(19, 2),
    quantity INT,
    description TEXT,
    created_date DATETIME,
    sold_count INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    category_id INT,
    sale_id INT,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(category_id),
    CONSTRAINT fk_product_sale FOREIGN KEY (sale_id) REFERENCES sale(sale_id)
);

CREATE TABLE image (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    image VARCHAR(255),
    description VARCHAR(255),
    product_id INT,
    CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE size (
    size_id INT AUTO_INCREMENT PRIMARY KEY,
    size_name VARCHAR(255)
);

CREATE TABLE product_size (
    product_size_id INT AUTO_INCREMENT PRIMARY KEY,
    quantity INT,
    size_id INT,
    product_id INT,
    CONSTRAINT fk_product_size_size FOREIGN KEY (size_id) REFERENCES size(size_id),
    CONSTRAINT fk_product_size_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE shopping_cart (
    shopping_cart_id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255),
    user_id INT,
    CONSTRAINT fk_shopping_cart_user FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE shopping_cart_detail (
    shopping_cart_id INT,
    product_size_id INT,
    quantity INT,
    total DECIMAL(19, 2),
    PRIMARY KEY (shopping_cart_id, product_size_id),
    CONSTRAINT fk_scd_shopping_cart FOREIGN KEY (shopping_cart_id) REFERENCES shopping_cart(shopping_cart_id),
    CONSTRAINT fk_scd_product_size FOREIGN KEY (product_size_id) REFERENCES product_size(product_size_id)
);

CREATE TABLE review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    comment TEXT,
    rating INT,
    time DATETIME,
    reply TEXT,
    product_id INT,
    user_id INT,
    CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(product_id),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE favorite_product (
    user_id INT,
    product_id INT,
    created_date DATETIME,
    PRIMARY KEY (user_id, product_id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_fav_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE bill (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    time DATETIME,
    status VARCHAR(50),
    payment_method VARCHAR(50),
    total DECIMAL(19, 2),
    user_id INT,
    address_id INT,
    CONSTRAINT fk_bill_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_bill_address FOREIGN KEY (address_id) REFERENCES address(address_id)
);

CREATE TABLE bill_detail (
    bill_id INT,
    product_size_id INT,
    quantity INT,
    PRIMARY KEY (bill_id, product_size_id),
    CONSTRAINT fk_bd_bill FOREIGN KEY (bill_id) REFERENCES bill(bill_id),
    CONSTRAINT fk_bd_product_size FOREIGN KEY (product_size_id) REFERENCES product_size(product_size_id)
);

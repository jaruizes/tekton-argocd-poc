DROP TABLE products IF EXISTS;
DROP TABLE labels IF EXISTS;
DROP TABLE labels_products IF EXISTS;

CREATE TABLE  products (
    id   INTEGER      NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    price DOUBLE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE labels (
    id   INTEGER NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE labels_products (
    product_id INTEGER NOT NULL,
    label_id INTEGER NOT NULL,
    PRIMARY KEY (product_id, label_id)
);

insert into products values(1,'Product 1', 20.50);
insert into products values(2,'Product 2', 120.00);

insert into labels values(1,'Label 1');
insert into labels values(2,'Label 2');

insert into labels_products values(1, 1);
insert into labels_products values(1, 2);
insert into labels_products values(2, 2);
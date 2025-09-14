CREATE TABLE role
(
    name        VARCHAR(100) PRIMARY KEY,
    description TEXT
);

CREATE TABLE user_account
(
    user_id       UUID PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE user_account_role
(
    user_id   UUID         NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES user_account (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_name) REFERENCES role (name),
    PRIMARY KEY (user_id, role_name)
);

INSERT INTO role (name, description)
VALUES ('ROLE_CUSTOMER', 'Default role for all registered users.'),
       ('ROLE_STAFF', 'Allows management of cinema operational data (movies, sessions).'),
       ('ROLE_ADMIN', 'Super-user with full system and user management access.');
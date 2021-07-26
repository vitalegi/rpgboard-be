CREATE TABLE Account (
    account_id varchar(40) PRIMARY KEY,
    name varchar(40) NOT NULL
);

CREATE TABLE Game (
    game_id SERIAL PRIMARY KEY,
    name varchar(40) NOT NULL
);
CREATE TABLE RPG_User (
    user_id varchar(40) PRIMARY KEY,
    username varchar(40) NOT NULL
);

CREATE TABLE RPG_Game (
    game_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name varchar(40) NOT NULL,
    owner_id varchar(40),
    is_open boolean
);

ALTER TABLE RPG_Game ADD CONSTRAINT fk_owner
FOREIGN KEY (owner_id)
REFERENCES RPG_User (user_id)
ON DELETE SET NULL ON UPDATE SET NULL;

CREATE TABLE RPG_Board (
    board_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    game_id uuid,
    name varchar(40),
    is_active boolean
);

ALTER TABLE RPG_Board ADD CONSTRAINT fk_game
FOREIGN KEY (game_id)
REFERENCES RPG_Game (game_id)
ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE RPG_GamePlayer (
  game_id uuid,
  user_id varchar(40),
  username varchar(40),
  PRIMARY KEY (game_id, user_id)
);

ALTER TABLE RPG_GamePlayer ADD CONSTRAINT fk_game
FOREIGN KEY (game_id)
REFERENCES RPG_Game (game_id)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE RPG_GamePlayer ADD CONSTRAINT fk_user
FOREIGN KEY (user_id)
REFERENCES RPG_User (user_id)
ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE RPG_GamePlayerRole (
  entry_id serial PRIMARY KEY,
  game_id uuid,
  user_id varchar(40),
  role varchar(40),
  UNIQUE (game_id, user_id, role)
);

ALTER TABLE RPG_GamePlayerRole ADD CONSTRAINT fk_game
FOREIGN KEY (game_id)
REFERENCES RPG_Game (game_id)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE RPG_GamePlayerRole ADD CONSTRAINT fk_user
FOREIGN KEY (user_id)
REFERENCES RPG_User (user_id)
ON DELETE CASCADE ON UPDATE CASCADE;


CREATE TABLE RPG_DD5e_Sheet (
    sheet_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    owner_id varchar(40),
    game_id uuid,
    content jsonb,
    last_update timestamptz
);

ALTER TABLE RPG_DD5e_Sheet ADD CONSTRAINT fk_game
FOREIGN KEY (game_id)
REFERENCES RPG_Game (game_id)
ON DELETE SET NULL ON UPDATE SET NULL;

ALTER TABLE RPG_DD5e_Sheet ADD CONSTRAINT fk_user
FOREIGN KEY (owner_id)
REFERENCES RPG_User (user_id)
ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE RPG_Asset (
    asset_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    game_id uuid,
    name varchar(100),
    size integer,
    content varchar(1000)
);

ALTER TABLE RPG_Asset ADD CONSTRAINT fk_game
FOREIGN KEY (game_id)
REFERENCES RPG_Game (game_id)
ON DELETE CASCADE ON UPDATE CASCADE;


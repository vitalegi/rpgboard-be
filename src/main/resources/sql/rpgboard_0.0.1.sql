CREATE TABLE RPG_User (
    user_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    external_user_id varchar(40),
    username varchar(40) NOT NULL,
    create_date timestamptz NOT NULL,
    last_update timestamptz NOT NULL
);

CREATE TABLE RPG_Game (
    game_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name varchar(40) NOT NULL,
    type VARCHAR(40),
    visibility_policy VARCHAR(40),
    status VARCHAR(40),
    owner_id uuid,
    create_date timestamptz NOT NULL,
    last_update timestamptz NOT NULL
);

ALTER TABLE RPG_Game ADD CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES RPG_User (user_id) ON DELETE SET NULL ON UPDATE SET NULL;


CREATE TABLE RPG_Board (
    board_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    game_id uuid,
    name varchar(160),
    visibility_policy VARCHAR(40),
    is_active boolean,
    user_id uuid,
    create_date timestamptz NOT NULL,
    last_update timestamptz NOT NULL
);

ALTER TABLE RPG_Board ADD CONSTRAINT fk_game FOREIGN KEY (game_id) REFERENCES RPG_Game (game_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE RPG_Board ADD CONSTRAINT fk_owner FOREIGN KEY (user_id) REFERENCES RPG_User (user_id) ON DELETE SET NULL ON UPDATE SET NULL;


CREATE TABLE RPG_GamePlayer (
  game_id uuid,
  user_id uuid,
  username varchar(40),
  PRIMARY KEY (game_id, user_id)
);

ALTER TABLE RPG_GamePlayer ADD CONSTRAINT fk_game FOREIGN KEY (game_id) REFERENCES RPG_Game (game_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE RPG_GamePlayer ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES RPG_User (user_id) ON DELETE CASCADE ON UPDATE CASCADE;


CREATE TABLE RPG_GamePlayerRole (
  entry_id serial PRIMARY KEY,
  game_id uuid,
  user_id uuid,
  user_role varchar(40),
  create_date timestamptz NOT NULL,
  last_update timestamptz NOT NULL,
  UNIQUE (game_id, user_id, user_role)
);

ALTER TABLE RPG_GamePlayerRole ADD CONSTRAINT fk_owner FOREIGN KEY (user_id) REFERENCES RPG_User (user_id) ON DELETE SET NULL ON UPDATE SET NULL;

ALTER TABLE RPG_GamePlayerRole ADD CONSTRAINT fk_game FOREIGN KEY (game_id) REFERENCES RPG_Game (game_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE RPG_GamePlayerRole ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES RPG_User (user_id) ON DELETE CASCADE ON UPDATE CASCADE;


CREATE TABLE RPG_GameItem (
    item_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    owner_id uuid,
    game_id uuid,
    visibility_policy VARCHAR(40),
    type varchar(40),
    content jsonb,
    create_date timestamptz NOT NULL,
    last_update timestamptz NOT NULL
);

ALTER TABLE RPG_GameItem ADD CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES RPG_User (user_id) ON DELETE SET NULL ON UPDATE SET NULL;

ALTER TABLE RPG_GameItem ADD CONSTRAINT fk_game FOREIGN KEY (game_id) REFERENCES RPG_Game (game_id) ON DELETE SET NULL ON UPDATE SET NULL;


CREATE TABLE RPG_Asset (
    asset_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    game_id uuid,
    name varchar(100),
    size integer,
    content varchar(1000),
    user_id uuid,
    create_date timestamptz NOT NULL,
    last_update timestamptz NOT NULL
);

ALTER TABLE RPG_Asset ADD CONSTRAINT fk_owner FOREIGN KEY (user_id) REFERENCES RPG_User (user_id) ON DELETE SET NULL ON UPDATE SET NULL;

ALTER TABLE RPG_Asset ADD CONSTRAINT fk_game FOREIGN KEY (game_id) REFERENCES RPG_Game (game_id) ON DELETE CASCADE ON UPDATE CASCADE;

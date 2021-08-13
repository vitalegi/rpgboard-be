CREATE TABLE RPG_Board_Element (
    entry_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    board_id uuid,
    visibility_policy VARCHAR(40),
    update_policy VARCHAR(40),
    config json,
    parent_id uuid,
    entry_position integer,
    user_id uuid,
    create_date timestamptz NOT NULL,
    last_update timestamptz NOT NULL
);

ALTER TABLE RPG_Board_Element ADD CONSTRAINT fk_board FOREIGN KEY (board_id) REFERENCES RPG_Board (board_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE RPG_Board_Element ADD CONSTRAINT fk_owner FOREIGN KEY (user_id) REFERENCES RPG_User (user_id) ON DELETE SET NULL ON UPDATE SET NULL;

ALTER TABLE RPG_Board_Element ADD CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES RPG_Board_Element (entry_id) ON DELETE CASCADE ON UPDATE CASCADE;


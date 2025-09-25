CREATE TABLE IF NOT EXISTS public.essentials_server_warps
(
    name         VARCHAR(64) PRIMARY KEY NOT NULL,
    permission   VARCHAR(128),
    display_name TEXT,
    location     TEXT                    NOT NULL
);

CREATE TABLE IF NOT EXISTS public.central_player_homes
(
    owner    UUID        NOT NULL,
    name     VARCHAR(64) NOT NULL,
    location TEXT        NOT NULL,
    PRIMARY KEY (owner, name)
)
CREATE TABLE gms_user (
	id BIGINT NOT NULL IDENTITY(1, 1),
	creation_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	credential VARCHAR(255) NULL DEFAULT NULL,
	email VARCHAR(255) NOT NULL,
	name VARCHAR(255) NOT NULL,
	roles VARCHAR(255) NOT NULL,
	status VARCHAR(255) NOT NULL,
	user_name VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gms_api_key (
	id BIGINT NOT NULL IDENTITY(1, 1),
	creation_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	name VARCHAR(255)  NOT NULL,
	description VARCHAR(255) NULL DEFAULT NULL,
	status VARCHAR(255) NOT NULL,
	user_id BIGINT NOT NULL,
	value VARCHAR(512) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gms_event (
	id BIGINT NOT NULL IDENTITY(1, 1),
	event_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	operation VARCHAR(255) NULL DEFAULT NULL,
	target VARCHAR(255) NULL DEFAULT NULL,
	user_name VARCHAR(255) NULL DEFAULT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gms_keystore (
	id BIGINT NOT NULL IDENTITY(1, 1),
	name VARCHAR(255) NOT NULL,
	creation_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	credential VARCHAR(512) NOT NULL,
	description VARCHAR(255) NULL DEFAULT NULL,
	file_name VARCHAR(255) NULL DEFAULT NULL,
	status VARCHAR(255) NOT NULL,
	type VARCHAR(255) NOT NULL,
	user_id BIGINT NULL DEFAULT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gms_keystore_alias (
	id BIGINT NOT NULL IDENTITY(1, 1),
	keystore_id BIGINT NULL DEFAULT NULL,
	alias VARCHAR(512) NOT NULL,
	alias_credential VARCHAR(512) NOT NULL,
	description VARCHAR(255) NULL DEFAULT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gms_secret (
	id BIGINT NOT NULL IDENTITY(1, 1),
	creation_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	keystore_alias_id BIGINT NOT NULL,
	last_rotated datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_updated datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	return_decrypted tinyint NOT NULL DEFAULT 1,
	rotation_enabled tinyint NOT NULL DEFAULT 1,
	rotation_period VARCHAR(255) NOT NULL,
	secret_id VARCHAR(255) NOT NULL,
	status VARCHAR(255) NULL DEFAULT NULL,
	type VARCHAR(255) NULL DEFAULT NULL,
	user_id BIGINT NOT NULL,
	value VARCHAR(512) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gms_announcement (
	id BIGINT NOT NULL IDENTITY(1, 1),
	author_id BIGINT NOT NULL,
	announcement_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	title VARCHAR(255) NULL DEFAULT NULL,
	description VARCHAR(255) NULL DEFAULT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gms_message (
	id BIGINT NOT NULL IDENTITY(1, 1),
	user_id BIGINT NOT NULL,
	creation_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	message VARCHAR(255) NULL DEFAULT NULL,
	opened TINYINT NOT NULL DEFAULT 0,
	PRIMARY KEY (id)
);

CREATE TABLE gms_api_key_restriction (
	id BIGINT NOT NULL IDENTITY(1, 1),
	secret_id BIGINT NULL DEFAULT NULL,
	api_key_id BIGINT NOT NULL,
	user_id BIGINT NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gms_system_property (
	id BIGINT NOT NULL IDENTITY(1, 1),
	key VARCHAR(255) NULL DEFAULT NULL,
	value VARCHAR(255) NULL DEFAULT NULL,
	last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (id)
);
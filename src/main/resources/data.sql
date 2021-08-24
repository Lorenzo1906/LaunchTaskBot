INSERT INTO user (id, username) VALUES
  (1, 'lorenzo1906');

INSERT INTO channel (id, name) VALUES
  (1, 'all'),
  (2, 'general');

INSERT INTO role (id, name, value, channel_id) VALUES
  (1, 'admin', 'value', 1);

INSERT INTO role_users (roles_id, users_id) VALUES
  (1, 1);
INSERT INTO user (id, username) VALUES
  (1, 'lorenzo1906');

INSERT INTO role (id, name, value) VALUES
  (1, 'admin', 'value');

INSERT INTO role_users (roles_id, users_id) VALUES
  (1, 1);

INSERT INTO project (id, name, slack_channel) VALUES
  (1, 'general', 'testbed');

INSERT INTO action (id, environment, name, service, url, project_id) VALUES
  (1, 'qa', 'show', 'print', 'https://ci.rivetlogic.com/rest/api/latest/queue/TP-LOR', 1);

INSERT INTO role_project_action (id, role_id, project_id, action_id) VALUES
  (1, 1, 1, 1);

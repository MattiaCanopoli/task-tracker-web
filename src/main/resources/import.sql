INSERT INTO status(status_name) VALUES ('TO-DO'),('IN-PROGRESS'),('DONE'),('DELETED');
INSERT INTO user (username, email,password) VALUES ('user01','user01@email.com','user01.pass'),('user02','user02@email.com','user02.pass');
INSERT INTO role (name) VALUES ('ADMIN'),('USER');
INSERT INTO user_roles (user_id, roles_id) VALUES (1,1),(1,2),(2,2);
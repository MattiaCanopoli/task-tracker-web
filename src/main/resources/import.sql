INSERT INTO status(status_name) VALUES ('TO-DO'),('IN-PROGRESS'),('DONE'),('DELETED');
INSERT INTO user (username, email,password) VALUES ('user01','user01@email.com','$2a$12$5oTunEg83FS7Lu2H0fqkWOEXi3IRVsvT5vhHxkGC2EzNsJ3Uf3sRm'),('user02','user02@email.com','$2a$12$itrD1hxZpGbYlTdGLAcqYOYGPGGMZMSIB//nCYw/yJPbHXRDMotNC');
INSERT INTO role (name) VALUES ('ADMIN'),('USER');
INSERT INTO user_roles (user_id, roles_id) VALUES (1,1),(1,2),(2,2);
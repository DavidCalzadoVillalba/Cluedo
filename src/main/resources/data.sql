-- One admin user, named admin1 with passwor 4dm1n and authority admin
INSERT INTO authorities(id,authority) VALUES (1,'ADMIN');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (1,'admin1','$2a$10$nMmTWAhPTqXqLDJTag3prumFrAJpsYtroxf0ojesFYq0k4PmcbWUS',1,'admin1@cluedo.test','ACTIVE');

-- Ten player users, named player1 with passwor 0wn3r
INSERT INTO authorities(id,authority) VALUES (2,'PLAYER');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (4,'player1','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player1@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (5,'player2','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player2@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (6,'player3','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player3@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (7,'player4','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player4@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (8,'player5','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player5@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (9,'player6','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player6@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (10,'player7','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player7@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (11,'player8','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player8@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (12,'player9','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player9@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (13,'player10','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'player10@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (14,'RNB4366','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'rnb4366@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (15,'JCN1074','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'jcn1074@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (16,'GQK8638','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'gqk8638@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (17,'MMT1651','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'mmt1651@cluedo.test','ACTIVE');
INSERT INTO appusers(id,username,password,authority,email,account_status) VALUES (19,'BYP5449','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,'byp5449@cluedo.test','ACTIVE');
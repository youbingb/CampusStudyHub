-- Campus Study Hub seed data
-- 默认密码 123456（BCrypt 后）：$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2

SET NAMES utf8mb4;

DELETE FROM sys_user;
DELETE FROM study_room;
DELETE FROM seat;
DELETE FROM reservation_rule;

INSERT INTO sys_user (id, username, password, real_name, student_no, phone, email, role, credit_score, status) VALUES
(1, 'admin',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', NULL,        '13800000001', 'admin@csh.edu', 'ADMIN',   100, 1),
(2, 'admin2', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '巡检管理员', NULL,        '13800000002', 'admin2@csh.edu','ADMIN',   100, 1),
(3, 'stu01',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张三',     '20230001',  '13900000001', 's01@csh.edu',   'STUDENT', 100, 1),
(4, 'stu02',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李四',     '20230002',  '13900000002', 's02@csh.edu',   'STUDENT', 100, 1),
(5, 'stu03',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王五',     '20230003',  '13900000003', 's03@csh.edu',   'STUDENT', 95,  1),
(6, 'stu04',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '赵六',     '20230004',  '13900000004', 's04@csh.edu',   'STUDENT', 100, 1),
(7, 'stu05',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '钱七',     '20230005',  '13900000005', 's05@csh.edu',   'STUDENT', 80,  1);

INSERT INTO study_room (id, name, location, capacity, open_time, close_time, status, description) VALUES
(1, 'A101 安静自习室', '图书馆 1 楼东侧', 30, '07:00', '22:30', 1, '禁止讨论，长时间深度自习首选'),
(2, 'A102 普通自习室', '图书馆 1 楼西侧', 30, '07:00', '22:30', 1, '允许低声交流'),
(3, 'B201 研讨自习室', '教学楼 B 座 2 层', 30, '08:00', '21:30', 1, '可小组讨论，配有插座');

-- 3 房 × 30 座 = 90 座位（5 行 × 6 列）
INSERT INTO seat (room_id, seat_no, row_no, col_no, status, feature) VALUES
-- Room 1: A101 - 全部安静，部分靠窗(col=1或6)、部分有插座(row=1)
(1,'A1', 1,1,'AVAILABLE','["window","socket","quiet"]'),(1,'A2', 1,2,'AVAILABLE','["socket","quiet"]'),(1,'A3', 1,3,'AVAILABLE','["socket","quiet"]'),
(1,'A4', 1,4,'AVAILABLE','["socket","quiet"]'),(1,'A5', 1,5,'AVAILABLE','["socket","quiet"]'),(1,'A6', 1,6,'AVAILABLE','["window","socket","quiet"]'),
(1,'A7', 2,1,'AVAILABLE','["window","quiet"]'),(1,'A8', 2,2,'AVAILABLE','["quiet"]'),(1,'A9', 2,3,'AVAILABLE','["quiet"]'),
(1,'A10',2,4,'AVAILABLE','["quiet"]'),(1,'A11',2,5,'AVAILABLE','["quiet"]'),(1,'A12',2,6,'AVAILABLE','["window","quiet"]'),
(1,'A13',3,1,'AVAILABLE','["window","quiet"]'),(1,'A14',3,2,'AVAILABLE','["quiet"]'),(1,'A15',3,3,'AVAILABLE','["quiet"]'),
(1,'A16',3,4,'AVAILABLE','["quiet"]'),(1,'A17',3,5,'AVAILABLE','["quiet"]'),(1,'A18',3,6,'AVAILABLE','["window","quiet"]'),
(1,'A19',4,1,'AVAILABLE','["window","quiet"]'),(1,'A20',4,2,'AVAILABLE','["quiet"]'),(1,'A21',4,3,'AVAILABLE','["quiet"]'),
(1,'A22',4,4,'AVAILABLE','["quiet"]'),(1,'A23',4,5,'AVAILABLE','["quiet"]'),(1,'A24',4,6,'AVAILABLE','["window","quiet"]'),
(1,'A25',5,1,'AVAILABLE','["window","quiet"]'),(1,'A26',5,2,'AVAILABLE','["quiet"]'),(1,'A27',5,3,'FAULT','["quiet"]'),
(1,'A28',5,4,'AVAILABLE','["quiet"]'),(1,'A29',5,5,'AVAILABLE','["quiet"]'),(1,'A30',5,6,'AVAILABLE','["window","quiet"]'),

-- Room 2: A102 - 普通
(2,'B1', 1,1,'AVAILABLE','["window","socket"]'),(2,'B2', 1,2,'AVAILABLE','["socket"]'),(2,'B3', 1,3,'AVAILABLE','["socket"]'),
(2,'B4', 1,4,'AVAILABLE','["socket"]'),(2,'B5', 1,5,'AVAILABLE','["socket"]'),(2,'B6', 1,6,'AVAILABLE','["window","socket"]'),
(2,'B7', 2,1,'AVAILABLE','["window"]'),(2,'B8', 2,2,'AVAILABLE','[]'),(2,'B9', 2,3,'AVAILABLE','[]'),
(2,'B10',2,4,'AVAILABLE','[]'),(2,'B11',2,5,'AVAILABLE','[]'),(2,'B12',2,6,'AVAILABLE','["window"]'),
(2,'B13',3,1,'AVAILABLE','["window"]'),(2,'B14',3,2,'AVAILABLE','[]'),(2,'B15',3,3,'AVAILABLE','[]'),
(2,'B16',3,4,'AVAILABLE','[]'),(2,'B17',3,5,'AVAILABLE','[]'),(2,'B18',3,6,'AVAILABLE','["window"]'),
(2,'B19',4,1,'AVAILABLE','["window"]'),(2,'B20',4,2,'AVAILABLE','[]'),(2,'B21',4,3,'AVAILABLE','[]'),
(2,'B22',4,4,'AVAILABLE','[]'),(2,'B23',4,5,'AVAILABLE','[]'),(2,'B24',4,6,'AVAILABLE','["window"]'),
(2,'B25',5,1,'AVAILABLE','["window"]'),(2,'B26',5,2,'AVAILABLE','[]'),(2,'B27',5,3,'AVAILABLE','[]'),
(2,'B28',5,4,'AVAILABLE','[]'),(2,'B29',5,5,'AVAILABLE','[]'),(2,'B30',5,6,'AVAILABLE','["window"]'),

-- Room 3: B201 - 全部带插座
(3,'C1', 1,1,'AVAILABLE','["window","socket"]'),(3,'C2', 1,2,'AVAILABLE','["socket"]'),(3,'C3', 1,3,'AVAILABLE','["socket"]'),
(3,'C4', 1,4,'AVAILABLE','["socket"]'),(3,'C5', 1,5,'AVAILABLE','["socket"]'),(3,'C6', 1,6,'AVAILABLE','["window","socket"]'),
(3,'C7', 2,1,'AVAILABLE','["window","socket"]'),(3,'C8', 2,2,'AVAILABLE','["socket"]'),(3,'C9', 2,3,'AVAILABLE','["socket"]'),
(3,'C10',2,4,'AVAILABLE','["socket"]'),(3,'C11',2,5,'AVAILABLE','["socket"]'),(3,'C12',2,6,'AVAILABLE','["window","socket"]'),
(3,'C13',3,1,'AVAILABLE','["window","socket"]'),(3,'C14',3,2,'AVAILABLE','["socket"]'),(3,'C15',3,3,'AVAILABLE','["socket"]'),
(3,'C16',3,4,'AVAILABLE','["socket"]'),(3,'C17',3,5,'AVAILABLE','["socket"]'),(3,'C18',3,6,'AVAILABLE','["window","socket"]'),
(3,'C19',4,1,'AVAILABLE','["window","socket"]'),(3,'C20',4,2,'AVAILABLE','["socket"]'),(3,'C21',4,3,'AVAILABLE','["socket"]'),
(3,'C22',4,4,'AVAILABLE','["socket"]'),(3,'C23',4,5,'AVAILABLE','["socket"]'),(3,'C24',4,6,'AVAILABLE','["window","socket"]'),
(3,'C25',5,1,'AVAILABLE','["window","socket"]'),(3,'C26',5,2,'AVAILABLE','["socket"]'),(3,'C27',5,3,'AVAILABLE','["socket"]'),
(3,'C28',5,4,'AVAILABLE','["socket"]'),(3,'C29',5,5,'AVAILABLE','["socket"]'),(3,'C30',5,6,'AVAILABLE','["window","socket"]');

INSERT INTO reservation_rule (id, max_daily, max_advance_days, min_credit, check_in_grace_min, max_duration_hours, no_show_credit_penalty)
VALUES (1, 2, 3, 60, 15, 4, 5);

INSERT INTO announcement (title, content, publisher_id, status, published_at) VALUES
('系统试运行公告', '校园自习室管理系统已上线试运行，请同学们文明使用。占座违规将扣除信誉分。', 1, 1, NOW());

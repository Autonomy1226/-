-- 创建数据库
CREATE DATABASE IF NOT EXISTS study_room DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE study_room;

-- 开启事件调度器
SET GLOBAL event_scheduler = ON;

-- 创建事件，每分钟执行一次
DELIMITER //
CREATE EVENT IF NOT EXISTS check_expired_reservations
ON SCHEDULE EVERY 0.5 MINUTE
DO
BEGIN
    -- 更新过期的预约状态
    UPDATE reservation r
    SET r.status = 'COMPLETED',
        r.update_time = NOW()
    WHERE r.status IN ('PENDING', 'IN_USE')
    AND r.end_time < NOW();
END //
DELIMITER ;

-- 创建用户表                                              
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    create_time DATETIME,
    update_time DATETIME
);

-- 创建自习室表
CREATE TABLE IF NOT EXISTS study_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20),
    description TEXT,
    create_time DATETIME,
    update_time DATETIME
);

-- 创建座位表
CREATE TABLE IF NOT EXISTS seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) CHECK (status IN ('AVAILABLE', 'UNAVAILABLE')),
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (room_id) REFERENCES study_room(id)
);

-- 删除已存在的触发器（如果存在）
DROP TRIGGER IF EXISTS seat_insert_trigger;

-- 创建触发器，在插入数据时根据room_id排序
DELIMITER //
CREATE TRIGGER seat_insert_trigger
BEFORE INSERT ON seat
FOR EACH ROW
BEGIN
    DECLARE seat_count INT;
    -- 获取当前房间的座位数量
    SELECT COUNT(*) INTO seat_count 
    FROM seat 
    WHERE room_id = NEW.room_id;
    
    -- 设置新的座位号
    SET NEW.seat_number = CONCAT(NEW.room_id, '-', LPAD(seat_count + 1, 3, '0'));
END //
DELIMITER ;

-- 创建预约表
CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20),
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (seat_id) REFERENCES seat(id)
);

-- 创建使用记录表
CREATE TABLE IF NOT EXISTS usage_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    actual_start_time DATETIME,
    actual_end_time DATETIME,
    status VARCHAR(20),
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (reservation_id) REFERENCES reservation(id)
);

-- 插入测试数据
-- 插入管理员用户
INSERT INTO user (username, password, real_name, phone, email, role, create_time, update_time)
VALUES 
('admin', '123456', '管理员', '13800138000', 'admin@example.com', 'ADMIN', NOW(), NOW()),
('admin2', '$2a$10$X/hX4Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y', '副管理员', '13800138001', 'admin2@example.com', 'ADMIN', NOW(), NOW());

-- 插入普通用户
INSERT INTO user (username, password, real_name, phone, email, role, create_time, update_time)
VALUES 
('user', '123456', '张三', '13800138002', 'user1@example.com', 'USER', NOW(), NOW()),
('user2', '$2a$10$X/hX4Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y', '李四', '13800138003', 'user2@example.com', 'USER', NOW(), NOW()),
('user3', '$2a$10$X/hX4Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y1Jz7Y', '王五', '13800138004', 'user3@example.com', 'USER', NOW(), NOW());

-- 插入测试自习室
INSERT INTO study_room (room_name, capacity, status, description, create_time, update_time)
VALUES 
('A区自习室', 50, 'OPEN', '安静舒适的学习环境，配备空调和饮水机', NOW(), NOW()),
('B区自习室', 30, 'OPEN', '配备空调和饮水机，有独立电源插座', NOW(), NOW()),
('C区自习室', 40, 'OPEN', '安静舒适的学习环境，有独立电源插座和WiFi', NOW(), NOW()),
('D区自习室', 35, 'MAINTENANCE', '正在维护中，预计下周开放', NOW(), NOW());

-- 插入测试座位
INSERT INTO seat (room_id, seat_number, price, status, create_time, update_time)
VALUES 
-- A区座位
(1, 'A-001', 5.00, 'AVAILABLE', NOW(), NOW()),
(1, 'A-002', 5.00, 'AVAILABLE', NOW(), NOW()),
(1, 'A-003', 8.00, 'UNAVAILABLE', NOW(), NOW()),
(1, 'A-004', 8.00, 'UNAVAILABLE', NOW(), NOW()),
(1, 'A-005', 10.00, 'UNAVAILABLE', NOW(), NOW()),
-- B区座位
(2, 'B-001', 5.00, 'AVAILABLE', NOW(), NOW()),
(2, 'B-002', 5.00, 'AVAILABLE', NOW(), NOW()),
(2, 'B-003', 8.00, 'UNAVAILABLE', NOW(), NOW()),
(2, 'B-004', 8.00, 'UNAVAILABLE', NOW(), NOW()),
-- C区座位
(3, 'C-001', 5.00, 'AVAILABLE', NOW(), NOW()),
(3, 'C-002', 5.00, 'AVAILABLE', NOW(), NOW()),
(3, 'C-003', 8.00, 'UNAVAILABLE', NOW(), NOW()),
(3, 'C-004', 8.00, 'UNAVAILABLE', NOW(), NOW());

-- 插入测试预约
INSERT INTO reservation (user_id, seat_id, start_time, end_time, total_price, status, create_time, update_time)
VALUES 
(3, 3, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 1 HOUR), 16.00, 'PENDING', NOW(), NOW()),
(4, 4, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL 1 HOUR), 16.00, 'IN_USE', NOW(), NOW()),
(5, 8, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 2 HOUR), 20.00, 'PENDING', NOW(), NOW()),
(3, 11, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 3 HOUR), 30.00, 'PENDING', NOW(), NOW());

-- 插入测试使用记录
INSERT INTO usage_record (reservation_id, actual_start_time, actual_end_time, status, create_time, update_time)
VALUES 
(2, DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, 'IN_PROGRESS', NOW(), NOW()),
(3, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY), 'COMPLETED', NOW(), NOW()),
(4, DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY), 'ABNORMAL', NOW(), NOW()); 
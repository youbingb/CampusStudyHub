-- Campus Study Hub schema (MySQL 8)
-- run: mysql -uroot -p campus_study_hub < schema.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  username      VARCHAR(64)  NOT NULL,
  password      VARCHAR(128) NOT NULL,
  real_name     VARCHAR(64),
  student_no    VARCHAR(32),
  phone         VARCHAR(20),
  email         VARCHAR(128),
  role          VARCHAR(16)  NOT NULL DEFAULT 'STUDENT',
  credit_score  INT          NOT NULL DEFAULT 100,
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  deleted       TINYINT      NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS study_room;
CREATE TABLE study_room (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(64)  NOT NULL,
  location    VARCHAR(128),
  capacity    INT          NOT NULL DEFAULT 0,
  open_time   VARCHAR(8)   NOT NULL DEFAULT '07:00',
  close_time  VARCHAR(8)   NOT NULL DEFAULT '22:30',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1开放 0关闭',
  description VARCHAR(255),
  deleted     TINYINT      NOT NULL DEFAULT 0,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS seat;
CREATE TABLE seat (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  room_id     BIGINT       NOT NULL,
  seat_no     VARCHAR(16)  NOT NULL,
  row_no      INT          NOT NULL,
  col_no      INT          NOT NULL,
  status      VARCHAR(16)  NOT NULL DEFAULT 'AVAILABLE',
  feature     VARCHAR(255) COMMENT 'JSON array: window/socket/quiet',
  deleted     TINYINT      NOT NULL DEFAULT 0,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_room_seat (room_id, seat_no),
  KEY idx_room_status (room_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS reservation;
CREATE TABLE reservation (
  id              BIGINT      NOT NULL AUTO_INCREMENT,
  user_id         BIGINT      NOT NULL,
  seat_id         BIGINT      NOT NULL,
  room_id         BIGINT      NOT NULL,
  start_time      DATETIME    NOT NULL,
  end_time        DATETIME    NOT NULL,
  status          VARCHAR(16) NOT NULL DEFAULT 'BOOKED',
  check_in_time   DATETIME,
  check_out_time  DATETIME,
  deleted         TINYINT     NOT NULL DEFAULT 0,
  created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_status (user_id, status),
  KEY idx_seat_time (seat_id, start_time, end_time),
  KEY idx_room_time (room_id, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS report;
CREATE TABLE report (
  id              BIGINT      NOT NULL AUTO_INCREMENT,
  reporter_id     BIGINT      NOT NULL,
  target_user_id  BIGINT,
  reservation_id  BIGINT,
  seat_id         BIGINT,
  type            VARCHAR(32) NOT NULL COMMENT '占座/喧哗/设施损坏/其他',
  description     VARCHAR(500),
  evidence_url    VARCHAR(500),
  status          VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  result          VARCHAR(500),
  handler_id      BIGINT,
  handled_at      DATETIME,
  deleted         TINYINT     NOT NULL DEFAULT 0,
  created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_reporter (reporter_id),
  KEY idx_target (target_user_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS credit_log;
CREATE TABLE credit_log (
  id           BIGINT      NOT NULL AUTO_INCREMENT,
  user_id      BIGINT      NOT NULL,
  delta        INT         NOT NULL,
  reason       VARCHAR(255) NOT NULL,
  related_type VARCHAR(32),
  related_id   BIGINT,
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  user_id     BIGINT       NOT NULL,
  type        VARCHAR(32)  NOT NULL,
  title       VARCHAR(128) NOT NULL,
  content     VARCHAR(500),
  read_flag   TINYINT      NOT NULL DEFAULT 0,
  related_id  BIGINT,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_read (user_id, read_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS inspection;
CREATE TABLE inspection (
  id            BIGINT      NOT NULL AUTO_INCREMENT,
  room_id       BIGINT      NOT NULL,
  inspector_id  BIGINT      NOT NULL,
  content       VARCHAR(500),
  issues        VARCHAR(500) COMMENT 'JSON array of seat ids with fault',
  created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS announcement;
CREATE TABLE announcement (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  title         VARCHAR(128) NOT NULL,
  content       TEXT,
  publisher_id  BIGINT       NOT NULL,
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1已发布 0草稿',
  published_at  DATETIME,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS reservation_rule;
CREATE TABLE reservation_rule (
  id                    BIGINT  NOT NULL AUTO_INCREMENT,
  max_daily             INT     NOT NULL DEFAULT 2 COMMENT '每日最多预约数',
  max_advance_days      INT     NOT NULL DEFAULT 3 COMMENT '最早提前天数',
  min_credit            INT     NOT NULL DEFAULT 60 COMMENT '预约最低信誉',
  check_in_grace_min    INT     NOT NULL DEFAULT 15 COMMENT '迟到宽限分钟',
  max_duration_hours    INT     NOT NULL DEFAULT 4 COMMENT '单次预约最长小时',
  no_show_credit_penalty INT    NOT NULL DEFAULT 5,
  updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS seat_fault;
CREATE TABLE seat_fault (
  id          BIGINT      NOT NULL AUTO_INCREMENT,
  seat_id     BIGINT      NOT NULL,
  reporter_id BIGINT      NOT NULL,
  description VARCHAR(255),
  status      VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/FIXED',
  fixed_at    DATETIME,
  created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_seat (seat_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  user_id     BIGINT,
  username    VARCHAR(64),
  module      VARCHAR(64),
  action      VARCHAR(128),
  target_id   VARCHAR(64),
  ip          VARCHAR(45),
  ua          VARCHAR(255),
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_module (module),
  KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

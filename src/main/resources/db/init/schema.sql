-- ============================================================
-- SJSJSS Database Schema
-- Database: SJSJSS
-- ============================================================

-- 기존 테이블 삭제 (재실행 시 오류 방지)
DROP TABLE IF EXISTS Member;

-- ============================================================
-- Member 테이블
-- ============================================================
CREATE TABLE Member (
    id      VARCHAR(50)  NOT NULL        COMMENT '회원 ID',
    name    VARCHAR(100) NOT NULL        COMMENT '회원 이름',
    email   VARCHAR(200) NOT NULL        COMMENT '이메일',

    CONSTRAINT PK_Member PRIMARY KEY (id),
    CONSTRAINT UQ_Member_email UNIQUE (email)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='회원 테이블';

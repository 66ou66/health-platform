-- ============================================
-- 健康颐养平台 建表脚本（与实际数据库一致）
-- 数据库：health_platform（utf8mb4）
-- 共 9 张表，含演示种子数据
-- 枚举约定：
--   users.role：0管理员 1患者 2志愿者
--   users.status：1正常 0冻结
--   service_requests.status：0待接单 1已接单 2服务中 3已完成 4已取消
--   task_assignments.status：0已接单(待签到) 1服务中(已签到) 2已完成(已签退) 3已取消
--   task_assignments.assign_type：1志愿者抢单 2管理员指派
--   sos_alerts.status：0待处理 1已指派 2已完成
--   volunteer_profiles.cert_status：0待审核 1已通过 2已驳回
--   service_requests.emergency_flag：0普通 1紧急
--   feedbacks.is_public：1公开 0不公开
-- ============================================

CREATE DATABASE IF NOT EXISTS health_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE health_platform;

-- 按依赖顺序删除（重新执行脚本时使用）
DROP TABLE IF EXISTS feedbacks;
DROP TABLE IF EXISTS sos_alerts;
DROP TABLE IF EXISTS health_records;
DROP TABLE IF EXISTS task_assignments;
DROP TABLE IF EXISTS service_requests;
DROP TABLE IF EXISTS service_categories;
DROP TABLE IF EXISTS volunteer_profiles;
DROP TABLE IF EXISTS patient_profiles;
DROP TABLE IF EXISTS users;

-- 1. 用户基础表（统一登录入口，role 区分角色）
CREATE TABLE users (
    id         INT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username   VARCHAR(50)  NOT NULL COMMENT '用户名',
    password   VARCHAR(255) NOT NULL COMMENT '密码',
    real_name  VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    phone      VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    role       TINYINT      NOT NULL DEFAULT 2 COMMENT '角色：0管理员 1患者 2志愿者',
    avatar     VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0冻结',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础表';

-- 2. 患者档案表（含敏感字段，仅管理员可见全部）
CREATE TABLE patient_profiles (
    id                     INT           NOT NULL AUTO_INCREMENT COMMENT '档案ID',
    user_id                INT           NOT NULL COMMENT '用户ID',
    id_card_no             VARCHAR(18)   DEFAULT NULL COMMENT '身份证号（敏感）',
    birthday               DATE          DEFAULT NULL COMMENT '出生日期',
    gender                 TINYINT       DEFAULT NULL COMMENT '性别：0女 1男',
    emergency_contact_name VARCHAR(50)   DEFAULT NULL COMMENT '紧急联系人姓名',
    emergency_contact_phone VARCHAR(20)  DEFAULT NULL COMMENT '紧急联系人电话',
    home_address           VARCHAR(255)  DEFAULT NULL COMMENT '家庭住址',
    home_longitude         DECIMAL(10,7) DEFAULT NULL COMMENT '家庭住址经度',
    home_latitude          DECIMAL(10,7) DEFAULT NULL COMMENT '家庭住址纬度',
    allergy_history        VARCHAR(500)  DEFAULT NULL COMMENT '过敏史',
    chronic_diseases       VARCHAR(500)  DEFAULT NULL COMMENT '慢性病（敏感）',
    medication_list        VARCHAR(500)  DEFAULT NULL COMMENT '服药清单（敏感）',
    building               VARCHAR(50)   DEFAULT NULL COMMENT '楼栋（院内派单用）',
    floor                  VARCHAR(50)   DEFAULT NULL COMMENT '楼层（院内派单用）',
    room                   VARCHAR(50)   DEFAULT NULL COMMENT '房间号',
    created_at             DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at             DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者档案表';

-- 3. 志愿者档案表（含认证状态与精神荣誉体系）
CREATE TABLE volunteer_profiles (
    id                  INT           NOT NULL AUTO_INCREMENT COMMENT '档案ID',
    user_id             INT           NOT NULL COMMENT '用户ID',
    id_card_no          VARCHAR(18)   DEFAULT NULL COMMENT '身份证号（实名认证）',
    skill_tags          VARCHAR(255)  DEFAULT NULL COMMENT '技能标签，逗号分隔',
    service_region      VARCHAR(100)  DEFAULT NULL COMMENT '服务区域',
    duty_area           VARCHAR(100)  DEFAULT NULL COMMENT '值班区域',
    total_service_hours INT           DEFAULT 0 COMMENT '累计服务时长（小时）',
    stars_total         INT           DEFAULT 0 COMMENT '获得星星总数',
    rating_score        DECIMAL(3,2)  DEFAULT 0.00 COMMENT '平均评分（星星折算）',
    cert_status         TINYINT       DEFAULT 0 COMMENT '认证状态：0待审核 1已通过 2已驳回',
    certify_reason      VARCHAR(200)  DEFAULT NULL COMMENT '认证驳回原因',
    last_active_time    DATETIME      DEFAULT NULL COMMENT '最近活跃时间',
    created_at          DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    updated_at          DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿者档案表';

-- 4. 服务字典表
CREATE TABLE service_categories (
    id            INT          NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50)  NOT NULL COMMENT '分类名称',
    icon          VARCHAR(100) DEFAULT NULL COMMENT '图标（emoji/图片地址）',
    sort          INT          DEFAULT 0 COMMENT '排序',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务分类字典表';

-- 5. 需求发布表
CREATE TABLE service_requests (
    id                INT           NOT NULL AUTO_INCREMENT COMMENT '需求ID',
    patient_id        INT           NOT NULL COMMENT '患者用户ID',
    category_id       INT           NOT NULL COMMENT '服务分类ID',
    title             VARCHAR(100)  NOT NULL COMMENT '需求标题',
    description       VARCHAR(500)  DEFAULT NULL COMMENT '需求描述',
    address           VARCHAR(255)  NOT NULL COMMENT '服务地址',
    service_longitude DECIMAL(10,7) DEFAULT NULL COMMENT '服务地址经度',
    service_latitude  DECIMAL(10,7) DEFAULT NULL COMMENT '服务地址纬度',
    appointment_time  DATETIME      DEFAULT NULL COMMENT '约定服务时间',
    status            TINYINT       DEFAULT 0 COMMENT '状态：0待接单 1已接单 2服务中 3已完成 4已取消',
    emergency_flag    TINYINT       DEFAULT 0 COMMENT '是否紧急：0普通 1紧急',
    created_at        DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    updated_at        DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_patient (patient_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务需求表';

-- 6. 任务指派表（签到/签退）
CREATE TABLE task_assignments (
    id                INT           NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    request_id        INT           NOT NULL COMMENT '需求ID',
    volunteer_id      INT           NOT NULL COMMENT '志愿者用户ID',
    assign_type       TINYINT       DEFAULT 1 COMMENT '指派方式：1志愿者抢单 2管理员指派',
    accept_time       DATETIME      DEFAULT NULL COMMENT '接单时间',
    sign_in_time      DATETIME      DEFAULT NULL COMMENT '签到时间',
    sign_out_time     DATETIME      DEFAULT NULL COMMENT '签退时间',
    sign_in_longitude DECIMAL(10,7) DEFAULT NULL COMMENT '签到经度',
    sign_in_latitude  DECIMAL(10,7) DEFAULT NULL COMMENT '签到纬度',
    status            TINYINT       DEFAULT 0 COMMENT '状态：0已接单(待签到) 1服务中 2已完成 3已取消',
    admin_remark      VARCHAR(255)  DEFAULT NULL COMMENT '管理员改派备注',
    created_at        DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_request (request_id),
    KEY idx_volunteer (volunteer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务指派表';

-- 7. 健康监测记录表
CREATE TABLE health_records (
    id                 INT           NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    patient_id         INT           NOT NULL COMMENT '患者用户ID',
    volunteer_id       INT           DEFAULT NULL COMMENT '上报的志愿者用户ID（志愿者代报时为非空）',
    record_time        DATETIME      NOT NULL COMMENT '测量时间',
    blood_pressure_sys INT           DEFAULT NULL COMMENT '收缩压（高压）mmHg',
    blood_pressure_dia INT           DEFAULT NULL COMMENT '舒张压（低压）mmHg',
    heart_rate         INT           DEFAULT NULL COMMENT '心率 次/分',
    blood_sugar        DECIMAL(5,2)  DEFAULT NULL COMMENT '血糖 mmol/L',
    mood_status        VARCHAR(20)   DEFAULT NULL COMMENT '心情状态：开心/平静/焦虑/低落',
    note               VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    created_at         DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    PRIMARY KEY (id),
    KEY idx_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康监测记录表';

-- 8. 评价反馈表（一条任务记录包含患者、志愿者双方的评价）
CREATE TABLE feedbacks (
    id               INT          NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    assignment_id    INT          NOT NULL COMMENT '任务ID',
    patient_score    TINYINT      NOT NULL COMMENT '患者给志愿者的评分 1-5 星',
    patient_comment  VARCHAR(255) DEFAULT NULL COMMENT '患者评价内容',
    volunteer_score  TINYINT      DEFAULT NULL COMMENT '志愿者给患者的评分 1-5 星',
    volunteer_comment VARCHAR(255) DEFAULT NULL COMMENT '志愿者评价内容',
    is_public        TINYINT      DEFAULT 1 COMMENT '是否公开：1公开 0不公开',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_assignment (assignment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价反馈表';

-- 9. SOS 紧急求助表
CREATE TABLE sos_alerts (
    id            INT           NOT NULL AUTO_INCREMENT COMMENT '求助ID',
    patient_id    INT           NOT NULL COMMENT '患者用户ID',
    volunteer_id  INT           DEFAULT NULL COMMENT '处理志愿者用户ID',
    longitude     DECIMAL(10,7) NOT NULL COMMENT '经度',
    latitude      DECIMAL(10,7) NOT NULL COMMENT '纬度',
    address       VARCHAR(255)  NOT NULL COMMENT '地址（高德逆地理编码）',
    contact_phone VARCHAR(20)   NOT NULL COMMENT '联系号码',
    remark        VARCHAR(500)  DEFAULT NULL COMMENT '情况备注',
    status        TINYINT       DEFAULT 0 COMMENT '状态：0待处理 1已指派 2已完成',
    trigger_time  DATETIME      NOT NULL COMMENT '触发时间',
    response_time DATETIME      DEFAULT NULL COMMENT '处理完成时间',
    created_at    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SOS紧急求助表';

-- 10. 实时位置心跳表（患者/志愿者定时上报，用于打车式实时地图）
CREATE TABLE real_time_locations (
    id         INT           NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id    INT           NOT NULL UNIQUE COMMENT '用户ID',
    longitude  DECIMAL(10,7) NOT NULL COMMENT '经度',
    latitude   DECIMAL(10,7) NOT NULL COMMENT '纬度',
    updated_at DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近上报时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实时位置心跳表';

-- ============================================
-- 演示种子数据
-- ============================================

INSERT INTO users (username, password, real_name, phone, role, status) VALUES
('admin',      '123456', '平台管理员', '13800000000', 0, 1),
('laoren',     '123456', '张爷爷',     '13811111111', 1, 1),
('zhiyuanzhe', '123456', '李志愿者',   '13822222222', 2, 1);

INSERT INTO patient_profiles (user_id, id_card_no, birthday, gender, emergency_contact_name, emergency_contact_phone,
                              home_address, allergy_history, chronic_diseases, medication_list, building, floor, room) VALUES
(2, '210204193801011234', '1948-01-01', 1, '张奶奶', '13833333333',
 '幸福养老院 3 号楼 205 房间', '青霉素过敏', '高血压、糖尿病',
 '氨氯地平片每日一次；二甲双胍每日两次', '3号楼', '2层', '205');

INSERT INTO volunteer_profiles (user_id, id_card_no, skill_tags, service_region, duty_area,
                                total_service_hours, stars_total, rating_score, cert_status) VALUES
(3, '210204200001015678', '陪诊,买药,陪伴', '幸福养老院', '3号楼',
 12, 25, 5.00, 1);

INSERT INTO service_categories (category_name, icon, sort) VALUES
('陪诊就医', '🏥', 1),
('买药送药', '💊', 2),
('生活照料', '🍲', 3),
('心理陪伴', '💬', 4),
('家政服务', '🧹', 5);

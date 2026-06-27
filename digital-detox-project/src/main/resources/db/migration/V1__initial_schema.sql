-- V1__initial_schema.sql
-- PostgreSQL

-- =========================
-- Security / Auth tables
-- =========================
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE INDEX idx_roles_name ON roles (name);

CREATE TABLE capabilities (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uk_capabilities_name UNIQUE (name)
);

CREATE INDEX idx_capabilities_name ON capabilities (name);

CREATE TABLE roles_capabilities (
    role_id       BIGINT NOT NULL,
    capability_id BIGINT NOT NULL,
    CONSTRAINT pk_roles_capabilities PRIMARY KEY (role_id, capability_id),
    CONSTRAINT fk_roles_capabilities_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_roles_capabilities_capability
        FOREIGN KEY (capability_id) REFERENCES capabilities (id) ON DELETE CASCADE
);

CREATE INDEX idx_roles_capabilities_capability_id ON roles_capabilities (capability_id);

CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    uuid       UUID         NOT NULL,
    username   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role_id    BIGINT       NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uk_users_uuid UNIQUE (uuid),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
);

CREATE INDEX idx_users_role_id ON users (role_id);
CREATE INDEX idx_users_deleted ON users (deleted);

-- =========================
-- Domain tables
-- =========================
CREATE TABLE member_profiles (
    id           BIGSERIAL PRIMARY KEY,
    uuid         UUID         NOT NULL,
    user_id      BIGINT       NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    timezone     VARCHAR(100),
    main_goal    VARCHAR(500),
    notes        TEXT,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at   TIMESTAMPTZ,
    CONSTRAINT uk_member_profiles_uuid UNIQUE (uuid),
    CONSTRAINT uk_member_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_member_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_member_profiles_deleted ON member_profiles (deleted);

CREATE TABLE coach_profiles (
    id                BIGSERIAL PRIMARY KEY,
    uuid              UUID         NOT NULL,
    user_id           BIGINT       NOT NULL,
    display_name      VARCHAR(255) NOT NULL,
    specialty         VARCHAR(255),
    bio               TEXT,
    approved          BOOLEAN      NOT NULL DEFAULT FALSE,
    years_experience  INT,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMPTZ,
    CONSTRAINT uk_coach_profiles_uuid UNIQUE (uuid),
    CONSTRAINT uk_coach_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_coach_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_coach_profiles_approved ON coach_profiles (approved);
CREATE INDEX idx_coach_profiles_deleted ON coach_profiles (deleted);

CREATE TABLE detox_plans (
    id                     BIGSERIAL PRIMARY KEY,
    uuid                   UUID         NOT NULL,
    member_profile_id      BIGINT       NOT NULL,
    coach_profile_id       BIGINT       NOT NULL,
    title                  VARCHAR(255) NOT NULL,
    description            TEXT,
    start_date             DATE         NOT NULL,
    end_date               DATE,
    status                 VARCHAR(50)  NOT NULL,
    target_screen_minutes  INT,
    target_social_minutes  INT,
    focus_area             VARCHAR(255),
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    deleted                BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at             TIMESTAMPTZ,
    CONSTRAINT uk_detox_plans_uuid UNIQUE (uuid),
    CONSTRAINT fk_detox_plans_member
        FOREIGN KEY (member_profile_id) REFERENCES member_profiles (id) ON DELETE RESTRICT,
    CONSTRAINT fk_detox_plans_coach
        FOREIGN KEY (coach_profile_id) REFERENCES coach_profiles (id) ON DELETE RESTRICT
);

CREATE INDEX idx_detox_plans_member_profile_id ON detox_plans (member_profile_id);
CREATE INDEX idx_detox_plans_coach_profile_id ON detox_plans (coach_profile_id);
CREATE INDEX idx_detox_plans_status ON detox_plans (status);
CREATE INDEX idx_detox_plans_deleted ON detox_plans (deleted);

CREATE TABLE goals (
    id            BIGSERIAL PRIMARY KEY,
    uuid          UUID         NOT NULL,
    detox_plan_id BIGINT       NOT NULL,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    metric_type   VARCHAR(50)  NOT NULL,
    target_value  INT          NOT NULL,
    current_value INT          NOT NULL DEFAULT 0,
    status        VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMPTZ,
    CONSTRAINT uk_goals_uuid UNIQUE (uuid),
    CONSTRAINT fk_goals_detox_plan
        FOREIGN KEY (detox_plan_id) REFERENCES detox_plans (id) ON DELETE CASCADE
);

CREATE INDEX idx_goals_detox_plan_id ON goals (detox_plan_id);
CREATE INDEX idx_goals_deleted ON goals (deleted);

CREATE TABLE daily_checkins (
    id                    BIGSERIAL PRIMARY KEY,
    uuid                  UUID         NOT NULL,
    detox_plan_id         BIGINT       NOT NULL,
    entry_date            DATE         NOT NULL,
    total_screen_minutes  INT          NOT NULL,
    social_media_minutes  INT,
    sleep_hours           NUMERIC(4, 2),
    focus_score           INT,
    stress_level          INT,
    craving_level         INT,
    notes                 TEXT,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at            TIMESTAMPTZ,
    CONSTRAINT uk_daily_checkins_uuid UNIQUE (uuid),
    CONSTRAINT uk_daily_checkins_plan_date UNIQUE (detox_plan_id, entry_date),
    CONSTRAINT fk_daily_checkins_detox_plan
        FOREIGN KEY (detox_plan_id) REFERENCES detox_plans (id) ON DELETE CASCADE
);

CREATE INDEX idx_daily_checkins_detox_plan_id ON daily_checkins (detox_plan_id);
CREATE INDEX idx_daily_checkins_entry_date ON daily_checkins (entry_date);

CREATE TABLE weekly_reviews (
    id               BIGSERIAL PRIMARY KEY,
    uuid             UUID         NOT NULL,
    detox_plan_id    BIGINT       NOT NULL,
    coach_profile_id BIGINT       NOT NULL,
    week_start       DATE         NOT NULL,
    summary          TEXT,
    recommendation   TEXT,
    risk_level       VARCHAR(50),
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at       TIMESTAMPTZ,
    CONSTRAINT uk_weekly_reviews_uuid UNIQUE (uuid),
    CONSTRAINT fk_weekly_reviews_detox_plan
        FOREIGN KEY (detox_plan_id) REFERENCES detox_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_weekly_reviews_coach
        FOREIGN KEY (coach_profile_id) REFERENCES coach_profiles (id) ON DELETE RESTRICT
);

CREATE INDEX idx_weekly_reviews_detox_plan_id ON weekly_reviews (detox_plan_id);

CREATE TABLE attachments (
    id                BIGSERIAL PRIMARY KEY,
    uuid              UUID          NOT NULL,
    daily_checkin_id  BIGINT        NOT NULL,
    original_filename VARCHAR(255)  NOT NULL,
    saved_name        VARCHAR(255)  NOT NULL,
    file_path         VARCHAR(1024) NOT NULL,
    content_type      VARCHAR(255)  NOT NULL,
    file_extension    VARCHAR(50),
    size_bytes        BIGINT,
    created_at        TIMESTAMPTZ   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL,
    deleted           BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMPTZ,
    CONSTRAINT uk_attachments_uuid UNIQUE (uuid),
    CONSTRAINT uk_attachments_saved_name UNIQUE (saved_name),
    CONSTRAINT fk_attachments_daily_checkin
        FOREIGN KEY (daily_checkin_id) REFERENCES daily_checkins (id) ON DELETE CASCADE
);

CREATE INDEX idx_attachments_daily_checkin_id ON attachments (daily_checkin_id);
CREATE INDEX idx_attachments_deleted ON attachments (deleted);

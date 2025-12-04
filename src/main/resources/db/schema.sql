use soloco;

CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(50),
    profile_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE travel_project (
    project_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    project_type ENUM('PERSONAL', 'GROUP') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	thumbnail VARCHAR(200),
    FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE project_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('OWNER', 'MEMBER') DEFAULT 'MEMBER',

    FOREIGN KEY (project_id) REFERENCES travel_project(project_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE project_place (
    place_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    place_name VARCHAR(100) NOT NULL,
    place_address VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,

    FOREIGN KEY (project_id) REFERENCES travel_project(project_id) ON DELETE CASCADE
);

CREATE TABLE project_itinerary (
    itinerary_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    day INT NOT NULL,               -- Day 1, Day 2...
    order_no INT NOT NULL,          -- 순서
    place_name VARCHAR(100) NOT NULL,
    description TEXT,               -- AI 설명 / 추천 이유
    safety_score INT,               -- 개인: 혼여 난이도, 치안 등
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (project_id) REFERENCES travel_project(project_id) ON DELETE CASCADE
);

CREATE TABLE project_invite (
    invite_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id   BIGINT NOT NULL,
    invite_code  VARCHAR(64) NOT NULL UNIQUE,
    expires_at   DATETIME NULL,
    max_uses     INT NULL,
    use_count    INT NOT NULL DEFAULT 0,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_invite_project
        FOREIGN KEY (project_id) REFERENCES travel_project(project_id)
);


USE soloco;

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(50),
    profile_image VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    refresh_token VARCHAR(255)
);

-- =========================
-- TRAVEL PROJECT (Aggregate Root)
-- =========================
CREATE TABLE travel_project (
    project_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    project_type ENUM('PERSONAL', 'GROUP') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    thumbnail VARCHAR(200),

    FOREIGN KEY (owner_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- =========================
-- PROJECT MEMBER
-- =========================
CREATE TABLE project_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('OWNER', 'MEMBER') DEFAULT 'MEMBER',

    FOREIGN KEY (project_id)
        REFERENCES travel_project(project_id)
        ON DELETE CASCADE,
    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- =========================
-- PROJECT PLACE
-- =========================
CREATE TABLE project_place (
    place_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    google_place_id VARCHAR(200),
    place_name VARCHAR(100) NOT NULL,
    place_address VARCHAR(255),
    place_type VARCHAR(100),
    thumbnail VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',

    FOREIGN KEY (project_id)
        REFERENCES travel_project(project_id)
        ON DELETE CASCADE
);

-- =========================
-- PROJECT ITINERARY PLACE
-- =========================
CREATE TABLE project_itinerary_place (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    day INT NOT NULL,
    order_no INT NOT NULL,
    place_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (project_id)
        REFERENCES travel_project(project_id)
        ON DELETE CASCADE,
    FOREIGN KEY (place_id)
        REFERENCES project_place(place_id)
        ON DELETE CASCADE
);

-- =========================
-- PROJECT ITINERARY META
-- =========================
CREATE TABLE project_itinerary_solo_meta (
    project_id BIGINT PRIMARY KEY,
    total_score INT,
    safety INT,
    transport_accessibility INT,
    route_simplicity INT,
    landmark_accessibility INT,
    solo_dining_difficulty INT,
    summary TEXT,
    recommendation VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (project_id)
        REFERENCES travel_project(project_id)
        ON DELETE CASCADE
);

CREATE TABLE project_itinerary_group_meta (
    project_id BIGINT PRIMARY KEY,
    summary TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (project_id)
        REFERENCES travel_project(project_id)
        ON DELETE CASCADE
);

-- =========================
-- PROJECT INVITE
-- =========================
CREATE TABLE project_invite (
    invite_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    invite_code VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NULL,
    max_uses INT NULL,
    use_count INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (project_id)
        REFERENCES travel_project(project_id)
        ON DELETE CASCADE
);

-- =========================
-- COMMUNITY : POST
-- =========================
CREATE TABLE project_post (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (project_id)
        REFERENCES travel_project(project_id)
        ON DELETE CASCADE,
    FOREIGN KEY (author_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- =========================
-- POST IMAGE
-- =========================
CREATE TABLE project_post_image (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    order_no INT DEFAULT 1,

    FOREIGN KEY (post_id)
        REFERENCES project_post(post_id)
        ON DELETE CASCADE
);

-- =========================
-- POST TAG
-- =========================
CREATE TABLE project_post_tag (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,

    FOREIGN KEY (post_id)
        REFERENCES project_post(post_id)
        ON DELETE CASCADE
);

-- =========================
-- POST VOTE
-- =========================
CREATE TABLE project_post_vote (
    vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    question VARCHAR(255) NOT NULL,
    multiple_choice BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (post_id)
        REFERENCES project_post(post_id)
        ON DELETE CASCADE
);

-- =========================
-- POST VOTE OPTION
-- =========================
CREATE TABLE project_post_vote_option (
    option_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vote_id BIGINT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    order_no INT DEFAULT 1,

    FOREIGN KEY (vote_id)
        REFERENCES project_post_vote(vote_id)
        ON DELETE CASCADE
);

-- =========================
-- POST VOTE RESULT
-- =========================
CREATE TABLE project_post_vote_result (
    result_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vote_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    voted_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (vote_id)
        REFERENCES project_post_vote(vote_id)
        ON DELETE CASCADE,
    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    FOREIGN KEY (option_id)
        REFERENCES project_post_vote_option(option_id)
        ON DELETE CASCADE
);

-- =========================
-- POST COMMENT
-- =========================
CREATE TABLE project_post_comment (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (post_id)
        REFERENCES project_post(post_id)
        ON DELETE CASCADE,
    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

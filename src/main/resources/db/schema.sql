use soloco;

CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(50),
    profile_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    refresh_token VARCHAR(255)
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
    google_place_id VARCHAR(200),
    place_name VARCHAR(100) NOT NULL,
    place_address VARCHAR(255),
    place_type VARCHAR(100),
    thumbnail VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    

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

##################### 커뮤니티 ####################

CREATE TABLE project_post (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,          -- 어느 프로젝트의 게시글인지
    author_id BIGINT NOT NULL,           -- 작성자(user_id)
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,               -- 게시글 내용 (markdown 가능)
    created_at DATETIME DEFAULT NOW(),
    updated_at DATETIME DEFAULT NOW() ON UPDATE NOW(),
    
    FOREIGN KEY (project_id) REFERENCES travel_project(project_id),
    FOREIGN KEY (author_id) REFERENCES users(user_id)
);

CREATE TABLE project_post_image (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    order_no INT DEFAULT 1,          -- 1~3이면 대표 이미지로 취급
    
    FOREIGN KEY (post_id) REFERENCES project_post(post_id)
);

CREATE TABLE project_post_tag (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    
    FOREIGN KEY (post_id) REFERENCES project_post(post_id)
);

CREATE TABLE project_post_vote (
    vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    question VARCHAR(255) NOT NULL,        -- "어디로 갈까요?"
    multiple_choice BOOLEAN DEFAULT FALSE, -- 복수 선택 여부
    created_at DATETIME DEFAULT NOW(),
    
    FOREIGN KEY (post_id) REFERENCES project_post(post_id)
);

CREATE TABLE project_post_vote_option (
    option_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vote_id BIGINT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    order_no INT DEFAULT 1,               -- 표시 순서
    
    FOREIGN KEY (vote_id) REFERENCES project_post_vote(vote_id)
);

CREATE TABLE project_post_vote_result (
    result_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vote_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    voted_at DATETIME DEFAULT NOW(),

    FOREIGN KEY (vote_id) REFERENCES project_post_vote(vote_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (option_id) REFERENCES project_post_vote_option(option_id)
);

CREATE TABLE project_post_comment (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT NOW(),
    updated_at DATETIME DEFAULT NOW() ON UPDATE NOW(),

    FOREIGN KEY (post_id) REFERENCES project_post(post_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

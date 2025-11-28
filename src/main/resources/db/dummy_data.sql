use soloco;

INSERT INTO users (username, password, email, name, profile_image)
VALUES 
('taejeong', '$2a$10$ZtTs/W5s1HGda3DdBF2bzeUw42i.McDHoa6pV.teGNQcqfJqhAtcu', 'tae@test.com', '허태정', '/img/p1.png'),
('jisun', '$2a$10$ZtTs/W5s1HGda3DdBF2bzeUw42i.McDHoa6pV.teGNQcqfJqhAtcu', 'jisun@test.com', '윤지선', '/img/p2.png'),
('jaehyung', '$2a$10$ZtTs/W5s1HGda3DdBF2bzeUw42i.McDHoa6pV.teGNQcqfJqhAtcu', 'jae@test.com', '박재형', '/img/p3.png');
-- password : pass1

INSERT INTO travel_project (owner_id, title, location, start_date, end_date, project_type, thumbnail)
VALUES
(1, '도쿄 혼자여행', '도쿄', '2025-01-03', '2025-01-06', 'PERSONAL', 'https://yourcdn.com/images/tokyo_main.jpg'),
(2, '부산 솔로 여행', '부산', '2025-02-10', '2025-02-12', 'PERSONAL', 'https://yourcdn.com/images/tokyo_main.jpg');

INSERT INTO travel_project (owner_id, title, location, start_date, end_date, project_type, thumbnail)
VALUES
(1, '제주도 3박4일 친구여행', '제주', '2025-03-01', '2025-03-04', 'GROUP', 'https://yourcdn.com/images/tokyo_main.jpg'),
(3, '강릉 바다 보러가기', '강릉', '2025-04-15', '2025-04-17', 'GROUP', 'https://yourcdn.com/images/tokyo_main.jpg');

INSERT INTO project_member (project_id, user_id, role)
VALUES
(3, 1, 'OWNER'),
(3, 2, 'MEMBER'),
(3, 3, 'MEMBER');

INSERT INTO project_member (project_id, user_id, role)
VALUES
(4, 3, 'OWNER'),
(4, 1, 'MEMBER');

INSERT INTO project_place (project_id, place_name, place_address, latitude, longitude)
VALUES
(1, '도쿄 타워', '도쿄 미나토구', 35.6586, 139.7454),
(1, '센소지', '아사쿠사', 35.7148, 139.7967),
(3, '성산일출봉', '제주 성산', 33.4584, 126.9425);

INSERT INTO project_itinerary (project_id, day, order_no, place_name, description, safety_score)
VALUES
(1, 1, 1, '도쿄 타워', '야경 명소', 85),
(1, 1, 2, '센소지', '전통 거리 탐방', 90),
(3, 1, 1, '성산일출봉', '일출 감상', NULL);

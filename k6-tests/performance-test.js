import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, login, getAuthHeader } from './auth.js';

/**
 * 테스트 구성
 * 10명의 유저가 30초 동안 지속적으로 요청을 보냄
 */
export const options = {
    stages: [
        { duration: '10s', target: 10 }, // 부하 증가
        { duration: '20s', target: 10 }, // 부하 유지
        { duration: '10s', target: 0 },  // 부하 감소
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이내여야 함
        http_req_failed: ['rate<0.01'],   // 에러율 1% 미만
    },
};

/**
 * 테스트 전처리: 토큰 획득
 */
export function setup() {
    const token = login(); // 환경 변수 사용
    return { token: token };
}

/**
 * 메인 테스트 시나리오
 */
export default function (data) {
    if (!data.token) return;

    const authHeader = getAuthHeader(data.token);

    // 1. 내 여행 목록 조회
    const projectsRes = http.get(`${BASE_URL}/travels`, authHeader);
    check(projectsRes, {
        'get projects status is 200': (r) => r.status === 200,
    });

    sleep(1);

    // 2. 여행 상세 조회 (첫 번째 프로젝트 ID가 있다고 가정하거나 실제 ID로 하드코딩)
    // 실제로는 목록에서 ID를 동적으로 추출하는 것이 좋음
    const projects = projectsRes.json();
    if (projects && projects.length > 0) {
        const projectId = projects[0].projectId;
        const detailRes = http.get(`${BASE_URL}/travels/${projectId}`, authHeader);
        check(detailRes, {
            'get project detail status is 200': (r) => r.status === 200,
        });
    }

    sleep(1);
}

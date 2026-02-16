import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, login, getAuthHeader } from './auth.js';

/**
 * 내구성(Soak) 테스트 구성
 * 일정한 부하를 장시간(10분 이상) 유지하여 자원 누수 확인
 */
export const options = {
    vus: 50,
    duration: '10m', // 실제 운영 환경에서는 30분~수시간 수행 권장
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export function setup() {
    const token = login();
    return { token: token };
}

export default function (data) {
    const authHeader = getAuthHeader(data.token);

    // 1. 목록 조회
    const res1 = http.get(`${BASE_URL}/travels`, authHeader);
    check(res1, { 'get travels ok': (r) => r.status === 200 });

    sleep(1);

    // 2. 상세 조회
    const projects = res1.json();
    if (projects && projects.length > 0) {
        const projectId = projects[0].projectId;
        const res2 = http.get(`${BASE_URL}/travels/${projectId}`, authHeader);
        check(res2, { 'get detail ok': (r) => r.status === 200 });
    }

    sleep(1);
}

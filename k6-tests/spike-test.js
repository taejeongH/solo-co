import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, TEST_USER, TEST_PASS } from './auth.js';

/**
 * 스파이크 테스트 구성
 * 아주 짧은 시간 내에 유저가 급격히 증가했다가 감소하는 패턴
 */
export const options = {
    stages: [
        { duration: '10s', target: 100 }, // 10초 만에 100명으로 증가
        { duration: '30s', target: 100 }, // 30초 동안 100명 유지
        { duration: '10s', target: 0 },   // 10초 만에 0명으로 감소
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],   // 급격한 부하 시 에러율 5% 미만 허용
        http_req_duration: ['p(95)<1000'], // p95 응답 시간 1초 이내
    },
};

export default function () {
    const payload = JSON.stringify({
        username: TEST_USER,
        password: TEST_PASS
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // 로그인 API 호출 (JWT 발급 부하 테스트)
    const res = http.post(`${BASE_URL}/auth/login`, payload, params);

    check(res, {
        'login status is 200': (r) => r.status === 200,
        'has token': (r) => r.json('accessToken') !== undefined,
    });

    sleep(1);
}

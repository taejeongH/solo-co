import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, login, getAuthHeader } from './auth.js';

/**
 * 지연 주입(Latency Injection) 테스트 구성
 * 외부 API가 느려졌을 때 서버 스레드 풀이 어떻게 반응하는지 확인
 */
export const options = {
    vus: 100,          // 스레드 점유 효과를 확실히 보기 위해 100명으로 상향
    duration: '1m',
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const authHeader = getAuthHeader(data.token);

    // 💡 캐시 우회를 위해 매번 다른 query 생성 (랜덤 값 추가)
    const randomQuery = `치킨${Math.floor(Math.random() * 1000000)}`;
    const res = http.get(`${BASE_URL}/places/search?query=${encodeURIComponent(randomQuery)}`, authHeader);

    const checkRes = check(res, {
        'status is 200': (r) => r.status === 200,
        'actually delayed (> 5s)': (r) => r.timings.duration >= 5000,
    });

    if (!checkRes) {
        console.log(`Latency test failed: Status=${res.status}, Body=${res.body}`);
    }

    sleep(1);
}

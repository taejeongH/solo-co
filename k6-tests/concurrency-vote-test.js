import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, login, getAuthHeader } from './auth.js';

/**
 * 동시성 테스트 구성
 * 50명의 유저가 동시에 투표를 시도
 */
export const options = {
    scenarios: {
        vote_concurrency: {
            executor: 'per-vu-iterations',
            vus: 100,
            iterations: 1,
            maxDuration: '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
    },
};

/**
 * 테스트 설정: 프로젝트 및 포스트 ID 정의
 */
const PROJECT_ID = 11; // 실제 테스트할 프로젝트 ID
const POST_ID = 14;    // 실제 테스트할 포스트 ID
const OPTION_ID = 28;  // 투표할 옵션 ID

export function setup() {
    // 테스트용 유저들의 토큰을 미리 생성 (실제 운영 환경에서는 다양한 유저 계정 필요)
    // 여기서는 간단하게 하나의 토큰을 공유하거나, VU ID를 이용해 분기 가능
    const token = login();
    return { token: token };
}

export default function (data) {
    const authHeader = getAuthHeader(data.token);

    // 투표 API 호출
    const payload = JSON.stringify({
        optionId: OPTION_ID
    });

    const res = http.post(`${BASE_URL}/travels/${PROJECT_ID}/posts/${POST_ID}/vote`, payload, authHeader);

    const voteCheck = check(res, {
        'vote status is 200 or 409': (r) => r.status === 200 || r.status === 409,
    });

    if (!voteCheck) {
        console.log(`Vote failed: Status=${res.status}, Body=${res.body}`);
    }

    // 투표 결과 조회
    const resultRes = http.get(`${BASE_URL}/travels/${PROJECT_ID}/posts/${POST_ID}/vote/result`, authHeader);
    check(resultRes, {
        'get vote result status is 200': (r) => r.status === 200,
    });

    sleep(0.5);
}

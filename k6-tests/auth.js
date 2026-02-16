import http from 'k6/http';
import { check } from 'k6';

/**
 * 전역 설정값 (환경 변수 또는 기본값)
 */
export const BASE_URL = __ENV.K6_BASE_URL || 'http://localhost:9090/api';
export const TEST_USER = __ENV.K6_USER || 'test';
export const TEST_PASS = __ENV.K6_PASS || '12341234';

/**
 * 로그인하여 Access Token을 얻어오는 함수
 * @param {string} username 
 * @param {string} password 
 * @returns {string|null} accessToken
 */
export function login(username = TEST_USER, password = TEST_PASS) {
    const payload = JSON.stringify({
        username: username,
        password: password,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(`${BASE_URL}/auth/login`, payload, params);

    const checkRes = check(res, {
        'login successful': (r) => r.status === 200,
        'has access token': (r) => r.json('accessToken') !== undefined,
    });

    if (checkRes) {
        return res.json('accessToken');
    } else {
        console.error(`Login failed for ${username}: ${res.status} ${res.body}`);
        return null;
    }
}

/**
 * 인증 헤더를 생성하는 함수
 * @param {string} token 
 * @returns {object} header
 */
export function getAuthHeader(token) {
    return {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    };
}

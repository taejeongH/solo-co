import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, login, getAuthHeader } from './auth.js';

export default function () {
    console.log(`Attempting login...`);
    const token = login();

    if (!token) {
        console.error('Login failed! No token received.');
        return;
    }

    console.log(`Login successful. Token: ${token.substring(0, 10)}...`);

    const authHeader = getAuthHeader(token);
    console.log(`Request Headers: ${JSON.stringify(authHeader.headers)}`);

    const query = '치킨';
    const url = `${BASE_URL}/places/search?query=${encodeURIComponent(query)}`;
    console.log(`Request URL: ${url}`);

    const res = http.get(url, authHeader);

    console.log(`Response Status: ${res.status}`);
    console.log(`Response Body: ${res.body}`);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}

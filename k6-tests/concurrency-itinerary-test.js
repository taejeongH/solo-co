import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { BASE_URL, login } from './auth.js';

/**
 * WebSocket 테스트 구성
 * STOMP 프로토콜을 사용하므로, 프레임 구조를 맞춰서 전송해야 함
 */
export const options = {
    vus: 100,
    duration: '30s',
};

const PROJECT_ID = 11;
const WS_URL = 'ws://localhost:9090/ws/websocket'; // SockJS 없이 직접 접속 시 경로

export function setup() {
    const token = login();
    return { token: token };
}

export default function (data) {
    if (!data.token) return;

    const url = WS_URL;

    const res = ws.connect(url, {}, function (socket) {
        socket.on('open', function () {
            // STOMP CONNECT 프레임 전송
            socket.send('CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\0');

            // 구독 (구독 경로는 서비스 기획에 따라 다름)
            socket.send(`SUBSCRIBE\nid:sub-0\ndestination:/topic/projects/${PROJECT_ID}/updates\n\n\0`);

            // 1초 후 이벤트 메시지 전송 (itinerary 업데이트 시뮬레이션)
            sleep(1);
            const eventPayload = JSON.stringify({
                type: 'ITINERARY_UPDATE',
                projectId: PROJECT_ID,
                data: { detail: 'k6 test update' }
            });
            socket.send(`SEND\ndestination:/app/projects/${PROJECT_ID}/events\ncontent-type:application/json\n\n${eventPayload}\0`);

            sleep(5);
            socket.close();
        });

        socket.on('message', function (msg) {
            // console.log(`Received message: ${msg}`);
            // 메시지 수신 확인 로직 추가 가능
        });

        socket.on('close', function () {
            // console.log('Disconnected');
        });

        socket.on('error', function (e) {
            console.error(`WS Error: ${e.error()}`);
        });
    });

    check(res, { 'status is 101': (r) => r && r.status === 101 });
}

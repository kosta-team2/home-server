/**
 * K6_WEB_DASHBOARD=true k6 run map-regions.test.js
 * 접속 http://localhost:5665
 * */
import http from 'k6/http';
import {check, sleep} from 'k6';

/**
 * k6 options
 * - 동시 사용자 10명
 * - 1분 동안 반복
 *  전체 요청 중 95%가 500ms 안에 끝나야 한다.
 *  10명이 1분 동안 최대한 자연스럽게 지도 API를 두드리는 시뮬레이션
 */
export const options = {
    vus: 100,
    duration: '1m',
    thresholds: {
        http_req_duration: ['p(95)<500'],
    },
};

const BASE_URL = 'https://api.homesearch.world';

/**
 * region level pool
 */
const REGION_LEVELS = ['si-do', 'si-gun-gu', 'eup-myeon-dong'];

/**
 * 서울/수도권 대략 범위
 */
const LAT_MIN = 37.30;
const LAT_MAX = 37.70;
const LNG_MIN = 126.70;
const LNG_MAX = 127.20;

/**
 * random util
 */
function randomBetween(min, max) {
    return Math.random() * (max - min) + min;
}

function randomPick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export default function () {
    /**
     * 랜덤 bounding box 생성
     * (지도 스크롤 시나리오)
     */
    const swLat = randomBetween(LAT_MIN, LAT_MAX - 0.05);
    const swLng = randomBetween(LNG_MIN, LNG_MAX - 0.05);

    const neLat = swLat + randomBetween(0.03, 0.08);
    const neLng = swLng + randomBetween(0.03, 0.08);

    const payload = JSON.stringify({
        swLat,
        swLng,
        neLat,
        neLng,
        region: randomPick(REGION_LEVELS),
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(
        `${BASE_URL}/api/v1/map/regions`,
        payload,
        params
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 400ms': (r) => r.timings.duration < 400,
    });

    // 지도 이동 간 텀
    sleep(randomBetween(0.5, 1.5));
}

/**
 * K6_WEB_DASHBOARD=true k6 run map-complexes.test.js
 * 접속 http://localhost:5665
 * */
import http from 'k6/http';
import {check, sleep} from 'k6';

/**
 * k6 options
 * - 동시 사용자 10명
 * - 1분간 테스트
 * 전체 요청 중 95%가 800ms 안에 끝나야 한다
 * 10명이 1분 동안 최대한 자연스럽게 지도 API를 두드리는 시뮬레이션
 */
export const options = {
    vus: 10,
    duration: '1m',
    thresholds: {
        http_req_duration: ['p(95)<800'], // complexes는 regions보다 무거움
    },
};

const BASE_URL = 'https://api.homesearch.world';

/**
 * 서울/수도권 대략 범위
 */
const LAT_MIN = 37.30;
const LAT_MAX = 37.70;
const LNG_MIN = 126.70;
const LNG_MAX = 127.20;

/**
 * util
 */
function randomBetween(min, max) {
    return Math.random() * (max - min) + min;
}

function randomInt(min, max) {
    return Math.floor(randomBetween(min, max));
}

export default function () {
    /**
     * 랜덤 bounding box (지도 이동)
     */
    const swLat = randomBetween(LAT_MIN, LAT_MAX - 0.05);
    const swLng = randomBetween(LNG_MIN, LNG_MAX - 0.05);

    const neLat = swLat + randomBetween(0.03, 0.08);
    const neLng = swLng + randomBetween(0.03, 0.08);

    /**
     * 랜덤 필터 (사용자 조작 시나리오)
     */
    const pyeongMin = randomInt(10, 30);
    const pyeongMax = pyeongMin + randomInt(10, 30);

    const priceEokMin = randomBetween(1, 5);
    const priceEokMax = priceEokMin + randomBetween(5, 20);

    const ageMin = randomInt(0, 20);
    const ageMax = ageMin + randomInt(5, 30);

    const unitMin = randomInt(50, 300);
    const unitMax = unitMin + randomInt(500, 3000);

    const payload = JSON.stringify({
        swLat,
        swLng,
        neLat,
        neLng,

        pyeongMin,
        pyeongMax,

        priceEokMin: Number(priceEokMin.toFixed(1)),
        priceEokMax: Number(priceEokMax.toFixed(1)),

        ageMin,
        ageMax,

        unitMin,
        unitMax,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(
        `${BASE_URL}/api/v1/map/complexes`,
        payload,
        params
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 700ms': (r) => r.timings.duration < 700,
    });

    // 지도 이동/필터 변경 간 텀
    sleep(randomBetween(0.7, 1.5));
}

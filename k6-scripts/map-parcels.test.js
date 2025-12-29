/**
 * 1000 동시요청 1회 스파이크 테스트
 * 필터 X
 */
import http from "k6/http";
import { check } from "k6";

export const options = {
    scenarios: {
        spike_1000_once: {
            executor: "per-vu-iterations",
            vus: 1000,          // 동시 사용자 1000
            iterations: 1,      // 각 VU가 1번만 실행
            maxDuration: "1m", // 전체 제한시간(여유)
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<800"],
    },
};

const BASE_URL = "https://api.homesearch.world";

/** 서울/수도권 대략 범위 */
const LAT_MIN = 37.30;
const LAT_MAX = 37.70;
const LNG_MIN = 126.70;
const LNG_MAX = 127.20;

function randomBetween(min, max) {
    return Math.random() * (max - min) + min;
}
function randomInt(min, max) {
    // min 포함, max 미포함
    return Math.floor(randomBetween(min, max));
}

export default function () {
    // 랜덤 bounding box
    const swLat = randomBetween(LAT_MIN, LAT_MAX - 0.05);
    const swLng = randomBetween(LNG_MIN, LNG_MAX - 0.05);
    const neLat = swLat + randomBetween(0.03, 0.08);
    const neLng = swLng + randomBetween(0.03, 0.08);

    // 랜덤 필터
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

        pyeongMin: null,
        pyeongMax: null,
        priceEokMin: null,
        priceEokMax: null,
        ageMin: null,
        ageMax: null,
        unitMin: null,
        unitMax: null,
    });


    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    const res = http.post(`${BASE_URL}/api/v1/map/complexes`, payload, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
    });

}

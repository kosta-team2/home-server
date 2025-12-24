import http from 'k6/http';
import {check, sleep} from 'k6';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<800'],
    http_req_failed: ['rate<0.01'], // 실패율도 같이 보는 게 좋음(1% 이하 예시)
  },
};

const BASE_URL = 'https://api.homesearch.world';

const LAT_MIN = 37.30;
const LAT_MAX = 37.70;
const LNG_MIN = 126.70;
const LNG_MAX = 127.20;

function randomBetween(min, max) {
  return Math.random() * (max - min) + min;
}

// max 포함
function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

const BOX_SIZES = [0.03, 0.05, 0.08]; // 줌인/보통/줌아웃

export default function () {
  const box = BOX_SIZES[randomInt(0, BOX_SIZES.length - 1)];

  const swLat = randomBetween(LAT_MIN, LAT_MAX - box);
  const swLng = randomBetween(LNG_MIN, LNG_MAX - box);

  const neLat = swLat + box;
  const neLng = swLng + box;

  const pyeongMin = randomInt(10, 30);
  const pyeongMax = pyeongMin + randomInt(10, 30);

  const priceEokMin = randomBetween(1, 5);
  const priceEokMax = priceEokMin + randomBetween(5, 20);

  const ageMin = randomInt(0, 20);
  const ageMax = ageMin + randomInt(5, 30);

  const unitMin = randomInt(50, 300);
  const unitMax = unitMin + randomInt(500, 3000);

  const payload = JSON.stringify({
    swLat, swLng, neLat, neLng,
    pyeongMin, pyeongMax,
    priceEokMin: Number(priceEokMin.toFixed(1)),
    priceEokMax: Number(priceEokMax.toFixed(1)),
    ageMin, ageMax,
    unitMin, unitMax,
  });

  const res = http.post(`${BASE_URL}/api/v1/map/complexes`, payload, {
    headers: {
      'Content-Type': 'application/json',
      // Authorization: `Bearer ${__ENV.ACCESS_TOKEN}`, // 필요시
    },
  });

  check(res, { 'status is 200': (r) => r.status === 200 });

  if (res.status !== 200 && Math.random() < 0.01) {
    console.log(`status=${res.status} body=${res.body}`);
  }

  sleep(randomBetween(0.7, 1.5));
}

import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  withCredentials: true, // 쿠키 전송을 위해 필수
});

// 요청 인터셉터: Access Token 추가
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// 응답 인터셉터: 토큰 만료 시 재발급 로직 추가 예정
api.interceptors.response.use(
  (res) => res,
  async (err) => {
    // 여기서 401 에러를 감지하고 /auth/refresh API를 호출하는 로직을 작성합니다.
    return Promise.reject(err);
  },
);

// src/lib/axios.ts (응답 인터셉터 보강)
api.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config;

    // 401 에러이고, 재시도한 적이 없다면
    if (err.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        // 서버의 /auth/refresh 엔드포인트 호출 (쿠키는 자동으로 전송됨)
        const res = await api.post("/auth/refresh");
        const newAccessToken = res.data.accessToken;
        localStorage.setItem("accessToken", newAccessToken);

        // 실패했던 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } catch (refreshErr) {
        // Refresh Token도 만료됨 -> 로그아웃 처리
        localStorage.removeItem("accessToken");
        window.location.href = "/login";
      }
    }
    return Promise.reject(err);
  },
);

export default api;

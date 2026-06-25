"use client";
import Link from "next/link";
import { useState } from "react";
import api from "@/lib/axios";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // 서버에서 쿠키(Refresh)와 바디(Access)를 반환하는 구조
      const res = await api.post("/auth/login", { email, password });
      localStorage.setItem("accessToken", res.data.accessToken);
      alert("로그인 성공!");
      window.location.href = "/"; // 메인 페이지로 이동
    } catch (err) {
      alert("로그인 실패");
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <h1>로그인</h1>
      <input
        type="email"
        onChange={(e) => setEmail(e.target.value)}
        placeholder="이메일"
      />
      <input
        type="password"
        onChange={(e) => setPassword(e.target.value)}
        placeholder="비밀번호"
      />
      <button type="submit">로그인</button>

      <p>
        아직 회원이 아니신가요? <Link href="/register">회원가입하러 가기</Link>
      </p>
    </form>
  );
}

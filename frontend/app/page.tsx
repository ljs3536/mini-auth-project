"use client"; // 클라이언트 컴포넌트로 지정

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function MainPage() {
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      router.push("/login");
    }
  }, [router]);

  return (
    <main>
      <h1>메인 페이지입니다.</h1>
      {/* 여기에 버튼들 추가 */}
    </main>
  );
}

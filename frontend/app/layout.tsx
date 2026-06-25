"use client";
export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body>
        {/* 모든 페이지가 이 안에서 렌더링됩니다 */}
        {children}
      </body>
    </html>
  );
}

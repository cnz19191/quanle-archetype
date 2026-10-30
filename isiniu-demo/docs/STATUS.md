# isiniu-demo

模块化单体标杆。鉴权、问题、评论三个领域都走 Controller → Service → Repository → Mapper。

测试不连接 MySQL。鉴权使用 Spring Security，支持 LOCAL 和 SSO 两种模式，不再使用 `X-User-Id` 占位。

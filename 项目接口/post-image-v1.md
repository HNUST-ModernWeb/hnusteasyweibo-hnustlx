# 帖子图片接口（V1）

## 新增接口
- 方法：`POST`
- 路径：`/post/image`
- 鉴权请求头：`user_token`
- 请求类型：`multipart/form-data`
- 表单字段：`image`
- 返回：`data` 为图片 URL（例如：`/upload/post/abc123.jpg`）

## 已更新字段
- `POST /post/add`：请求体新增 `images: string[]`
- `PUT /post/update`：请求体新增可选字段 `images: string[]`
- `GET /post/list`、`GET /post/detail/{postId}`：返回字段新增 `images: string[]`

## 约束说明
- 单条动态最多 9 张图片
- 允许格式：`.jpg`、`.jpeg`、`.png`、`.webp`、`.gif`
- 图片 URL 必须以 `/upload/post/` 开头

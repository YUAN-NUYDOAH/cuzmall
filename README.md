# 浙传校园交易广场（CuzMall）

面向浙江传媒学院学生的校园二手交易与代课互助平台。支持商品发布浏览、关键词/分类检索、WebSocket 实时聊天、微信支付 Native 扫码，以及 Docker + GitHub Actions 自动化部署。

## 技术栈

- Java 21、Spring Boot 3.3
- Spring Data JPA、MySQL 8
- Spring Security（表单登录 + BCrypt）
- Thymeleaf、Spring WebSocket
- 微信支付 API v3、ZXing 二维码
- Docker、GitHub Actions、Nginx

## 功能概览

- **交易广场**：商品列表、搜索筛选、详情、发布（需登录）
- **代课互助**：代课帖发布与检索、详情页实时聊天
- **支付中心**：微信 Native 扫码支付、异步回调、订单状态轮询
- **账号体系**：注册、登录、发布信息与当前用户绑定

## 本地运行

### 方式一：H2 文件库（无需 MySQL，适合快速体验）

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

访问 http://localhost:8023 ，H2 控制台：http://localhost:8023/h2-console

### 方式二：MySQL（与生产一致）

启动数据库：

```bash
docker compose up -d
```

等待 MySQL 就绪后启动应用：

```bash
mvn spring-boot:run
```

默认连接本地 MySQL：

| 项 | 值 |
|----|-----|
| URL | `jdbc:mysql://localhost:3306/school_transaction_db_local` |
| 用户名 | `root` |
| 密码 | `123456` |

库不存在时可执行：`mysql -u root -p < deploy/init-local-db.sql`

也可通过环境变量 `DB_HOST`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 覆盖。

换端口示例：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8024"
```

## 测试

```bash
mvn test
```

测试使用内存 H2，无需外部 MySQL。

## 商品图片

发布商品时可上传 JPG / PNG / WEBP（最大 5MB），文件保存在 `data/uploads/products/`，通过 `/uploads/**` 访问。

环境变量 `UPLOAD_DIR` 可自定义存储目录；Docker 部署建议挂载卷：

```bash
docker run -d --name cuzmall -p 8023:8023 \
  -v cuzmall-uploads:/app/data/uploads \
  -e UPLOAD_DIR=/app/data/uploads \
  --restart always cuzmall
```

## 支付与商品状态

商品微信支付成功后，订单状态变为「已支付」，对应商品自动标记为「已售出」，详情页不再展示支付按钮。

## Docker 部署

```bash
docker build -t cuzmall .
docker run -d --name cuzmall -p 8023:8023 \
  -v cuzmall-uploads:/app/data/uploads \
  -e UPLOAD_DIR=/app/data/uploads \
  --restart always cuzmall
```

生产环境请配置 MySQL 连接环境变量，并设置 `THYMELEAF_CACHE=true`。

## 微信支付（可选）

通过环境变量配置：

| 变量 | 说明 |
|------|------|
| `WECHAT_PAY_APP_ID` | 应用 AppID |
| `WECHAT_PAY_MCH_ID` | 商户号 |
| `WECHAT_PAY_API_V3_KEY` | APIv3 密钥 |
| `WECHAT_PAY_MERCHANT_SERIAL_NUMBER` | 商户证书序列号 |
| `WECHAT_PAY_PRIVATE_KEY_PATH` | 商户私钥文件路径 |
| `WECHAT_PAY_NOTIFY_URL` | 支付回调 URL |

未配置时支付按钮自动禁用，其余功能可正常使用。

## HTTPS 正式上线

1. 域名 `cuzmall.cn` 解析到服务器公网 IP。
2. 首次申请证书前可先用 `deploy/nginx-cuzmall-http-only.conf`。
3. 在服务器项目目录执行（需先设置邮箱）：

```bash
export CERTBOT_EMAIL=your@email.com
bash deploy/https-setup.sh
```

4. 正式配置使用 `deploy/nginx-cuzmall.conf`：HTTP 自动跳转 HTTPS，并单独代理 WebSocket（`/ws/`）。

应用已开启 `server.forward-headers-strategy=framework`，Nginx 需设置 `X-Forwarded-Proto https`。

微信支付回调地址示例：`https://cuzmall.cn/payments/wechat/notify`

## 项目文档

简历与面试说明见 [docs/简历项目说明.md](docs/简历项目说明.md)。

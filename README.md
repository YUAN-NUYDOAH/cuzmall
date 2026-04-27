# 浙江传媒学院校园交易网站

一个使用 Java、Spring Boot、Thymeleaf 编写的校园二手交易网站原型。

## 已实现功能

- 校园交易首页
- 商品搜索和分类筛选
- 商品详情页
- 发布商品表单
- 表单校验
- 内存商品数据存储

## 本地运行

```bash
mvn spring-boot:run
```

启动后访问：

```text
http://localhost:8080
```

如果 8080 端口被占用，可以换端口：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

## Docker 部署

构建镜像：

```bash
docker build -t cuzmall .
```

启动容器：

```bash
docker run -d --name cuzmall -p 8080:8080 --restart always cuzmall
```

如果使用域名 `cuzmall.cn`，可以把 `deploy/nginx-cuzmall.conf` 放到服务器 Nginx 配置目录，并把域名解析到服务器公网 IP。

## 后续可扩展

- 接入 MySQL 持久化商品、用户和订单
- 增加登录注册和浙传学生身份校验
- 增加商品图片上传
- 增加收藏、私信、下架、订单状态流转
- 增加后台审核和举报处理

# 浙江传媒学院校园交易网站

一个使用 Java、Spring Boot、Thymeleaf 编写的校园二手交易网站原型。

## 已实现功能

- 校园交易首页
- 商品搜索和分类筛选
- 商品详情页
- 发布商品表单
- 表单校验
- 内存商品数据存储

## 运行方式

```bash
mvn spring-boot:run
```

启动后访问：

```text
http://localhost:8080
```

## 后续可扩展

- 接入 MySQL 持久化商品、用户和订单
- 增加登录注册和浙传学生身份校验
- 增加商品图片上传
- 增加收藏、私信、下架、订单状态流转
- 增加后台审核和举报处理

# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```


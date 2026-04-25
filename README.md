# 凹凸桌面 (AutoHub Launcher)

车机桌面启动器应用

## 构建

项目使用GitHub Actions自动构建APK。

### 手动构建

```bash
./gradlew assembleRelease
```

## 下载

从 [Releases](../../releases) 页面下载最新APK。

## GitHub Secrets配置

构建需要以下Secrets：

| Secret | 值 |
|--------|-----|
| SIGNING_KEY | keystore文件的base64编码 |
| KEY_STORE_PASSWORD | autohub123 |
| KEY_PASSWORD | autohub123 |

## 签名密钥

- 别名: autohub
- 密码: autohub123

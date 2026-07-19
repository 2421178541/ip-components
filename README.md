# ip-components

这是一个 Android 桌面小组件项目，用于在手机桌面上显示当前的国内和国外 IP 地址。

## 功能
- 在桌面添加小组件后，显示国内和国外 IP
- 点击刷新按钮可手动刷新当前 IP
- 通过网络请求获取公开 IP 地址

## 使用方法
1. 安装 Android Studio 并配置 Android SDK。
2. 打开项目后同步 Gradle。
3. 运行应用到 Android 手机或模拟器。
4. 长按桌面，选择“小组件”，添加“IP Widget”。

## 说明
- 国内 IP 使用 http://myip.ipip.net
- 国外 IP 使用 https://api.ip.sb/ip
- 如果网络请求失败，会显示“获取失败”
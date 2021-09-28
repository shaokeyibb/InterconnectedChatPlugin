# InterconnectedChat-BukkitPlugin

使用 Spring Boot 提供的 HTTP 后端跨 Minecraft 服务器共享聊天记录，这里是整个项目的 Bukkit 插件部分

[English](README.md) **简体中文**

## 特点

- 无需依赖 BungeeCord,Velocity 等代理端即可跨服共享聊天记录
- 部署方便，即开即用

## 编译

1. 运行 `gradle shadowjar`
2. 编译成功后，文件将会生成于 `build/libs/InterconnectedChatPlugin-1.0-SNAPSHOT-all.jar`

## 部署

- 将插件放入 `plugins` 文件夹中，运行服务器

## 配置

1. [部署和配置后端服务](https://github.com/shaokeyibb/InterconnectedChat/blob/master/README-CN.md)

2. 前往 `plugins/InterconnectedChatPlugin/config.yml`，该文件内容大致如下

```yaml
query_period: 20
last_index: -1
remote_server_address: "http://127.0.0.1:8080"
server_name: "server"
chat_format: "[%server%]%message%"
```

3. `query_period` 代表向后端服务查询聊天消息的间隔，单位 ticks
4. `last_index` 代表单次查询获得的聊天记录数，设置为 `-1` 代表获得全部聊天记录
5. `remote_server_address` 代表后端服务 IP 地址
6. `server_name` 代表本服务器名称
7. `chat_format` 代表在服务器中显示的聊天格式，`%server%` 代表服务器名称，`%message%` 代表完整的聊天信息。支持使用样式代码（&）

8. 配置完成后，重启服务器即可生效

## 一些不足

- 使用了明文，未加密的手段进行不安全的 HTTP 传输，因此可能导致数据泄露，请在使用时注意尽量不要进行广域网传输（除非您认为您的数据并不重要）

## 开源许可

本项目使用 [GPLv3](LICENSE) 许可协议授权
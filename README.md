# 基于 Spring AI + Ollama 的智能聊天模板

本项目是针对大语言模型对话的模板工程，基于 Spring AI + Ollama，提供智能聊天的实现方案，支持对话历史记忆，便于初学者接触场景。

## 环境准备

借由 Ollama 拉取本地模型（如 `deepseek-r1:8b`）。

## 聊天响应

阻塞式响应和流式响应。

## 对话记忆

通过对话 ID （`conversationId`）区分对话，目前仅支持内存存储对话历史。

## 启动流程

1. **AI 模型配置**：编辑 `application.properties` 文件，配置 AI 模型源参数

2. **客户端配置**：按需调整 `config` 中的配置（如系统提示词）

3. **项目启动**：运行 `Application.java` 主类，启动 Spring Boot 应用
4. **接口测试**：发起请求调用控制层接口，进行聊天

# 基于 Spring AI + Ollama 的轻量级智能对话模板

本项目是针对大语言模型对话场景的模板工程，基于 Spring AI + Ollama，提供**轻量级**智能对话的解决方案，支持**人格模拟对话、检索增强生成（RAG）、对话历史存储**，便于初学者快速搭建应用。

## 模型选择

借由 Ollama 拉取本地模型，包括对话模型及嵌入模型，示例如下：

```shell
ollama pull deepseek-r1:8b
ollama pull nomic-embed-text
```

## 数据存储

| 存储类型              | 主要用途                           |
| :-------------------- | :--------------------------------- |
| PostgreSQL + pgvector | 人格设定、文档向量、对话消息持久化 |
| Redis                 | 对话上下文缓存                     |

## 对话构建

用户消息由 【人格设定】+【文段记录】+【对话历史】+【当前问题】+【规则约束】 五部分组成，以提示词向模型发起对话。

### 人格设定

人格设定包含个性特点、语言风格、背景故事、关键词等信息，人为预设，数据示例如下：

```json
{
    "id": 1,
    "name": "应届生",
    "personality": "建设欲和求知欲驱动积极兴趣与可持续行为\n自我怀疑，需要暂停工作思考方向\n内向倾向，偏好深度关系而非广泛社交",
    "languageStyle": "内省式叙述，注重内心探索\n 倾向哲学化表达，常使用比喻和抽象概念\n口语化与书面语混合，包含网络用语",
    "background": "高考失败触发自我觉醒，反思人生选择\n长期逃避不确定性，亦不断探索工作与人际关系\n对社会风气持批判态度，认为存在功利化倾向",
    "keywords": ["建设欲", "求知欲", "独处", "抽象", "思维极化", "自我怀疑", "深度关系"]
}
```

| 数据主体 | 存储位置          | 实体类    |
| -------- | ----------------- | --------- |
| 人格设定 | 数据表 `personas` | `Persona` |

### 文段记录

借助相应服务上传外部文档，支持 TXT、MD、PDF 等多种格式。

文本分割器，默认采用递归正则文本分割器 `RecursiveRegexTextSplitter`，按以下分隔符（正则表达式）优先级递归切割，直至块大小满足要求或分隔符用完。

```java
new Pattern[]{
        Pattern.compile("\n```[\\s\\S]*?```\n"),  // 代码块
        Pattern.compile("\n#{1,6} "),  // 标题
        Pattern.compile("\n\n"),  // 空行
        Pattern.compile("\r\n"),  // 空行
        Pattern.compile("\n"),  // 换行
        Pattern.compile("\r"),  // 换行
        Pattern.compile("[。！？.!?]"),  // 句子结束
        Pattern.compile("[，,]"),  // 逗号
        Pattern.compile("\\s")  // 空格
});
```

文本分割器，可根据需求在配置类修改，转为固定 token 文本分割器 `TokenTextSplitter`（框架提供）。

后续文本块经嵌入模型向量化后，借由 `PgVectorStore` （框架提供）存储至数据表（根据配置自动创建，注意向量维度一致）。

| 数据主体 | 存储位置              | 框架模型   |
| -------- | --------------------- | ---------- |
| 文段记录 | 数据表 `vector_store` | `Document` |

文段记录，可根据需求在配置类修改，借由 `SimpleVectorStore` （框架提供）转为存储于本地文件。

### 对话记忆

已取消 `ChatClient` 接收参数 `MessageChatMemoryAdvisor` （二者框架提供）附带的自动记忆对话功能，而在相应服务实现，由 `CacheService` 缓存近期对话，`PersistentService` 持久化所有对话。

| 数据主体   | 存储位置                               | 实体类              |
| ---------- | -------------------------------------- | ------------------- |
| 对话上下文 | 缓存键 `conversation:{conversationId}` | `List<ChatMessage>` |
| 对话消息   | 数据表 `chat_messages`                 | `ChatMessage`       |

对话上下文，优先查询缓存，若无缓存命中，则查询数据库，将所查对话消息拼凑成上下文，并同步至缓存。

对话上下文，可根据需求在配置类修改，借由 `InMemoryChatMemory` （框架提供）转为直接存储于内存。

### 规则约束

提示词示例如下：

```java
// 人格化约束
String rulePrompt1 = """
    1. 请以第一人称口吻，用自然、亲切的语气回答。
    2. 必须基于提供的人格设定和文字记录（体会语气、了解经历、理解观点）来回答。
    3. 如果记忆中没有相关信息，请坦诚地说：“根据我的记忆，我不太确定这件事”。
    4. 可以合理推断，但不要编造明确未提及的事实。
""";

// 助手向约束
String rulePrompt2 = """
    1. 请保持客观中立的态度，给出简洁、准确、专业的回答。
    2. 必须基于提供的文档内容，提取相关信息归纳、整理、分析。
    3. 如果文档中没有相关信息，请说：“根据现有文档，没有找到相关信息”。
    4. 基于整理的信息，不要编造、假设或猜测信息。
""";
```



## 对话交互

对话请求，以 `ChatRequest` 作为 DTO 传递参数，其中仅有 `userInput` 字段为必须，其余字段均有默认值，示例如下：

```json
{
    // 对话 ID
    "conversationId": "550e8400-e29b-41d4-a716-446655440000",
    // 用户输入
    "userInput": "你是谁？",
    // 功能开关
    "enablePersona": true,
    "enableRag": true,
    "enableContext": true,
    // 人格设定参数
    "personaId": 1,
    // 文档 RAG 参数
    "topK": 10,
    "threshold": 0.7,
    // 对话上下文参数
    "lastN": 10,
    // 规则类型
    "ruleType": "PERSONA"
}
```

对话响应，分为阻塞式响应与流式响应，按需选择接口。

## 启动流程

1. **AI 模型启动**：启动 Ollama
2. **客户端配置**：编辑 `application.properties` 文件，配置相应参数，并按需调整 `config` 中的配置
3. **数据源准备**：需安装 PostgreSQL 数据库向量扩展，执行 SQL 脚本，创建数据表
4. **项目启动**：运行 `Application.java` 主类，启动 Spring Boot 应用
5. **接口调用**：发起请求调用控制层接口，执行相关操作

## 相关脚本

脚本位于 `scripts/` 目录

- 数据表结构定义：`schema/`
- 初始数据参考：`seeding/`

## 参考资料

- Spring AI 文档：https://spring.io/projects/spring-ai
- Spring AI Alibaba 文档：https://java2ai.com/docs/overview/
- Ollama 模型库：https://ollama.com/library
- 《PersonaAI：利用检索增强生成和个性化上下文打造人工智能驱动的数字虚拟形象》：https://arxiv.org/html/2503.15489v1

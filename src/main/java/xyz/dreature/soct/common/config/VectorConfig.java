package xyz.dreature.soct.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import xyz.dreature.soct.common.util.RecursiveRegexTextSplitter;

// 向量配置
@Slf4j
@Configuration
public class VectorConfig {
    // 向量本地存储路径
    @Value("${app.vector.store.path}")
    private String vectorStorePath;

    // 本地文件向量存储
//    @Bean
//    public VectorStore localVectorStore(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
//        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
//
//        File jsonFile = new File(vectorStorePath);
//
//        if (jsonFile.exists()) {
//            vectorStore.load(jsonFile);
//        }
//
//        // 注册关闭钩子自动保存
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            vectorStore.save(jsonFile);
//            log.debug("向量已保存至：{}", vectorStorePath);
//        }));
//
//        return vectorStore;
//    }

    // 迭代字符文本分割器
    @Bean
    @Primary
    public RecursiveRegexTextSplitter recursiveCharacterTextSplitter() {
        // 按自定义分隔符列表递归切割，优先按段落、句子分
        return new RecursiveRegexTextSplitter();
    }

    // 固定 token 文本分割器
    @Bean
    @Lazy
    public TokenTextSplitter tokenTextSplitter() {
        // 按 Token 数切割，硬编码英文分隔符
        return new TokenTextSplitter(
                1000,    // chunkSize: 块的 token 数
                200,     // minChunkSizeChars: 块最小字符数（避免过小的块）
                50,      // minChunkLengthToEmbed: 块嵌入所需最小字符数（过滤太短的块）
                1000,    // maxNumChunks: 最多生成块数（防止文档太大）
                false    // keepSeparator: 是否保留保留分隔符
        );
    }
}

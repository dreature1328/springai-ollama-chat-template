package xyz.dreature.soct.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 向量服务
@Slf4j
@Service
public class VectorService {
    // 向量存储
    private final VectorStore vectorStore;
    // 文本分割器
    private final TextSplitter textSplitter;
    // 向量存储路径
    @Value("${app.ai.vector.store.path}")
    private String vectorStorePath;

    @Autowired
    public VectorService(VectorStore vectorStore, TextSplitter textSplitter) {
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
    }

    // 向量相似度检索
    public List<Document> similaritySearch(String userInput, int topK, double threshold) {
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userInput) // 用户输入
                        .topK(topK) // 返回向量数量
                        .similarityThreshold(threshold) // 相似度门槛
                        .build()
        );

        log.info("向量检索成功，匹配数量：{}", relevantDocs.size());
        return relevantDocs;
    }

    // 向量化并存储（多份文档）
    public List<Document> ingestBatch(List<MultipartFile> files) {
        return files.parallelStream()
                .map(this::ingest)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    // 向量化并存储（单份文档）
    public List<Document> ingest(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        try {
            // 1. 直接读取文件内容
            String content = read(file);
            // 2. 创建文档
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filename", originalFilename);
            metadata.put("filesize", file.getSize());
            metadata.put("contentType", file.getContentType());
            metadata.put("timestamp", System.currentTimeMillis());

            Document document = new Document(content, metadata);

            // 3. 分割文本
            List<Document> chunks = textSplitter.split(document);

            // 4. 存入向量数据库
            vectorStore.add(chunks);
            if (vectorStore instanceof SimpleVectorStore) {
                ((SimpleVectorStore) vectorStore).save(new File(vectorStorePath));
            }

            return chunks;
        } catch (Exception e) {
            throw new RuntimeException("文档处理失败", e);
        }
    }

    // 向量化并存储（纯文本）
    public List<Document> ingest(String text, String title, Map<String, Object> metadata) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("title", title != null ? title : "未命名文档");
        metadata.put("source", "text_input");
        metadata.put("timestamp", System.currentTimeMillis());

        Document document = new Document(text, metadata);
        List<Document> chunks = textSplitter.split(document);
        vectorStore.add(chunks);

        log.info("文档 {} 分割成功，文本块块数：{}", chunks.size());
        return chunks;
    }

    // 读取文件内容
    private String read(MultipartFile file) throws IOException {
        String extension = FileNameUtil.extName(file.getOriginalFilename());

        switch (extension) {
            case "txt":
            case "md":
            case "html":
            case "htm":
            case "json":
            case "xml":
            case "csv":
            case "log":
                return IoUtil.readUtf8(file.getInputStream());
            case "pdf":
                return readPdf(file);
            default:
                try {
                    return IoUtil.readUtf8(file.getInputStream());
                } catch (Exception e) {
                    throw new IOException("不支持的文件格式: " + extension);
                }
        }
    }

    // 读取 PDF 内容
    private String readPdf(MultipartFile file) {
        try {
            // 1. （可选）PDF 阅读器配置
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    // .withMaxWordsPerPage(500) // 每页最大字数限制
                    // .withPageTopMargin(20)    // 页面顶部边距（像素）
                    // .withPageBottomMargin(20) // 页面底部边距
                    .build();

            // 2. 根据需求选择合适的 PDF 阅读器
            // PagePdfDocumentReader，按页面提取，保持页面布局
            // ParagraphPdfDocumentReader，按逻辑段落提取，适合长文档
            Resource pdfResource = file.getResource();
            ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(pdfResource, config);

            // 3. 读取 PDF 并提取文档列表（每个Document通常对应一页或一段）
            List<Document> pdfDocuments = pdfReader.read(); // 或 pdfReader.get()[citation:3]

            // 4. 将所有文档内容合并为一个字符串
            StringBuilder contentBuilder = new StringBuilder();
            for (Document doc : pdfDocuments) {
                contentBuilder.append(doc.getText().strip()).append("\n\n");
                // 可选：保留元数据，如页码
                // Map<String, Object> metadata = doc.getMetadata();
                // metadata.put("originalFilename", file.getOriginalFilename());
            }

            // 记录处理信息
            log.info("PDF文件 '{}' 解析成功，共提取 {} 页/段文本",
                    file.getOriginalFilename(), pdfDocuments.size());

            return contentBuilder.toString();

        } catch (Exception e) {
            throw new RuntimeException("PDF 文件处理失败: ", e);
        }
    }

    // 清空向量存储
    public void clear() {
        try {
            Path path = Paths.get(vectorStorePath);
            Files.write(path, "{}".getBytes(StandardCharsets.UTF_8));
            // 重新加载
            if (vectorStore instanceof SimpleVectorStore) {
                ((SimpleVectorStore) vectorStore).load(path.toFile());
            }
        } catch (IOException e) {
            throw new RuntimeException("向量存储读取失败", e);
        }
    }
}

package xyz.dreature.soct.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xyz.dreature.soct.common.model.vo.Result;
import xyz.dreature.soct.service.VectorService;

import javax.validation.constraints.NotNull;
import java.util.List;

// 操作接口（向量）
@Slf4j
@RestController
@RequestMapping("/vector")
public class VectorController {
    @Autowired
    VectorService vectorService;

    @RequestMapping("/ingest")
    public Result<List<Document>> ingest(@NotNull MultipartFile file) {
        List<Document> result = vectorService.ingest(file);
        int resultCount = result.size();
        String message = String.format("分隔 %d 块文本", resultCount);
        log.info("文档分割完成，文本块块数：{}", resultCount);
        return Result.success(message, result);
    }

    @RequestMapping("/ingest-batch")
    public Result<List<Document>> ingestBatch(@NotNull List<MultipartFile> files) {
        List<Document> result = vectorService.ingestBatch(files);
        int resultCount = result.size();
        String message = String.format("分隔 %d 块文本", resultCount);
        log.info("文档分割完成，文本块块数：{}", resultCount);
        return Result.success(message, result);
    }

    @RequestMapping("/clear")
    public Result<Void> clear() {
        vectorService.clear();
        String message = "向量存储已清空";
        log.info("向量存储已清空");
        return Result.success(message, null);
    }
}

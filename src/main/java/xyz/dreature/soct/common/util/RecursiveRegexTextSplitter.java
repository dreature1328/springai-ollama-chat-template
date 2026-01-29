package xyz.dreature.soct.common.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

// 递归正则文本分割器
public class RecursiveRegexTextSplitter extends TextSplitter {
    private final int chunkSize;
    private final Pattern[] separators;

    public RecursiveRegexTextSplitter() {
        this(1024);
    }

    public RecursiveRegexTextSplitter(int chunkSize) {
        this(chunkSize, null);
    }

    public RecursiveRegexTextSplitter(int chunkSize, Pattern[] separators) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        } else {
            this.chunkSize = chunkSize;
            this.separators = Objects.requireNonNullElse(separators, new Pattern[]{
                    Pattern.compile("\n```[\\s\\S]*?```\n"),  // 代码块
                    Pattern.compile("\n#{1,6} "),  // 标题
                    Pattern.compile("\n\n"),  // 空行
                    Pattern.compile("\r\n"),  // 空行
                    Pattern.compile("\n"),  // 换行
                    Pattern.compile("\r"),  // 换行
                    Pattern.compile(">"),  // 引用
                    Pattern.compile("[。！？.!?]"),  // 句子结束
                    Pattern.compile("[，,]"),  // 逗号
                    Pattern.compile("\\s")  // 空格
            });
        }
    }

    public List<String> splitText(String text) {
        List<String> chunks = new ArrayList();
        this.splitText(text, 0, chunks);
        return chunks;
    }

    // 维护分隔符列表，递归依次使用分隔符进行分隔，直至块大小满足要求或分隔符用完
    private void splitText(String text, int separatorIndex, List<String> chunks) {
        if (!text.isEmpty()) {
            if (text.length() <= this.chunkSize && !StrUtil.isBlank(text)) {
                chunks.add(text);
            } else if (separatorIndex >= this.separators.length) {
                for (int i = 0; i < text.length(); i += this.chunkSize) {
                    int end = Math.min(i + this.chunkSize, text.length());
                    chunks.add(text.substring(i, end));
                }

            } else {
                Pattern pattern = this.separators[separatorIndex];
                String[] splits = pattern.split(text);

                for (String split : splits) {
                    if (!StrUtil.isBlank(split)) {
                        if (split.length() > this.chunkSize) {
                            this.splitText(split, separatorIndex + 1, chunks);
                        } else {
                            chunks.add(split);
                        }
                    }
                }
            }
        }
    }
}


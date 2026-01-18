package xyz.dreature.soct.common.util;

import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// 递归字符文本分割器（取自 Spring AI Alibaba 1.1.0 源码）
public class RecursiveCharacterTextSplitter extends TextSplitter {
    private final int chunkSize;
    private final String[] separators;

    public RecursiveCharacterTextSplitter() {
        this(1024);
    }

    public RecursiveCharacterTextSplitter(int chunkSize) {
        this(chunkSize, null);
    }

    public RecursiveCharacterTextSplitter(int chunkSize, String[] separators) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        } else {
            this.chunkSize = chunkSize;
            this.separators = Objects.requireNonNullElse(separators, new String[]{"\n\n", "\n", "。", "！", "？", "；", "，", " "});
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
            if (text.length() <= this.chunkSize) {
                chunks.add(text);
            } else if (separatorIndex >= this.separators.length) {
                for (int i = 0; i < text.length(); i += this.chunkSize) {
                    int end = Math.min(i + this.chunkSize, text.length());
                    chunks.add(text.substring(i, end));
                }

            } else {
                String separator = this.separators[separatorIndex];
                String[] splits;
                if (separator.isEmpty()) {
                    splits = new String[text.length()];

                    for (int i = 0; i < text.length(); ++i) {
                        splits[i] = String.valueOf(text.charAt(i));
                    }
                } else {
                    splits = text.split(separator);
                }

                String[] var12 = splits;
                int var7 = splits.length;

                for (int var8 = 0; var8 < var7; ++var8) {
                    String split = var12[var8];
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


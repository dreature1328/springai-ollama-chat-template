package xyz.dreature.soct.common.model.entity;

import jakarta.validation.constraints.Positive;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

// 人格实体
public class Persona {
    // ===== 字段 =====
    @NotNull(message = "ID 不能为空")
    @Positive(message = "ID 必须为正")
    private Long id;                // 唯一标识符
    @NotNull(message = "人格名称不能为空")
    private String name;            // 人格名称
    private String personality;     // 个性特点
    private String languageStyle;   // 语言风格
    private String background;      // 背景故事
    private String[] keywords;      // 关键词

    // ===== 构造方法 =====
    // 无参构造器
    public Persona() {
    }

    // 全参构造器
    public Persona(Long id, String name, String personality, String languageStyle, String background, String[] keywords) {
        this.id = id;
        this.name = name;
        this.personality = personality;
        this.languageStyle = languageStyle;
        this.background = background;
        this.keywords = keywords;
    }

    // 复制构造器
    public Persona(Persona persona) {
        this.id = persona.getId();
        this.name = persona.getName();
        this.personality = persona.getPersonality();
        this.languageStyle = persona.getLanguageStyle();
        this.background = persona.getBackground();
        this.keywords = persona.getKeywords();
    }

    // ===== Getter 与 Setter 方法 =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getLanguageStyle() {
        return languageStyle;
    }

    public void setLanguageStyle(String languageStyle) {
        this.languageStyle = languageStyle;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    // ===== 其他 =====
    // 字符串表示
    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", personality='" + personality + '\'' +
                ", languageStyle='" + languageStyle + '\'' +
                ", background='" + background + '\'' +
                ", keywords=" + Arrays.toString(keywords) +
                '}';
    }
}

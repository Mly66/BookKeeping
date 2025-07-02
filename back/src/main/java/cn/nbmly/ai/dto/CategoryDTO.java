package cn.nbmly.ai.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String type;
    private Long userId;

    @Override
    public String toString() {
        String typeText = "expense".equals(type) ? "支出" : "收入";
        return String.format("分类ID: %d | 名称: %s | 类型: %s", id, name, typeText);
    }
}
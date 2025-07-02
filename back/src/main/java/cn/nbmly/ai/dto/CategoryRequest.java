package cn.nbmly.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotBlank(message = "分类类型不能为空")
    private String type; // "expense" or "income"
}

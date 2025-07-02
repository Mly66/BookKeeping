package cn.nbmly.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class BillDTO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName; // For convenience
    private BigDecimal amount;
    private String type;
    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime billTime;

    @Override
    public String toString() {
        String typeText = "expense".equals(type) ? "支出" : "收入";
        String timeStr = billTime != null ? billTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "未知时间";
        String remarksStr = remarks != null && !remarks.trim().isEmpty() ? " (" + remarks + ")" : "";

        return String.format("账单ID: %d | %s | 金额: %.2f元 | 分类: %s | 时间: %s%s",
                id, typeText, amount, categoryName != null ? categoryName : "未知分类", timeStr, remarksStr);
    }
}
package com.devloom.ai.toolbox.investment.infrastructure.quality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据校验结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /**
     * 错误列表 (数据不可用)
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * 警告列表 (数据可用但需要关注)
     */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /**
     * 数据质量评分
     */
    private com.devloom.ai.toolbox.investment.domain.enums.DataQuality quality;

    /**
     * 是否有效
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * 添加错误
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * 添加警告
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    /**
     * 合并另一个校验结果
     */
    public void merge(ValidationResult other) {
        if (other != null) {
            this.errors.addAll(other.getErrors());
            this.warnings.addAll(other.getWarnings());
        }
    }

    /**
     * 获取摘要信息
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (!errors.isEmpty()) {
            sb.append(String.format("Errors(%d): %s; ", errors.size(), String.join("; ", errors)));
        }
        if (!warnings.isEmpty()) {
            sb.append(String.format("Warnings(%d): %s", warnings.size(), String.join("; ", warnings)));
        }
        return sb.toString().trim();
    }
}

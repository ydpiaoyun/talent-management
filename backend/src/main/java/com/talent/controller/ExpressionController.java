package com.talent.controller;

import com.talent.common.ExpressionEvaluator;
import com.talent.common.R;
import com.talent.entity.TalentAttribute;
import com.talent.mapper.TalentAttributeMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表达式管理接口
 * <p>
 * 提供评分公式的语法校验和变量合法性校验。
 * </p>
 *
 * @author talent-hr
 */
@RestController
@RequestMapping("/api/expression")
public class ExpressionController {

    private final TalentAttributeMapper attrMapper;

    /**
     * 构造方法
     *
     * @param attrMapper 指标 Mapper
     */
    public ExpressionController(TalentAttributeMapper attrMapper) {
        this.attrMapper = attrMapper;
    }

    /**
     * 校验公式是否合法
     * <p>
     * 校验步骤：1. 基础语法校验 2. 变量合法性校验（变量必须为已存在的指标 code）
     * </p>
     *
     * @param body 请求体（expression: 公式字符串）
     * @return 校验结果（valid: 是否合法, message: 提示信息）
     */
    @PostMapping("/validate")
    public R<Map<String, Object>> validate(@RequestBody Map<String, String> body) {
        String expr = body.get("expression");
        Map<String, Object> result = new HashMap<>();

        if (expr == null || expr.isBlank()) {
            result.put("valid", false);
            result.put("message", "公式不能为空");
            return R.ok(result);
        }

        // 1. 基础语法校验
        boolean syntaxValid = ExpressionEvaluator.validate(expr);
        if (!syntaxValid) {
            result.put("valid", false);
            result.put("message", "公式语法错误，请检查括号、运算符是否匹配");
            return R.ok(result);
        }

        // 2. 校验公式中使用的变量是否都是已存在的指标 code
        try {
            Set<String> usedVars = extractVars(expr);
            List<TalentAttribute> attrs = attrMapper.selectList(null);
            Set<String> validCodes = attrs.stream()
                    .map(TalentAttribute::getCode)
                    .collect(Collectors.toSet());

            List<String> invalidVars = usedVars.stream()
                    .filter(v -> !validCodes.contains(v))
                    .toList();

            if (!invalidVars.isEmpty()) {
                result.put("valid", false);
                result.put("message", "公式包含不存在的指标变量：" + String.join(", ", invalidVars));
                return R.ok(result);
            }
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "变量校验失败：" + e.getMessage());
            return R.ok(result);
        }

        result.put("valid", true);
        result.put("message", "公式校验通过");
        return R.ok(result);
    }

    /**
     * 提取 Aviator 表达式中的变量名
     * <p>
     * 匹配规则：字母开头，后接字母、数字、下划线，排除 Aviator 内置常量。
     * </p>
     *
     * @param expr 表达式字符串
     * @return 变量名集合
     */
    private Set<String> extractVars(String expr) {
        return java.util.regex.Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b")
                .matcher(expr)
                .results()
                .map(m -> m.group(1))
                // 排除 Aviator 内置常量
                .filter(v -> !Set.of("true", "false", "nil", "INF", "NaN").contains(v))
                .collect(Collectors.toSet());
    }
}

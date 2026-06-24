package com.talent.common;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aviator 表达式求值器
 * <p>
 * 线程安全的编译缓存 + 变量注入，用于评分公式的动态计算。
 * </p>
 *
 * @author talent-hr
 */
public class ExpressionEvaluator {

    private static final AviatorEvaluatorInstance ENGINE = AviatorEvaluator.newInstance();

    static {
        // 启用编译优化
        ENGINE.setOption(Options.OPTIMIZE_LEVEL, AviatorEvaluator.EVAL);
    }

    /** 表达式编译缓存 */
    private static final ConcurrentHashMap<String, Expression> CACHE = new ConcurrentHashMap<>();

    private ExpressionEvaluator() {
        // 工具类，禁止实例化
    }

    /**
     * 编译（或从缓存获取）表达式
     *
     * @param expr 表达式字符串
     * @return 编译后的 Expression 对象，空表达式返回 null
     * @throws IllegalArgumentException 表达式语法错误时抛出
     */
    public static Expression compile(String expr) {
        if (expr == null || expr.isBlank()) {
            return null;
        }
        return CACHE.computeIfAbsent(expr.strip(), key -> {
            try {
                return ENGINE.compile(key, true);
            } catch (Exception e) {
                throw new IllegalArgumentException("表达式语法错误: " + key, e);
            }
        });
    }

    /**
     * 执行表达式求值
     *
     * @param expr 表达式字符串
     * @param vars 变量 Map
     * @return 计算结果（double），表达式为空返回 0
     */
    public static double eval(String expr, Map<String, Object> vars) {
        Expression compiled = compile(expr);
        if (compiled == null) {
            return 0;
        }
        Object result = compiled.execute(vars);
        if (result instanceof Number n) {
            return n.doubleValue();
        }
        return 0;
    }

    /**
     * 验证表达式是否合法
     *
     * @param expr 表达式字符串
     * @return true 合法，false 不合法
     */
    public static boolean validate(String expr) {
        try {
            ENGINE.validate(expr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 格式化错误信息
     *
     * @param expr 表达式字符串
     * @param e    异常对象
     * @return 格式化后的错误信息
     */
    public static String formatError(String expr, Exception e) {
        return String.format("表达式 [%s] 求值失败: %s", expr, e.getMessage());
    }
}

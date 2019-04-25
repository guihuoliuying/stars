package com.stars.core.expr;

/**
 * 表达式工具类使用指南
 * <p>
 * 动机：
 * 1. 提供统一的可配置的条件判断
 * 2. 提供统一的可配置的失败提示信息
 * <p>
 * 数据类型
 * 1. 长整形
 * 2. 字符串
 * 3. 布尔值 - 转换成长整形：0->假；非0->真
 * <p>
 * 扩展
 * 1. 单值 - 返回长整形
 * A. 扩展：继承ExprValue
 * 2. 数据集 - 返回长整形，表示数据集中满足所有条件的元素个数
 * A. 格式：[dataSet, condition1, condition2 ...]
 * B. 条件：支持关系运算
 * 3. 函数 - 支持特殊条件
 * A. 格式：{functionName, parameter1, parameter2 ...}
 * B. 返回：所有数据类型
 * C. 扩展：继承ExprFunc
 * <p>
 * 操作符和优先级（从低到高）
 * 1. 或：or
 * 2. 与：and
 * 3. 非：not
 * 4. 关系：==, !=, >, >=, <, <=, in, between
 * 5. 加减：+, -
 * 6. 乘除：*, /, %
 * 7. 指数：^
 * 8. 数字，标识符，()，[]，{}:
 * A. () - 修改优先级
 * B. [] - 简略的select count(1) from ... where ...
 * C. {} - 函数调用
 * <p>
 *     提示说明
 *
 * <p>
 * 使用方式
 * 1. 对条件进行扩展，继承ExprValue，ExprDataSet，ExprFunc
 * 2. 在ExprConfig实例中注册这些扩展条件
 * 3. 对条件表达式进行解析
 * <code>ExprNode expr = new ExprParser(new ExprLexer("1 + 1 == 2"), config).parse()</code>
 * 4. 对条件表达式进行求值
 * <code>ExprUtil.isTrue(expr)</code>
 *
 * 想法：
 * 1. 增加双精度，编译时类型检查，类型转换
 * 2. 增加提示
 * 3. 执行时性能优化（ASM和javassist）
 * 4. 精准测试
 */
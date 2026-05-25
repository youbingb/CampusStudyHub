package com.csh.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记管理端写操作方法，由 {@link OperationLogAspect} 切面在方法成功返回后写入 operation_log。
 *
 * <p>切面会自动捕获：当前登录用户、客户端 IP 与 User-Agent、controller 调用时间。
 * 业务方填的 {@code module} / {@code action} 用于查询过滤；{@code targetIdSpEL} 用 Spring EL
 * 从方法参数（{@code #id}）或返回值（{@code #result}）中提取目标主键，例如：
 * <pre>
 * &#064;OperationLog(module="举报", action="处理", targetIdSpEL="#id")
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    String module();

    String action();

    String targetIdSpEL() default "";
}

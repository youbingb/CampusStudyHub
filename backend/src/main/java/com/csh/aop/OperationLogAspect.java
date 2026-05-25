package com.csh.aop;

import com.csh.modules.system.entity.OperationLog;
import com.csh.modules.system.mapper.OperationLogMapper;
import com.csh.security.LoginUser;
import com.csh.security.LoginUserHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ExpressionParser parser = new SpelExpressionParser();

    @AfterReturning(pointcut = "@annotation(com.csh.aop.OperationLog)", returning = "result")
    public void after(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            com.csh.aop.OperationLog annotation = method.getAnnotation(com.csh.aop.OperationLog.class);
            if (annotation == null) return;

            OperationLog row = new OperationLog();
            row.setModule(annotation.module());
            row.setAction(annotation.action());

            LoginUser u = LoginUserHolder.current();
            if (u != null) {
                row.setUserId(u.getId());
                row.setUsername(u.getUsername());
            }

            HttpServletRequest req = currentRequest();
            if (req != null) {
                row.setIp(clientIp(req));
                String ua = req.getHeader("User-Agent");
                if (ua != null && ua.length() > 255) ua = ua.substring(0, 255);
                row.setUa(ua);
            }

            String spel = annotation.targetIdSpEL();
            if (spel != null && !spel.isBlank()) {
                String targetId = evalSpEL(spel, signature, joinPoint.getArgs(), result);
                if (targetId != null && targetId.length() > 64) targetId = targetId.substring(0, 64);
                row.setTargetId(targetId);
            }

            operationLogMapper.insert(row);
        } catch (Exception e) {
            log.warn("操作日志记录失败 method={}", joinPoint.getSignature(), e);
        }
    }

    private String evalSpEL(String spel, MethodSignature sig, Object[] args, Object result) {
        try {
            EvaluationContext ctx = new StandardEvaluationContext();
            String[] names = sig.getParameterNames();
            if (names != null) {
                for (int i = 0; i < names.length && i < args.length; i++) {
                    ctx.setVariable(names[i], args[i]);
                }
            }
            ctx.setVariable("result", result);
            Expression exp = parser.parseExpression(spel);
            Object v = exp.getValue(ctx);
            return v == null ? null : v.toString();
        } catch (Exception e) {
            log.debug("SpEL 解析失败: {}", spel, e);
            return null;
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        String real = req.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) return real;
        return req.getRemoteAddr();
    }
}

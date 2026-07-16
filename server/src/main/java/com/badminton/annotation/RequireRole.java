package com.badminton.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限校验注解
 * 标注在 Controller 方法上，限制只有指定角色才能访问
 * 示例：@RequireRole("ADMIN") 或 @RequireRole({"ADMIN", "COACH"})
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 允许访问的角色列表
     */
    String[] value();
}

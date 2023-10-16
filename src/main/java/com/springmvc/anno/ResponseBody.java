package com.springmvc.anno;

import java.lang.annotation.*;

/**
 * @BelongsProject: SpringIoc-bruceliu
 * @BelongsPackage: org.springframework.mvc.annotation
 * @CreateTime: 2020-10-15 10:26
 * @Description: TODO
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}

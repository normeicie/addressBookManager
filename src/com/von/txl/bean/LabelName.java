package com.von.txl.bean;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Retention(RUNTIME)
@Target({FIELD})
public @interface LabelName {
    String value() default "";

    boolean isNull() default true;

    int orderIndex() default 0;

    String checkRule() default "";

    String checkRuleDesc() default "";
}

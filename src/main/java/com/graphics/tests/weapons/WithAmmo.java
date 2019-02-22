package com.graphics.tests.weapons;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface WithAmmo {
    int ticks() default 0;
    int max() default 10;
}

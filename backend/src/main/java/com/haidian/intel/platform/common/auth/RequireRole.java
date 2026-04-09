package com.haidian.intel.platform.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint that requires one of the specified role codes.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * Accepted role codes.
     *
     * @return role code list
     */
    String[] value();
}

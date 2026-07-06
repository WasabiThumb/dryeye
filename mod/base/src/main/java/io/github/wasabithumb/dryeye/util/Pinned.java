package io.github.wasabithumb.dryeye.util;

import java.lang.annotation.*;

/**
 * Marks a member as "pinned", meaning that it is
 * referred to via reflection or some other non-obvious
 * mechanism and cannot be immediately removed.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Pinned {
    String reason() default "";
}

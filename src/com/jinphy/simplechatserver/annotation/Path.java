package com.jinphy.simplechatserver.annotation;

import java.lang.annotation.*;

/**
 * Created by jinphy on 2017/12/5.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Path {
    String path();
}

package com.jfinal.ext.plugin.modelcallback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jfinal.plugin.activerecord.Model;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ModelCallback {
    Class<? extends Model<?>> modelClass();//规则 
}

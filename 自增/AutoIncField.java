package com.sucsoft.digital.model.po.mongo.AutoIncrease;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author 陈建
 * @Date 2020/9/23 15:44
 * Description 用于标识需要自动增长的字段
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoIncField {

}

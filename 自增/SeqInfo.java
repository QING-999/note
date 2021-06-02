package com.sucsoft.digital.model.po.mongo.AutoIncrease;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @Author 陈建
 * @Date 2020/9/23 15:39
 * Description 用于Mongo自增
 */
@Document("sequence")
public class SeqInfo {
    /**
     * 主键
     */
    @Id
    private String id;

    /**
     * 需要自增加的集合名
     */
    @Field
    private String name;

    /**
     * 序列值
     */
    @Field
    private Long seqValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCellName() {
        return name;
    }

    public void setCellName(String cellName) {
        this.name = cellName;
    }

    public Long getSeqValue() {
        return seqValue;
    }

    public void setSeqValue(Long seqValue) {
        this.seqValue = seqValue;
    }
}

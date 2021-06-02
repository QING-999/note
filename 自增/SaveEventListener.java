package com.sucsoft.digital.model.po.mongo.AutoIncrease;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @Author 陈建
 * @Date 2020/9/23 15:55
 * Description 自定义监听,监听save方法,在保存的时候自动生成需要自增的id
 */
@Component
public class SaveEventListener extends AbstractMongoEventListener<Object> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        Object source = event.getSource();

        if (source != null) {
            ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    ReflectionUtils.makeAccessible(field);
                    //若字段添加了自定义注解AutoIncField
                    if (field.isAnnotationPresent(AutoIncField.class)) {
                        field.set(source, getNextValue(source.getClass().getSimpleName()));
                    }
                }
            });
        }
    }

    private Long getNextValue(String cellname) {
        Query query = new Query(Criteria.where("name").is(cellname));
        Update update = new Update();
        update.inc("seqValue", 1);

        //进行原子性操作
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        SeqInfo seqInfo = mongoTemplate.findAndModify(query, update, options, SeqInfo.class);

        return seqInfo.getSeqValue();

    }
}

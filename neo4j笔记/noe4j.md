## 根据id删除

```CQL
match(a:book) where id(a)=1 delete a
```

其中book为标签名，a随意。




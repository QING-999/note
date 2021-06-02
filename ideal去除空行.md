去除@TableField

```java
@TableField.*
```

将两个空行变为一个

```
((^\s*\n){2})
替换为
\n
```



删除)后面的一个空行

```
(\)\n)
)
```



![image-20200917130510801](ideal去除空行.assets/image-20200917130510801.png)
[TOC]



 

# Search API

分为两种

- URI Search 。在URL中使用查询参数 

  ![image-20200929225245594](.assets/image-20200929225245594.png) 

-  Request Body Search 使用Elasticsearch提供的，基于JSON格式的更加完备的 Query Domain Specific Language(DSL)

![image-20200929225330496](.assets/image-20200929225330496.png)

**响应**
![image-20200929230307585](.assets/image-20200929230307585.png)



**match和term**



### 



## 相关性

**衡量相关性**

- Information Retrieve
  - Precision（查准率）-尽可能返回较少的无关文档
  - Recall（查全率） - 尽可能返回较多的相关文档



来看一幅图

![image-20200929230732624](.assets/image-20200929230732624.png)

## URL Search 详解

**通过url实现搜索**

![image-20200929231002731](.assets/image-20200929231002731.png)

**查询种类**

- 指定字段

![image-20201005165435089](.assets/image-20201005165435089.png)

- 泛查询

  ![image-20201008000222105](.assets/image-20201008000222105.png)

![image-20201008000733875](.assets/image-20201008000733875.png)

**term、phrase、bool**

- term query

  没有使用引号的单个查询

- phrase query

  使用引号，查询的对象必须都出现，并且顺序要一致

  ![image-20201008000922195](.assets/image-20201008000922195.png)

- bool query

  或者，title 中包含beautiful 或者 mind

  ![image-20201008001058214](.assets/image-20201008001058214.png)

![image-20201008001733943](.assets/image-20201008001733943.png)

![image-20201008001753107](.assets/image-20201008001753107.png)

- 通配符查询
  ![image-20201008002237420](.assets/image-20201008002237420.png)

- 正则表达式

- 模糊匹配与近似查询

![image-20201008002914015](.assets/image-20201008002914015.png)

>  title:"lord rings"~4 代表中间可以有最多可以有4个单词  例如
>
> ![image-20201008003023359](.assets/image-20201008003023359.png)

## Request Body Search



**脚本字段**

![image-20201008004416260](.assets/image-20201008004416260.png)



![image-20201008004647712](.assets/image-20201008004647712.png)



**判断字段是否存在**

![image-20201213182345198](.assets/image-20201213182345198.png)

**匹配查询字段**

![image-20201213183055459](.assets/image-20201213183055459.png)

### **查询表达式 —Match 和 term**

> match会对需要搜索的词进行分词，然后再搜索
>
> term不会
>
> 比如使用match能搜索到该条记录‘，使用term则没有



**match**

![image-20201213204742353](.assets/image-20201213204742353.png)

![image-20201008005211249](.assets/image-20201008005211249.png)

![image-20201008005408271](.assets/image-20201008005408271.png)



还可以设置需要匹配的最小词的数量，**注意：使用百分比的时候向下取整**

![image-20201213210226032](.assets/image-20201213210226032.png)

**还以使用数组**

![image-20201213205007870](.assets/image-20201213205007870.png)



**根据id查找**

![image-20201213210558241](.assets/image-20201213210558241.png) 



**在多个列中进行查询**

![image-20201213210919700](.assets/image-20201213210919700.png)

**match_phrase**

> 都包含，且按照顺序

![image-20201008005605905](.assets/image-20201008005605905.png)

> 中间可以多几个单词

![image-20201008005757018](.assets/image-20201008005757018.png)



先分词再搜索，结果中需要包含每个词，并且词语的顺序需要一致

![image-20201213205620886](.assets/image-20201213205620886.png)



> 默认的时候 match 使用的是or，表示对于分词的结果只要匹配到一个就能够查询出来，也可以手动指定。

![image-20201213205859623](.assets/image-20201213205859623.png)



### 权重

> 对于多个列的搜索，会有一种竞争的关系（哪一个结果排在前面）。可以对字段设置权重来提升它的位置。用户在对商品进行搜索的时候，肯定需要商品名称的权重更高，而简介可以低一些

没有设置权重的时候，nickname排的位置较为靠后，分数较低

![image-20201213211756833](.assets/image-20201213211756833.png)

提升nickname的权重

![image-20201213212059169](.assets/image-20201213212059169.png)



### bool查询

- must,   must 数组中可以存放多个条件，查询结果每个条件都要满足

  ![image-20201213223136243](.assets/image-20201213223136243.png)

- should，条件列表只要满足一个就行了

- must_not 所有条件都不满足的

加权

 ![image-20201213223930767](.assets/image-20201213223930767.png)

### **分页**  

es默认会进行分页，也可以指定分页

![image-20201213204325054](.assets/image-20201213204325054.png)



#### 深度分页

![image-20201216002430605](.assets/image-20201216002430605.png)

==当搜索的深度过深的时候将会报一个错==

![image-20201215234002505](.assets/image-20201215234002505.png)



由此可见，在前端分页的时候需要对页码进行一定的控制，保证不会超过范围



==如何拿到   9999-10009 条数据呢==

查看设置。如果没设置过 这个参数可能不会显示

 ![image-20201216002748599](.assets/image-20201216002748599.png)



进行修改

![image-20201216002832712](.assets/image-20201216002832712.png)

### 过滤

![image-20201213224625568](.assets/image-20201213224625568.png)

![image-20201213225131176](.assets/image-20201213225131176.png)

> 过滤只会对搜索结果过滤,不会计算文档相关性,相对于直接搜索来说,效率更高

### 排序

```java
{
    "query": {
        "match": {
            "desc": "天气真好"
        }
    }
    “sort": [
    	{
    		"age": "desc"
		},
		{
            "money": "asc" 
        }
    ]
}
```

对类型为text的字段排序会报错，因为text会进行分词，如果es要进行排序将会很杂乱，对keyword的类型排序则没有问题。如果非要对text进行排序，则可以加一个keyword的多字段类型

![image-20201215231848379](.assets/image-20201215231848379.png)



### 游标查询（滚动搜索）

### 批量操作

**查询**



![image-20201216225607073](.assets/image-20201216225607073.png)

#### bulk

![image-20201216225934336](.assets/image-20201216225934336.png) 

### 高亮显示

默认会对需要高亮的增加一个   <em><em>    标签，  

![image-20201215233642613](.assets/image-20201215233642613.png)

**如果  前端不是通过对em设置高亮属性， 也可以自定义标签   **

![image-20201215233818338](.assets/image-20201215233818338.png)

## Query String & Simple Query String

>  term 之间的关系默认为or，但是可以指定Operator

- Query String

> 此处的and相当于+，能够正常使用

![image-20201008181615558](.assets/image-20201008181615558.png)

- Simple Query String

  ![image-20201008182111582](.assets/image-20201008182111582.png)

> 会将and当成一个字符串来处理

![image-20201008181752420](.assets/image-20201008181752420.png)



![image-20201008182059483](.assets/image-20201008182059483.png)

## 乐观锁

![image-20201222232400825](.assets/image-20201222232400825.png)



> __primary_term：_ primary_term也和_seq_no一样是一个整数，每当Primary Shard发生重新分配时，比如重启，Primary选举等，_primary_term会递增1。
>
> __primary_term主要是用来恢复数据时处理当多个文档的_seq_no一样时的冲突，比如当一个shard宕机了，raplica需要用到最新的数据，就会根据_primary_term和_seq_no这两个值来拿到最新的document



**使用者两个参数即可完成版本控制**
![image-20201222232639145](.assets/image-20201222232639145.png)

将if_seq_no 改为2 执行成功

![image-20201222232722224](.assets/image-20201222232722224.png)
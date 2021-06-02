## 对内嵌文档的某个字段按天(小时)分组求最大值

数据格式

```json
{
    "_id" : ObjectId("5eb66d53fa92d16ba17cedc6"),
    "spCode" : "project_99c7041d7c274df6a0049935231d9f6c",
    "spName" : "交付中心",
    "productId" : "PC_1578358882051",
    "productName" : "红外传感器",
    "deviceType" : "WeiChuanInfraredSensor_304C",
    "deviceCode" : "ffffff100000a58b",
    "deviceName" : "公司红外感应",
    "reportData" : {
        "voltage" : "18.8"
    },
    "createDate" : ISODate("2020-05-09T08:44:03.000Z"),
    "deviceTypeName" : "人体红外",
    "productGroupCodes" : [],
    "msgSource" : "da",
    "msgCode" : "updata",
    "msgId" : "5608",
    "msgStatus" : "100",
    "serviceId" : "CoshipElevator",
    "confirm" : "false",
    "port" : "10",
    "imageCodes" : [],
    "_class" : "com.coship.da.model.DcReportRecords"
}
```

java代码

```java
Criteria criteria = Criteria.where("createDate").gte(DateUtil.string2Date(param.getStartTime())).lte(DateUtil.string2Date(param.getEndTime()));
 
		criteria.and("deviceCode").is(param.getDeviceCode());
		String propertyName = "reportData."+ param.getPropertyCode();
		String format ;
		if("1".equals(param.getGroupType())){
			format = "%Y-%m-%d";
		}else{
			format = "%H:00";
		}
 
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.match(criteria),
				Aggregation.project("reportData", "createDate").andExpression("{$dateToString:{format:'"+format+"', date: '$createDate', timezone: 'Asia/Shanghai'}}").as("date"),
				Aggregation.group("date")
						.max(propertyName).as("propertyValue")
						.first("createDate").as("createDate").first("date").as("date"));
		AggregationResults<JSONObject> results = mongoTemplate.aggregate(aggregation, "t_dc_report_records", JSONObject.class);
 
 
		List<JSONObject> mappedResults = results.getMappedResults();
```

## 拼接查询条件

利用Criteria.andOperator（） 方法可以简便的构造查询条件

例如：

```java
 List<Criteria> criteriaList = new ArrayList<>();
 
 criteriaList.add(Criteria.where("jobPosition.orgId").in(orgIds));
 
 //拼接查询条件
Query query = Query.query(criteria.andOperator(criteriaList.stream().toArray(Criteria[]::new)));

//统计总数
Long count = mongoTemplate.count(query, EmploymentInfo.class);
```



## 聚合查询

match() ，查询条件

project() 

> project()用法 需要查询的字段和字段映射，andExclude（）排除字段
>
> 1，project("name", "netPrice")
>
>   project方法的内容是你要映射的字段，相当于{$project: {sumfavour: 1, userid: 1}}。“映射”的意思，就是要显示哪个字段，或者把某个字段内容进行处理后显示。
>
> 2，project().and("foo").as("bar")
>
>   如果你想要把一个字段映射成别一个名字的话，就可以用上面的方法，相当于{$project: {bar: $foo}}
>
> 3，project("a","b").and("foo").as("bar") 
>
>   这个方法相当于上面两个方法的结合{$project: {a: 1, b: 1, bar: $foo}}。如果单独使用and()，后面不使用as()的话，没有任何效果，也不会出错。
>
> 4，project().and("_id").plus(100000000).as("statusid")
>
>   这个语句的意思是，把_id字段加上100000000，再重新命名为"statusid"然后输出。

bucket（）

> 需要查询的字段和字段映射，andExclude（）排除字段

previousOperation()

> previousOperation()就是把“上一次操作的结果中”的_id字段，命名为它前面and()中的名称。所以它一般都是和and()结合使用。

看看例子

```java
Aggregation agg = new Aggregation(
    project("tags"),
    unwind("tags"),
    group("tags").count().as("n"),
    project("n").and("tag").previousOperation(),
    sort(DESC, "n")

);
```

结果

```json
{ 
   "tag" : "627",   
   "n" : NumberInt(16)
}
```

## andExpression表达式

```java
/**
 * @description 查询各个司龄段的人数
 * @param
 * @Return java.util.List<org.bson.Document>
 */
private List<AgeCountVO> getNumByAge(String start, String end, String orgId) {
    //查询条件
    Criteria criteria = splicingCriteria(start, end, orgId);

    Aggregation time = Aggregation.newAggregation(
            Aggregation.match(criteria),
            /**
             * andExpression 中的表达式是  求离职时间与入职时间之间的月数
             * 转换成mongoDb的命令为:
             * $project: {
             *         time: {
             *             $add: [{
             *                     $multiply: [{
             *                         $subtract: [{
             *                             $year: '$resignationTime'
             *                         }, {
             *                             $year: '$entryTime'
             *                         }]
             *                     }, 12]
             *                 },
             *                 {
             *                     $subtract: [{
             *                         $month: '$resignationTime'
             *                     }, {
             *                         $month: '$entryTime'
             *                     }]
             *                 }
             *             ]
             *         }
             *     }
             */
            Aggregation.project().andExclude("_id").andExpression("{add(multiply(subtract(year($resignationTime),year($entryTime)),12),subtract(month($resignationTime),month($entryTime)))}").as("time"),
            Aggregation.bucket("time").withBoundaries(-1, 3 + 1, 12 + 1, 3 * 12 + 1, 5 * 12 + 1).withDefaultBucket(10000).andOutput("count").count().as("count")
    );
```

**数组更新操作符 Array Update Operators**

只能用在键值为数组的键上的数组操作。
**$ (query)**

语法: `{ "<array>.$" : value }`

当对数组字段进行更新时，且没有明确指定的元素在数组中的位置，我们使用定位操作符$标识一个元素，数字都是以0开始的。

**注意:**

- 定位操作符(“$”)作为第一个匹配查询条件的元素的占位符，也就是在数组中的索引值。
- 数组字段必须出现查询文档中。

向集合中插入两条数据

```sql
db.students.insert({ "_id" : 1, "grades" : [ 78, 88, 88 ] });
db.students.insert({ "_id" : 2, "grades" : [ 88, 90, 92 ] });
```

执行下列操作

```sql
//查询匹配的文档中，数组有2个88，只更新第一个匹配的元素，也就是"grades.1"
db.students.update( { _id: 1, grades: 88 }, { $set: { "grades.$" : 82 } }) ;
//查询文档中没有出现grades字段，查询报错
db.students.update( { _id: 2 }, { $set: { "grades.$" : 82 } } );
```
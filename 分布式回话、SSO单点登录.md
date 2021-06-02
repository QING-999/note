# 分布式会话

**springsession 实现分布式会话**

![image-20201123224008881](.assets/image-20201123224008881.png)



修改配置文件

![image-20201123224130350](.assets/image-20201123224130350.png)



开启

![image-20201123224213863](.assets/image-20201123224213863.png)



项目启动后spring会自动在redis中存入一些值

![image-20201123224326142](.assets/image-20201123224326142.png)



由于使用了security框架，必须要登录，可以使用springboot排除security的自动装配

![image-20201123224724722](.assets/image-20201123224724722.png)



# 分布式会话拦截器

![image-20201123225223735](.assets/image-20201123225223735.png)

**拦截器注册**

![image-20201123225449628](.assets/image-20201123225449628.png)



利用HttpServletResponse返回错误信息 

![image-20201124230628538](.assets/image-20201124230628538.png)



# SSO单点登录

## 构建工程





使用themeleaf 模板需要在spring配置文件中配置





![image-20201125232049989](.assets/image-20201125232049989.png)


























































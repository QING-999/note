[TOC]

# 项目构建

开启ideal的RunDashboard（窗口）

![image-20200906021850792](meetingfilm.assets/image-20200906021850792.png)

在.ideal中的workspace.xml中修改**RunDashboard** ，替换

```xml
<component name="RunDashboard">
    <option name="configurationTypes">
      <set>
        <option value="SpringBootApplicationConfigurationType" />
      </set>
    </option>
    <option name="ruleStates">
      <list>
        <RuleState>
          <option name="name" value="ConfigurationTypeDashboardGroupingRule" />
        </RuleState>
        <RuleState>
          <option name="name" value="StatusDashboardGroupingRule" />
        </RuleState>
      </list>
    </option>
</component>
```



## 通用异常处理

**创建通用业务异常类**

```java
@Data
@AllArgsConstructor
public class CommonServiceException extends Throwable {
    private Integer code;
    private String message;

}
```

**创建通用返回VO类**

```java
@Data
public class BaseResponseVO<M> {
    private Integer code;    //业务编号
    private String message;     //异常信息
    private M data;     //业务数据

    private BaseResponseVO(){};

    //成功但是无参数
    public static BaseResponseVO success(){
        BaseResponseVO responseVo = new BaseResponseVO();
        responseVo.setCode(200);
        responseVo.setMessage("");

        return responseVo;
    }

    //成功并且有参数
    public static<M> BaseResponseVO success(M data){
        BaseResponseVO responseVo = new BaseResponseVO();
        responseVo.setCode(200);
        responseVo.setMessage("");
        responseVo.setData(data);

        return responseVo;
    }

    //出现业务异常
    public static<M> BaseResponseVO serviceException(CommonServiceException e){
        BaseResponseVO responseVo = new BaseResponseVO();
        responseVo.setCode(e.getCode());
        responseVo.setMessage(e.getMessage());

        return responseVo;
    }

}
```

在controller中使用通用返回类

```java
 @RequestMapping(value = "/login", method = RequestMethod.POST)
    public BaseResponseVO login(@RequestBody LoginReqVO reqVO) throws CommonServiceException {

        //数据验证
        reqVO.checkParam();

        //校验用户名密码
        String uuid = userService.checkUserLogin(reqVO.getUsername(), reqVO.getPassword());

        System.out.println(reqVO);
        //randomkey  token
        //TODO
        Map<String, String> result = new HashMap<>();

        return BaseResponseVO.success(result);
    }
```

创建通用异常处理类

```java
@Slf4j
@ControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(CommonServiceException.class)
    @ResponseBody
    public BaseResponseVO serviceExceptionHandler(
            HttpServletRequest request, CommonServiceException e){

        log.error("CommonServiceException, code:{}, message",
                e.getCode(), e.getMessage());

        return BaseResponseVO.serviceException(e);
    }
}
```



#  用户模块开发

## JWT（Json Web Token）

**JWT概念和作用**

JWT全称为json web token，说白了是什么呢？ 就仅仅只是一个字符串而已，例如：

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.OLvs36KmqB9cmsUrMpUutfhV52_iSz4bQMYJjkI_TLQ 这样。

O(∩_∩)O 是不是特别长，特别丑？没关系，现在给大家解释一下这个东东到底是什么

**JWT组成【对于JWT有基本了解的人可以忽略这一部分】**

JWT包含了三个主要部分： Header.Payload.Signature，以" . "来进行分割，以上式举例：
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.
OLvs36KmqB9cmsUrMpUutfhV52_iSz4bQMYJjkI_TLQ

第一部分（head）：告诉怎么加密的，加密的形式是什么

第二部分(自定义payload)：加密后的信息

例如对

{
	“userId":"666"

​	"username":"chenj"

}

这一类信息进行加密后的格式

第三部分（Signature）：创建签名需要使用编码后的header和payload以及一个秘钥，组成的公式：编码后的header、编码后的payload、一个secret进行加密HMACSHA256( base64UrlEncode(header) + “.” + base64UrlEncode(payload), secret)

**实现流程**

![image-20200902230956551](meetingfilm.assets/image-20200902230956551.png)

**jwt集成**

1、添加依赖

```
<!-- guava支持 -->
<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <version>28.1-jre</version>
</dependency>

<!-- JSON处理 -->
<dependency>
  <groupId>com.alibaba</groupId>
  <artifactId>fastjson</artifactId>
  <version>1.2.60</version>
</dependency>

<!-- JWT依赖 -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt</artifactId>
  <version>0.9.0</version>
</dependency>
```

2、添加配置类，整理过的配置类如下

```java
@Data
public class JwtProperties {

    private static JwtProperties jwtProperties = new JwtProperties();
    private JwtProperties(){}
    public static JwtProperties getJwtProperties(){
        return jwtProperties;
    }

    public static final String JWT_PREFIX = "jwt"; 

    private String header = "Authorization";  //鉴权

    private String secret = "defaultSecret";	//加密的形式

    private Long expiration = 604800L;	//秒，这里是7天(7天toker过期)

    private String authPath = "login";	//颁发token的路径

    private String md5Key = "randomKey";
}
```

**jwt  Token工具包**

```java
public class JwtTokenUtil {

    private JwtProperties jwtProperties = JwtProperties.getJwtProperties();

    /**
     * 获取用户名从token中
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token).getSubject();
    }

    /**
     * 获取jwt发布时间
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token).getIssuedAt();
    }

    /**
     * 获取jwt失效时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token).getExpiration();
    }

    /**
     * 获取jwt接收者
     */
    public String getAudienceFromToken(String token) {
        return getClaimFromToken(token).getAudience();
    }

    /**
     * 获取私有的jwt claim
     */
    public String getPrivateClaimFromToken(String token, String key) {
        return getClaimFromToken(token).get(key).toString();
    }

    /**
     * 获取md5 key从token中
     */
    public String getMd5KeyFromToken(String token) {
        return getPrivateClaimFromToken(token, jwtProperties.getMd5Key());
    }

    /**
     * 获取jwt的payload部分
     */
    public Claims getClaimFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 解析token是否正确,不正确会报异常<br>
     */
    public void parseToken(String token) throws JwtException {
        Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token).getBody();
    }

    /**
     * <pre>
     *  验证token是否失效
     *  true:过期   false:没过期
     * </pre>
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException expiredJwtException) {
            return true;
        }
    }

    /**
     * 生成token(通过用户名和签名时候用的随机数)
     */
    public String generateToken(String userName, String randomKey) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(jwtProperties.getMd5Key(), randomKey);
        return doGenerateToken(claims, userName);
    }

    /**
     * 生成token
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + jwtProperties.getExpiration() * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret())
                .compact();
    }

    /**
     * 获取混淆MD5签名用的随机字符串
     */
    public String getRandomKey() {
        return getRandomString(6);
    }
    
    
    private String getRandomString(int length) {
		    String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		    Random random = new Random();
		    StringBuffer sb = new StringBuffer();
		    for (int i = 0; i < length; i++) {
		        int number = random.nextInt(base.length());
		        sb.append(base.charAt(number));
		    }
		    return sb.toString();
		}
		
}
```

**跨域**

```java
@Configuration
public class CorsConfig {

    public CorsConfig() {
    }

    @Bean
    public CorsFilter corsFilter() {
        // 1. 添加cors配置信息
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://shop.z.mukewang.com:8080");
        config.addAllowedOrigin("http://center.z.mukewang.com:8080");
        config.addAllowedOrigin("http://shop.z.mukewang.com");
        config.addAllowedOrigin("http://center.z.mukewang.com");
        config.addAllowedOrigin("*");

        // 设置是否发送cookie信息
        config.setAllowCredentials(true);

        // 设置允许请求的方式
        config.addAllowedMethod("*");

        // 设置允许的header
        config.addAllowedHeader("*");

        // 2. 为url添加映射路径
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**", config);

        // 3. 返回重新定义好的corsSource
        return new CorsFilter(corsSource);
    }

}
```

**cookie**

以键值对的形式存储信息在浏览器cookie不能跨域，当前及其父级域名可以取值cookie可以设置有效期

**session**

基于服务器内存的缓存（非持久化），可保存请求会话每个session通过sessionid来区分不同请求session可设置过期时间session也是以键值对形式存在的

**轮播图**

![image-20201028235201936](.assets/image-20201028235201936.png)
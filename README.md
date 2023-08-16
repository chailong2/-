# 一、苍穹外卖，二次开发笔记



## 1. 前端环境部署

下载Front-end operating environment文件夹，前端项目部署在nginx上，nginx已经配置好了，只需点击exe程序就可以将nginx启动，然后在浏览器中访问localhost就可以看到前端页面。

![](./img/1.png)

## 2. 后端环境开发

sky-take-out文件夹是后端初始项目文件夹，并且进行了分模块开发。

![](./img/2.png)

- Sky-common：子模块，存放公共类，例如工具类、常量类、异常类
- Sky-pojo：子模块，存放实体类、VO、DTO等
- Sky-server：子模块，后端服务，存放配置文件、Controller、Service和mapper等
- Sky-take-out：maven父工程，统一管理依赖版本，聚合其他子模块

## 3. git环境搭建

- 创建本地仓库

```shell
#在项目文件夹下打开终端输入下面的命令
git init
#将代码添加到暂存区
git add *
#将代码提交到本地仓库
git git commit -m "first commit" *
```

- 创建远程仓库

> github上创建远程仓库

![](./img/3.png)

- 将代码推送到远程仓库中

```shell
git remote add ori git@github.com:chailong2/sky-take-out.git #也可以直接使用链接
#这个地址是我的远程仓库的https地址 
git remote -v
#将本地仓库的内容推动到远程仓库
git push ori master
```

## 4. 数据库环境搭建

database中的文件夹中的sql文件在navicat中直接运行即可

![](./img/4.png)

## 5. 测试前后端通信

这里前后通信的关键是nginx.conf文件中配置的反向代理操作，这里nginx配置文件已经提前配置好了，直接运行项目（注意数据库密码修改成自己的密码，然后在登陆页面直接登陆，就可以进入主控制台）

![](./img/5.png)

## 6. 对用户的密码进行加密后进行存储（提高安全性）

这里使用MD5加密方式对明文密码进行加密

![](./img/6.png)

**完善登陆功能**

```java
 //密码比对
 // 对前端传递过来的明文密码进行MD5加密
 password= DigestUtils.md5DigestAsHex(password.getBytes());
```

注意数据库中明文密码也要改为，加密后的MD5

![](./img/7.png)

## 7. 导入接口文档

- 前后端分离开发流程

![](./img/8.png)

- 操作

将资料接口文档中的两个Json文件导入到APIFOX中，网页版可以直接使用

![](./img/9.png)

## 8. Swagger使用

Knife4j是为java MVC框架集成Swagger生存Api文档的增强解决方案，其使用方式如下：

- 导入knife4j的maven坐标

```xml
<dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-spring-boot-starter</artifactId>
        </dependency>
```

- 在配置类中加入knife4j相关配置

```java
 @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
```

- 设计静态资源映射，否则接口文档页面无法访问

```java
 protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
```

- 自动生成接口文档

![](./img/10.png)

## 9. pageHelper实现分页查询

PageHelper是MyBatis的一个插件，内部实现了一个PageInterceptor[拦截器](https://so.csdn.net/so/search?q=拦截器&spm=1001.2101.3001.7020)。Mybatis会加载这个拦截器到拦截器链中。在我们使用过程中先使用PageHelper.startPage这样的语句在当前线程上下文中设置一个ThreadLocal变量，再利用PageInterceptor这个分页拦截器拦截，从ThreadLocal中拿到分页的信息，如果有分页信息拼装分页SQL（limit语句等）进行分页查询，最后再把ThreadLocal中的东西清除掉。pageHelper使用了ThreadLocal保存分页参数，分页参数和线程是绑定的。因此**我们需要保证PageHelper 的startPage调用后紧跟 MyBatis 查询方法，这就是安全的**。因为 PageHelper 在 finally 代码段中自动清除了 ThreadLocal 存储的对象。如果代码在进入Executor前发生异常，就会导致线程不可用，这属于人为Bug（例如接口方法和XML中的不匹配，导致找不到MapppedStatement时）。这种情况由于线程不可用，也不会导致 ThreadLocal参数被错误使用。但是如果写出以下代码，就是不安全的用法：

```java
PageHelper.startPage(1, 10);
List<Country> list;
if(param1 != null){
    list = countryMapper.selectIf(param1);
} else {
    list = new ArrayList<Country>();
}

```

这种情况下由于param1存在null的情况，就会导致PageHelper产生了一个分页参数，但是没有被消费，也没有被清理。这个参数就会一直保存在ThreadLocal中。可能导致其它不该分页的方法去消费了这个参数，莫名其妙的做了分页。此外如果考虑到发生异常，可以加一个finally块，手动清理参数。

## 10. 解决日期格式不规范问题

如下图这是我们现在页面展示出来的日期信息：

![](./img/11.png)

我们有两种方法解决上面问题：

- 在日期属性加入注解，对日期进行格式话（缺点是每个日期属性可能都需要加注解，没有实现统一管理）

```java
@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
```

- 在WebMvcConfiguration中扩展Spring MVC消息转换器，统一对日期格式进行格式化处理

```java
   @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器....");
        //创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //需要为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列化为Json字符串
        converter.setObjectMapper(new JacksonObjectMapper());
        //将自己的消息转换器放到容器中
        converters.add(0,converter);
    }
```

![](./img/12.png)
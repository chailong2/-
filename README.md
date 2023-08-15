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
```

- 创建远程仓库


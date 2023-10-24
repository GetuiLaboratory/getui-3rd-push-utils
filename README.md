# GT SDK Libraries for Java

该SDK是多厂商推送工具集，目前包装的功能有：icon上传。支持的厂商模块有OPPO、XM。

需要的jdk版本：

- JDK 1.8 or higher.

## Adding dependency to your build

使用maven添加依赖如下

```xml
<dependency>
    <groupId>com.getui.push</groupId>
    <artifactId>getui-3rd-push-utils</artifactId>
    <version>1.1.0.1</version>
</dependency>
```

## Usage

### sdk初始化

在应用配置类中初始化执行一次如下代码

```java
// 配置文件的路径是运行程序所在的相对路径
GtSDKStarter.getInstance().loadPropertyFile("/src/main/resources/application.properties").init();
```

### 配置说明

请在上一步指定的配置文件内添加以下参数，

```properties
## 是否需要初始化厂商服务实例（主要包含各厂商服务类实例化、鉴权）。默认是true
GtSDK.manufacturerInitSwitch=true
## 需要执行的厂商模块（不区分大小写），目前支持oppo和xm。不启用配置就默认执行所有厂商模块，启用配置单不填值就所有厂商模块都不执行。
GtSDK.moduleSet=oppo,xm
## 判断文件是否相同的方式，支持sha1和fileName。如不配置默认使用fileName。（为了避免文件重复上传，文件上传后sdk会将厂商返回的链接缓存起来，下次从缓存里取。
## 这里就是决定使用什么方式来判断文件是否重复。缓存的时效使用各厂商icon最大存储时效，由于目前使用的本地缓存，应用重启也会导致缓存清空）
GtSDK.judgeFile=sha1
## 多厂商接口调用是否默认使用多线程
GtSDK.mThread=true
## 接口调用超时等待时间，单位毫秒，默认500毫秒
GtSDK.callTimeout=500
## 以下是各厂商配置参数
GtSDK.XM.AppId=
GtSDK.XM.AppKey=
GtSDK.XM.AppSecret=
GtSDK.XM.MasterSecret=
GtSDK.OPPO.AppId=
GtSDK.OPPO.AppKey=
GtSDK.OPPO.AppSecret=
GtSDK.OPPO.MasterSecret=
```

### 服务调用

目前提供了四个服务：

1. 多厂商上传同一个icon文件。完成上两步后，只需要在需要上传icon的代码处编写以下代码即可得到各厂商上传结果。

```java
// 配置文件的路径是运行程序所在的相对路径
Map<String, Result> result = ManufacturerFactory.uploadIcon(new File("/xxx/xxx/xxx.png"));
```

2. 多厂商上传不同的icon文件。代码如下：

```java
// 配置文件的路径是运行程序所在的相对路径
ManufacturerFile file1 = new ManufacturerFile(ManufacturerConstants.ManufacturerName.OPPO, "/xxx/xxx/xxx/xxx1.png");
ManufacturerFile file2 = new ManufacturerFile(ManufacturerConstants.ManufacturerName.XM, "/xxx/xxx/xxx2.png");
ManufacturerFile[] manufacturerFiles = new ManufacturerFile[]{file1, file2};
Map<String, Result> result = ManufacturerFactory.uploadIcon(manufacturerFiles);
```

3. 多厂商上传同一个图片文件。代码如下：

```java
// 配置文件的路径是运行程序所在的相对路径
Map<String, Result> result = ManufacturerFactory.uploadPic(new File("/xxx/xxx/xxx.png"));
```

4. 多厂商上传不同的图片文件。代码如下：

```java
// 配置文件的路径是运行程序所在的相对路径
ManufacturerFile file1 = new ManufacturerFile(ManufacturerConstants.ManufacturerName.OPPO, "/xxx/xxx/xxx/xxx1.png");
ManufacturerFile file2 = new ManufacturerFile(ManufacturerConstants.ManufacturerName.XM, "/xxx/xxx/xxx2.png");
ManufacturerFile[] manufacturerFiles = new ManufacturerFile[]{file1, file2};
Map<String, Result> result = ManufacturerFactory.uploadPic(manufacturerFiles);
```

### 服务结果解析

上一步可以看出上传接口返回的都是个Map，Map的key是厂商名（OPPO、XM），value是一个Result对象。Result包含以下三个属性：

- code：结果码，0成功、1失败、2超时失败、3没有厂商实例（正常情况是配置没配这个厂商，但代码里却想使用这个厂商的服务）、4鉴权失败
- message：success、fail、timeout、has no manufacturer instance、auth fail
- data：成功时，值为icon在各厂商的上传url结果（或者picId）；失败时，值是失败原因。

## 其他说明

由于该sdk本质只是各厂商api的包装，所以对于一些接口限制和返回处理，需要遵循各厂商的api文档。下面放出
[OPPO](https://open.oppomobile.com/wiki/doc#id=10693) 和[XM](https://dev.mi.com/console/doc/detail?pId=1163#_10_1) 的API在线文档供参考。

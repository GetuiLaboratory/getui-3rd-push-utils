[1.0.0.0] - 2021-01-04

### 新增

* OPPO、XM icon和图片上传接口实现

[1.0.1.0] - 2021-01-26

* 优化了配置文件读取方式，支持读取jar包内配置文件
* 提供厂商接口单线程执行的配置

[1.0.1.1] - 2021-01-27

* 优化OPPO获取AuthToken的机制

[1.1.0.0] - 2021-02-04

* 去除org.reflections.reflections依赖，涉及到使用reflections工具的地方自己实现
* 调用XM和OP上传接口上传失败后，XM和OP的返回信息完整输出到返回结果的data中
* XM的图片上传接口由/media/upload/smallIcon调整为/media/upload/image

[1.1.0.1] - 2023-10-23

* 修复FileInputStream没有正确关闭的问题
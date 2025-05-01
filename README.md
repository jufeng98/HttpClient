# HttpClient

## Provides the ability to edit and execute HTTP requests from the code editor of IDEA

### features
- Support sending GET, POST and other requests
- Support sending WebSocket request
- Support sending Dubbo request
- Support Environment, build-in variable and build-in method
- Support jump to SpringMVC Controller method from url
- Show SpringMVC Controller method information when hover in url
- Support jump to SpringMVC Controller method bean field from json property
- Show SpringMVC Controller method bean field information when hover in json key
- Support JavaScript previous handler, post handler and global handler(JavaScript syntax highlighting depends on WebCalm 
  plugin. After install WebCalm, please restart IDEA, Otherwise JavaScript syntax highlighting will not work.)
- Support reading file content as http request body
- Support saving http response body to file
- When the response Content-Type is image type, it supports direct preview of the corresponding image
- Support searching SpringMVC Api in the SearchEverywhere Dialog

### Getting Started

1. Create a file with an HTTP suffix
2. Type gtr or ptr to trigger live templates and quickly create HTTP requests
3. Click the run button on the left side of the editor to sending request and view the response

## 提供从 IDEA Editor 编写和执行 HTTP 请求的能力

### 主要功能如下：

- 支持发起 GET、POST 等请求
- 支持发起 WebSocket 请求
- 支持发起 Dubbo 请求
- 支持环境变量、内置变量和内置方法
- 支持从 url 跳转到对应的 SpringMVC Controller 方法
- url 悬浮提示对应的 SpringMVC Controller 方法信息
- 支持从 json 属性跳转到对应的 SpringMVC Controller 方法的出入参 Bean 字段
- json 属性悬浮提示对应的 SpringMVC Controller 方法的出入参 Bean 字段信息
- 支持 JavaScript 语法的前置处理器、后置处理器和全局前置处理器(JavaScript 语法高亮功能依赖 WebCalm 插件. 在安装 WebCalm 插件后, 请重启 IDEA, 否则 js 语法高亮功能不会生效)
- 支持从文件读取内容作为请求体
- 支持保存响应到文件
- 当响应的 Content-Type 为图片时支持直接预览响应的图片
- 支持在 SearchEverywhere 窗口搜索 SpringMVC Api

### 开始使用

1. 创建一个 http 后缀的文件
2. 输入 ptr 或者 gtr 等触发实时模板以快速创建 http 请求
3. 点击文件左侧的运行按钮发起请求并查看响应

#### GET 请求

![get.png](./images/get.png)

#### POST 请求

![post.png](./images/post.png)

#### DUBBO 请求(针对项目中存在 Dubbo 接口类情况)

![dubbo-1.png](./images/dubbo-1.png)

#### DUBBO 请求(针对项目中不存在 Dubbo 接口类情况)

![dubbo-2.png](./images/dubbo-2.png)

#### WebSocket 请求

![websocket.png](./images/websocket.png)

#### 环境变量跳转

![variable-jump.gif](./images/variable-jump.gif)

#### url 跳转 SpringMVC Controller 方法

![controller-jump.gif](./images/controller-jump.gif)

#### url 悬浮提示 SpringMVC Controller 方法

![url-hover.png](./images/url-hover.png)

#### json 属性跳转 SpringMVC Controller 方法出入参 Bean 字段

![field-jump.gif](./images/field-jump.gif)

#### json 属性悬浮提示 SpringMVC Controller 方法出入参 Bean 字段

![json-key-hover.png](./images/json-key-hover.png)
![response-hover.png](./images/response-hover.png)

#### 在 SearchEverywhere 窗口搜索 SpringMVC Api

![search-everywhere.png](./images/search-everywhere.png)

#### js处理器和全局变量

![js-handler.png](./images/js-handler.png)

#### 保存响应到文件

![save-response.png](./images/res-to-file.png)

#### 预览图片

![preview-img.png](./images/preview-image.png)

#### multipart请求

![multipart.png](./images/multipart.jpg)

## 作者信息

- 作者博客：[知乎](https://www.zhihu.com/people/liang-yu-dong-44)
- 作者邮箱：375709770@qq.com
- github 地址：https://github.com/jufeng98

## 捐赠

如果项目帮到了您，请作者喝杯咖啡吧！
![donate](./images/donate.jpg)

## 技术支持

微信记得备注 ```HttpRequest```，共同进步。
![wechat](./images/wechat.jpg)

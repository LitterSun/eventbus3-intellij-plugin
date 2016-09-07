# eventbus3-intellij-plugin

引导 [EventBus](https://github.com/greenrobot/EventBus) 的 `post` 和 `event`

## 更新日志

### 2016-09-07
- 增加了 `build version` 值
- 修改了 `plugin id`，否则不能在 https://plugins.jetbrains.com 插件官网发布新版本
- 添加 `plugin.xml` 的一个配置，使此插件支持在 Android Studio 中在线安装

### 2016-09-05
这是个比较实用的插件，然而对于最新版的 `EventBus 3.0.0` 无效，原作者久久没有回应 `issue`，我尝试让这个插件可以工作

主要Bug修复工作：
- 修改包名和方法名以适应 `EventBus 3.X`
- 替换一个在新版的 `intellij plugin SDK` 已经不存在的类
- 增加若干 `try-catch` ，防止插件崩溃

---


### 从`Post` 到 `@Subscribe` ，从 `@Subscribe` 到 `Post`

![](https://raw.githubusercontent.com/likfe/eventbus3-intellij-plugin/master/art/cap.gif)


## 安装
两种方法：
- Android Studio -> Preference -> Plugins -> Browse Repositories : `EventBus` -> 选择 `EventBus3 Intellij Plugin` -> 点击 `Install` 按钮.
- [下载](https://github.com/likfe/eventbus3-intellij-plugin/raw/master/eventbus3-intellij-plugin.jar) jar 文件， 然后在插件界面选择"Install Plugin From Disk" 进行安装


## License

```
Copyright 2016

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

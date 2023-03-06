# 概述
此项目是javafx-ensemble8的汉化版本

# 运行项目
1. clone项目到本地
2. 使用maven打包(直接执行无法显示源码,原因是打包后没有将java文件打包到target中,查看源码的时候无法找到源码文件)
3. 运行ensemble.EnsembleBootstrap.main

# 可能遇到的问题
1. javafx相关包不存在:JDK版本过高,需要依赖1.8(1.8以后的版本移除了javafx)
2. 搜索栏无法使用:应该是代码存在问题(待解决)
3. 媒体播放器相关的示例无法正常显示:应该是代码存在问题(待解决)

# 汉化解释
### 汉化流程
1. 在ensemble.EnsembleApp.initMappingChinese方法中进行中英文转换配置文件的加载
2. 通过ensemble.ChineseAndEnglishController.translatorOfChinese进行中英文显示的控制

### 汉化文件
1. 汉化配置文件:src/main/resources/nameEnglishMappingChinese.properties
2. 汉化文件的key值生成规则:ensemble.ChineseAndEnglishController.replaceAllSpecialStr

# 解释
汉化内容大部分是机翻,因此质量堪忧
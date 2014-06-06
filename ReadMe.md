#### 功能介绍
* 主界面（播放界面）
![](http://img3.tuchuang.org/uploads/2014/06/Screenshot_2014_06_06_10_59_24.png)
* 曲库
![](http://img3.picbed.org/uploads/2014/06/Screenshot_2014_06_06_10_59_47.png)
* 歌手
![](http://img4.tuchuang.org/uploads/2014/06/Screenshot_2014_06_06_10_59_55.png* 歌单
![](http://img4.tuchuang.org/uploads/2014/06/Screenshot_2014_06_06_10_59_58.png)
* 搜索
![](http://img5.tuchuang.org/uploads/2014/06/Screenshot_2014_06_06_11_00_36.png)
* 更新本地歌曲
![](http://img4.tuchuang.org/uploads/2014/06/Screenshot_2014_06_06_11_00_10.png)

#### 更新日志
##### 2014.05.30(version:0.9.1.0)
* 为MusicListItem添加Alpha动画显示
* 几个icon的替换，细节UI调整

##### 2014.05.29(version:0.8.3.1)
* 搜索歌曲代码逻辑优化，更好的避免出错，添加读取歌曲信息的进度显示
* 播放歌曲代码结构优化，如果播放出错，则显示出错歌曲路径，并模拟点击“下一首”按钮，继续播放
* 添加设置：更新本地歌曲

##### 2014.05.28(version:0.8.2.0)
* 主界面左滑显示当前播放列表
* 添加功能：搜索

##### 2014.05.27(version:0.7.2.0)
* 代码结构优化，获取歌曲列表写成一个方法
* 添加功能：记忆退出时播放的歌曲
* 添加设置选项，是否记忆退出时播放的歌曲（因为Switch不支持Android4.0以下版本，故采用开源项目：[SwitchCompatLibrary](https://github.com/ankri/SwitchCompatLibrary) 以向下支持到Android2.2）

##### 2014.05.23(version:0.6)
* 实现播放界面的“喜爱”按钮功能
* 在播放列表中显示“我的喜爱”列表

##### 2014.05.22
* 将项目上传至Github管理

##### 2014.05.21(version:0.4)
* 主界面中部换用ViewPager+fragment
* 通过在MusicService中sendReceiver，在MainActivity中接收，调用MusicInfo.showInfo()在Fragment中显示歌曲信息

##### 2014.05.19-20
* 调整数据库结构，音乐表中path作主键，播放列表音乐表中path+playlist作联合主键

##### 2014.05.18(version:0.2)
* 开始写开发日志，为了给自己提供动力
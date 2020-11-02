# linux面试题及答案

#  

**1、说一下常用的Linux命令？**

- 列出文件列表：ls【参数 -a -l】
- 创建目录和移除目录：mkdir rmdir
- 用于显示文件后几行内容：tail打包：tar -xvf
- 打包并压缩：tar -zcvf
- 查找字符串：grep
- 显示当前所在目录：pwd创建空文件：touch
- 编辑器：vim vi

 

**2、Linux中如何查看日志？**

动态打印日志信息：tail –f 日志文件

 

**3、Linux怎么关闭进程？**

通常用ps查看进程PID，用kill命令终止进程。ps命令用于查看当前正在运行的进程。grep是搜索；-aux显示所有状态；

例如：

ps –ef | grep java表示查看所有进程里CMD是java的进程信息。

ps –aux | grep java

kill命令用于终止进程。例如：kill -9 [PID]  -9表示强迫进程立即停止。



awk、cat、sort、cut、grep、uniq、wc、top



 # 4proc文件系统

# 5 查看cpu状态 top。查看占用端口的进程号netstat grep 

# 6 Linux大文件怎么查某一行的内容 


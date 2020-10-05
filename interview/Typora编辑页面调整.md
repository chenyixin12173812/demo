# Typora编辑页面调整

我一直在使用 Typora 来写博客，但是官方展示的效果有很多不尽人意的地方，例如：

1. 文章正文区域宽度
2. 查看源码时的正文区域宽度
3. 另存为 PDF 时页面的大小

所以我对 Typora 的样式做了一些修改，以适应普遍的大屏幕显示，优化显示步骤为：

首先打开偏好设置 -> 打开主题文件夹 -> 新建 github.user.css 文件，填入如下内容。

```

#write{
    max-width: 90%;
}

/* 调整源码正文宽度 */
#typora-source .CodeMirror-lines {
    max-width: 90%;
}

/* 调整输出 PDF 文件宽度 */
@media print {
    #write{
        max-width: 95%;
    }
    @page {
        size: A3;
    }
}

/* 调整正文字体,字体需单独下载 */
body {
    font-family: IBM Plex Sans;
}
```
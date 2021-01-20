package com.chenyixin.test.spark.core.framework.application

import com.chenyixin.test.spark.core.framework.common.TApplication
import com.chenyixin.test.spark.core.framework.controller.WordCountController

object WordCountApplication extends App with TApplication{

    // 启动应用程序
    start(){
        val controller = new WordCountController()
        controller.dispatch()
    }

}

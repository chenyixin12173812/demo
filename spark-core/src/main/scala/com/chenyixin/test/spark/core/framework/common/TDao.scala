package com.chenyixin.test.spark.core.framework.common

import com.chenyixin.test.spark.core.framework.util.EnvUtil

trait TDao {

    def readFile(path:String) = {
        EnvUtil.take().textFile(path)
    }
}

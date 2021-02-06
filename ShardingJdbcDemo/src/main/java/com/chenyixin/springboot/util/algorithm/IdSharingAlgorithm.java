package com.chenyixin.springboot.util.algorithm;

import java.util.Collection;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;


public class IdSharingAlgorithm implements PreciseShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        Long id = preciseShardingValue.getValue();
        for (String name : collection) {
            if (name.endsWith(id % collection.size() + "")) {
                return name;
            }
        }
        throw new IllegalArgumentException();
    }
}

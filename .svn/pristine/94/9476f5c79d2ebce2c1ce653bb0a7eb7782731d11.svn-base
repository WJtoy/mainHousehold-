package com.wolfking.jeesite.common.config.redis;

import org.springframework.data.redis.connection.RedisZSetCommands;

/**
 * Created by yanshenglu on 2017/5/23.
 */
public class RedisTuple  implements RedisZSetCommands.Tuple {
    public RedisTuple(byte[] value,Double score){
        this.value = value;
        this.score = score;
    }

    private final byte[] value;
    private final  Double score;

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public Double getScore() {
        return score;
    }

    @Override
    public int compareTo(Double o) {
        if(score<o){
            return -1;
        }
        if(score>0){
            return 1;
        }
        return 0 ;
    }
}

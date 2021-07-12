package com.wolfking.jeesite.ms.material.service;

import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * B2B配件微服务工厂
 * @autor Ryan Lu
 * @date 2019/9/11 16:15
 */
@Component
public class B2BMaterialExecutorFactory {

    @Autowired
    private JoyoungMaterialExecutor joyoungMaterialExecutor;

    @Autowired
    private XYYPlusMaterialExecutor xyyPlusMaterialExecutor;

    @Autowired
    private LbMaterialExecutor lbMaterialExecutor;


    public B2BMaterialExecutor getExecutor(B2BDataSourceEnum dataSource){
        //B2BMaterialExecutor executor = null;
        switch (dataSource){
            case JOYOUNG:
                //九阳
                return joyoungMaterialExecutor;
            case XYINGYAN:
                //新迎燕
                return xyyPlusMaterialExecutor;
            case LB:
                //乐邦
                return lbMaterialExecutor;
            default:
                return null;
        }
        //return executor;
    }
}

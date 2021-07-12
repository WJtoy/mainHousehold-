package com.wolfking.jeesite.ms.tmall.md.utils;

import com.kkl.kklplus.entity.common.MSBase;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;

public class MSBaseUtils {

    /**
     * 为实体设置创建者、创建时间、更新者、更新时间
     * @param baseEntity
     */
    public static void preInsert(MSBase baseEntity) {
        baseEntity.preInsert();
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            baseEntity.setCreateById(user.getId());
            baseEntity.setUpdateById(user.getId());
        }
        else {
            baseEntity.setCreateById(0L);
            baseEntity.setUpdateById(0L);
        }
    }

}

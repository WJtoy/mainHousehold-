package com.wolfking.jeesite.modules.sys.service;

import com.wolfking.jeesite.modules.sys.dao.UserAttributesDao;
import com.wolfking.jeesite.modules.sys.entity.UserAttributes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserAttributesService {

    @Resource
    private UserAttributesDao userAttributesDao;

    public UserAttributes getUserAttributesList(Long userId, Integer type) {
        return userAttributesDao.getUserAttributesList(userId, type);
    }

    public Integer saveUserAttributes(UserAttributes userAttributes) {
        return userAttributesDao.saveUserAttributes(userAttributes);
    }

    public Integer deleteUserAttributes(Long userId, Integer type) {
        return userAttributesDao.deleteUserAttributes(userId, type);
    }
}

package com.wolfking.jeesite.modules.sys.service;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.BaseService;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.sys.dao.RoleDao;
import com.wolfking.jeesite.modules.sys.dao.UserKeFuDao;
import com.wolfking.jeesite.modules.sys.dao.UserServicePointDao;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.UserProductCategory;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserServicePointService extends BaseService {

    @Resource
    private UserKeFuDao userKeFuDao;

    @Resource
    private UserServicePointDao userServicePointDao;

    @Resource
    private RoleDao roleDao;

    public Page<User> findUser(Page<User> page, User user) {
        // 生成数据权限过滤条件（dsf为dataScopeFilter的简写，在xml中使用 ${sqlMap.dsf}调用权限SQL）
        user.getSqlMap().put("dsf", dataScopeFilter(user.getCurrentUser(), "o", "a"));
        // 设置分页参数
        user.setPage(page);
        // 执行分页查询

        List<User> userList = userServicePointDao.findList(user);

        Set<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toSet());
        List<UserProductCategory> productCategory = Lists.newArrayList();
        if (userIds.size() > 0) {
            productCategory = userKeFuDao.getProductCategoryIds(Lists.newArrayList(userIds));
        }

        Map<Long, ProductCategory> allProductCategoryMap = ProductUtils.getAllProductCategoryMap();
        Map<Long, List<Long>> productCategoryMap = productCategory.stream().collect(Collectors.groupingBy(UserProductCategory::getUserId, Collectors.mapping(UserProductCategory::getProductCategoryId, Collectors.toList())));

        if (!ObjectUtils.isEmpty(userList)) {
            List<Long> productCategoryIds;
            List<String> productCategoryNames;
            String productCategoryName;
            for (User newUser : userList) {
                if (newUser != null && newUser.getOffice() != null && newUser.getOffice().getId() != null) {

                    if (newUser.getRoleList() == null || newUser.getRoleList().size() == 0) {
                        List<Role> roles = roleDao.getUserRoles(newUser.getId());
                        newUser.setRoleList(roles);
                    }
                    if (newUser.getId() != null) {
                        productCategoryIds = productCategoryMap.get(newUser.getId());
                        productCategoryNames = Lists.newArrayList();
                        if (productCategoryIds != null) {
                            for (Long productCategoryId : productCategoryIds) {
                                if (allProductCategoryMap.get(productCategoryId) != null) {
                                    productCategoryName = allProductCategoryMap.get(productCategoryId).getName();
                                    if (productCategoryName != null && !productCategoryName.equals("")) {
                                        productCategoryNames.add(productCategoryName);
                                    }
                                }


                            }
                        }
                        newUser.setProductCategoryNames(StringUtils.join(productCategoryNames, ","));
                    }
                }
            }
        }
        page.setList(userList);
        return page;
    }
}

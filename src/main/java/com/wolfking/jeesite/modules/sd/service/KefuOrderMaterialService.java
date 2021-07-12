package com.wolfking.jeesite.modules.sd.service;


import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sd.dao.KefuOrderMaterialDao;
import com.wolfking.jeesite.modules.sd.entity.MaterialMaster;
import com.wolfking.jeesite.modules.sd.entity.MaterialReturn;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderMaterialSearchModel;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


/**
 * 客服配件数据访问业务
 * @author wangshoujiang
 * @date 2021-03-22
 */

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class KefuOrderMaterialService extends OrderRegionService{


    @Autowired
    private KefuOrderMaterialDao dao;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MSUserService userService;

    /**
     * 分页查询配件单(按status查询)
     * @param page
     * @param searchModel
     * @return
     */
    public Page<MaterialMaster> findKefuMaterialList(Page<OrderMaterialSearchModel> page, OrderMaterialSearchModel searchModel){
        searchModel.setPage(page);
        List<MaterialMaster> list = dao.findKefuMaterialList(searchModel);
        getMaterialMasterInfo(list,searchModel.isMerchRjectReasonAndRemark());
        Page<MaterialMaster> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        rtnPage.setList(list);
        return rtnPage;
    }

    /**
     * 读取配件单基本信息
     * 包含客户，区域，产品等
     * @param list
     * @param merchRjectReasonAndRemark 合并驳回原因及详细描述到closeRemark属性，用去前端显示
     */
    private void getMaterialMasterInfo(List<MaterialMaster> list,boolean merchRjectReasonAndRemark){
        Map<Long,Customer> customerHashMap = Maps.newHashMap();
        Map<String,Dict> pendingTypes = MSDictUtils.getDictMap("material_pending_type");
        Map<String,Dict> statuses = MSDictUtils.getDictMap("material_apply_status");
        Map<String,Dict> materialTypes = MSDictUtils.getDictMap("MaterialType");
        Map<String,Dict> applyTypes = MSDictUtils.getDictMap("material_apply_type");
        Map<String,Dict> rejectTypes = MSDictUtils.getDictMap("material_reject_type");
        Map<Long,User> userMaps = Maps.newHashMap();
        Map<Long,Area> areaMap = Maps.newHashMap();
        MaterialMaster materialMaster;
        Customer customer;
        Product product;
        Area area, subArea;
        long id;
        Dict pendingType, status, rejectType;
        User user;
        for(int i=0,size=list.size();i<size;i++){
            materialMaster = list.get(i);
            //customer
            id = materialMaster.getCustomer().getId();
            if(customerHashMap.containsKey(id)){
                customer = customerHashMap.get(id);
            }else{
                customer = customerService.getFromCache(id);
                customerHashMap.put(id,customer);
            }
            if(customer != null) {
                materialMaster.setCustomer(customer);
            }
            // 产品名称中前后逗号处理
            String[] productNameArr=null;
            List<String> productNameList = Lists.newArrayList();
            if(materialMaster.getProductNames().startsWith(",")){
                productNameArr = org.apache.commons.lang3.StringUtils.split(materialMaster.getProductNames(),",");
                materialMaster.setProductNames(org.apache.commons.lang3.StringUtils.substringBetween(materialMaster.getProductNames(),","));
                if(productNameArr!=null && productNameArr.length>0){
                    productNameList = Lists.newArrayList(productNameArr);
                }
                materialMaster.setProductNameList(productNameList);
            }
            //area
            if(areaMap.containsKey(materialMaster.getArea().getId())){
                area = areaMap.get(materialMaster.getArea().getId());
            }else{
                area = areaService.getFromCache(materialMaster.getArea().getId());
                if(area != null){
                    areaMap.put(materialMaster.getArea().getId(),area);
                }
            }
            if(area != null) {
                materialMaster.setArea(area);
            }
            //sub area
            if(materialMaster.getSubArea().getId()>3){
                if(areaMap.containsKey(materialMaster.getSubArea().getId())){
                    subArea = areaMap.get(materialMaster.getSubArea().getId());
                }else {
                    subArea = areaService.getTownFromCache(area.getId(), materialMaster.getSubArea().getId());
                    if(subArea != null){
                        areaMap.put(materialMaster.getSubArea().getId(),subArea);
                    }
                }
                if(subArea != null){
                    materialMaster.setSubArea(subArea);
                }
            }else{
                materialMaster.getSubArea().setName("");
            }
            //pending type
            if(materialMaster.getPendingType().getIntValue()>0) {
                pendingType = pendingTypes.get(materialMaster.getPendingType().getValue());
                if (pendingType == null) {
                    materialMaster.getPendingType().setLabel(materialMaster.getPendingType().getValue() + ":跟踪状态已删除");
                } else {
                    materialMaster.getPendingType().setLabel(pendingType.getLabel());
                }
            }
            //status
            status = statuses.get(materialMaster.getStatus().getValue());
            if(status == null){
                materialMaster.getStatus().setLabel(materialMaster.getStatus().getValue() + ":状态已删除");
            }else {
                materialMaster.getStatus().setLabel(status.getLabel());
            }
            //materialType
            //pendingType = materialTypes.get(materialMaster.getMaterialType().getValue());
            //if(pendingType == null){
            //    materialMaster.getMaterialType().setLabel(materialMaster.getMaterialType().getValue() + ":配件类型已删除");
            //}else{
            //    materialMaster.getMaterialType().setLabel(pendingType.getLabel());
            //}
            //apply type
            pendingType = applyTypes.get(materialMaster.getApplyType().getValue());
            if(pendingType == null){
                materialMaster.getApplyType().setLabel(materialMaster.getApplyType().getValue() + ":申请类型已删除");
            }else{
                materialMaster.getApplyType().setLabel(pendingType.getLabel());
            }
            //createBy
            if(userMaps.containsKey(materialMaster.getCreateBy().getId())){
                user = userMaps.get(materialMaster.getCreateBy().getId());
            }else {
                user = userService.get(materialMaster.getCreateBy().getId());
                userMaps.put(materialMaster.getCreateBy().getId(),user);
            }
            if (user != null) {
                materialMaster.setCreateBy(user);
            }
            //updateBy
            if(userMaps.containsKey(materialMaster.getUpdateBy().getId())){
                user = userMaps.get(materialMaster.getUpdateBy().getId());
            }else {
                user = userService.get(materialMaster.getUpdateBy().getId());
                userMaps.put(materialMaster.getUpdateBy().getId(),user);
            }
            if (user != null) {
                materialMaster.setUpdateBy(user);
            }
            if (materialMaster.getStatus().getIntValue() == 5){//驳回
                materialMaster.setCloseDate(materialMaster.getUpdateDate());
                if(materialMaster.getCloseBy().getId() > 0) {
                    if (userMaps.containsKey(materialMaster.getCloseBy().getId())) {
                        user = userMaps.get(materialMaster.getCloseBy().getId());
                    } else {
                        user = userService.get(materialMaster.getCloseBy().getId());
                        userMaps.put(materialMaster.getCloseBy().getId(), user);
                    }
                    if (user != null) {
                        materialMaster.setCloseBy(user);
                    }
                }
                String closeType = materialMaster.getCloseType();
                if(StrUtil.isNotBlank(closeType)){
                    //驳回原因和详细描述都存储在closeRemark
                    rejectType = rejectTypes.computeIfAbsent(closeType, k -> new Dict(closeType, ""));
                    materialMaster.setCloseType(rejectType.getLabel());
                    if(merchRjectReasonAndRemark) {
                        if (StrUtil.isNotBlank(materialMaster.getCloseRemark())) {
                            materialMaster.setCloseRemark(String.format("%s-%s", rejectType.getLabel(), materialMaster.getCloseRemark()));
                        } else {
                            materialMaster.setCloseRemark(rejectType.getLabel());
                        }
                    }
                }
            } else if(materialMaster.getCloseBy() != null && materialMaster.getCloseBy().getId() != null && materialMaster.getCloseBy().getId()>0) {
                //closeBy
                if (userMaps.containsKey(materialMaster.getCloseBy().getId())) {
                    user = userMaps.get(materialMaster.getCloseBy().getId());
                } else {
                    user = userService.get(materialMaster.getCloseBy().getId());
                    userMaps.put(materialMaster.getCloseBy().getId(), user);
                }
                if (user != null) {
                    materialMaster.setCloseBy(user);
                }
            }

        }
    }

    /**
     * 分页查询已发货返件单
     * @param page
     * @param searchModel
     * @return
     */
    public Page<MaterialReturn> waitSignMaterialReturnList(Page<OrderMaterialSearchModel> page, OrderMaterialSearchModel searchModel){
        searchModel.setPage(page);
        List<MaterialReturn> list = dao.waitSignMaterialReturnList(searchModel);
        getMaterialReturnInfo(list);
        Page<MaterialReturn> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        rtnPage.setList(list);
        return rtnPage;
    }

    /**
     * 读取返件单基本信息
     * 包含客户，区域，产品等
     * @param list
     */
    public void getMaterialReturnInfo(List<MaterialReturn> list){
        Map<Long,Customer> customerHashMap = Maps.newHashMap();
        //改用 productNames 2019/08/09
        //Map<Long,Product> productHashMap = Maps.newHashMap();
        Map<String,Dict> applyTypes = MSDictUtils.getDictMap("material_apply_type");
        Map<String,Dict> companyMap = MSDictUtils.getDictMap("express_type");
        Map<Long,User> userMaps = Maps.newHashMap();
        Map<Long,Area> areaMap = Maps.newHashMap();
        MaterialReturn materialReturn;
        Customer customer;
        Area area;
        Area subArea;
        long id;
        Dict pendingType;
        User user;
        for(int i=0,size=list.size();i<size;i++){
            materialReturn = list.get(i);
            //customer
            id = materialReturn.getCustomer().getId();
            if(customerHashMap.containsKey(id)){
                customer = customerHashMap.get(id);
            }else{
                customer = customerService.getFromCache(id);
                customerHashMap.put(id,customer);
            }
            if(customer != null) {
                materialReturn.setCustomer(customer);
            }

            String[] productNameArr=null;
            List<String> productNameList = Lists.newArrayList();
            if(materialReturn.getProductNames().startsWith(",")){
                productNameArr = org.apache.commons.lang3.StringUtils.split(materialReturn.getProductNames(),",");
                materialReturn.setProductNames(org.apache.commons.lang3.StringUtils.substringBetween(materialReturn.getProductNames(),","));
                if(productNameArr!=null && productNameArr.length>0){
                    productNameList = Lists.newArrayList(productNameArr);
                }
                materialReturn.setProductNameList(productNameList);
            }
            //area
            if(areaMap.containsKey(materialReturn.getArea().getId())){
                area = areaMap.get(materialReturn.getArea().getId());
            }else{
                area = areaService.getFromCache(materialReturn.getArea().getId());
                if(area != null){
                    areaMap.put(materialReturn.getArea().getId(),area);
                }
            }
            if(area != null) {
                materialReturn.setArea(area);
            }
            //sub area
            if(materialReturn.getSubArea().getId()>3){
                if(areaMap.containsKey(materialReturn.getSubArea().getId())){
                    subArea = areaMap.get(materialReturn.getSubArea().getId());
                }else {
                    subArea = areaService.getTownFromCache(area.getId(), materialReturn.getSubArea().getId());
                    if(subArea != null){
                        areaMap.put(materialReturn.getSubArea().getId(),subArea);
                    }
                }
                if(subArea != null){
                    materialReturn.setSubArea(subArea);
                }
            }else{
                materialReturn.getSubArea().setName("");
            }

            //apply type
            pendingType = applyTypes.get(materialReturn.getApplyType().getValue());
            if(pendingType == null){
                materialReturn.getApplyType().setLabel(materialReturn.getApplyType().getValue() + ":申请类型已删除");
            }else{
                materialReturn.getApplyType().setLabel(pendingType.getLabel());
            }
            //upda
            if(userMaps.containsKey(materialReturn.getUpdateBy().getId())){
                user = userMaps.get(materialReturn.getUpdateBy().getId());
            }else {
                user = userService.get(materialReturn.getUpdateBy().getId());
                userMaps.put(materialReturn.getUpdateBy().getId(),user);
            }
            if (user != null) {
                materialReturn.setUpdateBy(user);
            }
            if(StringUtils.isNotBlank(materialReturn.getExpressCompany().getValue())){
                Dict company = companyMap.get(materialReturn.getExpressCompany().getValue());
                if(company != null){
                    materialReturn.setExpressCompany(company);
                }
            }
        }
    }

}

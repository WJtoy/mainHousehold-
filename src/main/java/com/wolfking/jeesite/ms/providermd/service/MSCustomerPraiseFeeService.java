package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeeExamplePicItem;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFeePraiseStandardItem;
import com.kkl.kklplus.entity.praise.CustomerPraisePaymentTypeEnum;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerPraiseFeeFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MSCustomerPraiseFeeService {
    @Autowired
    private MSCustomerPraiseFeeFeign msCustomerPraiseFeeFeign;

    private static final String DICT_DEFAULTPRAISEFEE = "DefaultPraiseFee";        // 缺省好评费
    private static final String DICT_PRAISEFEEEXAMPLEPIC = "PraiseFeeExamplePic";  //好评示例图片
    private static final String DICT_PRAISESTANDARD ="PraiseStandardType";         //好评标准

    /**
     * 根据ID查询客户好评费
     *
     * @param id id
     * @return
     */
    public MDCustomerPraiseFee getById(Long id) {
        return MDUtils.getObjUnnecessaryConvertType(()->msCustomerPraiseFeeFeign.getById(id));
    }

    /**
     * 根据客户ID查询客户好评费
     *
     * @param customerId 客户id
     * @return
     */
    public MDCustomerPraiseFee getByCustomerIdFromCacheForCP(Long customerId) {
        return MDUtils.getObjUnnecessaryConvertType(()->msCustomerPraiseFeeFeign.getByCustomerIdFromCacheForCP(customerId));
    }

    /**
     * 根据客户ID查询客户好评费  2020-4-30
     *
     * @param customerId 客户id
     * @return
     */
    public MDCustomerPraiseFee getByCustomerIdFromCacheNewForCP(Long customerId) {
        MSResponse<MDCustomerPraiseFee> msResponse = msCustomerPraiseFeeFeign.getByCustomerIdFromCacheNewForCP(customerId);
        if (msResponse.getCode() >0) {
            return null;
        } else {
            MDCustomerPraiseFee customerPraiseFee = msResponse.getData();
            List<Dict> praiseStandardList = MSDictUtils.getDictList(DICT_PRAISESTANDARD);
            if (customerPraiseFee == null) {
                customerPraiseFee = new MDCustomerPraiseFee();
                customerPraiseFee.setCustomerId(customerId);
                customerPraiseFee.setPraiseFeeFlag(0);
                //修复客户未设置好评标准，此属性导致空指针问题 2020-07-22
                customerPraiseFee.setOnlineFlag(CustomerPraisePaymentTypeEnum.ONLINE.code);

                Dict pariseRequirementDict = MSDictUtils.getDictByValue("praiseRequirement", DICT_DEFAULTPRAISEFEE);
                customerPraiseFee.setPraiseRequirement(pariseRequirementDict.getRemarks());

                List<MDCustomerPraiseFeePraiseStandardItem> initCustomerPraiseFeePraiseStandardItems = Lists.newArrayList();
                List<MDCustomerPraiseFeePraiseStandardItem> customerPraiseFeePraiseStandardItems = mergeAllItems(initCustomerPraiseFeePraiseStandardItems, praiseStandardList);
                customerPraiseFee.setPraiseStandardItems(customerPraiseFeePraiseStandardItems);
            }
            customerPraiseFee.setExamplePicItems(mergeAllExamplePicItems(praiseStandardList));
            return customerPraiseFee;
        }
    }

    /**
     * 根据客户ID判断客户是否已添加好评费
     */
    public boolean isExistsByCustomerId(Long customerId) {
        Boolean isExists  = MDUtils.getObjUnnecessaryConvertType(()->msCustomerPraiseFeeFeign.isExistsByCustomerId(customerId));
        if (isExists == null) {
            return false;
        }
        return isExists;
    }

    /**
     * 分页获取客户好评费
     *
     * @param mdCustomerPraiseFee
     */
    public Page<MDCustomerPraiseFee> findList(Page<MDCustomerPraiseFee> page, MDCustomerPraiseFee mdCustomerPraiseFee) {
         return MDUtils.findMDEntityListForPage(page, mdCustomerPraiseFee, msCustomerPraiseFeeFeign::findList);
    }

    /**
     * 保存客户好评费记录
     * @param mdCustomerPraiseFee
     */
    public void save(MDCustomerPraiseFee mdCustomerPraiseFee) {
        Boolean isNew = mdCustomerPraiseFee.getIsNewRecord();
        User user = UserUtils.getUser();
        if (user != null) {
            if (isNew) {
                mdCustomerPraiseFee.setCreateById(user.getId());
                mdCustomerPraiseFee.setCreateDate(new Date());
            } else {
                MSResponse<Double> discount = msCustomerPraiseFeeFeign.getDiscountById(mdCustomerPraiseFee.getId());
                mdCustomerPraiseFee.setUpdateById(user.getId());
                mdCustomerPraiseFee.setUpdateDate(new Date());
                LogUtils.saveLog("基础资料-修改客户好评设定", mdCustomerPraiseFee.getCustomerId().toString(),"平台提成保存前:" + discount.getData() + "--平台提成保存后:" + mdCustomerPraiseFee.getDiscount(), null, user);
        }
        }
        MDUtils.customSave(isNew?()->msCustomerPraiseFeeFeign.insert(mdCustomerPraiseFee):()->msCustomerPraiseFeeFeign.update(mdCustomerPraiseFee));
    }

    /**
     * 删除客户好评记录
     * @param mdCustomerPraiseFee
     */
    public void delete(MDCustomerPraiseFee mdCustomerPraiseFee) {
        User user = UserUtils.getUser();
        if (user != null) {
            mdCustomerPraiseFee.setUpdateById(user.getId());
            mdCustomerPraiseFee.setUpdateDate(new Date());
        }
        MDUtils.customSave(()->msCustomerPraiseFeeFeign.delete(mdCustomerPraiseFee));
    }

    public List<MDCustomerPraiseFeePraiseStandardItem> mergeAllItems(List<MDCustomerPraiseFeePraiseStandardItem> items, List<Dict> dicts) {
        if (ObjectUtils.isEmpty(dicts)) {
            return Lists.newArrayList();
        }
        Map<String, MDCustomerPraiseFeePraiseStandardItem> map = Maps.newHashMap();
        if (!ObjectUtils.isEmpty(items)) {
            map  = items.stream().collect(Collectors.toMap(MDCustomerPraiseFeePraiseStandardItem::getCode, Function.identity()));
        }
        List<MDCustomerPraiseFeePraiseStandardItem> returnItems = Lists.newArrayList();
        for(Dict dict : dicts) {
            MDCustomerPraiseFeePraiseStandardItem item = map.get(dict.getValue());
            if (item != null) {
                item.setName(dict.getLabel());
                item.setSort(dict.getSort());
            } else {
                item = new MDCustomerPraiseFeePraiseStandardItem();
                item.setCode(dict.getValue());
                item.setName(dict.getLabel());
                item.setSort(dict.getSort());
                item.setMustFlag(0);     // 是否必选初始化为不选,0-不选，1-选中
                item.setVisibleFlag(0);  // 是否显示初始化为不选,0-不选, 1-选中
                item.setFee(0.0D);
                item.setRemarks("");
            }
            returnItems.add(item);
        }
        return returnItems.stream().sorted(Comparator.comparing(r->r.getSort())).collect(Collectors.toList());
    }

    /**
     * 归并所有的示例图片项次
     * @return
     */
    public List<MDCustomerPraiseFeeExamplePicItem> mergeAllExamplePicItems(List<Dict> dicts) {
        //String strPrfix =
        List<MDCustomerPraiseFeeExamplePicItem> customerPraiseFeeExamplePicItems = Lists.newArrayList();
        //List<Dict> examplePicDictList = MSDictUtils.getDictList(DICT_PRAISEFEEEXAMPLEPIC);
        if (!ObjectUtils.isEmpty(dicts)) {

            for(Dict dict: dicts) {
                MDCustomerPraiseFeeExamplePicItem item = new MDCustomerPraiseFeeExamplePicItem();
                item.setCode(dict.getValue());
                item.setName(dict.getLabel());
                item.setSort(dict.getSort());
                item.setUrl(dict.getRemarks());

                customerPraiseFeeExamplePicItems.add(item);
            }
        }
        return customerPraiseFeeExamplePicItems.stream().sorted(Comparator.comparing(r->r.getSort())).collect(Collectors.toList());
    }
}

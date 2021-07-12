package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDProductInsurance;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.InsurancePrice;
import com.wolfking.jeesite.ms.providermd.feign.MSProductInsuranceFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MSProductInsuranceService  {
    @Autowired
    private MSProductInsuranceFeign msProductInsuranceFeign;

    /**
     * 根据id获取保险价格
     * @param id
     * @return
     */
    public InsurancePrice getById(Long id) {
        return MDUtils.getById(id, InsurancePrice.class, msProductInsuranceFeign::getById);
    }

    /**
     * 分页列表
     * @param insurancePricePage
     * @param insurancePrice
     * @return
     */
    public Page<InsurancePrice> findList(Page<InsurancePrice> insurancePricePage, InsurancePrice insurancePrice) {
        return MDUtils.findListForPage(insurancePricePage, insurancePrice, InsurancePrice.class, MDProductInsurance.class, msProductInsuranceFeign::findList);
    }

    public List<InsurancePrice> findAllList() {
        return MDUtils.findAllList(InsurancePrice.class, msProductInsuranceFeign::findAllList);
    }

    /**
     * 添加/更新
     * @param insurancePrice
     * @param isNew
     * @return
     */
    public MSErrorCode save(InsurancePrice insurancePrice, boolean isNew) {
        return MDUtils.genericSave(insurancePrice, MDProductInsurance.class, isNew, isNew?msProductInsuranceFeign::insert:msProductInsuranceFeign::update);
    }

    /**
     * 删除
     * @param insurancePrice
     * @return
     */
    public MSErrorCode delete(InsurancePrice insurancePrice) {
        return MDUtils.genericSave(insurancePrice, MDProductInsurance.class, false, msProductInsuranceFeign::delete);
    }

    public MSResponse<Long> getIdByCategoryId(Long productCategoryId){
        return msProductInsuranceFeign.getIdByCategoryId(productCategoryId);
    }
}

package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDProductTimeLiness;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.TimeLinessPrice;
import com.wolfking.jeesite.ms.providermd.feign.MSProductTimeLinessFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MSProductTimeLinessService {

    @Autowired
    private MSProductTimeLinessFeign msProductTimeLinessFeign;
    /**
     * 根据产品类别获取产品价格
     * @param productCategoryId
     * @return
     */
    public List<TimeLinessPrice> getPrices(Long productCategoryId) {
        return MDUtils.findListByCustomCondition(productCategoryId, TimeLinessPrice.class, msProductTimeLinessFeign::getPrices);
    }

    /**
     * 查询所有的时效补贴信息
     * @return
     */
    public List<TimeLinessPrice> findAllList() {
        return MDUtils.findAllList(TimeLinessPrice.class, msProductTimeLinessFeign::findAllList);
    }

    /**
     * 根据产品类别删除
     * @param ProductCategoryId
     * @return
     */
    public MSErrorCode deleteByProductCategoryId(Long ProductCategoryId) {
        MSResponse<Integer> msResponse = msProductTimeLinessFeign.deleteByProductCategoryId(ProductCategoryId);
        return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
    }

    /**
     * 批量插入数据
     * @param timeLinessPriceList
     * @return
     */
    public MSErrorCode batchInsert(List<TimeLinessPrice> timeLinessPriceList) {
        return MDUtils.genericBatchSave(timeLinessPriceList, MDProductTimeLiness.class, msProductTimeLinessFeign::batchInsert);
    }
}

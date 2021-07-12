package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDProductPicMapping;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePic;
import com.wolfking.jeesite.ms.providermd.feign.MSProductPicMappingFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MSProductPicMappingService {
    @Autowired
    private MSProductPicMappingFeign msProductPicMappingFeign;

    /**
     * 根据id获取完工图片
     * @param id
     * @return
     */
    public ProductCompletePic get(Long id) {
        return MDUtils.getById(id, ProductCompletePic.class, msProductPicMappingFeign::get);
    }

    /**
     * 根据id获取完工图片
     * @param productId
     * @return
     */
    public ProductCompletePic getByProductId(Long productId) {
        return MDUtils.getById(productId, ProductCompletePic.class, msProductPicMappingFeign::getByProductId);
    }

    /**
     * 获取所有的完工图片
     * @return
     */
    public List<ProductCompletePic> findAllList() {
        return MDUtils.findAllList(ProductCompletePic.class, msProductPicMappingFeign::findAllList);
    }

    /**
     * 获取分页完工图片
     * @param productCompletePicPage
     * @param productCompletePic
     * @return
     */
    public Page<ProductCompletePic> findList(Page<ProductCompletePic> productCompletePicPage, ProductCompletePic productCompletePic) {
        return MDUtils.findListForPage(productCompletePicPage, productCompletePic, ProductCompletePic.class, MDProductPicMapping.class,msProductPicMappingFeign::findList);
    }

    /**
     * 添加/更新
     * @param productCompletePic
     * @param isNew
     * @return
     */
    public MSErrorCode save(ProductCompletePic productCompletePic, boolean isNew) {
        return MDUtils.genericSave(productCompletePic, MDProductPicMapping.class, isNew, isNew?msProductPicMappingFeign::insert:msProductPicMappingFeign::update);
    }

    /**
     * 删除
     * @param productCompletePic
     * @return
     */
    public MSErrorCode delete(ProductCompletePic productCompletePic) {
        return MDUtils.genericSave(productCompletePic, MDProductPicMapping.class, false, msProductPicMappingFeign::delete);
    }
}

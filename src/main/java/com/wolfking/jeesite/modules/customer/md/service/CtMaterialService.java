package com.wolfking.jeesite.modules.customer.md.service;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductMaterial;
import com.wolfking.jeesite.ms.providermd.service.MSMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CtMaterialService {

    @Autowired
    private MSMaterialService msMaterialService;
    /**
     * 根据产品ID查找配件列表
     * @param productId
     * @return
     */
    public List<Material> getMaterialListByProductId(Long productId){
        //return dao.getMaterialListByProductId(productId);
        return findMaterialsByProductIdMS(productId);
    }

    /**
     * 根据产品id获取配件
     * @param productId
     * @return
     */
    public List<Material> findMaterialsByProductIdMS(long productId) {
        List<ProductMaterial> productMaterialList = msMaterialService.findMaterialIdByProductId(productId);
        return findMaterialListMS(productMaterialList);
    }

    private List<Material> findMaterialListMS(List<ProductMaterial> productMaterialList){
        List<Material> list = Lists.newArrayList();
        if (productMaterialList != null && productMaterialList.size() > 0) {
            Map<Long, Material> map = msMaterialService.findAllMaterialMap();
            if (map != null && map.size() > 0) {
                for (ProductMaterial item : productMaterialList) {
                    Material material = map.get(item.getMaterialId());
                    if (material != null) {
                        Material materialEntity = new Material();
                        materialEntity.setId(material.getId());
                        materialEntity.setName(material.getName());
                        materialEntity.setIsReturn(material.getIsReturn());
                        materialEntity.setPrice(material.getPrice());
                        materialEntity.setModel(material.getModel());
                        materialEntity.setProduct(new Product(item.getProductId()));
                        materialEntity.setRecycleFlag(material.getRecycleFlag());
                        materialEntity.setRecyclePrice(material.getRecyclePrice());
                        materialEntity.setRemarks(material.getRemarks());
                        list.add(materialEntity);
                    }
                }
            }
        }
        return list;
    }
}

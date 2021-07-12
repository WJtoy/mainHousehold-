package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDMaterial;
import com.kkl.kklplus.entity.md.MDProductMaterial;
import com.kkl.kklplus.entity.viomi.sd.ProductParts;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.md.entity.ProductMaterial;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSMaterialFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.viomi.sd.feign.VioMiOrderFeign;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MSMaterialService {

    @Autowired
    private MSMaterialFeign msMaterialFeign;

    @Autowired
    private MapperFacade mapper;

    @Autowired
    private VioMiOrderFeign vioMiOrderFeign;

    /**
     * 根据id获取配件信息
     * @param id
     * @return
     */
    public Material getById(Long id) {
        return MDUtils.getById(id, Material.class, msMaterialFeign::getById);
    }

    /**
     * 根据配件name获取id
     * @param material
     * @return
     */
    public Long getIdByName(Material material) {
        MSResponse<Long> msResponse = msMaterialFeign.getIdByName(material.getName());
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 获取所有数据
     * @return
     */
    public List<Material> findAllList() {
        return MDUtils.findAllList(Material.class, msMaterialFeign::findAllList);
    }

    /**
     * 获取分页数据
     * @param materialPage
     * @param material
     * @return
     */
    public Page<Material> findList(Page<Material> materialPage, Material material) {
        return MDUtils.findListForPage(materialPage, material, Material.class, MDMaterial.class, msMaterialFeign::findList);
    }


    /**
     * 添加/更新
     * @param material
     * @param isNew
     * @return
     */
    public MSErrorCode save(Material material, boolean isNew) {
        return MDUtils.genericSave(material, MDMaterial.class, isNew, isNew?msMaterialFeign::insert:msMaterialFeign::update);
    }

    /**
     * 删除
     * @param material
     * @return
     */
    public MSErrorCode delete(Material material) {
        return MDUtils.genericSave(material, MDMaterial.class, false, msMaterialFeign::delete);
    }

    /**
     * 根据配件类别id获取配件id
     * @param materialCategoryId
     * @return
     */
    public Long getByMaterialCategoryId(long materialCategoryId){
        MSResponse<Long> msResponse = msMaterialFeign.getByMaterialCategoryId(materialCategoryId);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }

    /**
     * 根据配件id集合获取配件
     * @param materialIds
     * @return
     */
    public List<Material> findMaterialListByIds(List<Long> materialIds){
       MSResponse<List<MDMaterial>> msResponse = msMaterialFeign.findMaterialListByIds(materialIds);
       if(MSResponse.isSuccess(msResponse)){
            List<MDMaterial> mdMaterialList = msResponse.getData();
            List<Material> materialList = mapper.mapAsList(mdMaterialList,Material.class);
            return materialList;
       }else{
           return Lists.newArrayList();
       }

    }

    /**
     * 根据产品Id获取产品配件列表
     * @param productId
     * @return
     */
    public List<ProductMaterial> findMaterialIdByProductId(Long productId){
        MSResponse<List<MDProductMaterial>> msResponse = msMaterialFeign.findMaterialIdByProductId(productId);
        return findProductMaterial(msResponse);
    }


    /**
     * 根据产品Id集合获取产品配件列表
     * @param productIds
     * @return
     */
    public List<ProductMaterial> findProductMaterialListByProductIdList(List<Long> productIds){
        MSResponse<List<MDProductMaterial>> msResponse = msMaterialFeign.findProductMaterialListByProductIdList(productIds);
        return findProductMaterial(msResponse);
    }

    private List<ProductMaterial> findProductMaterial(MSResponse<List<MDProductMaterial>> msResponse){
        if(MSResponse.isSuccess(msResponse)){
            List<MDProductMaterial> mdProductMaterialList = msResponse.getData();
            List<ProductMaterial> productMaterialList = mapper.mapAsList(mdProductMaterialList,ProductMaterial.class);
            return productMaterialList;
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * list配件集合转map
     * @return
     */
    public Map<Long,Material> findAllMaterialMap(){
        List<Material> list= findAllList();
        Map<Long,Material> map =new HashMap<>();
        if(list!=null && list.size()>0){
            map = list.stream().distinct().collect(Collectors.toMap(Material::getId, material -> material));
        }
        return map;
    }


    /**
     * 获取密码产品配件
     * @param product69Code
     * @return
     */
    public List<ProductParts> getProductParts(String product69Code) {
        User user = UserUtils.getUser();
        MSResponse<List<ProductParts>> msResponse = vioMiOrderFeign.getProductParts(product69Code, user.getId());
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("调用云米微服务获取产品配件失败,失败原因:"+msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public List<Material> findIdAndNameByCategoryId(Long materialCategoryId){
        MSResponse<List<NameValuePair<Long, String>>> msResponse = msMaterialFeign.findIdAndNameByCategoryId(materialCategoryId);
        List<NameValuePair<Long, String>> nameValuePairList = msResponse.getData();
        if (nameValuePairList != null && !nameValuePairList.isEmpty()) {
            return nameValuePairList.stream().map(nv->{
                Material material = new Material();
                material.setId(nv.getName());
                material.setName(nv.getValue());
                return material;
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public Long saveMaterial(Material material){
        MDMaterial mdMaterial = mapper.map(material,MDMaterial.class);
        MSResponse<Long> msResponse = msMaterialFeign.save(mdMaterial);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("保存配件失败,失败原因:"+msResponse.getMsg());
        }
        return msResponse.getData();
    }
}

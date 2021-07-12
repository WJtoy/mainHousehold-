package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDMaterialRequirement;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.MaterialDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.utils.MaterialRequirementAdapter;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerMaterialService;
import com.wolfking.jeesite.ms.providermd.service.MSMaterialService;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Jeff on 2017/4/24.
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MaterialService extends LongIDCrudService<MaterialDao, Material> {

    /* //mark on 2020-1-8
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MaterialCategoryService materialCategoryService;
    */

    private static final String DICT_TYPE = "MaterialRequirement";

    @Autowired
    private MSMaterialService msMaterialService;

    @Autowired
    private MSCustomerMaterialService msCustomerMaterialService;

    @Autowired
    private MSDictService msDictService;

    /**
     * 根据id获取配件类别信息
     * @param id
     * @return
     */
    @Override
    public Material get(long id){
       return msMaterialService.getById(id);
    }

    /**
     * 分页查询配件信息
     * @param page
     * @return
     */
    @Override
    public Page<Material> findPage(Page<Material> page, Material material){
        return msMaterialService.findList(page,material);
    }

    @Override
    @Transactional()
    public void save(Material material){
        boolean isNew = material.getIsNewRecord();
        //super.save(material); //mark on 2020-1-7
        //调用微服务 2019-9-11
        if(material.getRecycleFlag() == 0){
            material.setRecyclePrice(0.0D);
        }
        MSErrorCode msErrorCode = msMaterialService.save(material,isNew);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("保存配件失败.失败原因:" + msErrorCode.getMsg());
        }
        /*
        // mark on 2020-1-7 begin
        if (redisUtils.zCount(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL, material.getId(), material.getId()) > 0) {
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL, material.getId(), material.getId());
        }
        redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL, material, material.getId(), 0);
        // mark on 2020-1-7 end
        */
    }

    @Override
    @Transactional()
    public void delete(Material material){
        //super.delete(material);  // mark on 2020-1-7
        //调用微服务 2019-9-11
        MSErrorCode msErrorCode = msMaterialService.delete(material);
        if(msErrorCode.getCode()>0){
            throw new RuntimeException("删除配件失败.失败原因:" + msErrorCode.getMsg());
        }
        /*
        // mark on 2020-1-7
        if (redisUtils.zCount(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL, material.getId(), material.getId()) > 0) {
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL, material.getId(), material.getId());
        }
        */
    }

    /**
     * 根据配件名称获取配件ID，最多一条，用于判断配件名称是否存在于数据库中
     * @param material
     * @return
     */
    public Boolean isExistMaterialName(Material material){

        //return dao.getIdByName(material) != null;
        Long id = msMaterialService.getIdByName(material);
        if(material.getId()==null || material.getId()<=0){
            return (id==null || id<=0)?false:true;
        }else{
            if((id!=null && id>0) && !id.equals(material.getId())){
                return true;
            }else{
                return false;
            }
        }
    }

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
     * 根据产品ID列表查找配件列表
     * @param productIds
     * @return
     */
    public List<Material> getMaterialListByProductIdList(List<Long> productIds){
        //return dao.getMaterialListByProductIdList(productIds);
        List<Material> materialListFromMS = findMaterialsByProductIdListMS(productIds);
        return materialListFromMS;
    }

    public List<Material> getMaterialListByProductIdListNew(Integer datasourceId, Long customerId, List<NameValuePair<Long,String>> nameValuePairs) {
        if (nameValuePairs == null || nameValuePairs.isEmpty()) {
            return Lists.newArrayList();
        }
        if (B2BDataSourceEnum.VIOMI.id == datasourceId) {
            return findMaterialsByCustomerIdAndProductIdsMS(customerId, nameValuePairs);
        } else {
            List<Long> productIds = nameValuePairs.stream().map(NameValuePair::getName).collect(Collectors.toList());
            return getMaterialListByProductIdList(productIds);
        }
    }


    /****************************************************************************
     * redis操作
     ****************************************************************************/

    /**从数据库读取产品信息至缓存
     * @return
     */
    /*
    // mark on 2020-1-8  // 此方法没有地方调用，全部注释
    private List<Material> loadMaterialDataFromDB2Cache(){
        //List<Material> list = super.findAllList();
        //调用微服务 2019-9-12
        List<Material> list = msMaterialService.findAllList();
        List<MaterialCategory> materialCategoryList = materialCategoryService.findAllList();
        Map<Long,MaterialCategory> map = materialCategoryList.stream().collect(Collectors.toMap(MaterialCategory::getId,materialCategory ->materialCategory));
        if(list != null && list.size()>0) {
            for (Material material : list) {
//                //缓存配件信息
//                redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL, material.getId().toString(), material, 0);
                //缓存配件列表
                MaterialCategory materialCategory = map.get(material.getMaterialCategory().getId());
                if(materialCategory!=null){
                    material.setMaterialCategory(materialCategory);
                }
                //redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL, material, material.getId(), 0);  //mark on 2020-1-7
            }
        }
        return list;
    }
    */


    /**
     * 加载所有配件，当缓存未命中则从数据库装载至缓存
     * @return
     */
    @Override
    public List<Material> findAllList(){
        //调用微服务 start 2019-9-12
        List<Material> list = msMaterialService.findAllList();
        if(list!=null && list.size()>0){
            return list;
        }
        return Lists.newArrayList();
        //end 2019-9-12

        /*
        // mark on 2020-1-7 begin
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL);
        if (!isExistsCache){
            return loadMaterialDataFromDB2Cache();
        }

        List<Material> materialList = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL,0,-1, Material.class);
        return materialList;
        // mark on 2020-1-7 end
        */
    }

    /**
     * 获得配件信息
     * @param id
     * @return
     */
    public Material getFromCache(long id) {
        //调用微服务 start 2019-9-12
        Material material = get(id);
        if(material!=null){
            return material;
        }
        return null;
        // end 2019-9-12
        /*
        // mark on 2020-1-7 begin
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL);
        if (!isExistsCache){
            loadMaterialDataFromDB2Cache();
        }
        try {
            return (Material) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL, id, id, Material.class);
        }catch (Exception e){
            return null;
        }
        // mark on 2020-1-7 end
         */
    }

    /**
     * 根据配件类别获取配件ID，最多一条，用于判断删除配件类别是是否已经绑定配件
     * @param materialCategoryId
     * @return
     */
    public Long getByMaterialCategoryId(Long materialCategoryId){
        //return dao.getByMaterialCategoryId(materialCategoryId);
        //调用微服务
       return msMaterialService.getByMaterialCategoryId(materialCategoryId);
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

    /**
     * 根据产品id集合获取配件
     * @param productIds
     * @return
     */
    public List<Material> findMaterialsByProductIdListMS(List<Long> productIds){
        List<ProductMaterial> productMaterialList = msMaterialService.findProductMaterialListByProductIdList(productIds);
        return findMaterialListMS(productMaterialList);
    }

    public List<Material> findMaterialsByCustomerIdAndProductIdMS(Long customerId, Long productId, String customerModel){
        List<ProductMaterial> productMaterialList = msCustomerMaterialService.findProductMaterialByCustomerAndProduct(customerId, productId, customerModel);
        return findMaterialListMS(productMaterialList);
    }

    public List<Material> findMaterialsByCustomerIdAndProductIdsMS(Long customerId, List<NameValuePair<Long,String>> nameValuePairs){
        List<ProductMaterial> productMaterialList = msCustomerMaterialService.findProductMaterialByCustomerAndProductIds(customerId, nameValuePairs);
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
                        materialEntity.setRecycleFlag(material.getRecycleFlag());
                        materialEntity.setRecyclePrice(material.getRecyclePrice());
                        materialEntity.setModel(material.getModel());
                        materialEntity.setProduct(new Product(item.getProductId()));
                        materialEntity.setRemarks(material.getRemarks());
                        list.add(materialEntity);
                    }
                }
            }
        }
        return list;
    }

    public List<MDMaterialRequirement> findMaterialRequirementList() {
        List<Dict> picTypes = MSDictUtils.getDictList(DICT_TYPE);

        List<MDMaterialRequirement> returnItems = Lists.newArrayList();
        if (!ObjectUtils.isEmpty(picTypes)) {
            for (Dict dict : picTypes) {
                MDMaterialRequirement mdMaterialRequirement = new MDMaterialRequirement();
                mdMaterialRequirement.setCode(dict.getValue());
                mdMaterialRequirement.setName(dict.getLabel());
                mdMaterialRequirement.setSort(dict.getSort());
                if (!StringUtils.isBlank(dict.getDescription())) {
                    String description = dict.getDescription();
                    description = description.replace("(","{").replace(")","}").replace("'","\"");
                    try {
                        MDMaterialRequirement jsonMaterialRequirement = GsonUtils.getInstance().fromJson(description, MDMaterialRequirement.class);
                        if (jsonMaterialRequirement != null) {
                            mdMaterialRequirement.setUrl(jsonMaterialRequirement.getUrl());
                            mdMaterialRequirement.setMustFlag(jsonMaterialRequirement.getMustFlag());
                            mdMaterialRequirement.setVisibleFlag(jsonMaterialRequirement.getVisibleFlag());
                        }
                    } catch(Exception ex) {
                        log.error("从字典中获取配件的照片要求json数据解析出错，{}", ex.getStackTrace());
                    }
                }

                mdMaterialRequirement.setRemarks(dict.getRemarks());

                returnItems.add(mdMaterialRequirement);
            }
        }
        return returnItems.stream().sorted(Comparator.comparing(MDMaterialRequirement::getSort)).collect(Collectors.toList());
    }

    public void saveMaterialRequirementList(List<MDMaterialRequirement> materialRequirementList) {
        List<Dict> picTypes = MSDictUtils.getDictList(DICT_TYPE);
        if (ObjectUtils.isEmpty(materialRequirementList)) {
            throw new RuntimeException("照片要求数据为空.");
        }
        Map<String, MDMaterialRequirement> map = materialRequirementList.stream().collect(Collectors.toMap(r->r.getCode(), Function.identity()));
        if (!ObjectUtils.isEmpty(picTypes)) {
            for (Dict dict : picTypes) {
                MDMaterialRequirement mdMaterialRequirement = map.get(dict.getValue());
                if (mdMaterialRequirement != null) {
                    try {
                        Gson gson = new GsonBuilder().registerTypeAdapter(MDMaterialRequirement.class, MaterialRequirementAdapter.getInstance()).create();
                        String json = gson.toJson(mdMaterialRequirement, new TypeToken<MDMaterialRequirement>() {
                        }.getType());

                        json = json.replace("{", "(").replace("}", ")").replace("\"", "'");
                        dict.setDescription(json);
                    } catch(Exception ex) {
                        log.error("配件的照片要求数据保存到字典中转换成json出错，{}", ex.getStackTrace());
                    }
                    dict.setRemarks(mdMaterialRequirement.getRemarks());
                    msDictService.save(dict);
                }
            }
        }
    }
}

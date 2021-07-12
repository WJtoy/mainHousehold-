package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDCustomerMaterial;
import com.kkl.kklplus.entity.viomi.sd.ProductParts;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.dao.CustomerMaterialDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.mapper.MaterialMapper;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerMaterialService;
import com.wolfking.jeesite.ms.providermd.service.MSMaterialService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 客户配件Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerMaterialService extends LongIDCrudService<CustomerMaterialDao, CustomerMaterial> {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MSCustomerMaterialService msCustomerMaterialService;

    @Autowired
    private MSMaterialService msMaterialService;

    /**
     * 分页查询配件信息
     * @param page
     * @return
     */
    @Override
    public Page<CustomerMaterial> findPage(Page<CustomerMaterial> page, CustomerMaterial customerMaterial){
        return msCustomerMaterialService.findList(page,customerMaterial);
    }
    /**
     * 根据客户品类产品分页获取分页数据
     * @param page
     * @return
     */
    public Page<CustomerMaterial> findAllPage(Page<CustomerMaterial> page, CustomerMaterial customerMaterial){
        return msCustomerMaterialService.findAllList(page,customerMaterial);
    }

    /**
     * 根据Id获取
     * @param id
     * @return
     */
    @Override
    public CustomerMaterial get(long id){
        return msCustomerMaterialService.getById(id);
    }


    /**
     * 添加数据
     * @param  customerMaterial
     * @return Long
     */
    @Override
    @Transactional()
    public void save(CustomerMaterial customerMaterial){
        Customer customer = customerMaterial.getCustomer();
        Product product = customerMaterial.getProduct();
        //dao.deleteByCustomerAndProduct(customer.getId(),product.getId()); //mark on 2020-1-10
        Map<String,Object> map = Maps.newHashMap();
        List<CustomerMaterial> list = Lists.newArrayList();
        CustomerMaterial entity;
        for(CustomerMaterialItem item:customerMaterial.getItemList()){
            entity = new CustomerMaterial();
            Material material = new Material();
            material.setId(item.getMaterialId());
            entity.setCustomer(customer);
            entity.setProduct(product);
            entity.setMaterial(material);
            entity.setIsReturn(item.getIsReturn());
            entity.setCustomerPartCode(item.getCustomerPartCode()==null?"":item.getCustomerPartCode());
            entity.setCustomerPartName(item.getCustomerPartName()==null?"":item.getCustomerPartName());
            entity.setWarrantyDay(item.getWarrantyDay()==null? 0:item.getWarrantyDay());
            entity.setPrice(item.getPrice());
            entity.setRecycleFlag(item.getRecycleFlag());
            if(item.getRecycleFlag() == 0){
                entity.setRecyclePrice(0.0D);
            }else {
                entity.setRecyclePrice(item.getRecyclePrice());
            }
            entity.setRemarks(item.getRemarks());
            entity.preInsert();
            //super.save(entity);  //mark on 2020-1-10
            map.put(item.getMaterialId().toString(),entity);
            list.add(entity);
        }
        //调用微服务 2019-9-21
        if (list.isEmpty()) {
            if(customer.getId() != null && product.getId() != null){
                msCustomerMaterialService.deleteByCustomerAndProduct(customer.getId(), product.getId());
            }
        } else {
            MSErrorCode msErrorCode = msCustomerMaterialService.batchInsert(list);
            if (msErrorCode.getCode() > 0) {
                throw new RuntimeException("保存客户配件.失败原因:" + msErrorCode.getMsg());
            }
        }
        /*
        // mark on 2020-1-11
        String key = String.format(RedisConstant.MD_CUSTOMER_MATERIAL,customer.getId(),product.getId());
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB,key);
        redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_MD_DB,key,map,0L);
        */
    }

    /**
     * 添加数据
     * @param  customerMaterial
     * @return Long
     */
    @Override
    @Transactional()
    public void delete(CustomerMaterial customerMaterial){
        //customerMaterial = super.get(customerMaterial.getId());
        //super.delete(customerMaterial);  //mark on 2020-1-10
        //调用微服务 2019-9-21
        MSErrorCode msErrorCode = msCustomerMaterialService.delete(customerMaterial);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("删除客户配件.失败原因:"+msErrorCode.getMsg());
        }
       /*
       // mark on 2020-1-11
       String key = String.format(RedisConstant.MD_CUSTOMER_MATERIAL,customerMaterial.getCustomer().getId(),customerMaterial.getProduct().getId());
       redisUtils.hdel(RedisConstant.RedisDBType.REDIS_MD_DB,key,customerMaterial.getMaterial().getId().toString());
       */
    }

    /**
     * 根据客户,产品获取数据
     * @param  customerId,productId
     * @return customerMaterial
     */
    public List<CustomerMaterial> getByCustomerAndProduct(Long customerId,Long productId){
       //调用微服务 2019-9-23
       List<CustomerMaterial> list = msCustomerMaterialService.findListByCustomerAndProduct(customerId,productId);
       if(list!=null && !list.isEmpty()){
           return list;
       }
       return Lists.newArrayList();
       //调用微服务end
       /*
       // mark on 2020-1-10
       list = dao.getListByCustomerAndProduct(customerId,productId);
       if(list == null || list.size()<=0){
           list = Lists.newArrayList();
       }
       return list;
       */
    }

    /**
     * 检测数据是否已经存在
     * @param  customerMaterial
     * @return Long
     */
    /*
    // mark on 2020-1-10
    public Long checkIsExist(CustomerMaterial customerMaterial){
        return dao.checkIsExist(customerMaterial);
    }
    */

    /****************************************************************************
     * redis操作
     ****************************************************************************/

    /**
     * 根据客户Id 产品Id,配件Id获取去缓存
     */
    public CustomerMaterial getFromCache(long customerId,long productId,Long mateiralId){
        //调用微服务 2019-9-20 start
        CustomerMaterial customerMaterial = msCustomerMaterialService.getCustomerMaterialByCustomerAndProductAndMaterial(customerId,productId,mateiralId);
        if(customerMaterial!=null){
            return customerMaterial;
        }
        return null;
        //调用微服务 end
        /*
        // mark on 2020-1-10
        String key = String.format(RedisConstant.MD_CUSTOMER_MATERIAL,customerId,productId);
        customerMaterial = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_MD_DB,key,mateiralId.toString(),CustomerMaterial.class);
        if(customerMaterial ==null){
            customerMaterial =dao.getByCustomerAndProductAndMaterial(customerId,productId,mateiralId);
            if(customerMaterial !=null){
                redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_MD_DB,key,customerMaterial.getMaterial().getId().toString(),customerMaterial,0L);
            }
        }
        return customerMaterial;
        */
    }

    /**
     * 根据客户Id + 产品Id 读取配件设定列表
     */
    public List<CustomerMaterial> getListFromCache(long customerId,long productId){
        //调用微服务 2019-9-23 start
        List<CustomerMaterial> customerMaterials = msCustomerMaterialService.findListByCustomerAndProduct(customerId,productId);
        if(customerMaterials!=null && customerMaterials.size()>0){
            return customerMaterials;
        }
        return Lists.newArrayList();
        /*
        //mark on 2020-1-11 begin
        String key = String.format(RedisConstant.MD_CUSTOMER_MATERIAL,customerId,productId);
        List<CustomerMaterial> list = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> maps = redisUtils.hGetAll(RedisConstant.RedisDBType.REDIS_MD_DB,key);
        if (maps != null && maps.size() > 0) {
            CustomerMaterial material;
            for (Map.Entry<String, byte[]> entry : maps.entrySet()) {
                material = (CustomerMaterial) redisUtils.gsonRedisSerializer.fromJson(StringUtils.toString(entry.getValue()), CustomerMaterial.class);
                list.add(material);
            }
        }else{
            list =dao.getListByCustomerAndProduct(customerId,productId);
            if(!ObjectUtils.isEmpty(list)){
                Map<String,Object> map = Maps.newHashMap();
                for(CustomerMaterial entity:list){
                    map.put(entity.getMaterial().getId().toString(),entity);
                }
                redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_MD_DB,key,map,0L);
            }else{
                list = Lists.newArrayList();
            }
        }
        return list;
        //mark on 2020-1-11 end
        */
    }

    /**
     * 装载所有数据到缓存
     * @param
     * @return
     */
    /*
    // mark on 2020-1-10
    public void loadDataFromDB2Cache(){
        List<CustomerMaterial> list = dao.findAllList();
        if(list!=null && list.size()>0){
            Map<Long,List<CustomerMaterial>> customerIdMap = Maps.newHashMap();
            for(CustomerMaterial item:list){
                if(!customerIdMap.containsKey(item.getCustomer().getId())){
                    List<CustomerMaterial>  customerMaterials = Lists.newArrayList();
                    customerMaterials = list.stream().filter(t->t.getCustomer().getId().equals(item.getCustomer().getId())).collect(Collectors.toList());
                    if(customerMaterials!=null && customerMaterials.size()>0){
                        customerIdMap.put(item.getCustomer().getId(),customerMaterials);
                    }
                }
            }
            if(customerIdMap!=null && customerIdMap.size()>0){
                for(Map.Entry<Long,List<CustomerMaterial>> itemList:customerIdMap.entrySet()){
                    Map<Long,List<CustomerMaterial>> productIdMap = Maps.newHashMap();
                    for(CustomerMaterial customerMaterial:itemList.getValue()){
                        if(!productIdMap.containsKey(customerMaterial.getProduct().getId())){
                            List<CustomerMaterial> customerMaterials = Lists.newArrayList();
                            customerMaterials = itemList.getValue().stream().filter(t->t.getProduct().getId().equals(customerMaterial.getProduct().getId())).collect(Collectors.toList());
                            if(customerMaterials !=null && customerMaterials.size()>0){
                                Map<String,Object> map = Maps.newHashMap();
                                for(CustomerMaterial entity:customerMaterials){
                                    map.put(entity.getMaterial().getId().toString(),entity);
                                }
                                String key = String.format(RedisConstant.MD_CUSTOMER_MATERIAL,itemList.getKey(),customerMaterial.getProduct().getId(),customerMaterial.getProduct().getId());
                                redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_MD_DB,key,map,0L);
                            }
                        }
                    }
                }
            }
        }
    }*/

    /**
     * 读取配件返件标识及价格
     * 1.先读取客户配置
     * 2.1没有设定，读取配件设定
     * @param customerId
     * @param productId
     * @param materialId
     * @return Material(isReturn,price)
     */
    public Material getMaterialInfoOfCustomer(long customerId, long productId, long materialId){
        CustomerMaterial customerMaterial = getFromCache(customerId,productId,materialId);
        Material material = materialService.getFromCache(materialId);
        if(material == null){
            return material;
        }
        if(customerMaterial != null){
            material.setIsReturn(customerMaterial.getIsReturn());
            material.setPrice(customerMaterial.getPrice());
        }
        return material;
    }


    /**
     * 根据客户Id + 产品Id 读取配件设定列表
     * 1.先读取客户配置
     * 2.再读取配件设定，以1为准
     */
    public Map<Long,Material> getMapFromCache(long customerId,long productId){
        Map<Long,Material> maps = Maps.newHashMap();
        //1.先读取客户配置
        List<CustomerMaterial> list = getListFromCache(customerId,productId);
        if(!ObjectUtils.isEmpty(list)){
            MaterialMapper mapper = Mappers.getMapper(MaterialMapper.class);
            maps.putAll(list.stream().collect(Collectors.toMap(CustomerMaterial::getMaterialId, item -> mapper.customerToMaterial(item))));
        }
        //2.get material
        List<Material> materials = materialService.getMaterialListByProductId(productId);
        Material material;
        if(!ObjectUtils.isEmpty(materials)){
            materials.stream().forEach(t->{
                if(maps.containsKey(t.getId())){
                    Material m = maps.get(t.getId());
                    if(StringUtils.isBlank(m.getName())){
                        m.setName(t.getName());
                    }
                }else{
                    maps.put(t.getId(),t);
                }
            });
        }
        return maps;
    }


    /**
     * 读取客户配件列表
     * @param cid
     * @param ids
     * @return
     */
    public List<CustomerMaterial> getMaterialListByProductIdList(long cid, List<Long> ids) {
        List<CustomerMaterial> allList = Lists.newArrayList();
        List<CustomerMaterial> list;
        long pid;
        for(int i=0,size=ids.size();i<size;i++) {
            pid = ids.get(i);
            list = getListFromCache(cid, pid);
            if(!ObjectUtils.isEmpty(list)){
                allList.addAll(list);
            }
        }
        return allList;
    }

    /**
     * 更新客户配件列表
     * @param customerId
     * @param productId
     * @param customerProductModelId
     * @param customerModeId
     */
    public String updateMaterialList(Long customerId, Long productId, Long customerProductModelId, String customerModeId) {
        List<ProductParts> productPartsList = msMaterialService.getProductParts(customerModeId);
        User user = UserUtils.getUser();
        Long userId = Optional.ofNullable(user).map(r->r.getId()).orElse(0L);
        String strReturnMsg = "";
        List<MDCustomerMaterial> mdCustomerMaterialList = Lists.newArrayList();
        if (!ObjectUtils.isEmpty(productPartsList)) {
            for(ProductParts productParts: productPartsList) {
               MDCustomerMaterial mdCustomerMaterial = new MDCustomerMaterial();
               mdCustomerMaterial.setCustomerId(customerId);
               mdCustomerMaterial.setProductId(productId);
               mdCustomerMaterial.setCustomerProductModelId(customerProductModelId);
               mdCustomerMaterial.setMaterialId(0L);
               mdCustomerMaterial.setIsReturn(productParts.getReturnFactory().equals("是")?1:0);
               mdCustomerMaterial.setPrice(Double.valueOf(productParts.getPriceNew()));
               mdCustomerMaterial.setCustomerPartCode(productParts.getYunmiCode());
               mdCustomerMaterial.setCustomerPartName(productParts.getPartsName());
               mdCustomerMaterial.setWarrantyDay(productParts.getWarrantyDay());
               java.util.Date currentDate = new java.util.Date();
               mdCustomerMaterial.setCreateById(userId);
               mdCustomerMaterial.setCreateDate(currentDate);
               mdCustomerMaterial.setUpdateById(userId);
               mdCustomerMaterial.setUpdateDate(currentDate);

               mdCustomerMaterialList.add(mdCustomerMaterial);
            }
            strReturnMsg = msCustomerMaterialService.updateCustomerMaterials(mdCustomerMaterialList);
        }
        return strReturnMsg;
    }

    public void insertCustomerMaterial(MDCustomerMaterial mdCustomerMaterial){
        User user = UserUtils.getUser();
        mdCustomerMaterial.setCreateById(user.getId());
        mdCustomerMaterial.setCreateDate(new Date());
        mdCustomerMaterial.setUpdateById(user.getId());
        msCustomerMaterialService.insertCustomerMaterial(mdCustomerMaterial);
    }
    public void updateCustomerMaterial(MDCustomerMaterial mdCustomerMaterial){
        User user = UserUtils.getUser();
        mdCustomerMaterial.setUpdateById(user.getId());
        mdCustomerMaterial.setUpdateDate(new Date());
        msCustomerMaterialService.updateCustomerMaterial(mdCustomerMaterial);
    }

    public void updateCustomerMaterialId(MDCustomerMaterial mdCustomerMaterial){
        msCustomerMaterialService.updateCustomerMaterialId(mdCustomerMaterial);
    }

    public Long getIdByCustomerAndProductAndMaterial(Long customerId,Long productId,Long materialId){
        return msCustomerMaterialService.getIdByCustomerAndProductAndMaterial(customerId, productId, materialId);
    }
}

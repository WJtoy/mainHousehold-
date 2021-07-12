package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.GlobalMappingSyncTypeEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.api.entity.common.RestCommonIds;
import com.wolfking.jeesite.modules.api.entity.md.RestMaterial;
import com.wolfking.jeesite.modules.api.entity.md.RestProduct;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.md.dao.ProductDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.ms.globalmapping.service.ProductCategoryProductMappingService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerProductService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryNewService;
import com.wolfking.jeesite.ms.providermd.service.MSProductService;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class ProductService extends LongIDCrudService<ProductDao, Product> {
	@Autowired
	private RedisUtils redisUtils;

	@SuppressWarnings("rawtypes")
	@Autowired
	public RedisTemplate redisTemplate;

	@Autowired
	private MapperFacade mapper;

	@Autowired
    private CustomerService customerService;

//	@Autowired
//	private MSProductCategoryService msProductCategoryService;

	@Autowired
	private MSProductCategoryNewService msProductCategoryNewService;

	@Autowired
    private MSProductService msProductService;

	@Autowired
	private MaterialService materialService;

	@Autowired
	private ProductCategoryProductMappingService productCategoryProductMappingService;

	@Autowired
	private MSCustomerProductService msCustomerProductService;

	@Override
	public Product get(long id) {
		// add on 2019-8-14
		// productCategory微服务
		//Product product = super.get(id);
        Product product = msProductService.getById(id);  // 调用product微服务
		if (product != null) {
			// mark on 2020-3-16 begin
			//ProductCategory productCategory = msProductCategoryService.getById(product.getCategory().getId());
			//product.getCategory().setName(productCategory.getName());
			// mark on 2020-3-16 end
			//String strName = msProductCategoryService.getNameById(product.getCategory().getId());  //add on 2020-3-16  //mark on 2020-3-16
			String strName = msProductCategoryNewService.getFromCacheForMD(product.getCategory().getId());  //add on 2020-4-1
			product.getCategory().setName(strName); //add on 2020-3-16
		}
		return product;
	}

	@Override
	public Page<Product> findPage(Page<Product> page, Product entity) {
		/*
		// mark on 2019-8-15
		entity.setPage(page);
		List<Product> list = dao.findList(entity);
		if(list != null && list.size()>0) {
			list = handleProductCategory(list);
		}
		page.setList(list);
		return page;
		*/

		// add on 2019-8-15
        // Product微服务
		if (entity.getCustomerId() != null) {
			List<CustomerProduct> customerProductList = customerService.getListByCustomer(entity.getCustomerId());
			String productIds = "";
			if (customerProductList != null && !customerProductList.isEmpty()) {
				productIds = customerProductList.stream().map(r->r.getProduct().getId().toString()).collect(Collectors.joining(","));
			}
			entity.setProductIds(productIds);
		}

		Page<Product> productPage = msProductService.findList(page, entity);
        List<Product> list = productPage.getList();
        if(list != null && list.size()>0) {
            list = handleProductCategory(list);
        }
        return productPage;
	}

	/**
	 * 新增，更新
	 * @param product
	 */
	@Override
	@Transactional()
	public void save(Product product){
	    boolean isNew = product.getIsNewRecord();  // add on 2019-8-15
		//if (product.getIsNewRecord()){           // mark on 2019-8-15
        if (isNew){
			product.preInsert();
			//dao.insert(product);  // mark on 2020-2-11
		}else{
			product.preUpdate();
			//dao.update(product);  // mark on 2020-2-11
			//dao.deleteProductMaterialByProductId(product.getId());  // mark on 2020-2-11
		}
		//  mark on 2020-2-11
		/*if (product.getMaterialList() != null && product.getMaterialList().size() > 0){
			HashMap<String, Object> map = new HashMap<>();
			map.put("productId", product.getId());
			map.put("materialList", product.getMaterialList());
			dao.insertProductMaterial(map);
		}*/

		// add on 2019-8-15 begin
        MSErrorCode msErrorCode = msProductService.save(product, isNew);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用产品微服务保存数据失败。失败原因:"+msErrorCode.getMsg());
        }
        productCategoryProductMappingService.saveProductCategoryProductMapping(isNew? GlobalMappingSyncTypeEnum.ADD: GlobalMappingSyncTypeEnum.UPDATE,
				product.getId(), product.getCategory().getId());
        // add on 2019-8-15 end
		//清除产品缓存
		//delProductCache();  mark on 2020-2-11
	}

	@Override
	@Transactional()
	public void delete(Product product){
		//super.delete(product);  //mark on 2020-2-11
		// add on 2019-8-15 begin
		product.preUpdate();
		MSErrorCode msErrorCode = msProductService.delete(product);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用产品微服务删除数据失败。失败原因:"+msErrorCode.getMsg());
        }
		productCategoryProductMappingService.deleteProductCategoryProductMapping(product.getId());
        // add on 2019-8-15 end
		//清除产品缓存
		//delProductCache();
		//delProductCache(product.getId(),Double.valueOf(product.getSort()),product.getSetFlag());
	}

	/**
	 * 更新排序
	 * @param ids
	 * @param sorts
	@Transactional()
	public void updateSort(String[]ids, Integer[] sorts){
		Product product = new Product();
		for(int index = 0; index < ids.length; index++){
			product.setId(Long.parseLong(ids[index]));
			product.setSort(sorts[index]);
			dao.updateSort(product);
		}
		//删除所有产品列表
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_ALL);
		//删除套组产品列表
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SET);
		//删除非套组产品列表
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SINGLE);

		//delProductCache();
	}
	*/

	/**
	 * 更新排序
	 */
	@Transactional()
	public void updateSort(List<Product> products){
	    List<Product> productList = Lists.newArrayList();   // add on 2019-8-15
		Product product;
		for(int index = 0,size=products.size(); index < size; index++){
			product = products.get(index);
			if(product == null){
				continue;
			}
			if(product.getId() != null && product.getSort() >=0){
				//dao.updateSort(product);     // mark on 2019-8-15  // 在微服务没有完全切开，这里要保存 // add on 2019-9-24
                productList.add(product);    // add on 2019-8-15
			}
		}
		// add on 2019-8-15 begin
		if (productList != null && !productList.isEmpty()) {
		    MSErrorCode msErrorCode = msProductService.updatSort(productList);
		    if (msErrorCode.getCode() >0) {
		        throw  new RuntimeException("调用产品微服务更新排序失败.失败原因:" + msErrorCode.getMsg());
            }
        }
        // add on 2019-8-15 end

		//删除所有产品列表
		//redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_ALL); //mark on 2020-2-11
		//删除套组产品列表
		//redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SET); //mark on 2020-2-11
		//删除非套组产品列表
		//redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SINGLE); //mark on 2020-2-11
		//删除所有产品列表
		//redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT); //mark on 2020-2-11
		//delProductCache();
	}

	/**
	 * 审核产品
	 * @param id
	 */
	@Transactional()
	public void approveProduct(Long id){
		Product product = new Product(id);
		product.preUpdate();
		//dao.approveProduct(product);    // mark on 2019-8-15   // 在微服务没有完全切开，这里要保存 // add on 2019-9-24  //mark on 2020-2-11

		// add on 2019-8-15 begin
		MSErrorCode msErrorCode = msProductService.approveProduct(product);
		if (msErrorCode.getCode() > 0) {
		    throw new RuntimeException("调用产品微服务审核产品失败.失败原因:"+msErrorCode.getMsg());
        }
        // add on 2019-8-15 end
	}

	/**
	 * 根据产品名称获取产品，最多一条，用于判断产品名称是否存在于数据库中
	 * @param product
	 * @return
	 */
	public Boolean isExistProductName(Product product){
//		return dao.getIdByName(product) != null;   //mark on 2019-8-15
        // add on 2019-8-15 begin
		Long id = msProductService.getIdByName(product.getName());
		return id==null?false:(id.equals(product.getId())?false:true);
        // add on 2019-8-15 end
	}

	/**
	 * 判断是否有套组产品包含此产品
	 * @param productId
	 * @return
	 */
	public HashMap<String, Object> getSetProductByProductId(Long productId){
//		return dao.getSetProductByProductId(productId);  // mark on 2019-8-16
        HashMap<String, Object> map = null;
		Product product = msProductService.getSetProductByProductId(productId);
		if (product != null) {
			map = Maps.newHashMap();
		    map.put("id", product.getId());
		    map.put("name", product.getName());
        }
        return map;
	}

	/**
	 * 根据配件ID获取产品信息，最多一条，用于判断配件是否被产品使用
	 * @param materialId
	 * @return
	 */
	public HashMap<String, Object> getProductByMaterialId(Long materialId){
		//return dao.getProductByMaterialId(materialId);  // mark on 2019-8-16

        // add on 2019-8-16 //Product 微服务
		/*Long productId = dao.getProductMaterialByMaterialId(materialId);
		Product product  = msProductService.getById(productId);
		HashMap<String,Object> map = null;
		if (product != null) {
			map = Maps.newHashMap();
		    map.put("id", product.getId());
		    map.put("name", product.getName());
        }
        return map;*/
        // add on 2019-9-20 //Product 微服务
		HashMap<String,Object> map = msProductService.getProductByMaterialId(materialId);
		return map;
	}

	/**
	 * 查询分页数据--用于产品价格展示
	 * @param page 分页对象
	 * @param product
	 * @return
	 */
	public Page<Product> findPageForPrice(Page<Product> page, Product product) {
		/*
		// mark on 2019-8-16
		product.setPage(page);
		page.setList(dao.findListForPrice(product));
		return page;*/
		return msProductService.findListForPrice(page,product);  // add on 2019-8-16  //调用product微服务获取
	}

	/**
	 * 拆分套组产品，返回组成产品列表
	 *
	 * @param id 套组产品id
	 */
	public List<Product> getProductListOfSet(Long id)
	{
		List<Product> list = Lists.newArrayList();
		if (id == null || id<=0)
		{
			return list;
		}
		Product pset = getProductByIdFromCache(id);
		if(pset == null || StringUtils.isBlank(pset.getProductIds())){
			return list;
		}
		String[] ids = pset.getProductIds().split(",");
		String pid = new String("");
		for(int i=0,size=ids.length;i<size;i++){
			pid = ids[i];
			if(StringUtils.isBlank(pid)){
				continue;
			}
			Product p = getProductByIdFromCache(Long.valueOf(pid));
			if(p !=null){
				list.add(p);
			}
		}
		return list;
	}

	/**
	 * 根据产品ID获取配件列表
	 * @param productId
	 * @return
	 */
	public List<RestMaterial> getMaterialByProductId(Long productId){
		/*boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT);
		if (!isExistsCache){
			loadProductDataFromDB2Cache(LoadProductType.ALL);
		}
		List<RestMaterial> materialList = Lists.newArrayList();
		List<Long> materialIds = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_MATERIAL, productId),0,-1, Long.class);
		Material material;
		for (Long materialId : materialIds){
			material = (Material) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_MATERIAL_ALL, materialId, materialId, Material.class);
			materialList.add(mapper.map(material, RestMaterial.class));
		}
		return materialList;*/
		//调用微服务 start on 2019-9-17
		List<RestMaterial> restMaterialList = Lists.newArrayList();
		List<Material> materialList = materialService.getMaterialListByProductId(productId);
		if(materialList!=null && materialList.size()>0){
			restMaterialList= mapper.mapAsList(materialList,RestMaterial.class);
		}
		return restMaterialList;
	}

	/****************************************************************************
	 * redis操作
	 ****************************************************************************/
	//region redis操作
	/**
	 * 读取类型
	 */
	private enum LoadProductType{
		ALL,
		SET,
		SINGLE
	}

	/**从数据库读取产品信息至缓存
	 * @return
	 */
	private List<Product> loadProductDataFromDB2Cache(LoadProductType loadProductType){
		List<Product> returnList = Lists.newArrayList();
		//调用微服务 2019-9-20
		//List<ProductMaterial> productMaterialIdList = dao.getProductMaterialIdList();
        List<ProductMaterial> productMaterialIdList = msProductService.findAllProductMaterial();
        //end 2019-9-20
//		List<Product> list = super.findAllList();              // mark on 2019-8-15
		List<Product> list = msProductService.findAllList();   // add on 2019-8-15
		if(list != null && list.size()>0) {
			list = handleProductCategory(list);  // add on 2019-8-14
			final List<Product> finalProductList = list; // add on 2019-8-14

			redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
				connection.select(RedisConstant.RedisDBType.REDIS_MD_DB.ordinal());
				/*
				// mark on 2019-8-14
				for(int index = 0; index < list.size(); index++){
					Product product = list.get(index);
				*/
				for(int index = 0; index < finalProductList.size(); index++){  // add on 2019-8-14
					Product product = finalProductList.get(index);	           // add on 2019-8-14
					//缓存产品信息
					connection.hSet(RedisConstant.MD_PRODUCT.getBytes(StandardCharsets.UTF_8), product.getId().toString().getBytes(StandardCharsets.UTF_8), redisUtils.gsonRedisSerializer.serialize(product));
					//缓存所有产品列表
					connection.zAdd(RedisConstant.MD_PRODUCT_ALL.getBytes(StandardCharsets.UTF_8), product.getSort(), product.getId().toString().getBytes(StandardCharsets.UTF_8));
					//缓存套组产品列表
					if (product.getSetFlag() == 1) {
						connection.zAdd(RedisConstant.MD_PRODUCT_SET.getBytes(StandardCharsets.UTF_8), product.getSort(), product.getId().toString().getBytes(StandardCharsets.UTF_8));
						if (loadProductType == LoadProductType.SET){
							returnList.add(product);
						}
					} else {
						//缓存非套组产品列表
						connection.zAdd(RedisConstant.MD_PRODUCT_SINGLE.getBytes(StandardCharsets.UTF_8), product.getSort(), product.getId().toString().getBytes(StandardCharsets.UTF_8));
						if (loadProductType == LoadProductType.SINGLE){
							returnList.add(product);
						}
					}
					if (productMaterialIdList != null && productMaterialIdList.size() > 0) {
						List<ProductMaterial> materialIdList = productMaterialIdList.stream().filter(t -> t.getProductId().equals(product.getId())).collect(Collectors.toList());
						if (materialIdList != null && materialIdList.size() > 0) {
							for (ProductMaterial productMaterial : materialIdList){
								//缓存非套组产品列表
								connection.zAdd(String.format(RedisConstant.MD_PRODUCT_MATERIAL, product.getId()).getBytes(StandardCharsets.UTF_8), productMaterial.getMaterialId(), productMaterial.getMaterialId().toString().getBytes(StandardCharsets.UTF_8));
							}
						}
					}
				}
				return null;
			});
		}
		return list;
	}

	/**
	 * 从缓存读取产品信息，当缓存未命中则从数据库装载至缓存
	 * @param id
	 * @return
	 */
	public Product getProductByIdFromCache(Long id){
		// mark on 2020-2-11
		/*boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT);
		if (!isExistsCache){
			loadProductDataFromDB2Cache(LoadProductType.ALL);
		}
		return redisUtils.hGet(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT, id.toString(), Product.class);*/
		Product product = msProductService.getProductByIdFromCache(id);
		if(product!=null && product.getCategory()!=null && product.getCategory().getId()>0){
			/*
			ProductCategory productCategory = msProductCategoryService.getFromCache(product.getCategory().getId());
			if(productCategory!=null){
				product.getCategory().setName(productCategory.getName());
			}
			*/
			//product.getCategory().setName(msProductCategoryService.getNameFromCache(product.getCategory().getId()));  //mark on 2020-4-1
			product.getCategory().setName(msProductCategoryNewService.getFromCacheForMD(product.getCategory().getId())); //add on 2020-4-1
			return product;
		}else{
			return null;
		}
	}

	/**
	 * 加载所有产品，当缓存未命中则从数据库装载至缓存
	 * @return
	 */
	@Override
	public List<Product> findAllList(){
		/*boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT);
		if (!isExistsCache){
			return loadProductDataFromDB2Cache(LoadProductType.ALL);
		}

		List<Long> productIds = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_ALL,0,-1, Long.class);
		List<Product> productList = Lists.newArrayList();
		Iterator<Long> it = productIds.iterator();
		while(it.hasNext()){
			Long productId = it.next();
			productList.add(redisUtils.hGet(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT, productId.toString(), Product.class));
		}*/
		//return productList;
		List<Product> productList = msProductService.findAllList();
		return handleProductCategory(productList);
	}

	public Map<Long, Product> getProductMap(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Maps.newHashMap();
        }
        // mark on 2020-2-11
        /*if (!redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT)) {
            loadProductDataFromDB2Cache(LoadProductType.ALL);
        }
        List<String> productIds = ids.stream().map(Object::toString).collect(Collectors.toList());
        List<Product> list = redisUtils.getObjFromHashByKeys(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT, productIds, Product.class);*/
        // 调用微服务 add on 2020-2-11
		List<Product> list = msProductService.findProductByIdListFromCache(ids);
		handleProductCategory(list);
        Map<Long, Product> productMap = list.stream().collect(Collectors.toMap(LongIDBaseEntity::getId, i->i));
        return productMap;
    }

	/**
	 * 加载套组产品，当缓存未命中则从数据库装载至缓存
	 * @return
	 */
	public List<Product> getSetProductList(){
		// mark on 2020-2-11
		/*boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT);
		if (!isExistsCache){
			return loadProductDataFromDB2Cache(LoadProductType.SET);
		}

		List<Long> productIds = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SET,0,-1, Long.class);
		List<Product> productList = Lists.newArrayList();
		Iterator<Long> it = productIds.iterator();
		while(it.hasNext()){
			Long productId = it.next();
			productList.add(redisUtils.hGet(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT, productId.toString(), Product.class));
		}
		return productList;*/
		// 调用微服务 add on 2020-2-11
		List<Product> productListAll = msProductService.findAllList();
		if(productListAll !=null && productListAll.size()>0){
			List<Product> setProductList = productListAll.stream().filter(t->t.getSetFlag()==1).collect(Collectors.toList());
			return handleProductCategory(setProductList);
		}else{
			return Lists.newArrayList();
		}
	}

	/**
	 * 加载非套组产品，当缓存未命中则从数据库装载至缓存
	 * @return
	 */
	public List<Product> getSingleProductList(){
		// mark on 2020-2-11
		/*boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT);
		if (!isExistsCache){
			return loadProductDataFromDB2Cache(LoadProductType.SINGLE);
		}

		List<Long> productIds = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SINGLE,0,-1, Long.class);
		List<Product> productList = Lists.newArrayList();
		Iterator<Long> it = productIds.iterator();
		while(it.hasNext()){
			Long productId = it.next();
			productList.add(redisUtils.hGet(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT, productId.toString(), Product.class));
		}
		return productList;*/

		// 调用微服务 add on 2020-2-11
		List<Product> productListAll = msProductService.findAllList();
		if(productListAll !=null && productListAll.size()>0){
			List<Product> singleProductList = productListAll.stream().filter(t->t.getSetFlag()==0).collect(Collectors.toList());
			return handleProductCategory(singleProductList);
		}else{
			return Lists.newArrayList();
		}
	}

	/**
	 * 获取客户下面的所有产品，当缓存未命中则从数据库装载至缓存
	 */
	public List<Product> getCustomerProductList(Long customerId){
		/*String customerProductCacheKey = String.format(RedisConstant.MD_PRODUCT_CUSTOMER, customerId);
		List<Product> productList = Lists.newArrayList();
		Product product;
		boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, customerProductCacheKey);
		if (!isExistsCache){
//			List<HashMap<String, Object>> customerProductIdList = dao.getIdsByCustomerId(customerId);  //mark on 2019-8-16

            List<HashMap<String, Object>> customerProductIdList = Lists.newArrayList();
			// add on 2019-8-16 begin
			List<CustomerProduct> customerProductList = customerService.getListByCustomer(customerId);
            List<Product> allProductList = msProductService.findAllList();
            Map<Long, Product> productMap = Maps.newHashMap();
            if (allProductList != null && !allProductList.isEmpty()) {
                productMap = allProductList.stream().collect(Collectors.toMap(Product::getId, r->r));
            }
            final Map<Long, Product> finalProductMap = productMap;
            if (customerProductList != null && !customerProductList.isEmpty()) {
                customerProductList.stream().forEach(customerProduct -> {
                    Product product1 = finalProductMap.get(customerProduct.getProduct().getId());
                    if (product1 != null) {
                        HashMap<String, Object> map = Maps.newHashMap();
                        map.put("product_id", product1.getId());
                        map.put("sort", product1.getSort());

                        customerProductIdList.add(map);
                    }
                });
            }

            // add on 2019-8-16 end


			for (HashMap<String, Object> idMap : customerProductIdList){
				Long productId = Long.parseLong(idMap.get("product_id").toString());
				int sort = Integer.parseInt(idMap.get("sort").toString());
				redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, customerProductCacheKey, productId, sort, 0);
				product = getProductByIdFromCache(productId);
				if(product != null) {
					productList.add(product);
				}
			}

			return productList;
		}

		List<Long> productIds = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, customerProductCacheKey,0,-1, Long.class);
		Iterator<Long> it = productIds.iterator();
		while(it.hasNext()){
			Long productId = it.next();
			product = getProductByIdFromCache(productId);
			if(product == null) {
				log.error("Cache has no product:{}",productId);
			}else{
				productList.add(product);
			}
		}

		return productList;*/
		//改为从微服务中调用 add on 2020-1-9
		List<Product> productList = msCustomerProductService.findProductByCustomerIdFromCache(customerId);
		if(productList !=null && productList.size()>0){
			return productList;
		}else{
			return Lists.newArrayList();
		}
	}

	/**
	 * 获取服务网点下面的所有产品，当缓存未命中则从数据库装载至缓存

	public List<Product> getServicePointProductList(Long servicePointId){
		String servicePointProductCacheKey = String.format(RedisConstant.MD_PRODUCT_SERVICE_POINT, servicePointId);
		List<Product> productList = Lists.newArrayList();
		boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, servicePointProductCacheKey);
		if (!isExistsCache){
			List<HashMap<String, Object>> servicePointProductIdList = dao.getIdsByServicePointId(servicePointId);
			for (HashMap<String, Object> idSortMap : servicePointProductIdList){
				Long productId = Long.parseLong(idSortMap.get("product_id").toString());
				int sort = Integer.parseInt(idSortMap.get("sort").toString());
				redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, servicePointProductCacheKey, productId, sort, 0);
				productList.add(getProductByIdFromCache(productId));
			}

			return productList;
		}

		List<Long> productIds = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, servicePointProductCacheKey,0,-1, Long.class);
		Iterator<Long> it = productIds.iterator();
		while(it.hasNext()){
			Long productId = it.next();
			productList.add(getProductByIdFromCache(productId));
		}

		return productList;
	}
	 */

	/**
	 * 删除所有产品缓存，在产品发生变化时调用
	 */
	public void delProductCache(){
		//删除产品信息
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT);
		//删除所有产品列表
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_ALL);
		//删除套组产品列表
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SET);
		//删除非套组产品列表
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SINGLE);
		//删除产品下的配件列表
		redisUtils.removePattern(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_MATERIAL, "*"));
		//删除客户关联产品列表 去掉缓存 add on 2020-1-9
		//redisUtils.removePattern(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_CUSTOMER, "*"));
		//删除服务网点关联产品列表
		//redisUtils.removePattern(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_SERVICE_POINT, "*"));
	}

	/**
	 * 删除指定产品相关缓存
	 * @param productId
	 */
	public void delProductCache(Long productId,Double sort,int setFlag){
		//移除产品信息
		redisUtils.hdel(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT,productId.toString());
		//移除产品
		redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_ALL,sort,sort);
		if(setFlag == 1) {
			//移除套组产品
			redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SET,sort,sort);
		}else{
			//移除非套组产品
			redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_SINGLE,sort,sort);
		}
		//移除产品下的配件列表
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_MATERIAL, productId));
		//删除客户关联产品列表  去掉缓存 add on 2020-1-9
		//redisUtils.removePattern(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_CUSTOMER, "*"));
		//删除服务网点关联产品列表
		//redisUtils.removePattern(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_SERVICE_POINT, "*"));
	}

	/**
	 * 删除批定客户下的产品缓存，在客户与产品关联发生变化时调用
	 * @param customerId
	 */
	// 去掉缓存 客户产品缓存 add on 2020-1-9
	/*public void delCustomerProductCache(Long customerId){
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_CUSTOMER, customerId));
	}*/

	/**
	 * 删除批定服务网点下的产品缓存，在服务网点与产品关联发生变化时调用 (未使用)
	public void delServicePointProductCache(Long servicePointId){
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_PRODUCT_SERVICE_POINT, servicePointId));
	}
	*/

	//endregion redis操作

	//region api functions
	public RestResult<Object> getProductMaterialList(@RequestBody RestCommonIds commonIds){
		List<RestProduct> productMaterials = Lists.newArrayList();
		for (String id : commonIds.getIds()) {
			Long productId = Long.valueOf(id);
			RestProduct restProduct = new RestProduct();
			restProduct.setId(id);
			restProduct.setMaterials(getMaterialByProductId(productId));
			productMaterials.add(restProduct);
		}
		return RestResultGenerator.success(productMaterials);
	}
	//endregion api functions


	/**
	 * 处理产品中的产品分类
	 * @param productList
	 * @return
	 */
	private List<Product> handleProductCategory(List<Product> productList) {
		if (productList == null) {
			return Lists.newArrayList();
		}
		// ProductCategory微服务调用
		//List<ProductCategory> productCategoryList = msProductCategoryService.findAllList();
		List<Long> ids = productList != null && !productList.isEmpty() ? productList.stream().map(x->x.getCategory().getId()).distinct().collect(Collectors.toList()) : Lists.newArrayList();
		//List<ProductCategory> productCategoryList = ids!= null && !ids.isEmpty()? msProductCategoryService.findListByIds(ids):Lists.newArrayList();  //mark on 2020-4-1
		List<ProductCategory> productCategoryList = ids!= null && !ids.isEmpty()? msProductCategoryNewService.findListByIdsForMDWithEntity(ids):Lists.newArrayList(); //add on 2020-4-1
		Map<Long,ProductCategory> productCategoryMap = Maps.newHashMap();
		if (productCategoryList != null && !productCategoryList.isEmpty()) {
			productCategoryMap = productCategoryList.stream().collect(Collectors.toMap(ProductCategory::getId, r->r));
		}

		Map<Long,ProductCategory> finalProductCategoryMap = productCategoryMap;
		productList.stream().forEach(product -> {
			ProductCategory productCategory = finalProductCategoryMap.get(product.getCategory().getId());
			product.getCategory().setName(productCategory==null?"":productCategory.getName());
		});

		return productList;
	}


	/**
	 * 产品分类获取产品列表
	 * @param productCategoryId
	 * @return
	 */
	public List<Product> ProductByCategoryIdFindAllList(Long customerId,Long productCategoryId){
		return msProductService.findListByCustomerIdAndCategoryId(customerId,productCategoryId);
	}
}

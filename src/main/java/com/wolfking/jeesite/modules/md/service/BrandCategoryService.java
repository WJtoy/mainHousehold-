package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDProductCategoryBrand;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.dao.BrandCategoryDao;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.entity.BrandCategory;
import com.wolfking.jeesite.modules.md.entity.BrandsCaterotyModel;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.ms.providermd.service.MSBrandService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryBrandService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryService;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 品牌和产品分类Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class BrandCategoryService extends LongIDCrudService<BrandCategoryDao, BrandCategory> {

//    @Autowired
//    private BrandService brandService;

//    @Autowired
//    private RedisUtils redisUtils;

//    @Autowired
//    private ProductCategoryService productCategoryService;

    @Autowired
    private MSProductCategoryBrandService msProductCategoryBrandService;

    @Autowired
    private MSBrandService msBrandService;


    @Override
    public Page<BrandCategory> findPage(Page<BrandCategory> page, BrandCategory entity) {
       return msProductCategoryBrandService.findList(page, entity);
    }

    /**
     * 查询列表数据
     *
     * @param entity
     * @return
     */
    @Override
    public List<BrandCategory> findList(BrandCategory entity) {
        List<BrandCategory> brandCategoryList = Lists.newArrayList();

        int pageNo = 1;
        int pageSize = 100;
        Page<BrandCategory> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);

        entity.setPage(page);

        Page<BrandCategory> returnPage =  msProductCategoryBrandService.findList(page, entity);
        brandCategoryList.addAll(returnPage.getList());
        while(pageNo < returnPage.getPageCount()) {
            pageNo++;
            page.setPageNo(pageNo);
            entity.setPage(page);

            Page<BrandCategory> whileReturnPage =  msProductCategoryBrandService.findList(page, entity);
            brandCategoryList.addAll(whileReturnPage.getList());
        }
        return brandCategoryList;
    }

    @Transactional
    public void saveBrandsCaterotyModel(BrandsCaterotyModel brandsCaterotyModel) {
        String brandIds= brandsCaterotyModel.getBrandIds();
        if (StringUtils.isNotBlank(brandIds) && brandsCaterotyModel.getCategory()!=null && brandsCaterotyModel.getCategory().getId()!=null){

            //先删除分类下的信息
            deteleByCategoryId(brandsCaterotyModel.getCategory().getId());

            //add on 2019-9-5 begin
            List<Brand> allBrandList = msBrandService.findAllList();
            Map<Long, Brand> brandMap = Maps.newHashMap();
            if (allBrandList != null && !allBrandList.isEmpty()) {
                brandMap = allBrandList.stream().collect(Collectors.toMap(Brand::getId, Function.identity()));
            }

            List<Brand> brandList=new LinkedList<>();
            String[] ids = brandIds.split(",");
            if (ids != null){
                List<BrandCategory> brandCategoryList = Lists.newArrayList();   // add on 2019-9-5
                for (String id:ids) {
                    Long brandId=Long.valueOf(id);
                    BrandCategory entity = new BrandCategory();
                    entity.setBrand(new Brand(brandId));
                    entity.setCategory(brandsCaterotyModel.getCategory());
                    //super.save(entity);              //mark on 2020-1-6
                    entity.preInsert();                //add on 2020-1-6
                    brandCategoryList.add(entity);     // add on 2019-9-5
                    //为了写入缓存
                    //Brand brand = brandService.getFromCache(brandId);  // mark on 2019-9-5
                    Brand brand = brandMap.get(brandId);                 // add on 2019-9-5
                    brandList.add(brand);
                }
                // add on 2019-9-5 begin
                // ProductCategoryBrand
                MSErrorCode msErrorCode = msProductCategoryBrandService.batchInsert(brandCategoryList);
                if (msErrorCode.getCode() > 0) {
                    throw new RuntimeException("批量保存产品品牌到微服务出错,出错原因:"+msErrorCode.getMsg());
                }
                // add on 2019-9-5 end
            }
        }
    }

    @Override
    public void delete(BrandCategory entity) {
        //super.delete(entity); //mark on 2020-1-6
        // add on 2019-9-5 begin
        // ProductCategoryBrand 微服务
        MSErrorCode msErrorCode = msProductCategoryBrandService.delete(entity.getId());
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("删除产品品牌失败.失败原因:"+msErrorCode.getMsg());
        }
        // add on 2019-9-5 end
    }

    public void deteleByCategoryId(Long categoryId)
    {
        //dao.deteleByCategoryId(categoryId);  //mark on 2020-1-6
        // add on 2019-9-5 begin
        // ProductCategoryBrand 微服务
        MSErrorCode msErrorCode = msProductCategoryBrandService.deleteByCategoryId(categoryId);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("根据类别删除产品品牌失败.失败原因:"+msErrorCode.getMsg());
        }
        // add on 2019-9-5 end
        //remCache(categoryId); //mark on 2020-1-6
    }


    //删除缓存
    /*
    // mark on 2020-1-6
    public void remCache(Long categoryId){
        String key = String.format(RedisConstant.MD_PRODUCT_CATEGORY_BRND,categoryId);
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB,key);
    }
    */

    /**
     * 通过产品分类获取品牌列表
     * @param categoryId
     * @return
     */
    public List<Brand> getBrandListByCategory(Long categoryId){
        /*
        // mark on 2020-1-6
        String key = String.format(RedisConstant.MD_PRODUCT_CATEGORY_BRND,categoryId);
        BrandsCaterotyModel model= (BrandsCaterotyModel) redisUtils.get(RedisConstant.RedisDBType.REDIS_MD_DB,key,BrandsCaterotyModel.class);

        List<Brand> brandList=new LinkedList<Brand>();
        //如果没有缓存则写入
        if (model == null || model.getBrandList() ==null || model.getBrandList().size()<=0) {
         */
        List<Brand> brandList = new LinkedList<Brand>();
        //add on 2019-9-5 begin
        List<Brand> allBrandList = msBrandService.findAllList();
        Map<Long, Brand> brandMap = Maps.newHashMap();
        if (allBrandList != null && !allBrandList.isEmpty()) {
            brandMap = allBrandList.stream().collect(Collectors.toMap(Brand::getId, Function.identity()));
        }

        //add on 2019-9-5 end

        //List<Long> list = dao.getBrandIdsByCategoryId(categoryId);  //mark on 2019-9-5
        List<Long> list = msProductCategoryBrandService.getBrandIdsByCategoryId(categoryId); //add on 2019-9-5 //ProductCategoryBrand微服务
        for (Long brandId: list) {
            //Brand brand=brandService.getFromCache(brandId);  //mark on 2019-9-5
            Brand brand = brandMap.get(brandId);   //add on 2019-9-5
            brandList.add(brand);
        }
        return brandList;
    }
}

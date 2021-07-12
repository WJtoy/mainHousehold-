package com.wolfking.jeesite.modules.customer.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerBrandCategory;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.CustomerBrandCategoryFeign;
import com.wolfking.jeesite.ms.providermd.service.MSBrandService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CtCustomerBrandCategoryService {
    @Autowired
    private CustomerBrandCategoryFeign customerBrandCategoryFeign;

    @Autowired
    private MSProductCategoryBrandService msProductCategoryBrandService;

    @Autowired
    private MSBrandService msBrandService;

    /**
     * 分页查询
     * @param page,b2BServiceTypeMapping
     * @return
     */
    public Page<CustomerBrandCategory> getList(Page<CustomerBrandCategory> page, CustomerBrandCategory customerBrandCategory) {
        if (customerBrandCategory.getPage() == null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        Page<CustomerBrandCategory> customerBrandCategoryPage = new Page<>();
        customerBrandCategoryPage.setPageSize(page.getPageSize());
        customerBrandCategoryPage.setPageNo(page.getPageNo());
        customerBrandCategory.setPage(new MSPage<>(customerBrandCategoryPage.getPageNo(), customerBrandCategoryPage.getPageSize()));
        MSResponse<MSPage<CustomerBrandCategory>> returnCustomerBrandCategory= customerBrandCategoryFeign.getList(customerBrandCategory);
        if (MSResponse.isSuccess(returnCustomerBrandCategory)) {
            MSPage<CustomerBrandCategory> data = returnCustomerBrandCategory.getData();
            customerBrandCategoryPage.setCount(data.getRowCount());
            customerBrandCategoryPage.setList(data.getList());
        }else{
            customerBrandCategoryPage.setCount(0);
            customerBrandCategoryPage.setList(Lists.newArrayList());
        }
        return customerBrandCategoryPage;
    }


    /**
     * 保存
     * @param customerBrandCategory
     * @return
     */
    public MSErrorCode save(CustomerBrandCategory customerBrandCategory) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerBrandCategory.setCreateById(user.getId());
            customerBrandCategory.setUpdateById(user.getId());
        }
        MSResponse<CustomerBrandCategory> msResponse = customerBrandCategoryFeign.insert(customerBrandCategory);
        return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public MSResponse<CustomerBrandCategory> getById(Long id) {
        MSResponse<CustomerBrandCategory> msResponse = customerBrandCategoryFeign.getById(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse;
        }else{
            return new MSResponse<>(MSErrorCode.SUCCESS,new CustomerBrandCategory());
        }
    }

    /**
     * 根据客户Id和客户品牌Id获取
     * @param customerId,brandId
     * @return
     */
    public List<CustomerBrandCategory> findListByBrand(Long customerId,Long brandId){
        MSResponse<List<CustomerBrandCategory>> msResponse = customerBrandCategoryFeign.findListByBrand(customerId,brandId);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 删除
     * @param customerBrandCategory
     * @return
     */
    public MSResponse<Integer> delete(CustomerBrandCategory customerBrandCategory) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerBrandCategory.setUpdateById(user.getId());
        }
        customerBrandCategory.preUpdate();
        MSResponse<Integer> msResponse = customerBrandCategoryFeign.delete(customerBrandCategory);
        return msResponse;
    }


    /**
     * 根据客户Id和产品Id获取客户品牌
     * @param customerId,productId
     * @return
     */
    public List<CustomerBrandCategory> getFromCache(Long customerId,Long productId){
        MSResponse<List<CustomerBrandCategory>> msResponse = customerBrandCategoryFeign.findListByCustomerAndCagtegory(customerId,productId);
        if(MSResponse.isSuccess(msResponse)){
            List<CustomerBrandCategory> list =  msResponse.getData();
            return list;
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 根据客户ID产品ID获取品牌列表
     * @param customerId,productId
     * @return
     */
    public List<Brand> getBranList(Long customerId, Long productId){
        Set<Brand> brandSet = Sets.newHashSet();
        MSResponse<List<CustomerBrandCategory>> msResponse = customerBrandCategoryFeign.findListByCustomerAndCagtegory(customerId,productId);
        if(MSResponse.isSuccess(msResponse)){
            List<CustomerBrandCategory> list =  msResponse.getData();
            for(CustomerBrandCategory entity:list){
                Brand brand = new Brand();
                brand.setId(entity.getBrandId());
                brand.setName(entity.getBrandName());
                brand.setSort(entity.getSort());
                //brand.setName(brandService.getFromCache(entity.getBrandId()).getName());
                brandSet.add(brand);
            }
        }
        if(brandSet.isEmpty()){
            return Lists.newArrayList();
        }else{
            return brandSet.stream().sorted(Comparator.comparing(Brand::getName)).sorted(Comparator.comparing(Brand::getSort)).collect(Collectors.toList());
        }
    }

    /**
     * 根据客户ID产品ID获取品牌列表
     */
    public List<String> getBrandNames(Long customerId,Long productId){
        List<Brand> brands = getBranList(customerId, productId);
        List<String> brandNames = Lists.newArrayList();
        if (brands != null && !brands.isEmpty()) {
            brandNames = brands.stream()
                    .filter(i->i!= null && StringUtils.isNotBlank(i.getName()))
                    .map(Brand::getName)
                    .sorted()
                    .collect(Collectors.toList());
        }
        return brandNames;
    }

    /**
     * 通过产品分类获取品牌列表
     * @param categoryId
     * @return
     */
    public List<Brand> getBrandListByCategory(Long categoryId){
        List<Brand> brandList = new LinkedList<Brand>();
        List<Brand> allBrandList = msBrandService.findAllList();
        Map<Long, Brand> brandMap = Maps.newHashMap();
        if (allBrandList != null && !allBrandList.isEmpty()) {
            brandMap = allBrandList.stream().collect(Collectors.toMap(Brand::getId, Function.identity()));
        }


        List<Long> list = msProductCategoryBrandService.getBrandIdsByCategoryId(categoryId); //add on 2019-9-5 //ProductCategoryBrand微服务
        for (Long brandId: list) {
            Brand brand = brandMap.get(brandId);
            brandList.add(brand);
        }
        return brandList;
    }
}

package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerBrandCategory;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.service.BrandService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.CustomerBrandCategoryFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class CustomerBrandCategoryService {

    @Autowired
    private CustomerBrandCategoryFeign customerBrandCategoryFeign;

    @Autowired
    private BrandService brandService;



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
    public List<Brand> getBranList(Long customerId,Long productId){
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
}

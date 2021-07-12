package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerBrand;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.CustomerBrandFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * 客户品牌服务
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class CustomerBrandService {

    @Autowired
    private CustomerBrandFeign customerBrandFeign;


    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public CustomerBrand getById(Long id) {
        MSResponse<CustomerBrand> msResponse = customerBrandFeign.getById(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return new CustomerBrand();
        }
    }

    /**
     * 分页查询
     *
     * @param page,b2BServiceTypeMapping
     * @return
     */
    public Page<CustomerBrand> getList(Page<CustomerBrand> page, CustomerBrand customerBrand) {
        if (customerBrand.getPage() == null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        Page<CustomerBrand> customerBrandPage = new Page<>();
        customerBrandPage.setPageSize(page.getPageSize());
        customerBrandPage.setPageNo(page.getPageNo());
        customerBrand.setPage(new MSPage<>(customerBrandPage.getPageNo(), customerBrandPage.getPageSize()));
        MSResponse<MSPage<CustomerBrand>> returnCustomerBrand = customerBrandFeign.getList(customerBrand);
        if (MSResponse.isSuccess(returnCustomerBrand)) {
            MSPage<CustomerBrand> data = returnCustomerBrand.getData();
            customerBrandPage.setCount(data.getRowCount());
            customerBrandPage.setList(data.getList());
        }else{
            customerBrandPage.setCount(0);
            customerBrandPage.setList(new ArrayList<>());
        }
        return customerBrandPage;
    }

    /**
     * 根据客户id获取
     * @param customerId
     * @return
     */
    public List<CustomerBrand> getListByCustomer(Long customerId){
        MSResponse<List<CustomerBrand>> msResponse = customerBrandFeign.getListByCustomer(customerId);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 根据客户Id查询或者查看所有
     * @param customerBrand
     * @return
     */
    public List<CustomerBrand> findAllList(CustomerBrand customerBrand){
        MSResponse<List<CustomerBrand>> msResponse = customerBrandFeign.findAllList(customerBrand);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }


    /**
     * 保存
     *
     * @param customerBrand
     * @return
     */
    public MSErrorCode save(CustomerBrand customerBrand) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerBrand.setCreateById(user.getId());
            customerBrand.setUpdateById(user.getId());
        }
        if (customerBrand.getId() != null && customerBrand.getId() > 0) {
            customerBrand.preUpdate();
            MSResponse<Integer> msResponse = customerBrandFeign.update(customerBrand);
            return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
        } else {
            customerBrand.preInsert();
            MSResponse<CustomerBrand> msResponse = customerBrandFeign.insert(customerBrand);
            return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
        }
    }


    /**
     * 删除
     *
     * @param customerBrand
     * @return
     */
    public MSResponse<Integer> delete(CustomerBrand customerBrand) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerBrand.setUpdateById(user.getId());
        }
        customerBrand.preUpdate();
        MSResponse<Integer> msResponse = customerBrandFeign.delete(customerBrand);
        return msResponse;
    }

}

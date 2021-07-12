package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MSCustomerService {
    @Autowired
    private MSCustomerFeign msCustomerFeign;
    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取单个客户信息
     * @param id
     * @return
     * id
     * name
     * salesId
     * paymentType
     */
    public Customer get(Long id) {
        return MDUtils.getById(id, Customer.class, msCustomerFeign::get);
    }

    /**
     * 从缓存中获取客户信息
     * @param id
     * @return
     */
    public Customer getFromCache(Long id) {
        return MDUtils.getById(id, Customer.class, msCustomerFeign::getFromCache);
    }

    public Customer getCustomerByIdFromCache(Long id) {
        return MDUtils.getById(id, Customer.class, msCustomerFeign::getCustomerByIdFromCache);
    }

    /**
     * 根据ID获取客户信息
     * @param id
     * @return
     * id
     * code
     * name
     * salesId
     * remarks
     */
    public Customer getByIdToCustomer(Long id) {
        return MDUtils.getById(id, Customer.class, msCustomerFeign::getByIdToCustomer);
    }

    /**
     * 根据id获取单个客户信息
     * @param id
     * @return
     * id,
     * phone,
     * logoId,
     * code,
     * name,
     * fullName,
     * master,
     * sales,
     * zipcode,
     * email,
     * address,
     * fax,
     * contractDate,
     * remarks,
     * minUploadNumber,
     * maxUploadNumber,
     * returnAddress,
     * effectFlag,
     * shortMessageFlag,
     * timeLinessFlag,
     * urgentFlag,
     * projectOwner,
     * projectOwnerPhone,
     * projectOwnerQq,
     * serviceOwner,
     * serviceOwnerPhone,
     * serviceOwnerQq,
     * financeOwner,
     * financeOwnerPhone,
     * financeOwnerQq,
     * technologyOwner,
     * technologyOwnerPhone,
     * technologyOwnerQq,
     * useDefaultPrice,
     * attachment1Id,
     * attachment2Id,
     * attachment3Id,
     * attachment4Id,
     */
    public Customer getByIdToCustomerSpecifiedColumn(Long id) {
        return MDUtils.getById(id, Customer.class, msCustomerFeign::getByIdToCustomerSpecifiedColumn);
    }

    /**
     * 根据customer code获取customer id
     * @param code
     * @return
     * true/false
     */
    public boolean getCustomerIdByCode(String code) {
        //return MDUtils.getByCustomCondition(code, msCustomerFeign::getCustomerIdByCode) != null;
        return MDUtils.getObjUnnecessaryConvertType(()->msCustomerFeign.getCustomerIdByCode(code)) != null;
    }


    /**
     * 根据id获取customer列表
     * @param ids
     * @return
     * id
     * code
     * name
     * salesId
     */
    public List<Customer> findBatchByIds(String ids) {
        // 这个方法后续要废除 // add on 2019-10-16
        // 此方法已于2020-3-17 停止调用了
        List<Customer> customerList = Lists.newArrayList();
        customerList = MDUtils.findListByCustomCondition(ids, Customer.class, msCustomerFeign::findBatchByIds);
        return customerList;
    }


    /**
     * 根据id获取customer列表
     * @param ids
     * @return
     *   id,code,name,salesId,contractDate
     */
    public List<Customer> findListByBatchIds(List<Long> ids) {
        // add on 2019-10-16
        List<Customer> customerList = Lists.newArrayList();
        if (ids != null && !ids.isEmpty()) {
            Lists.partition(ids, 100).forEach(longList -> {
                List<Customer> customersFromMS = MDUtils.findListNecessaryConvertType(Customer.class,()->msCustomerFeign.findByBatchIds(longList));
                if (customersFromMS != null && !customersFromMS.isEmpty()) {
                    customerList.addAll(customersFromMS);
                }
            });
        }
        return customerList;
    }

    /**
     * 根据客户id列表获取客户id，客户名字列表 2020-3-17
     * @param ids
     * @return
     */
    public List<Customer> findIdAndNameListByIds(List<Long> ids) {
        List<Customer> customerList = Lists.newArrayList();
        if (ids != null && !ids.isEmpty()) {
            Lists.partition(ids, 100).forEach(longList -> {
                List<Customer> customersFromMS = MDUtils.findListNecessaryConvertType(Customer.class, ()->msCustomerFeign.findIdAndNameListByIds(longList));
                if (customersFromMS != null && !customersFromMS.isEmpty()) {
                    customerList.addAll(customersFromMS);
                }
            });
        }
        return customerList;
    }

    /**
     * 查找所有的非贵宾客户
     * @return
     */
    public List<Customer> findNoVIPList() {
        List<Customer> customersFromMS = MDUtils.findListNecessaryConvertType(Customer.class, ()->msCustomerFeign.findNoVIPList());
        return customersFromMS;
    }

    /**
     * 根据id获取customer列表
     * @param ids
     * @return
     *   id,code,name,paymenttype,salesid,contractDate
     */
    public List<Customer> findCustomersWithIds(List<Long> ids) {
        List<Customer> customerList = Lists.newArrayList();
        if (ids != null && !ids.isEmpty()) {
            Lists.partition(ids, 200).forEach(longList -> {
                //List<Customer> customersFromMS = MDUtils.findListByCustomCondition(longList, Customer.class, msCustomerFeign::findCustomersWithIds);
                List<Customer> customersFromMS = MDUtils.findListNecessaryConvertType(Customer.class,()->msCustomerFeign.findCustomersWithIds(longList));
                if (customersFromMS != null && !customersFromMS.isEmpty()) {
                    customerList.addAll(customersFromMS);
                }
            });
        }
        return customerList;
    }

    public Map<Long, Customer> findCutomersWithIdsToMap(List<Long> ids) {
        List<Customer> customerList = findCustomersWithIds(ids);
        return customerList != null && !customerList.isEmpty()?customerList.stream().collect(Collectors.toMap(Customer::getId, Function.identity())): Maps.newHashMap();
    }


    /**
     * 获取客户列表
     * @param page
     * @param customer(code,name,salesid)
     * @return
     * id,
     * code,
     * name,
     * master,
     * phone,
     * email,
     * technologyOwner,
     * technologyOwnerPhone,
     * defaultBrand,
     * effectFlag,
     * shortMessageFlag,
     * remarks
     */
    public Page<MDCustomer> findMDCustomerList(Page<MDCustomer> page, MDCustomer customer) {
        if (customer.getPage() == null) {
            PageMapper.INSTANCE.toMSPage(page);
        }
        Page<MDCustomer> customerPage = new Page<>();
        customerPage.setPageSize(page.getPageSize());
        customerPage.setPageNo(page.getPageNo());
        customer.setPage(new MSPage<>(customerPage.getPageNo(), customerPage.getPageSize()));
        MSResponse<MSPage<MDCustomer>>  returnCustomer = msCustomerFeign.findCustomerList(customer);
        if (MSResponse.isSuccess(returnCustomer)) {
            MSPage<MDCustomer> data = returnCustomer.getData();
            customerPage.setCount(data.getRowCount());
            customerPage.setList(data.getList());
            log.warn("findMDCustomerList返回的数据:{}", data.getList());
        } else {
            customerPage.setCount(0);
            customerPage.setList(new ArrayList<>());
            log.warn("findMDCustomerList返回无数据返回,参数customer:{}", customer);
        }
        return customerPage;
    }

    /**
     * 获取客户列表
     * @param page
     * @param customer(code,name,salesid)
     * @return
     * id,
     * code,
     * name,
     * master,
     * phone,
     * email,
     * technologyOwner,
     * technologyOwnerPhone,
     * defaultBrand,
     * effectFlag,
     * shortMessageFlag,
     * remarks
     */
    public Page<Customer> findCustomerList(Page<Customer> page, Customer customer) {
        MDCustomer mdCustomer = mapper.map(customer, MDCustomer.class);
        if (mdCustomer.getPage() == null) {
            PageMapper.INSTANCE.toMSPage(page);
        }
        Page<MDCustomer> mdCustomerPage = new Page<>();
        mdCustomerPage.setPageSize(page.getPageSize());
        mdCustomerPage.setPageNo(page.getPageNo());
        mdCustomer.setPage(new MSPage<>(mdCustomerPage.getPageNo(), mdCustomerPage.getPageSize()));
        MSResponse<MSPage<MDCustomer>> returnCustomer = msCustomerFeign.findCustomerList(mdCustomer);
        if (MSResponse.isSuccess(returnCustomer)) {
            MSPage<MDCustomer> data = returnCustomer.getData();
            page.setCount(data.getRowCount());
            page.setList(mapper.mapAsList(data.getList(),Customer.class));
            log.warn("findCustomerList返回的数据:{}", data.getList());
        } else {
            page.setCount(0);
            page.setList(new ArrayList<>());
            log.warn("findCustomerList返回无数据返回,参数customer:{}", customer);
        }
        return page;
    }

    /**
     * 获取所有customer
     * @return
     * id,
     * code,
     * name,
     * full_name,
     * vip,
     * salesman,
     * salesman_phone,
     * salesman_qq,
     * address,
     * zip_code,
     * master,
     * mobile,
     * phone,
     * fax,
     * email,
     * contract_date,
     * project_owner,
     * project_owner_phone,
     * project_owner_qq,
     * service_owner,
     * service_owner_phone,
     * service_owner_qq,
     * finance_owner,
     * finance_owner_phone,
     * finance_owner_qq,
     * technology_owner,
     * technology_owner_phone,
     * technology_owner_qq,
     * default_brand,
     * effect_flag,
     * logo,
     * attachment1,
     * attachment2,
     * attachment3,
     * attachment4,
     * isfrontshow,
     * sort,
     * description,
     * min_upload_number,
     * max_upload_number,
     * return_address,
     * order_approve_flag,
     * remarks,
     * sales_id,
     * short_message_flag,
     * time_liness_flag,
     * urgent_flag,
     * payment_type,
     * createById,
     * updateById,
     * create_date,
     * update_date
     */
    public List<Customer> findAll() {
        return MDUtils.findAllList(Customer.class, msCustomerFeign::findAll);
    }

    public List<NameValuePair<Long, String>> findBatchListByIds(List<Long> ids) {
        List<NameValuePair<Long, String>> customerList = Lists.newArrayListWithCapacity(ids.size());
        if (ids != null && !ids.isEmpty()) {
            Lists.partition(ids, 100).forEach(longList -> {
                List<NameValuePair<Long, String>> customersFromMS = msCustomerFeign.findBatchListByIds(longList).getData();
                if (customersFromMS != null && !customersFromMS.isEmpty()) {
                    customerList.addAll(customersFromMS);
                }
            });
        }
        return customerList;
    }

    /**
     * 获取所有客户列表
     * @return
     * id
     * code
     * name
     * contractDate
     * salesMan
     * paymentType
     */
    public List<Customer> findAllSpecifiedColumn() {
        return MDUtils.findAllList(Customer.class, msCustomerFeign::findAllSpecifiedColumn);
    }

    /*
     *  获取所有客户列表,并带出支付类型值和业务信息
     */
    public List<Customer>  findAllSalesManAndPaymentTypeList() {
        List<Customer> customerList = findAllSpecifiedColumn();
        if (!ObjectUtils.isEmpty(customerList)) {
            Set<Long> salesList = customerList.stream()
                    .filter(i -> i.getSales() != null && i.getSales().getId() != null)
                    .map(customer -> customer.getSales().getId()).collect(Collectors.toSet());
            Map<Long, User> userMap = MSUserUtils.getMapByUserIds(Lists.newArrayList(salesList));
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            for (Customer customer : customerList) {
                if (customer.getFinance() != null && customer.getFinance().getPaymentType() != null && Integer.parseInt(customer.getFinance().getPaymentType().getValue()) > 0) {
                    customer.getFinance().setPaymentType(paymentTypeMap.get(customer.getFinance().getPaymentType().getValue()));
                    customer.setPaymentType(paymentTypeMap.get(customer.getPaymentType().getValue()));
                }
                if (customer.getSales() != null && customer.getSales().getId() != null) {
                    User sales = userMap.get(customer.getSales().getId());
                    if (sales != null) {
                        customer.setSales(sales);
                    }
                }
            }
        }
        return customerList;
    }

    /**
     * 获取所有客户列表
     * @return
     * id
     * name
     */
    public List<Customer> findAllCustomerList() {
        List<Customer> customerList = Lists.newArrayList();
        customerList = MDUtils.findAllList(Customer.class, msCustomerFeign::findAllWithIdAndName);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    public List<Customer> findAllCustomerListFromDB() {
        List<Customer> customerList = Lists.newArrayList();
        customerList = MDUtils.findAllList(Customer.class, msCustomerFeign::findAllListWithIdAndName);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    public List<Customer> findCustomersWithSales(MDCustomer customer) {
        List<Customer> customers = Lists.newArrayList();
        customers = MDUtils.findListByCustomCondition(customer, Customer.class, msCustomerFeign::findCustomersWithSales);
        return customers != null && !customers.isEmpty() ? customers.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据业务员id获取客户列表
     * @param salesId
     * @return
     * id
     * name
     */
    public List<Customer> findListBySalesId(Integer salesId) {
        List<Customer> customerList = Lists.newArrayList();
        customerList = MDUtils.findListByCustomCondition(salesId, Customer.class, msCustomerFeign::findListBySalesId);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据跟单员id获取客户列表  // add on 2019-11-22
     * @param merchandiserId
     * @return
     */
    public List<Customer> findListByMerchandiserId(Long merchandiserId) {
        List<Customer> customerList = MDUtils.findListByCustomCondition(merchandiserId, Customer.class, msCustomerFeign::findListByMerchandiserId);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 获取所有Vip客户列表
     * @return
     * id
     * name
     */
    public List<Customer> findAllVipCustomerList() {
        List<Customer> customerList = Lists.newArrayList();
        customerList = MDUtils.findAllList(Customer.class, msCustomerFeign::findListByVipCustomer);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据业务员id获取VIP客户列表
     * @param salesId
     * @return
     * id
     * name
     */
    public List<Customer> findVipListBySalesId(Integer salesId) {
        List<Customer> customerList = Lists.newArrayList();
        customerList = MDUtils.findListByCustomCondition(salesId, Customer.class, msCustomerFeign::findVipListBySalesId);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据跟单员id获取Vip客户列表  // add on 2019-11-22
     * @param merchandiserId
     * @return
     */
    public List<Customer> findVipListByMerchandiserId(Long merchandiserId) {
        List<Customer> customerList = MDUtils.findListByCustomCondition(merchandiserId, Customer.class, msCustomerFeign::findVipListByMerchandiserId);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }


    /**
     * 获取所有非Vip客户列表
     * @return
     * id
     * name
     */
    public List<Customer> findAllNotVipCustomerList() {
        List<Customer> customerList = Lists.newArrayList();
        customerList = MDUtils.findAllList(Customer.class, msCustomerFeign::findListByNotVipCustomer);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据业务员id获取非VIP客户列表
     * @param salesId
     * @return
     * id
     * name
     */
    public List<Customer> findNotVipListBySalesId(Integer salesId) {
        List<Customer> customerList = Lists.newArrayList();
        customerList = MDUtils.findListByCustomCondition(salesId, Customer.class, msCustomerFeign::findNotVipListBySalesId);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据跟单员id获取非Vip客户列表  // add on 2019-11-22
     * @param merchandiserId
     * @return
     */
    public List<Customer> findNotVipListByMerchandiserId(Long merchandiserId) {
        List<Customer> customerList = MDUtils.findListByCustomCondition(merchandiserId, Customer.class, msCustomerFeign::findNotVipListByMerchandiserId);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据客户ids列表查询VIP客户列表（id，name） 2021-6-12
     *
     * @param customerIds
     * @return
    */
    public List<Customer> findVipListByCustomerIdsFromCacheForRPT(List<Long> customerIds) {
        List<Customer> customerList = Lists.newArrayList();
        if (customerIds != null && !customerIds.isEmpty()) {
            if (customerIds.size() < 50) {  //50
                customerList = MDUtils.findListNecessaryConvertType(Customer.class, ()->msCustomerFeign.findVipListByCustomerIdsFromCacheForRPT(customerIds));
            } else {
                List<Customer> tempCustomers = Lists.newArrayList();
                Lists.partition(customerIds, 50).forEach(partCustomerIds -> {
                    List<Customer> partCustomers = MDUtils.findListNecessaryConvertType(Customer.class, ()->msCustomerFeign.findVipListByCustomerIdsFromCacheForRPT(partCustomerIds));
                    if (partCustomers != null && !partCustomers.isEmpty()) {
                        tempCustomers.addAll(partCustomers);
                    }
                });
                customerList.addAll(tempCustomers);
            }
        }
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据客户ids列表查询非VIP客户（普通客户）列表（id，name） 2021-6-12
     *
     * @param customerIds
     * @return
    */
    public List<Customer> findNotVipListByCustomerIdsFromCacheForRPT(List<Long> customerIds) {
        List<Customer> customerList = Lists.newArrayList();
        if (customerIds != null && !customerIds.isEmpty()) {  //50
            if (customerIds.size() < 50) {
                customerList = MDUtils.findListNecessaryConvertType(Customer.class, ()->msCustomerFeign.findNotVipListByCustomerIdsFromCacheForRPT(customerIds));
            } else {
                List<Customer> tempCustomers = Lists.newArrayList();
                Lists.partition(customerIds, 50).forEach(partCustomerIds -> {
                    List<Customer> partCustomers = MDUtils.findListNecessaryConvertType(Customer.class, ()->msCustomerFeign.findNotVipListByCustomerIdsFromCacheForRPT(partCustomerIds));
                    if (partCustomers != null && !partCustomers.isEmpty()) {
                        tempCustomers.addAll(partCustomers);
                    }
                });
                customerList.addAll(tempCustomers);
            }
        }
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据id或paymentType获取customer列表
     * @param id
     * @param paymentType
     * @return
     * id,
     * code,
     * name,
     * full_name,
     * contract_date,
     * sales_id as 'sales.id',
     * payment_type as 'paymentType.value',
     * address,
     * phone,
     * fax,
     * master,
     * mobile,
     * email,
     * project_owner,
     * project_owner_phone,
     * project_owner_qq,
     * service_owner,
     * service_owner_phone,
     * service_owner_qq,
     * technology_owner,
     * technology_owner_phone,
     * technology_owner_qq,
     * finance_owner,
     * finance_owner_phone,
     * finance_owner_qq
     */
    public List<Customer> findAllByIdAndPaymentType(Long id, Integer paymentType) {
        List<Customer> customerList = Lists.newArrayList();
        Customer customer = new Customer();
        customer.setId(id);
        customer.setPaymentType(paymentType==null?null:new Dict(paymentType,""));

        customerList = MDUtils.findList(customer, Customer.class, MDCustomer.class, msCustomerFeign::findAllByIdAndPaymentType);
        return customerList!=null && !customerList.isEmpty() ?customerList.stream().sorted(Comparator.comparing(Customer::getCode)).collect(Collectors.toList()):Lists.newArrayList();
    }

    /**
     * 根据paymentType查询客户列表id
     * @param paymentType
     * @return
     * id
     */
    public List<Long> findCustomerIdByPaymentType(Integer paymentType) {
        MSResponse<List<Long>> listMSResponse = msCustomerFeign.findCustomerIdByPaymentType(paymentType);
        if (MSResponse.isSuccess(listMSResponse)) {
            List<Long> customerIdList = listMSResponse.getData();
            log.warn("Customer微服务findCustomerIdByPaymentType方法返回:{}", customerIdList);
            return customerIdList;
        }
        return Lists.newArrayList();
    }

    /**
     * 获取VIP客户列表
     * @return
     *   id,name
     */
    public List<Customer> findListByVipCustomer() {
        return MDUtils.findAllList(Customer.class, msCustomerFeign::findListByVipCustomer);
    }

    /**
     * 保存客户信息
     * @param customer
     * @param isNew
     * @return
     */
    public MSErrorCode save(Customer customer,boolean isNew) {
        return MDUtils.genericSaveShouldReturnId(customer, MDCustomer.class, isNew, isNew?msCustomerFeign::saveCustomer:msCustomerFeign::updateCustomer,true);
    }

    /**
     * 修改客户业务员信息
     * @param customer
     * @return
     */
    public MSErrorCode updateSales(Customer customer) {
        return MDUtils.genericSave(customer, MDCustomer.class, false, msCustomerFeign::updateSales);
    }

    /**
     * 修改跟单员信息  //2019-11-14
     * @param customer
     */
    public void updateMerchandiser(Customer customer) {
        MSErrorCode msErrorCode = MDUtils.genericSave(customer, MDCustomer.class, false, msCustomerFeign::updateMerchandiserId);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务修改跟单员信息失败.失败原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 删除
     *
     * @param customer
     * @return
     */
    public MSErrorCode delete(Customer customer) {
        return MDUtils.genericSave(customer, MDCustomer.class, false, msCustomerFeign::removeCustomer);
    }

    /**
     * 修改业务授权客户
     * @param id
     * @param type
     * @param customerIds
     */
    public void updateAuthorizedCustomers(Long id, Integer type, List<Long> customerIds){
        MSResponse<Integer> response = msCustomerFeign.updateAuthorizedCustomers(id, type, customerIds);
        if(response.getCode() > 0){
            throw new RuntimeException("修改业务授权客户失败，失败原因:" + response.getMsg());
        }
    }

    public List<Long> findIdListByOfflineOrderFlagFromCacheForSD(){
        MSResponse<List<Long>> listMSResponse = msCustomerFeign.findIdListByOfflineOrderFlagFromCacheForSD();
        if (MSResponse.isSuccess(listMSResponse)) {
            return listMSResponse.getData();
        }
        return Lists.newArrayList();
    }
}

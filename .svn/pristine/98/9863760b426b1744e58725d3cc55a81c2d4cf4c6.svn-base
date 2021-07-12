package com.wolfking.jeesite.ms.cc.entity.mapper;

import com.kkl.kklplus.entity.cc.ReminderStatus;
import com.kkl.kklplus.entity.cc.ReminderType;
import com.kkl.kklplus.entity.cc.vm.ReminderListModel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.mapstruct.*;

import java.util.Map;

/**
 * 催单实体转换
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ReminderModelMapper {

    private static String DateFormat = "yyyy-MM-dd HH:mm:ss";

    private static CustomerService customerService = SpringContextHolder.getBean(CustomerService.class);
    private static ServicePointService servicePointService = SpringContextHolder.getBean(ServicePointService.class);

    //region ReminderListModel to viewModel
    /**
     * 数据转视图模型
     */
    @Mappings({
            @Mapping(target = "reminderTypeName",ignore = true),
            @Mapping(target = "dataSourceName",ignore = true),
            @Mapping(target = "statusName",ignore = true),
            @Mapping(target = "customer",ignore = true),
            @Mapping(target = "servicePoint",ignore = true),
            @Mapping(target = "subArea",ignore = true),
            @Mapping(target = "creator",ignore = true),
            @Mapping(target = "logs",ignore = true),
            @Mapping(target = "processDate",ignore = true),
            @Mapping(target = "completeDate",ignore = true),
            //@Mapping(target = "reminderLog",ignore = true),
            //@Mapping(target = "createBy",ignore = true),
            @Mapping(target = "createDate",ignore = true),
    })
    public abstract ReminderModel toViewModel(ReminderListModel model,
                                              Map<Long, ServicePoint> spMap,
                                              Map<Long, Customer> customerMap,
                                              Map<Integer, String> dataSourceMap,
                                              Map<Long, Area> areaMap );

    @AfterMapping
    protected void after(@MappingTarget final ReminderModel viewModel,
                         ReminderListModel model,
                         Map<Long, ServicePoint> spMap,
                         Map<Long, Customer> customerMap,
                         Map<Integer, String> dataSourceMap,
                         Map<Long, Area> areaMap
    ) {
        StringBuilder stName = new StringBuilder(100);
        if(model.getCustomerId()>0){
            Customer customer = customerMap.get(model.getCustomerId());
            if (customer == null) {
                customer = customerService.getFromCacheAsRequired(model.getCustomerId(), null);
                if (customer != null) {
                    customerMap.put(customer.getId(), customer);
                }
            }
            if (customer != null) {
                viewModel.setCustomer(customer);
            }
        }
        if(model.getServicepointId() >0){
            ServicePoint servicePoint = spMap.get(model.getServicepointId());
            if (servicePoint == null) {
                servicePoint = servicePointService.getFromCacheAsRequired(model.getServicepointId(),null);
                if (servicePoint != null) {
                    spMap.put(servicePoint.getId(), servicePoint);
                }
            }
            if (servicePoint != null) {
                viewModel.setServicePoint(servicePoint);
            }
        }
        ReminderType reminderType = ReminderType.fromCode(model.getReminderType());
        if(reminderType != null){
            viewModel.setReminderTypeName(reminderType.getMsg());
        }
        //status
        ReminderStatus status = ReminderStatus.fromCode(model.getStatus());
        if(status != null){
            viewModel.setStatusName(status.getMsg());
        }
        //data source
        if(model.getDataSource() > 0){
            stName.setLength(0);
            stName.append(dataSourceMap.get(model.getDataSource()));
            if(stName.toString().equalsIgnoreCase("null")) {
                stName.setLength(0);
                stName.append(MSDictUtils.getDictLabel(String.valueOf(model.getDataSource()), "order_data_source", ""));
                if(StringUtils.isNotBlank(stName.toString())) {
                    dataSourceMap.put(model.getDataSource(),stName.toString());
                    viewModel.setDataSourceName(stName.toString());
                }
            }else{
                viewModel.setDataSourceName(StringUtils.EMPTY);
            }
        }

        viewModel.setCreator(new User(0L,model.getCreateName(),""));
        //街道
        Area subArea = null;
        if(model.getSubAreaId()>3){
            subArea = areaMap.get(model.getSubAreaId());
            if(subArea == null) {
                subArea = new Area(model.getSubAreaId());
                subArea.setName(AreaUtils.getTownName(model.getAreaId(), model.getSubAreaId()));
                areaMap.put(model.getSubAreaId(),subArea);
            }
            viewModel.setSubArea(subArea);
        }else if (model.getAreaId() > 0) {
            subArea = areaMap.get(model.getAreaId());
            if (subArea == null) {
                subArea = new Area(model.getAreaId());
                subArea.setName(AreaUtils.getCountyName(model.getAreaId()));
                areaMap.put(model.getSubAreaId(), subArea);
            }
            viewModel.setSubArea(subArea);
        }else{
            viewModel.setSubArea(new Area(model.getAreaId()));
        }

        //日期格式化
        if(model.getCreateAt() >0){
            viewModel.setCreateDate(DateFormatUtils.format(model.getCreateAt(), DateFormat));
        }

        if(model.getProcessAt() >0){
            viewModel.setProcessDate(DateFormatUtils.format(model.getProcessAt(), DateFormat));
        }

        if(model.getCompleteAt() >0) {
            viewModel.setCompleteDate(DateFormatUtils.format(model.getCompleteAt(), DateFormat));
        }
        if(model.getOrderCloseAt() >0) {
            viewModel.setOrderCloseDate(DateFormatUtils.format(model.getOrderCloseAt(), DateFormat));
        }
    }

    /**
     * 转List
    public abstract List<ReminderModel> toViewModels(List<ReminderListModel> models);
     */

    //endregion

}

package com.wolfking.jeesite.ms.cc.entity.mapper;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.cc.*;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.cc.entity.ReminderItemModel;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.mapstruct.*;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 催单实体转换
 * Reminder -> ReminderModel
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ReminderViewModelMapper {

    private static String DateFormat = "yyyy-MM-dd HH:mm:ss";

    private static CustomerService customerService = SpringContextHolder.getBean(CustomerService.class);
    private static ServicePointService servicePointService = SpringContextHolder.getBean(ServicePointService.class);
    private static OrderService orderService = SpringContextHolder.getBean(OrderService.class);
    private static B2bCustomerMapService b2bCustomerMapService = SpringContextHolder.getBean(B2bCustomerMapService.class);

    //region reminder to view model

    @Mappings({
            @Mapping(target = "createRemark",source = "reminderRemark"),
            @Mapping(target = "createAt",source = "createDt"),
            @Mapping(target = "processName",source = "processBy"),
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
            @Mapping(target = "createDate",ignore = true),
            @Mapping(target = "items",ignore = true),
    })
    public abstract ReminderModel toViewModel(Reminder model);

    @AfterMapping
    protected void after(@MappingTarget final ReminderModel viewModel, Reminder model) {
        StringBuilder stName = new StringBuilder(100);
        if(model.getCustomerId()>0){
            Customer customer = customerService.getFromCacheAsRequired(model.getCustomerId(),null);
            if(customer != null){
                viewModel.setCustomer(customer);
            }
        }
        if(model.getServicepointId() >0){
            ServicePoint servicePoint = servicePointService.getFromCacheAsRequired(model.getServicepointId(),null);
            if(servicePoint != null){
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
            stName.append(MSDictUtils.getDictLabel(String.valueOf(model.getDataSource()),"order_data_source",""));
            viewModel.setDataSourceName(stName.toString());
        }
        //createby
        User creator = new User();
        if(model.getCreateById() != null && model.getCreateById() >0){
            creator.setId(model.getCreateById());
            stName.setLength(0);
            stName.append(MSUserUtils.getName(model.getCreateById()));
            creator.setName(stName.toString());
            viewModel.setCreateName(stName.toString());
            stName.setLength(0);
        }
        viewModel.setCreator(creator);
        //街道
        Area subArea = null;
        if(model.getSubAreaId()>3){
            subArea = new Area(model.getSubAreaId());
            subArea.setName(AreaUtils.getTownName(model.getAreaId(),model.getSubAreaId()));
        }else{
            subArea = new Area(model.getAreaId());
            if(model.getAreaId() >0){
                subArea.setName(AreaUtils.getCountyName(model.getAreaId()));
            }
        }
        viewModel.setSubArea(subArea==null?new Area():subArea);

        //日期格式化
        if(model.getCreateDt() >0){
            viewModel.setCreateDate(DateFormatUtils.format(model.getCreateDt(), DateFormat));
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
        if(!CollectionUtils.isEmpty(model.getItems())){
            List<ReminderItem> items = model.getItems();
            List<ReminderItemModel> itemModels = Lists.newArrayListWithCapacity(items.size());
            ReminderItem item;
            ReminderItemModel.ReminderItemModelBuilder builder;
            for(int i=0,size=items.size();i<size;i++){
                item = items.get(i);
                builder = ReminderItemModel.builder()
                        .id(item.getId())
                        .itemNo(item.getItemNo())
                        .createName(item.getCreateName())
                        .creatorTypeName(ReminderCreatorType.fromCode(item.getCreatorType()).getMsg())
                        .createAt(item.getCreateAt())
                        //.createDate(item.getCreateAt()>0?DateFormatUtils.format(item.getCreateAt(),DateFormat):"")
                        .createRemark(item.getCreateRemark())
                        .processName(item.getProcessName())
                        .processorTypeName(ReminderCreatorType.fromCode(item.getProcessorType()).getMsg())
                        .processAt(item.getProcessAt())
                        //.processDate(item.getProcessAt()>0?DateFormatUtils.format(item.getProcessAt(),DateFormat):"")
                        .processRemark(item.getProcessRemark())
                        .processTimeLiness(item.getProcessTimeLiness())
                        .timeoutTimeLiness(item.getTimeoutTimeLiness())
                        .timeoutFlag(item.getTimeoutFlag())
                        .timeoutAt(item.getTimeoutAt())
                        .cutOffTimeLiness(item.getCutOffTimeLiness())
                        .status(item.getStatus())
                        .strId(String.valueOf(item.getId()))
                        .handleTimeLiness(item.getHandleTimeLiness())
                        .reminderReason(item.getReminderReason());
                //.timeoutDate(item.getTimeoutAt()>0?DateFormatUtils.format(item.getTimeoutAt(),DateFormat):"");
                itemModels.add(builder.build());
            }
            viewModel.setItems(itemModels);
        }

        Order b2bInfo = orderService.getB2BInfoById(model.getOrderId(),model.getQuarter());
        String shopName="";
        if(b2bInfo!=null){
            if(b2bInfo.getB2bShop()!=null && StringUtils.isNoneBlank(b2bInfo.getB2bShop().getShopId())){
                if(B2BDataSourceEnum.isB2BDataSource(model.getDataSource())){
                    shopName = b2bCustomerMapService.getShopName(model.getDataSource(),model.getCustomerId(),b2bInfo.getB2bShop().getShopId());
                }else{
                    shopName = B2BMDUtils.getShopName(model.getCustomerId(), b2bInfo.getB2bShop().getShopId());
                }
            }
            viewModel.setParentBizOrderId(b2bInfo.getParentBizOrderId());
        }
        viewModel.setShopName(shopName);
    }

    /**
     * 转List
     */
    public abstract List<ReminderModel> reminderToViewModels(List<Reminder> reminders);

    //endregion
}

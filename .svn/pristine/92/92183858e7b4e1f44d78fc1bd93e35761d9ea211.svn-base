package com.wolfking.jeesite.ms.cc.entity.mapper;

import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.cc.entity.AbnormalFormModel;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;

/**
 * 催单实体转换
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class AbnormalFormModelMapper {

    private static String DateFormat = "yyyy-MM-dd HH:mm:ss";

    //region ReminderListModel to viewModel
    /**
     * 数据转视图模型
     */
    public abstract AbnormalFormModel toViewModel(AbnormalForm model, Map<Long, User> userMap);

    @AfterMapping
    protected void after(@MappingTarget AbnormalFormModel viewModel, AbnormalForm model, Map<Long, User> userMap) {
        AppFeedbackEnum.Channel channel = AppFeedbackEnum.Channel.fromValue(model.getChannel());
        if (channel != null) {
            viewModel.setChannelName(channel.getLabel());
        }
/*
        if (model.getCustomerId() > 0L) {
            Customer customer = (Customer)customerMap.get(model.getCustomerId());
            if (customer == null) {
                customer = customerService.getFromCacheAsRequired(model.getCustomerId(), (Integer)null);
                if (customer != null) {
                    customerMap.put(customer.getId(), customer);
                }
            }

            if (customer != null) {
                viewModel.setCustomer(customer);
            }
        }*/

       /* if (model.getServicepointId() > 0L) {
            ServicePoint servicePoint = (ServicePoint)spMap.get(model.getServicepointId());
            if (servicePoint == null) {
                servicePoint = servicePointService.getFromCacheAsRequired(model.getServicepointId(), (Integer)null);
                if (servicePoint != null) {
                    spMap.put(servicePoint.getId(), servicePoint);
                }
            }

            if (servicePoint != null) {
                viewModel.setServicePoint(servicePoint);
            }
        }*/

        /*if (model.getProductCategoryId() != null && model.getProductCategoryId() > 0L) {
            str.setLength(0);
            str.append((String)categoryMap.get(model.getProductCategoryId()));
            if (str.toString().equalsIgnoreCase("null")) {
                ProductCategory productCategory = productCategoryService.getFromCache(model.getProductCategoryId());
                if (productCategory != null) {
                    categoryMap.put(model.getProductCategoryId(), productCategory.getName());
                    str.setLength(0);
                    str.append(productCategory.getName());
                }
            }

            viewModel.setProductCategoryName(str.toString());
        }
*/
        User closeBy;
        if (model.getKefuId() != null && model.getKefuId() > 0L) {
            closeBy = (User)userMap.get(model.getKefuId());
            if (closeBy == null) {
                closeBy = MSUserUtils.get(model.getKefuId());
                if (closeBy != null) {
                    userMap.put(model.getKefuId(), closeBy);
                }
            }

            if (closeBy != null) {
                viewModel.setKefu(closeBy);
            }
        }

        if (model.getCreateById() != null && model.getCreateById() > 0L) {
            closeBy = (User)userMap.get(model.getKefuId());
            if (closeBy == null) {
                closeBy = MSUserUtils.get(model.getCreateById());
                if (closeBy != null) {
                    userMap.put(model.getCreateById(), closeBy);
                }
            }

            if (closeBy != null) {
                viewModel.setCreator(closeBy);
            }
        }

        if (model.getCloseBy() != null && model.getCloseBy() > 0L) {
            closeBy = (User)userMap.get(model.getCloseBy());
            if (closeBy == null) {
                closeBy = MSUserUtils.get(model.getCloseBy());
                if (closeBy != null) {
                    userMap.put(model.getCloseBy(), closeBy);
                }
            }

            if (closeBy != null) {
                viewModel.setCloseByName(closeBy.getName());
            }
        }

        if (model.getCreateDt() != null && model.getCreateDt() > 0L) {
            viewModel.setStrCreateDate(DateFormatUtils.format(model.getCreateDt(), DateFormat));
        }

        if (model.getCloseAt() != null && model.getCloseAt() > 0L) {
            viewModel.setCloseDate(DateFormatUtils.format(model.getCloseAt(), DateFormat));
        }

    }

    /**
     * 转List
     */
    public abstract List<AbnormalFormModel> toViewModels(List<AbnormalForm> models);

    //endregion
}

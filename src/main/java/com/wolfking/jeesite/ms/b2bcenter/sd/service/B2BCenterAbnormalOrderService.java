package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.b2bcenter.sd.dao.B2BOrderDao;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class B2BCenterAbnormalOrderService {

    @Resource
    private B2BOrderDao b2BOrderDao;

    /**
     * 查询B2B工单退单待审核列表
     */
    public Page<Order> getB2BOrderReturnApproveList(Page<OrderSearchModel> page, OrderSearchModel searchModel) {
        searchModel.setPage(page);

        List<Order> orderList = b2BOrderDao.getB2BOrderReturnApproveList(searchModel);
        List<Long> cancelApplyByIdList = orderList.stream().map(order -> order.getOrderStatus().getCancelApplyBy().getId()).collect(Collectors.toList());
        Map<Long, String> names = MSUserUtils.getNamesByUserIds(cancelApplyByIdList);
        Page<Order> rtnPage = new Page<>(page.getPageNo(), page.getPageSize(), page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (orderList.size() > 0) {
            Map<String, Dict> cancelResponsibleMap = MSDictUtils.getDictMap(Dict.DICT_TYPE_CANCEL_RESPONSIBLE);
            Dict cancelResponsibleDict = null;
            for (Order item : orderList) {
                if (item.getOrderStatus().getCancelResponsible() != null && item.getOrderStatus().getCancelResponsible().getValue() != null) {
                    cancelResponsibleDict = cancelResponsibleMap.get(item.getOrderStatus().getCancelResponsible().getValue());
                    if (cancelResponsibleDict != null) {
                        item.getOrderStatus().setCancelResponsible(cancelResponsibleDict);
                    }
                }
                if (names.get(item.getOrderStatus().getCancelApplyBy().getId()) != null) {
                    item.getOrderStatus().getCancelApplyBy().setName(names.get(item.getOrderStatus().getCancelApplyBy().getId()));
                }
            }
            rtnPage.setList(orderList);
        }
        return rtnPage;
    }

}

package com.wolfking.jeesite.modules.sd.service;

import com.wolfking.jeesite.modules.sd.dao.OrderReturnCompleteDao;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderReturnComplete;
import com.wolfking.jeesite.modules.sd.entity.viewModel.ReturnCompleteModel;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

/**
 * 退换货完工
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderReturnCompleteService {

    @Resource
    private OrderReturnCompleteDao orderReturnCompleteDao;

    @Autowired
    private OrderService orderService;


    /**
     * 保存
     * 新增或删除
     * @param entry
     * @return
     */
    @Transactional()
    public void save(ReturnCompleteModel entry) {

        if (entry == null || CollectionUtils.isEmpty(entry.getItems())) {
            throw new RuntimeException("提交数据有误。");
        }

        if (entry.getOrderId() == null || entry.getOrderId() <= 0 || entry.getDataSource() == null || entry.getDataSource() <= 0) {
            throw new RuntimeException("未关联订单:无法保存。");
        }

        Order order = orderService.getOrderById(entry.getOrderId(), entry.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);

        if (order == null || order.getOrderCondition() == null) {
            throw new RuntimeException("读取关联订单信息失败");
        }
        if(entry.getOperateDate() == null){
            entry.setOperateDate(new Date());
        }

        for(OrderReturnComplete item:entry.getItems()) {
            if(item.getUploadFlag() == 1){
                continue;
            }
            if (item.getId() != null && item.getId() > 0) {
                orderReturnCompleteDao.updateEntity(item);
            } else {
                orderReturnCompleteDao.insert(item);
            }
        }
    }


    /**
     * 获取上传的附件数据/
     *
     * @param orderId
     * @param quarter
     * @return
     */
    public List<OrderReturnComplete> getByOrderId(Long orderId, String quarter) {
        return orderReturnCompleteDao.getByOrderId(orderId, quarter);
    }

    public Boolean isCompleted(Long orderId, String quarter){
        List<OrderReturnComplete> items = getByOrderId(orderId,quarter);
        return completeCheckPredicate.test(items);
    }

    private Predicate<List<OrderReturnComplete>> completeCheckPredicate = m -> {
      if(CollectionUtils.isEmpty(m)){
          return false;
      }
      long noUploadCnt = m.stream().filter(t->t.getUploadFlag() == 0).count();
      return noUploadCnt == 0;
    };

    /**
     * 修改产品条码
     */
    @Transactional()
    public void updateSN(OrderReturnComplete item) {
        if (item == null) {
            throw new RuntimeException("参数值未空。");
        }
        orderReturnCompleteDao.updateSN(item);
    }

    /**
     * Api调用成功
     * @param id
     * @param quarter
     * @param updateBy
     * @param updateDate
     */
    @Transactional()
    public int uploadSuccess(@NotNull Long id, @NotNull String quarter, @NotNull String updateBy, @NotNull Date updateDate){
        return orderReturnCompleteDao.uploadSuccess(id,quarter,updateBy,updateDate);
    }

}

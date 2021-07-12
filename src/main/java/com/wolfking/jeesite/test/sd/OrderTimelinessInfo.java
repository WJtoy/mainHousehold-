package com.wolfking.jeesite.test.sd;

import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.sd.entity.OrderSuspendFlagEnum;
import com.wolfking.jeesite.modules.sd.entity.OrderSuspendTypeEnum;
import com.wolfking.jeesite.modules.sd.utils.OrderConditionAdapter;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 检查订单时效信息
 */
@Data
@ToString
@JsonAdapter(OrderTimelinessInfoAdapter.class)
public class OrderTimelinessInfo extends LongIDDataEntity<OrderTimelinessInfo> {

    public OrderTimelinessInfo() {
    }

    public OrderTimelinessInfo(Long orderId) {
        this.orderId = orderId;
    }

    private Long version = 0l;//版本，用于检查订单时候有变更，每次对订单操作都要在redis中递增

    private Long orderId;
    private String orderNo = "";// 订单号
    //派单日期
    private Date planDate;
    //到货日期
    private Date arrivalDate;
    //客评日期
    private Date closeDate;

    private String appCompleteType = ""; //APP完工类型(数据字典：completed_type)
    private Date appCompleteDate; //app自动完工日期

    private Double timeLiness = 0.00;//网点时效(派单~客评的用时) //2018/05/17

}

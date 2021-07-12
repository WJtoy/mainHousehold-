package com.wolfking.jeesite.modules.sd.web;


import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderServicePointSearchModel;
import com.wolfking.jeesite.modules.sd.service.ServicePointOrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 网点工单Controller
 *
 * @author Ryan
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/servicepoint/")
@Slf4j
public class ServicePointOrderController extends BaseController {

    @Autowired
    private ServicePointOrderService servicePointOrderService;

    @Autowired
    private ServicePointService servicePointService;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    //region 网点工单列表




    //endregion 网点


}


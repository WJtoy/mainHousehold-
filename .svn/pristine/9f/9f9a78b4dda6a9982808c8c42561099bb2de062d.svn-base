package com.wolfking.jeesite.modules.api.controller.receipt;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.md.MDCustomerPraiseFee;
import com.wolfking.jeesite.common.exception.AttachmentSaveFailureException;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.controller.RestBaseController;
import com.wolfking.jeesite.modules.api.entity.common.AppPageBaseEntity;
import com.wolfking.jeesite.modules.api.entity.common.RestAppException;
import com.wolfking.jeesite.modules.api.entity.md.RestLoginUserInfo;
import com.wolfking.jeesite.modules.api.entity.receipt.praise.*;
import com.wolfking.jeesite.modules.api.service.sd.AppOrderPraiseService;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.api.util.RestSessionUtils;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * APP工单好评控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/receipt/praise")
public class AppOrderPraiseController extends RestBaseController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private AppOrderPraiseService orderPraiseService;

    private RestLoginUserInfo getUserInfo(HttpServletRequest request) {
        return RestSessionUtils.getLoginUserInfoFromRestSession(request.getAttribute("sessionUserId").toString());
    }

    /**
     * CS9001
     * 获取工单好评信息
     */
    @RequestMapping(value = "getOrderPraiseInfo", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public RestResult<Object> getOrderPraiseInfo(HttpServletRequest request, @RequestBody AppGetOrderPraiseInfoRequest params) {
        return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "此版本好评单接口已停用，请及时更新APP");
//        if (params == null || params.getOrderId() == null || params.getOrderId() <= 0) {
//            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
//        }
//        RestLoginUserInfo userInfo = null;
//        try {
//            userInfo = getUserInfo(request);
//            if (userInfo == null || userInfo.getUserId() == null || userInfo.getServicePointId() == null) {
//                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
//            }
//            Order order = orderService.getOrderById(params.getOrderId(), params.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
//            if (order == null) {
//                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单不存在，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
//            }
//            AppGetOrderPraiseInfoResponse response = orderPraiseService.getOrderPraiseInfo(order, userInfo.getServicePointId());
//            return RestResultGenerator.success(response);
//        } catch (OrderException | RestAppException oe) {
//            return RestResultGenerator.exception(oe.getMessage());
//        } catch (Exception e) {
//            log.error("[RestOrderPraiseController.getOrderPraiseInfo] user:{}", userInfo != null && userInfo.getUserId() != null ? userInfo.getUserId() : 0, e);
//            return RestResultGenerator.exception("获取工单好评信息失败");
//        }
    }

    /**
     * CS9002
     * 保存工单好评信息
     */
    @RequestMapping(value = "saveOrderPraiseInfo", consumes = "multipart/form-data", method = RequestMethod.POST)
    public RestResult<Object> saveOrderPraiseInfo(HttpServletRequest request,
                                                  @RequestParam("file") MultipartFile[] files,
                                                  @RequestParam("json") String json) {
        return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "此版本好评单接口已停用，请及时更新APP");
//        if (StringUtils.isBlank(json)) {
//            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
//        }
//        try {
//            AppSaveOrderPraiseInfoJsonParameterRequest params = GsonUtils.fromJsonNew(json, AppSaveOrderPraiseInfoJsonParameterRequest.class);
//            if ((files == null || files.length == 0) && (params.getPics() == null || !params.hasPictures())) {
//                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
//            }
//            if (params.getOrderId() == null || params.getOrderId() == 0) {
//                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
//            }
//            RestLoginUserInfo userInfo = getUserInfo(request);
//            if (userInfo == null || userInfo.getUserId() == null || userInfo.getServicePointId() == null || userInfo.getEngineerId() <= 0) {
//                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
//            }
//            User user = UserUtils.getAcount(userInfo.getUserId());
//            if (null == user) {
//                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
//            }
//            Order order = orderService.getOrderById(params.getOrderId(), params.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
//            if (order == null || order.getOrderCondition() == null) {
//                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单不存在，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
//            }
//            List<String> picUrls = Lists.newArrayList();
//            if (params.getPics() != null && !params.getPics().isEmpty()) {
//                String picUrl;
//                for (AppSaveOrderPraiseInfoJsonParameterRequest.PicItem picItem : params.getPics()) {
//                    if (StringUtils.isNotBlank(picItem.getUrl())) {
//                        picUrl = StringUtils.replace(picItem.getUrl(), OrderPicUtils.getOrderPicHostDir(), "");
//                        picUrls.add(picUrl);
//                    }
//                }
//            }
//            if (files != null && files.length > 0) {
//                TwoTuple<Boolean, List<String>> saveFileResponse = OrderPicUtils.saveImageFiles(request, files);
//                if (!saveFileResponse.getAElement() || saveFileResponse.getBElement().isEmpty()) {
//                    throw new AttachmentSaveFailureException("保存好评图片失败");
//                }
//                picUrls.addAll(saveFileResponse.getBElement());
//            }
//            MDCustomerPraiseFee customerPraiseFee = orderPraiseService.getCustomerPraiseFee(order.getOrderCondition().getCustomerId());
//            if (picUrls.size() < customerPraiseFee.getPicCount()) {
//                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "好评图片不能少于" + customerPraiseFee.getPicCount() + "张");
//            }
//            if (picUrls.size() > 5) {
//                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "好评图片最多只允许上传5张");
//            }
//            orderPraiseService.saveOrderPraiseInfo(order, userInfo.getServicePointId(), userInfo.getEngineerId(), picUrls, user);
//            return RestResultGenerator.success();
//        } catch (AttachmentSaveFailureException ae) {
//            return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVECOMPONENT.code, ae.getMessage());
//        } catch (OrderException oe) {
//            return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, oe.getMessage());
//        } catch (Exception e) {
//            return RestResultGenerator.exception("保存工单好评失败");
//        }
    }

    /**
     * CS9003
     * 取消工单好评
     */
    @RequestMapping(value = "cancelOrderPraise", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public RestResult<Object> cancelOrderPraise(HttpServletRequest request,
                                                @RequestBody AppCancelOrderPraiseRequest params) {
        return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "此版本好评单接口已停用，请及时更新APP");
//        if (params == null || params.getOrderId() == null || params.getOrderId() <= 0) {
//            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
//        }
//        try {
//            RestLoginUserInfo userInfo = getUserInfo(request);
//            if (userInfo == null || userInfo.getUserId() == null || userInfo.getServicePointId() == null) {
//                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
//            }
//            User user = UserUtils.getAcount(userInfo.getUserId());
//            if (null == user) {
//                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
//            }
//            Order order = orderService.getOrderById(params.getOrderId(), params.getQuarter(), OrderUtils.OrderDataLevel.HEAD, true);
//            if (order == null) {
//                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单不存在，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
//            }
//            orderPraiseService.cancelOrderPraise(order, userInfo.getServicePointId(), user);
//            return RestResultGenerator.success();
//        } catch (AttachmentSaveFailureException ae) {
//            return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVECOMPONENT.code, ae.getMessage());
//        } catch (OrderException oe) {
//            return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, oe.getMessage());
//        } catch (Exception e) {
//            return RestResultGenerator.exception("保存工单好评失败");
//        }
    }

    /**
     * CS9004
     * 获取被驳回的好评单列表
     */
    @RequestMapping(value = "getRejectedOrderPraiseList", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public RestResult<Object> getRejectedOrderPraiseList(HttpServletRequest request, @RequestBody AppPageBaseEntity params) {
        return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "此版本好评单接口已停用，请及时更新APP");
//        if (params == null || params.getPageNo() == null || params.getPageNo() <= 0
//                || params.getPageSize() == null || params.getPageSize() <= 0) {
//            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
//        }
//        RestLoginUserInfo userInfo = null;
//        try {
//            userInfo = getUserInfo(request);
//            if (userInfo == null || userInfo.getUserId() == null || userInfo.getServicePointId() == null || userInfo.getServicePointId() <= 0
//            || userInfo.getEngineerId() <= 0) {
//                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
//            }
//            Page<AppGetOrderPraiseListItemResponse> response = orderPraiseService.getRejectedOrderPraiseList(userInfo.getServicePointId(),
//                    userInfo.getEngineerId(), userInfo.getPrimary(),
//                    params.getPageNo(), params.getPageSize());
//            return RestResultGenerator.success(response);
//        } catch (OrderException | RestAppException oe) {
//            return RestResultGenerator.exception(oe.getMessage());
//        } catch (Exception e) {
//            log.error("[RestOrderPraiseController.getRejectedOrderPraiseList] user:{}", userInfo != null && userInfo.getUserId() != null ? userInfo.getUserId() : 0, e);
//            return RestResultGenerator.exception("获取被驳回的好评单列表失败");
//        }
    }
}

package com.wolfking.jeesite.modules.api.controller.receipt;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.validate.ValidatePicItem;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.exception.AttachmentSaveFailureException;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.api.controller.RestBaseController;
import com.wolfking.jeesite.modules.api.entity.common.RestAppException;
import com.wolfking.jeesite.modules.api.entity.md.RestLoginUserInfo;
import com.wolfking.jeesite.modules.api.entity.receipt.validate.AppGetOrderValidateInfoRequest;
import com.wolfking.jeesite.modules.api.entity.receipt.validate.AppGetOrderValidateInfoResponse;
import com.wolfking.jeesite.modules.api.entity.receipt.validate.AppGetOrderValidateStandardResponse;
import com.wolfking.jeesite.modules.api.entity.receipt.validate.AppSaveOrderValidateJsonParameterRequest;
import com.wolfking.jeesite.modules.api.service.sd.AppOrderValidateService;
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
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.validate.service.MSOrderValidateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * APP工单鉴定控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/receipt/validate")
public class AppOrderValidateController extends RestBaseController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private AppOrderValidateService appOrderValidateService;
    @Autowired
    private MSOrderValidateService msOrderValidateService;
    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;

    private RestLoginUserInfo getUserInfo(HttpServletRequest request) {
        return RestSessionUtils.getLoginUserInfoFromRestSession(request.getAttribute("sessionUserId").toString());
    }

    /**
     * CS10001
     * 获取鉴定标准
     */
    @RequestMapping(value = "getOrderValidateStandard", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public RestResult<Object> getOrderValidateStandard(HttpServletRequest request) {
        RestLoginUserInfo userInfo = null;
        try {
            userInfo = getUserInfo(request);
            if (userInfo == null || userInfo.getUserId() == null || userInfo.getServicePointId() == null) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            AppGetOrderValidateStandardResponse response = appOrderValidateService.getOrderValidateStandard();
            return RestResultGenerator.success(response);
        } catch (OrderException | RestAppException oe) {
            return RestResultGenerator.exception(oe.getMessage());
        } catch (Exception e) {
            log.error("[AppOrderValidateController.getOrderValidateStandard] user:{}", userInfo != null && userInfo.getUserId() != null ? userInfo.getUserId() : 0, e);
            return RestResultGenerator.exception("获取工单鉴定标准失败");
        }
    }

    /**
     * CS10002
     * 获取工单鉴定信息
     */
    @RequestMapping(value = "getOrderValidateInfo", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public RestResult<Object> getOrderValidateInfo(HttpServletRequest request, @RequestBody AppGetOrderValidateInfoRequest params) {
        if (params == null || params.getOrderId() == null || params.getOrderId() <= 0) {
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
        }
        RestLoginUserInfo userInfo = null;
        try {
            userInfo = getUserInfo(request);
            if (userInfo == null || userInfo.getUserId() == null || userInfo.getServicePointId() == null) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            Order order = orderService.getOrderById(params.getOrderId(), params.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单不存在，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
            }
            if (order.isSuspendedForValidate() == 0) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单找不到未处理的鉴定单");
            }
            AppGetOrderValidateInfoResponse response = appOrderValidateService.getOrderValidateInfo(order.getId(), order.getQuarter());
            return RestResultGenerator.success(response);
        } catch (OrderException | RestAppException oe) {
            return RestResultGenerator.exception(oe.getMessage());
        } catch (Exception e) {
            log.error("[AppOrderValidateController.getOrderValidateInfo] user:{}", userInfo != null && userInfo.getUserId() != null ? userInfo.getUserId() : 0, e);
            return RestResultGenerator.exception("获取工单鉴定信息失败");
        }
    }


    /**
     * CS10003
     * 保存鉴定信息
     */
    @RequestMapping(value = "saveOrderValidateInfo", consumes = "multipart/form-data", method = RequestMethod.POST)
    public RestResult<Object> saveOrderValidateInfo(HttpServletRequest request,
                                                    @RequestParam("file") MultipartFile[] files,
                                                    @RequestParam("json") String json) {
        if (StringUtils.isBlank(json)) {
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
        }
        try {
            AppSaveOrderValidateJsonParameterRequest params = GsonUtils.fromJsonNew(json, AppSaveOrderValidateJsonParameterRequest.class);
            if (files == null || files.length == 0) {
                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
            }
            if (params.getOrderId() == null || params.getOrderId() == 0
                    || params.getProductId() == null || params.getProductId() == 0 || StringUtils.isBlank(params.getProductSn())) {
                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
            }
            if (params.getErrorTypeId() == null || params.getErrorTypeId() == 0 || params.getActionCodeId() == null || params.getActionCodeId() == 0) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "必须选择故障类型");
            }
            RestLoginUserInfo userInfo = getUserInfo(request);
            if (userInfo == null || userInfo.getUserId() == null || userInfo.getServicePointId() == null || userInfo.getEngineerId() <= 0) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            User user = UserUtils.getAcount(userInfo.getUserId());
            if (null == user) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            Order order = orderService.getOrderById(params.getOrderId(), params.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单不存在，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
            }
            if (order.getDataSourceId() != B2BDataSourceEnum.VIOMI.id) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "该工单无法进行退换货操作");
            }
            if (order.isSuspended() == 1) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单被挂起，无法操作，请联系客服");
            }
            MSResponse msResponse = b2BCenterOrderService.checkProductSN(order.getDataSourceId(), order.getWorkCardId(), params.getProductSn(), user);
            if (!MSResponse.isSuccessCode(msResponse)) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "产品条码检查错误，请检查后重试");
            }
            List<ValidatePicItem> validatePicItems = Lists.newArrayList();
            ValidatePicItem picItem;
            Map<String, ValidatePicItem> validatePicItemStandardMap = msOrderValidateService.getValidatePicStandardMap();
            ValidatePicItem standardItem;
            TwoTuple<Boolean, List<String>> saveFileResponse = OrderPicUtils.saveImageFiles(request, files);
            if (!saveFileResponse.getAElement() || saveFileResponse.getBElement().size() != params.getPicFileCodes().size()) {
                throw new AttachmentSaveFailureException("保存鉴定图片失败");
            }
            for (int i = 0; i < params.getPicFileCodes().size(); i++) {
                standardItem = validatePicItemStandardMap.get(params.getPicFileCodes().get(i));
                if (standardItem != null) {
                    picItem = new ValidatePicItem();
                    picItem.setCode(standardItem.getCode());
                    picItem.setName(standardItem.getName());
                    picItem.setUrl(saveFileResponse.getBElement().get(i));
                    validatePicItems.add(picItem);
                }
            }
            appOrderValidateService.saveOrderValidateInfo(order.getId(), order.getQuarter(), params, validatePicItems, user);
            return RestResultGenerator.success();
        } catch (AttachmentSaveFailureException ae) {
            return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVECOMPONENT.code, ae.getMessage());
        } catch (OrderException oe) {
            return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, oe.getMessage());
        } catch (Exception e) {
            return RestResultGenerator.exception("保存工单鉴定信息失败");
        }
    }

}

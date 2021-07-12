package com.wolfking.jeesite.modules.api.controller.md;

import com.wolfking.jeesite.modules.api.controller.RestBaseController;
import com.wolfking.jeesite.modules.api.entity.md.RestCheckUpdate;
import com.wolfking.jeesite.modules.api.entity.md.RestGetVerifyCode;
import com.wolfking.jeesite.modules.api.entity.md.RestLoginUserInfo;
import com.wolfking.jeesite.modules.api.entity.md.RestUploadPushInfo;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestSessionUtils;
import com.wolfking.jeesite.modules.sys.service.APPNoticeService;
import com.wolfking.jeesite.modules.sys.service.DictService;
import com.wolfking.jeesite.modules.td.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/common/")
public class CommonController extends RestBaseController {
    @Autowired
    private DictService dictService;
    @Autowired
    private APPNoticeService appNoticeService;
    @Autowired
    private MessageService messageService;

    /**
     * 检查更新
     * @param checkUpdate
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "checkUpdate", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    public RestResult<Object> checkUpdate(@RequestBody RestCheckUpdate checkUpdate)  throws Exception {
        return dictService.checkUpdate(checkUpdate);
    }

    /**
     * 上传推送相关信息
     * @param request
     * @param pushInfo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "uploadPushInfo", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    public RestResult<Object> uploadPushInfo(HttpServletRequest request, @RequestBody RestUploadPushInfo pushInfo)  throws Exception {
        RestLoginUserInfo restLoginUserInfo = RestSessionUtils.getLoginUserInfoFromRestSession(request.getAttribute("sessionUserId").toString());
        return appNoticeService.uploadPushInfo(restLoginUserInfo.getUserId() , restLoginUserInfo.getPhoneType(), pushInfo);
    }

    /**
     * 获取短信验证码
     * @param verifyCode
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "getVerifyCode", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    public RestResult<Object> getVerifyCode(@RequestBody RestGetVerifyCode verifyCode)  throws Exception {
        return messageService.getVerifyCode(verifyCode);
    }
}

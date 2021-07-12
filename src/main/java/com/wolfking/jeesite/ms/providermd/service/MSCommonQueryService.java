package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.ms.providermd.feign.MSCommonQueryFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MSCommonQueryService {
    @Autowired
    private MSCommonQueryFeign msCommonQueryFeign;

    /**
     * 检查连接是否通畅
     */
    public void checkConnection() {
        MSResponse<Integer> msResponse = msCommonQueryFeign.checkConnection();
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("调用连接是否通畅微服务失败.错误原因:" + msResponse.getMsg());
        }
    }
}

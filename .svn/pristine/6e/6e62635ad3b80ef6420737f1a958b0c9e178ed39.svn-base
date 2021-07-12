package com.wolfking.jeesite.ms.tmall.rpt.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.rpt.B2BProcesslog;
import com.kkl.kklplus.entity.b2b.rpt.ProcesslogSearchModel;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderFailureLog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.tmall.rpt.dao.B2BOrderInfoDao;
import com.wolfking.jeesite.ms.tmall.rpt.entity.B2BRptSearchModel;
import com.wolfking.jeesite.ms.tmall.rpt.feign.MSTmallOrderRptFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderInfoRptService {

    @Autowired
    private MSTmallOrderRptFeign msOrderRptFeign;

    @Autowired
    private B2BOrderInfoDao b2BOrderInfoDao;

   //获取天猫状态数据查询信息
    public Page<B2BProcesslog> getList(Page<ProcesslogSearchModel> page, ProcesslogSearchModel processlogSearchModel){
        Page<B2BProcesslog> processlogPage  = new Page<>();
        processlogPage.setPageSize(page.getPageSize());
        processlogPage.setPageNo(page.getPageNo());
        processlogSearchModel.setPage(new MSPage<>(processlogPage.getPageNo(),processlogPage.getPageSize()));
        MSResponse<MSPage<B2BProcesslog>> processLog = msOrderRptFeign.getList(processlogSearchModel);
        if (MSResponse.isSuccess(processLog)){
            MSPage<B2BProcesslog> data = processLog.getData();
            processlogPage.setCount(data.getRowCount());
            processlogPage.setList(data.getList());
        }
        return  processlogPage;
    }

    /**
     * 根据workCardId查询订单id和分片
     * @param workCardId
     * @return
     */
    public Map<String,Object> getOrderId(String workCardId){
       return b2BOrderInfoDao.getOrderIdQuarter(workCardId);
    }

    /**
     * 获取天猫失败日志信息列表
     * @param page
     * @param processlogSearchModel
     * @return
     */
    public Page<B2BOrderFailureLog> getTmallFailLogList(Page<B2BRptSearchModel> page, B2BRptSearchModel processlogSearchModel){
        Page<B2BOrderFailureLog> processlogPage  = new Page<>();
        processlogPage.setPageSize(page.getPageSize());
        processlogPage.setPageNo(page.getPageNo());
        processlogSearchModel.setPage(new MSPage<>(processlogPage.getPageNo(),processlogPage.getPageSize()));
        MSResponse<MSPage<B2BOrderFailureLog>> processLog = msOrderRptFeign.getFailLogList(processlogSearchModel);
        if (MSResponse.isSuccess(processLog)){
            MSPage<B2BOrderFailureLog> data = processLog.getData();
            processlogPage.setCount(data.getRowCount());
            processlogPage.setList(data.getList());
        }
        return  processlogPage;
    }
    /**
     * 根据Id获取信息
     * @param id
     * @return
     */
    public B2BOrderFailureLog getLogById(long id){
        MSResponse<B2BOrderFailureLog> logById = msOrderRptFeign.getLogById(id);
        B2BOrderFailureLog data = null;
        if (MSResponse.isSuccess(logById)){
            data = logById.getData();
        }
        return data;
    }

    /**
     * 重发
     * @param retryOperationData
     */
    public AjaxJsonEntity retryData(B2BRetryOperationData retryOperationData){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (retryOperationData!=null){
            MSResponse response = msOrderRptFeign.retryData(retryOperationData);
            if (MSResponse.isSuccessCode(response)){
                ajaxJsonEntity.setSuccess(true);
            }else {
                ajaxJsonEntity.setSuccess(false);
            }
            ajaxJsonEntity.setMessage(response.getMsg());
        }
        return ajaxJsonEntity;
    }

    /**
     * 忽略本条信息
     * @param retryOperationData
     */
    public AjaxJsonEntity closeLog(B2BRetryOperationData retryOperationData){
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        if (retryOperationData!=null){
            MSResponse response = msOrderRptFeign.closeLog(retryOperationData);
            if (MSResponse.isSuccessCode(response)){
                ajaxJsonEntity.setSuccess(true);
            }else {
                ajaxJsonEntity.setSuccess(false);
            }
            ajaxJsonEntity.setMessage(response.getMsg());
        }
        return ajaxJsonEntity;
    }

    public String getQuarterFromOrderNo(String orderNo){
        if (orderNo!=null && orderNo.length()>0){
            String yearMonth =orderNo.substring(1, 7);
            String quarter = yearMonth.substring(0, 4);
            int substring = Integer.parseInt(yearMonth.substring(4, 6));
            if (substring<4){
                quarter = quarter + "1";
            }else if (substring<7){
                quarter = quarter + "2";
            }else if (substring<10){
                quarter = quarter + "3";
            }else {
                quarter = quarter + "4";
            }
            return quarter;
        }
        return "";
    }
}

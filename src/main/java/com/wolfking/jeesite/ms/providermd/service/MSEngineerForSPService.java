package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDEngineer;
import com.kkl.kklplus.entity.md.dto.MDEngineerDto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.ms.providermd.feign.MSEngineerFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MSEngineerForSPService {
    @Autowired
    private MSEngineerFeign msEngineerFeign;

    /**
     * 根据id获取安维人员信息
     * @param id
     * @return
     */
    public Engineer getById(Long id) {
        if (id == null) {
            return null;
        }
        Engineer engineer = MDUtils.getById(id, Engineer.class, msEngineerFeign::getById);
        if (engineer == null) {
            engineer = new Engineer(id);
        }
        return engineer;
    }

    /**
     * 分页查询安维数据
     * @param page
     * @param engineer
     * @return
     */
    public Page<Engineer> findEngineerList(Page<Engineer> page, Engineer engineer) {
        return MDUtils.findListForPage(page, engineer, Engineer.class, MDEngineerDto.class, msEngineerFeign::findEngineerList);
    }

    /**
     * 保存安维信息
     * @param engineer
     * @param isNew
     * @return
     */
    public MSErrorCode save(Engineer engineer, boolean isNew) {
        return MDUtils.genericSaveShouldReturnId(engineer, MDEngineer.class, isNew, isNew?msEngineerFeign::save:msEngineerFeign::update, true);
    }

    /**
     * 升级安维人员
     * @param engineer
     */
    public void upgradeEngineer(Engineer engineer) {
        MSErrorCode msErrorCode = MDUtils.genericSave(engineer, MDEngineer.class, false, msEngineerFeign::updateServicePointId);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务升级安维出错.错误原因:" + msErrorCode.getMsg());
        }
    }

    /**
     *  获取当前网点下所有的非当前人员的安维人员id列表
     * @param engineerId
     * @param servicePointId
     * @return
     */
    public List<Long> findSubEngineerIds(Long engineerId, Long servicePointId) {
        MSResponse<List<Long>> msResponse = msEngineerFeign.findSubEngineerIds(engineerId, servicePointId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return Lists.newArrayList();
    }

    /**
     * 重置安维人员的主账号标志
     * @param engineerIds
     * @return
     */
    public MSErrorCode resetEngineerMasterFlag(List<Long> engineerIds) {
        return MDUtils.customSave(()->msEngineerFeign.resetEngineerMasterFlag(engineerIds));
    }

    /**
     * 删除安维人员
     *
     * @param engineer
     * @return
     */
    public void delete(Engineer engineer) {
        MSErrorCode msErrorCode = MDUtils.genericSave(engineer, MDEngineer.class, false, msEngineerFeign::enableOrDisable);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务停用或激活安维人员出错.错误原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 检查网点下是否存在其他的主帐号
     * @param servicePointId
     * @param exceptEngineerId
     * @return
     */
    public Integer checkMasterEngineer(Long servicePointId,Long exceptEngineerId) {
        MSResponse<Integer> msResponse = msEngineerFeign.checkMasterEngineer(servicePointId, exceptEngineerId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return 0;
    }

    /**
     * 查询师傅列表
     * @param engineer
     * @return
     */
    public Page<Engineer> findEngineerForKeFu(Page<Engineer> page, Engineer engineer) {
        return MDUtils.findListForPage(page,engineer, Engineer.class, MDEngineer.class, msEngineerFeign::findEngineerForKeFu);
    }
}

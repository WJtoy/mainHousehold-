package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.AppFeedbackEnum;
import com.kkl.kklplus.entity.md.MDAppFeedbackType;
import com.kkl.kklplus.entity.md.dto.MDAppFeedbackTypeDto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.mapper.md.MDAppFeedbackTypeMapper;
import com.wolfking.jeesite.ms.providermd.entity.AppFeedbackTypeVModel;
import com.wolfking.jeesite.ms.providermd.feign.MSAppFeedbackTypeFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MSAppFeedbackTypeService {
    @Autowired
    private MSAppFeedbackTypeFeign msAppFeedbackTypeFeign;

    /**
     * 获取所有app反馈类型
     *
     * @return
     */
    public List<MDAppFeedbackType> findAllList() {
        return MDUtils.findListUnnecessaryConvertType(()-> msAppFeedbackTypeFeign.findAllList());
    }


    /**
     * 分页查询
     *
     * @return
     */
     public Page<AppFeedbackTypeVModel> findListForPage(Page<MDAppFeedbackTypeDto> appFeedbackTypePage, MDAppFeedbackType appFeedbackType){
         Page<MDAppFeedbackTypeDto> page = MDUtils.findMDEntityListForPage(appFeedbackTypePage, appFeedbackType, msAppFeedbackTypeFeign::findList);
         Page<AppFeedbackTypeVModel> returnPage = new Page<>(appFeedbackTypePage.getPageNo(),appFeedbackTypePage.getPageSize());
         List<AppFeedbackTypeVModel> appFeedbackTypeVModelList = Lists.newArrayList();
         AppFeedbackTypeVModel feedbackTypeVModel;
         for(MDAppFeedbackTypeDto appFeedbackTypeDto:page.getList()){
             feedbackTypeVModel = Mappers.getMapper(MDAppFeedbackTypeMapper.class).toAppFeedbackTypeVModel(appFeedbackTypeDto);
             feedbackTypeVModel.setLabel(feedbackTypeVModel.getLabel()+"(" + AppFeedbackEnum.FeedbackType.fromValue(feedbackTypeVModel.getFeedbackType()).getLabel() +")");
             feedbackTypeVModel.setActionTypeName(AppFeedbackEnum.AciontType.fromValue(feedbackTypeVModel.getActionType()).getLabel());
             feedbackTypeVModel.setSumTypeName(AppFeedbackEnum.SummaryType.fromValue(feedbackTypeVModel.getSumType()).getLabel());
             feedbackTypeVModel.setUserTypeName(AppFeedbackEnum.UserType.fromValue(feedbackTypeVModel.getUserType()).getLabel());
             if(appFeedbackTypeDto.getChildrenList()!=null && appFeedbackTypeDto.getChildrenList().size()>0){
                 List<AppFeedbackTypeVModel> list = Lists.newArrayList();
                 AppFeedbackTypeVModel entity;
                 for(MDAppFeedbackType item:appFeedbackTypeDto.getChildrenList()){
                     entity = Mappers.getMapper(MDAppFeedbackTypeMapper.class).FeedbackTypeToVModel(item);
                     if(entity!=null){
                         entity.setFeedbackTypeName(AppFeedbackEnum.FeedbackType.fromValue(entity.getFeedbackType()).getLabel());
                         entity.setActionTypeName(AppFeedbackEnum.AciontType.fromValue(entity.getActionType()).getLabel());
                         entity.setSumTypeName(AppFeedbackEnum.SummaryType.fromValue(entity.getSumType()).getLabel());
                         entity.setUserTypeName(AppFeedbackEnum.UserType.fromValue(entity.getUserType()).getLabel());
                         list.add(entity);
                     }
                 }
                 feedbackTypeVModel.setFeedbackTypeVModelList(list);
             }
             appFeedbackTypeVModelList.add(feedbackTypeVModel);
         }
         returnPage.setCount(page.getCount());
         returnPage.setList(appFeedbackTypeVModelList);
         return returnPage;
     }


    /**
     * 根据反馈类型标识获取指定app反馈类型
     *
     * @param feedbackType
     * @return
     */
    public List<MDAppFeedbackType> findListByFeedbackType(Integer feedbackType) {
        return MDUtils.findListUnnecessaryConvertType(()-> msAppFeedbackTypeFeign.findListByFeedbackType(feedbackType));
    }

    /**
     * DB查询根据id
     *
     * @param id
     * @return
     */
    public MDAppFeedbackType getById(Long id) {
        MSResponse<MDAppFeedbackType> msResponse = msAppFeedbackTypeFeign.getById(id);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }


    /**
     * 根据parentId和label判断是否已近存在
     */
    public Long checkLabel(Long parentId,String label){
        MSResponse<Long> msResponse = msAppFeedbackTypeFeign.checkLabel(parentId,label);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return 0L;
        }
    }

    /**
     * 根据parentId和label,value返回id
     */
    public Long checkValue(MDAppFeedbackType appFeedbackType){
        MSResponse<Long> msResponse = msAppFeedbackTypeFeign.checkValue(appFeedbackType.getParentId(),appFeedbackType.getValue());
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }


    /**
     * 保存数据
     * @param
     * @return
     */
    public void save(MDAppFeedbackType appFeedbackType){
        Long appFeedbackTypeId = checkLabel(appFeedbackType.getParentId(),appFeedbackType.getLabel());
        if(appFeedbackType.getId()!=null && appFeedbackType.getId()>0){
            if(appFeedbackTypeId!=null && appFeedbackTypeId>0 && !appFeedbackTypeId.equals(appFeedbackType.getId())){
                throw new RuntimeException("该反馈类型已经存在");
            }
            if(appFeedbackType.getParentId()==0){
                Long id = checkValue(appFeedbackType);
                if(id!=null && id>0 && !id.equals(appFeedbackType.getId())){
                    throw new RuntimeException("反馈类型的数值已经存在");
                }
            }
            MSResponse<Integer> msResponse = msAppFeedbackTypeFeign.update(appFeedbackType);
            if(msResponse.getCode()>0){
                throw new RuntimeException("保存app分类失败.失败原因" + msResponse.getMsg());
            }
        }else{
            if(appFeedbackTypeId!=null && appFeedbackTypeId>0){
                throw new RuntimeException("该反馈类型已经存在");
            }
            if(appFeedbackType.getParentId()==0){
                Long id = checkValue(appFeedbackType);
                if(id!=null && id>0){
                    throw new RuntimeException("反馈类型的数值已经存在");
                }
            }
            MSResponse<Integer> msResponse = msAppFeedbackTypeFeign.insert(appFeedbackType);
            if(msResponse.getCode()>0){
                throw new RuntimeException("保存app分类失败.失败原因" + msResponse.getMsg());
            }
        }
    }


    /**
     * 缓存命中根据id
     *
     * @param id
     * @return
     */
    public MDAppFeedbackType getByIdFromCache(Long id) {
        MSResponse<MDAppFeedbackType> msResponse = msAppFeedbackTypeFeign.getByIdFromCache(id);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }


    /**
     * 获取app反馈类型的枚举键值对(在页面展示)
     * */
    public List<AppFeedbackEnum.FeedbackType> findFeedbackType(){
        List<AppFeedbackEnum.FeedbackType> list = Lists.newArrayList();
        for(AppFeedbackEnum.FeedbackType appFeedbackTypeEnum:AppFeedbackEnum.FeedbackType.values()){
            list.add(appFeedbackTypeEnum);
        }
        return list;
    }


    /**
     * 获取反馈类型处理方式的枚举键值对(在页面展示)
     * */
    public List<AppFeedbackEnum.AciontType> findActionTypeList(){
        List<AppFeedbackEnum.AciontType> list = Lists.newArrayList();
        for(AppFeedbackEnum.AciontType actionType:AppFeedbackEnum.AciontType.values()){
            list.add(actionType);
        }
        return list;
    }

    /**
     * 获取使用方枚举(在页面展示)
     * */
    public List<AppFeedbackEnum.UserType> findUserTypeList(){
       List<AppFeedbackEnum.UserType> list = Lists.newArrayList();
        for(AppFeedbackEnum.UserType serType:AppFeedbackEnum.UserType.values()){
            list.add(serType);
        }
        return list;
    }

    /**
     * 获取记次方式枚举(在页面展示)
     * */
    public List<AppFeedbackEnum.SummaryType> findSumTypeList(){
        List<AppFeedbackEnum.SummaryType> list = Lists.newArrayList();
        for(AppFeedbackEnum.SummaryType serType:AppFeedbackEnum.SummaryType.values()){
            list.add(serType);
        }
        return list;
    }


    /**
     * 获取最大的排序
     */
    public Integer getMaxSortBy(){
        MSResponse<Integer> msResponse = msAppFeedbackTypeFeign.getMaxSortBy();
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return 0;
        }
    }

    /**
     * 停用或启用
     */
    public void disableOrEnable(MDAppFeedbackType appFeedbackType){
       MSResponse<Integer> msResponse = msAppFeedbackTypeFeign.disableOrEnable(appFeedbackType);
       if(msResponse.getCode()>0){
           throw new RuntimeException("启用或者停用失败:" + msResponse.getMsg());
       }
    }
}

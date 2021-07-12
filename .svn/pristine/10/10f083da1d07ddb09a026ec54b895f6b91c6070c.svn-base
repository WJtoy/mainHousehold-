package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.BaseException;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialItem;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.AuxiliaryMaterialItemFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class AuxiliaryMaterialItemService {
    @Autowired
    private AuxiliaryMaterialItemFeign auxiliaryMaterialItemFeign;


    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public MDAuxiliaryMaterialItem get(Long id){
        MSResponse<MDAuxiliaryMaterialItem> msResponse = auxiliaryMaterialItemFeign.get(id);
        if(MSResponse.isSuccess(msResponse)){
            MDAuxiliaryMaterialItem auxiliaryMaterialItem = msResponse.getData();
            return auxiliaryMaterialItem;
        }else{
            return new MDAuxiliaryMaterialItem();
        }
    }

    /**
     * 分页查询
     *
     * @param page,auxiliaryMaterialItem
     * @return
     */
    public Page<MDAuxiliaryMaterialItem> getList(Page<MDAuxiliaryMaterialItem> page, MDAuxiliaryMaterialItem auxiliaryMaterialItem){
        Page<MDAuxiliaryMaterialItem> auxiliaryMaterialItemPage = new Page<>();
        if (auxiliaryMaterialItem.getPage() == null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        auxiliaryMaterialItemPage.setPageSize(page.getPageSize());
        auxiliaryMaterialItemPage.setPageNo(page.getPageNo());
        auxiliaryMaterialItem.setPage(new MSPage<>(auxiliaryMaterialItemPage.getPageNo(), auxiliaryMaterialItemPage.getPageSize()));
        MSResponse<MSPage<MDAuxiliaryMaterialItem>> returnMDAuxiliaryMaterialItem = auxiliaryMaterialItemFeign.getList(auxiliaryMaterialItem);
        if (MSResponse.isSuccess(returnMDAuxiliaryMaterialItem)) {
            MSPage<MDAuxiliaryMaterialItem> data = returnMDAuxiliaryMaterialItem.getData();
            auxiliaryMaterialItemPage.setCount(data.getRowCount());
            auxiliaryMaterialItemPage.setList(data.getList());
        } else {
            auxiliaryMaterialItemPage.setCount(0);
            auxiliaryMaterialItemPage.setList(Lists.newArrayList());
        }
        return auxiliaryMaterialItemPage;
    }


    /**
     * 保存或修改
     *
     * @param auxiliaryMaterialItem
     * @return
     */
      public void save(MDAuxiliaryMaterialItem auxiliaryMaterialItem){
          if (auxiliaryMaterialItem.getId() == null || auxiliaryMaterialItem.getId() <= 0) {
              auxiliaryMaterialItem.preInsert();
              MSResponse<MDAuxiliaryMaterialItem> msResponse = auxiliaryMaterialItemFeign.insert(auxiliaryMaterialItem);
              if (MSResponse.isSuccess(msResponse)) {
                  auxiliaryMaterialItem.setId(msResponse.getData().getId());
              } else {
                  throw new RuntimeException(msResponse.getMsg());
              }
          } else {
              auxiliaryMaterialItem.preUpdate();
              MSResponse<Integer> msResponse = auxiliaryMaterialItemFeign.update(auxiliaryMaterialItem);
              if (!MSResponse.isSuccess(msResponse)) {
                  throw new BaseException(msResponse.getMsg());
              }
          }
      }

    /**
     * 删除
     *
     * @param auxiliaryMaterialItem
     * @return
     */
     public void delete(MDAuxiliaryMaterialItem auxiliaryMaterialItem) {
         auxiliaryMaterialItem.preUpdate();
         MSResponse<Integer> msResponse = auxiliaryMaterialItemFeign.delete(auxiliaryMaterialItem);
         if (!MSResponse.isSuccess(msResponse)) {
             throw new BaseException(msResponse.getMsg());
         }
     }

    /**
     * 获取所有辅件收费项目
     * @param
     * @return
     */
    public List<MDAuxiliaryMaterialItem> findAllList(){
        MSResponse<List<MDAuxiliaryMaterialItem>> msResponse = auxiliaryMaterialItemFeign.findAllList();
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }


    /**
     * 根据产品产品id集合存获取辅件信息
     * @param productIds
     * @return
     */
    public List<MDAuxiliaryMaterialItem> getListByProductId(List<String> productIds){
        MSResponse<List<MDAuxiliaryMaterialItem>> msResponse = auxiliaryMaterialItemFeign.getListByProductId(productIds);
        if(MSResponse.isSuccess(msResponse)){
             return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }
}

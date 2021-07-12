package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.BaseException;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAuxiliaryMaterialCategory;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.AuxiliaryMaterialCategoryFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class AuxiliaryMaterialCategoryService {
    @Autowired
    private AuxiliaryMaterialCategoryFeign auxiliaryMaterialCategoryFeign;


    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public MDAuxiliaryMaterialCategory get(Long id){
        MSResponse<MDAuxiliaryMaterialCategory> msResponse = auxiliaryMaterialCategoryFeign.get(id);
        if(MSResponse.isSuccess(msResponse)){
            MDAuxiliaryMaterialCategory auxiliaryMaterialCategory = msResponse.getData();
            return auxiliaryMaterialCategory;
        }else{
            return new MDAuxiliaryMaterialCategory();
        }
    }

    /**
     * 分页查询
     *
     * @param page,b2BServiceTypeMapping
     * @return
     */
    public Page<MDAuxiliaryMaterialCategory> getList(Page<MDAuxiliaryMaterialCategory> page, MDAuxiliaryMaterialCategory auxiliaryMaterialCategory){
        Page<MDAuxiliaryMaterialCategory> auxiliaryMaterialCategoryPage = new Page<>();
        if (auxiliaryMaterialCategory.getPage() == null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        auxiliaryMaterialCategoryPage.setPageSize(page.getPageSize());
        auxiliaryMaterialCategoryPage.setPageNo(page.getPageNo());
        auxiliaryMaterialCategory.setPage(new MSPage<>(auxiliaryMaterialCategoryPage.getPageNo(), auxiliaryMaterialCategoryPage.getPageSize()));
        MSResponse<MSPage<MDAuxiliaryMaterialCategory>> returnAuxiliaryMaterialCategory = auxiliaryMaterialCategoryFeign.getList(auxiliaryMaterialCategory);
        if (MSResponse.isSuccess(returnAuxiliaryMaterialCategory)) {
            MSPage<MDAuxiliaryMaterialCategory> data = returnAuxiliaryMaterialCategory.getData();
            auxiliaryMaterialCategoryPage.setCount(data.getRowCount());
            auxiliaryMaterialCategoryPage.setList(data.getList());
        } else {
            auxiliaryMaterialCategoryPage.setCount(0);
            auxiliaryMaterialCategoryPage.setList(Lists.newArrayList());
        }
        return auxiliaryMaterialCategoryPage;
    }


    /**
     * 保存或修改
     *
     * @param auxiliaryMaterialCategory
     * @return
     */
      public void save(MDAuxiliaryMaterialCategory auxiliaryMaterialCategory){
          if (auxiliaryMaterialCategory.getId() == null || auxiliaryMaterialCategory.getId() <= 0) {
              auxiliaryMaterialCategory.preInsert();
              MSResponse<MDAuxiliaryMaterialCategory> msResponse = auxiliaryMaterialCategoryFeign.insert(auxiliaryMaterialCategory);
              if (MSResponse.isSuccess(msResponse)) {
                  auxiliaryMaterialCategory.setId(msResponse.getData().getId());
              } else {
                  throw new RuntimeException(msResponse.getMsg());
              }
          } else {
              auxiliaryMaterialCategory.preUpdate();
              MSResponse<Integer> msResponse = auxiliaryMaterialCategoryFeign.update(auxiliaryMaterialCategory);
              if (!MSResponse.isSuccess(msResponse)) {
                  throw new BaseException(msResponse.getMsg());
              }
          }
      }

    /**
     * 删除
     *
     * @param auxiliaryMaterialCategory
     * @return
     */
     public void delete(MDAuxiliaryMaterialCategory auxiliaryMaterialCategory) {
         auxiliaryMaterialCategory.preUpdate();
         MSResponse<Integer> msResponse = auxiliaryMaterialCategoryFeign.delete(auxiliaryMaterialCategory);
         if (!MSResponse.isSuccess(msResponse)) {
             throw new BaseException(msResponse.getMsg());
         }
     }

    /**
     * 根据所有数据
     *
     * @return
     */
    public List<MDAuxiliaryMaterialCategory> findAllList(){
        MSResponse<List<MDAuxiliaryMaterialCategory>> msResponse = auxiliaryMaterialCategoryFeign.findAllList();
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }
}

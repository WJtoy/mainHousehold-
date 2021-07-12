package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDBrand;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.ms.providermd.feign.MSBrandFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MSBrandService {

    @Autowired
    private MSBrandFeign msBrandFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取品牌信息
     * @param id
     * @return
     */
    public Brand getById(Long id) {
        return MDUtils.getById(id, Brand.class, msBrandFeign::getById);
    }

    /**
     * 根据code获取id
     * @param brand
     * @return
     */
    public Long getIdByCode(Brand brand) {
        MDBrand mdBrand = mapper.map(brand, MDBrand.class);
        MSResponse<Long> msResponse = msBrandFeign.getIdByCode(mdBrand);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 根据name获取id
     * @param brand
     * @return
     */
    public Long getIdByName(Brand brand) {
        MDBrand mdBrand = mapper.map(brand, MDBrand.class);
        MSResponse<Long> msResponse = msBrandFeign.getIdByName(mdBrand);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 获取所有数据
     * @return
     */
    public List<Brand> findAllList() {
        return MDUtils.findAllList(Brand.class, msBrandFeign::findAllList);
    }

    /**
     * 获取分页数据
     * @param brandPage
     * @param brand
     * @return
     */
    public Page<Brand> findList(Page<Brand> brandPage, Brand brand) {
        return MDUtils.findListForPage(brandPage, brand, Brand.class, MDBrand.class, msBrandFeign::findList);
    }


    /**
     * 添加/更新
     * @param brand
     * @param isNew
     * @return
     */
    public MSErrorCode save(Brand brand, boolean isNew) {
        return MDUtils.genericSave(brand, MDBrand.class, isNew, isNew?msBrandFeign::insert:msBrandFeign::update);
    }

    /**
     * 删除
     * @param brand
     * @return
     */
    public MSErrorCode delete(Brand brand) {
        return MDUtils.genericSave(brand, MDBrand.class, false, msBrandFeign::delete);
    }
}

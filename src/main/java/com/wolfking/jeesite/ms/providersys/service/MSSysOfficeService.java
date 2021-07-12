package com.wolfking.jeesite.ms.providersys.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.sys.SysOffice;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.providersys.feign.MSSysOfficeFeign;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSSysOfficeService {

    @Autowired
    private MSSysOfficeFeign msSysOfficeFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取部门信息
     * @param id
     * @return
     */
    public Office get(Long id) {
        return MDUtils.getObjNecessaryConvertType(Office.class, ()->msSysOfficeFeign.get(id));
    }

    /**
     * 保存部门信息
     * @param office
     */
    public void save(Office office) {
        MSErrorCode msErrorCode =  MDUtils.genericSave(office, SysOffice.class, office.getIsNewRecord(), office.getIsNewRecord()?msSysOfficeFeign::insert:msSysOfficeFeign::update);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务保存部门信息出错了。出错原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 更新部门的上级部门id
     *
     * @param office
     * @return
     */
    public void updateParentIds(Office office) {
        SysOffice sysOffice = mapper.map(office, SysOffice.class);
        MSErrorCode msErrorCode = MDUtils.customSave(()->msSysOfficeFeign.updateParentIds(sysOffice));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务更新部门的上级部门id信息出错了。出错原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * @param office
     * @return
     */
    public void delete(Office office) {
        SysOffice sysOffice = mapper.map(office, SysOffice.class);
        MSErrorCode msErrorCode = MDUtils.customSave(()->msSysOfficeFeign.delete(sysOffice));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务更新部门的上级部门id信息出错了。出错原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 获取所有的部门
     *
     * @return
     */
    public List<Office> findAllList() {
        return MDUtils.findListNecessaryConvertType(Office.class, ()->msSysOfficeFeign.findAllList());
    }

    /**
     * 根据部门id获取部门列表
     *
     * @param id
     * @return
     */
    public List<Office> findList(Long id) {
        return MDUtils.findListNecessaryConvertType(Office.class, ()->msSysOfficeFeign.findList(id));
    }

    /**
     * 根据父id串查询部门列表
     *
     * @param parentIds
     * @return
     */
    public List<Office> findByParentIdsLike(String parentIds) {
        return MDUtils.findListNecessaryConvertType(Office.class, ()->msSysOfficeFeign.findByParentIdsLike(parentIds));
    }

    /**
     * 根据部门父编码查询所属的下级部门
     *
     * @param parentCode
     * @return
     */
    /*
    public List<Office> findSubListByParentCode(String parentCode) {
        return MDUtils.findListNecessaryConvertType(Office.class, ()->msSysOfficeFeign.findSubListByParentCode(parentCode));
    }
    */

    /**
     * 根据id获取id，name，code属性
     *
     * @param id
     * @return id, name, code
    */
    public Office getSpecColumnById(Long id) {
        return MDUtils.getObjNecessaryConvertType(Office.class, ()->msSysOfficeFeign.getSpecColumnById(id));
    }

    /**
     * 根据部门id列表获取部门的(id,name,code)
     *
     * @param ids
     * @return
    */
    public List<Office> findSpecColumnListByIds(List<Long> ids) {
        return MDUtils.findListNecessaryConvertType(Office.class, ()->msSysOfficeFeign.findSpecColumnListByIds(ids));
    }

    public List<Long> findIdListById(Long id) {
        return MDUtils.findListUnnecessaryConvertType(()->msSysOfficeFeign.findIdListById(id));
    }

    /*@Deprecated // 此方法为临时方法,用来比较数据用, 不用时请删除 2020-8-7
    public void compareSingleOffice(String param, Office localOffice, Office msOffice, String methodName) {
        try {
            StringBuffer msg = new StringBuffer();
            if (localOffice == null) {
                msg.append("本地获取区域为空.");
            }
            if (msOffice == null) {
                msg.append("从微服务获取的区域为空.");
            }
            if (localOffice != null && msOffice != null && !localOffice.getId().equals(1L)) {
                if (!localOffice.getId().equals(msOffice.getId()) ||
                        //!localOffice.getType().equals(msOffice.getType()) ||
                        !localOffice.getCode().equals(msOffice.getCode()) ||
                        !localOffice.getName().equals(msOffice.getName()) // ||
                        //!localOffice.getParentIds().equals(msOffice.getParentIds()) ||
                        //!localOffice.getParent().getId().equals(msOffice.getParent().getId())
                ) {
                    msg.append("数据不相等。");
                    msg.append("localOffice:"+ GsonUtils.toGsonString(localOffice));
                    msg.append("msOffice:"+ GsonUtils.toGsonString(msOffice));
                }
            }
            if (msg.length() > 0) {
                msg.append(",输入参数：" + param + ", 方法名称:" + methodName);
                LogUtils.saveLog("基础资料_部门", methodName, msg.toString(), null, null);
            }
        } catch (Exception ex) {
            LogUtils.saveLog("基础资料_部门", methodName, param, ex, null);
        }
    }*/

    /*
    @Deprecated // 此方法为临时方法,用来比较数据用, 不用时请删除 2020-11-28
    public void compareListOffice(String param, List<Office> localOfficeList, List<Office> msOfficeList, String methodName) {
        try {
            StringBuffer msg = new StringBuffer();
            if (localOfficeList == null || localOfficeList.isEmpty()) {
                msg.append("本地获取部门列表为空.");
                if (msOfficeList != null && !msOfficeList.isEmpty()) {
                    List<Long> longList = msOfficeList.stream().map(r->r.getId()).collect(Collectors.toList());
                    msg.append("微服务数据:" + longList.toString());
                }
            }
            if (msOfficeList == null || msOfficeList.isEmpty()) {
                msg.append("从微服务获取的部门列表为空.");
                if (localOfficeList != null && !localOfficeList.isEmpty()) {
                    List<Long> longList = localOfficeList.stream().map(r->r.getId()).collect(Collectors.toList());
                    msg.append("本地数据:" + longList.toString());
                }
            }
            if (localOfficeList != null && msOfficeList != null && !localOfficeList.isEmpty() && !msOfficeList.isEmpty()) {
                if (localOfficeList.size() != msOfficeList.size()) {
                    msg.append(" 列表长度不相等。本地:"+localOfficeList.size()+", MS:"+msOfficeList.size());
                    List<Long> longList = localOfficeList.stream().map(r->r.getId()).distinct().sorted(Long::compareTo).collect(Collectors.toList());
                    List<Long> longList2 = msOfficeList.stream().map(r->r.getId()).distinct().sorted(Long::compareTo).collect(Collectors.toList());
                    msg.append(" 数据差别。本地:"+longList.toString()+", MS:"+longList2.toString());
                } else {
                    // 根据id排序
                    localOfficeList = localOfficeList.stream().sorted(Comparator.comparing(r->r.getId())).collect(Collectors.toList());
                    msOfficeList = msOfficeList.stream().sorted(Comparator.comparing(r->r.getId())).collect(Collectors.toList());
                    for (int i=0; i< localOfficeList.size(); i++) {
                        Office localOffice = localOfficeList.get(i);
                        Office msOffice = msOfficeList.get(i);
                        if (!localOffice.getId().equals(1L)) {
                            if (!localOffice.getId().equals(msOffice.getId()) ||
                                //!localOffice.getType().equals(msOffice.getType()) ||
                                !localOffice.getCode().equals(msOffice.getCode()) ||
                                !localOffice.getName().equals(msOffice.getName()) //||
                                //!localOffice.getParentIds().equals(msOffice.getParentIds()) ||
                                //!localOffice.getParent().getId().equals(msOffice.getParent().getId())
                            ) {
                                msg.append("id:" + localOffice.getId() + " 数据不相等。");
                                msg.append("localOffice:" + GsonUtils.toGsonString(localOffice));
                                msg.append("msOffice:" + GsonUtils.toGsonString(msOffice));
                            }
                        }
                    }
                }
            }
            if (msg.length() > 0) {
                msg.append(",输入参数：" + param + ", 方法名称:" + methodName);
                LogUtils.saveLog("基础资料_部门", methodName, msg.toString(), null, null);
            }
        } catch (Exception ex) {
            LogUtils.saveLog("基础资料_部门", methodName, param, ex, null);
        }
    }*/

    public List<Office> findListByNameAndType(String name,Integer userType) {
        return MDUtils.findListNecessaryConvertType(Office.class, ()->msSysOfficeFeign.findListByNameAndType(name,userType));
    }

    public List<Long> findParentListByNameAndType(String name,Integer userType){
        return MDUtils.findListUnnecessaryConvertType(()->msSysOfficeFeign.findParentListByNameAndType(name,userType));
    }
    public List<Long> findIdListByParentId(Long parentId){
        return MDUtils.findListUnnecessaryConvertType(()->msSysOfficeFeign.findIdListByParentId(parentId));
    }

}

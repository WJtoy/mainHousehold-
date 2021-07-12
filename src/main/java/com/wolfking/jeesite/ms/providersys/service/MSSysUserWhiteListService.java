package com.wolfking.jeesite.ms.providersys.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.sys.SysUserWhiteList;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providersys.entity.SysUserWhiteListView;
import com.wolfking.jeesite.ms.providersys.entity.mapper.SysUserWhiteListViewMapper;
import com.wolfking.jeesite.ms.providersys.feign.MSSysUserWhiteListFeign;
import io.jsonwebtoken.lang.Collections;
import org.joda.time.DateTime;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class MSSysUserWhiteListService {

    @Autowired
    private MSSysUserWhiteListFeign msSysUserWhiteListFeign;

    /**
     * 分页获取数据
     * */
    public Page<SysUserWhiteListView> findListForPage(Page<SysUserWhiteListView> sysUserWhiteListPage, SysUserWhiteList sysUserWhiteList){
        sysUserWhiteList.setPage(new MSPage<>(sysUserWhiteListPage.getPageNo(), sysUserWhiteListPage.getPageSize()));
        MSResponse<MSPage<SysUserWhiteList>> msResponse = msSysUserWhiteListFeign.findList(sysUserWhiteList);
        if (MSResponse.isSuccess(msResponse)) {
            MSPage<SysUserWhiteList> data = msResponse.getData();
            sysUserWhiteListPage.setCount(data.getRowCount());
            List<SysUserWhiteListView> list = Mappers.getMapper(SysUserWhiteListViewMapper.class).toViewModels(data.getList());

            List<SysUserWhiteListView> newList = Collections.isEmpty(list)? Lists.newArrayList(): list.stream().filter(x->!"".equals(x.getUserName())).filter(x->x.getUserName() != null).collect(Collectors.toList());
            sysUserWhiteListPage.setList(newList);
        } else {
            sysUserWhiteListPage.setCount(0);
            sysUserWhiteListPage.setList(Lists.newArrayList());
        }
        return sysUserWhiteListPage;
    }

    /**
     * 批量保存白名单
     * */
    public void batchInsert(List<SysUserWhiteList> sysUserWhiteLists){
        if(sysUserWhiteLists!=null && sysUserWhiteLists.size()<=50){
            MSResponse<Integer> msResponse = msSysUserWhiteListFeign.batchInsert(sysUserWhiteLists);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException(msResponse.getMsg());
            }
        }else{
            List<List<SysUserWhiteList>> batchUserWhiteLists = Lists.partition(sysUserWhiteLists,50);
            for(List<SysUserWhiteList> item:batchUserWhiteLists){
                MSResponse<Integer> msResponse = msSysUserWhiteListFeign.batchInsert(item);
                if(!MSResponse.isSuccessCode(msResponse)){
                    throw new RuntimeException(msResponse.getMsg());
                }
            }
        }
    }

    /**
     * 获取所有白名单id集合
     * */
    public List<Long> findAllIdList(){
        MSResponse<List<Long>> msResponse = msSysUserWhiteListFeign.findAllIdList();
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 根据id获取白名单
     * */
    public SysUserWhiteList getById(Long id){
        MSResponse<SysUserWhiteList> msResponse = msSysUserWhiteListFeign.getById(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }

    /**
     * 修改
     * */
    public void update(SysUserWhiteList sysUserWhiteList){
       MSResponse<Integer> msResponse = msSysUserWhiteListFeign.update(sysUserWhiteList);
       if(!MSResponse.isSuccess(msResponse)){
           throw new RuntimeException("调用微服务修改白名单失败.失败原因:" +msResponse.getMsg());
       }
    }

    /**
     * 根据id物理删除
     * */
    public void delete(Long id){
        MSResponse<Integer> msResponse = msSysUserWhiteListFeign.delete(id);
        if(!MSResponse.isSuccess(msResponse)){
            throw new RuntimeException("调用微服务删除白名单失败.失败原因:" +msResponse.getMsg());
        }else{
            //删除成功记录log
            User user = UserUtils.getUser();
            LogUtils.saveLog("删除白名单成功", "MSSysUserWhiteListService.delete", id.toString(), null,user);
        }
    }

    /**
     * 判断帐号是否在白名单清单中
     * */
    public Boolean IsWhiteUser(Long userId){
        MSResponse<SysUserWhiteList> msResponse = msSysUserWhiteListFeign.getByUserIdFromCache(userId);
        if(MSResponse.isSuccess(msResponse)){
            SysUserWhiteList data = msResponse.getData();
            if(data.getDelFlag() == 1){
                return false;
            }
            DateTime endDate = new DateTime(data.getEndDate().getTime());
            if(endDate.isBeforeNow()){
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

}

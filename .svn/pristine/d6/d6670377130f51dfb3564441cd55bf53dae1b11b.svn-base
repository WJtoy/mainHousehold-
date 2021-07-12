package com.wolfking.jeesite.ms.providersys.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sys.SysUserCustomer;
import com.wolfking.jeesite.ms.providersys.feign.MSSysUserCustomerFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MSSysUserCustomerFallbackFactory implements FallbackFactory<MSSysUserCustomerFeign> {

    @Override
    public MSSysUserCustomerFeign create(Throwable throwable) {
        return new MSSysUserCustomerFeign() {
            /**
             * 根据客户id获取用户id列表
             *
             * @param customerId
             * @return
             */
            @Override
            public MSResponse<List<Long>> findUserIdListByCustomerId(Long customerId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 查询客户id列表
             *
             * @param sysUserCustomer
             * @return
             */
            @Override
            public MSResponse<List<Long>> findCustomerIdList(SysUserCustomer sysUserCustomer) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 获取所有的customerId列表
             *
             * @return
             */
            @Override
            public MSResponse<List<Long>> findAllCustomerIdList() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 批量插入用户客户
             *
             * @param userId
             * @param customerIds
             * @return
             */
            @Override
            public MSResponse<Integer> batchInsert(Long userId, List<Long> customerIds) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据用户id删除用户客户
             *
             * @param userId
             * @return
             */
            @Override
            public MSResponse<Integer> deleteByUserId(Long userId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}

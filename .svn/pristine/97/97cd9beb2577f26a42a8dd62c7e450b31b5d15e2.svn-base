package com.wolfking.jeesite.ms.tmall.md.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.servicestore.ServiceStore;
import com.kkl.kklplus.entity.b2b.servicestore.ServiceStoreCapacity;
import com.kkl.kklplus.entity.b2b.servicestore.ServiceStoreCoverService;
import com.kkl.kklplus.entity.b2b.servicestore.Worker;
import com.wolfking.jeesite.ms.tmall.md.fallback.MSServiceStoreFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "kklplus-b2b", fallbackFactory = MSServiceStoreFeignFallbackFactory.class)
public interface MSServiceStoreFeign {

    //region 网点基础资料

    @PostMapping("/serviceStore/insert")
    MSResponse<String> insertServiceStore(@RequestBody ServiceStore serviceStore);

    @PostMapping("/serviceStore/update")
    MSResponse<String> updateServiceStore(@RequestBody ServiceStore serviceStore);

    @PostMapping("/serviceStore/delete")
    MSResponse<String> deleteServiceStore(@RequestBody ServiceStore serviceStore);

    //endregion

    //region 网点覆盖的服务

    @PostMapping("/serviceStoreCoverService/insert")
    MSResponse<String> insertServiceStoreCoverService(@RequestBody ServiceStoreCoverService serviceStoreCoverService);

    @PostMapping("/serviceStoreCoverService/update")
    MSResponse<String> updateServiceStoreCoverService(@RequestBody ServiceStoreCoverService serviceStoreCoverService);

    @PostMapping("/serviceStoreCoverService/delete")
    MSResponse<String> deleteServiceStoreCoverService(@RequestBody ServiceStoreCoverService serviceStoreCoverService);

    //endregion

    //region 网点容量

    @PostMapping("/serviceStoreCapacity/insert")
    MSResponse<String> insertServiceStoreCapacity(@RequestBody ServiceStoreCapacity serviceStoreCapacity);

    @PostMapping("/serviceStoreCapacity/update")
    MSResponse<String> updateServiceStoreCapacity(@RequestBody ServiceStoreCapacity serviceStoreCapacity);

    @PostMapping("/serviceStoreCapacity/delete")
    MSResponse<String> deleteServiceStoreCapacity(@RequestBody ServiceStoreCapacity serviceStoreCapacity);

    //endregion

    //region 服务商工人信息

    @PostMapping("/worker/insert")
    MSResponse<String> insertWorker(@RequestBody Worker worker);

    @PostMapping("/worker/update")
    MSResponse<String> updateWorker(@RequestBody Worker worker);

    @PostMapping("/worker/delete")
    MSResponse<String> deleteWorker(@RequestBody Worker worker);

    //endregion
}

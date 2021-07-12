package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDBrand;
import com.wolfking.jeesite.ms.providermd.fallback.MSBrandFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="provider-md", fallbackFactory = MSBrandFeignFallbackFactory.class)
public interface MSBrandFeign {
    @GetMapping("/brand/getById/{id}")
    MSResponse<MDBrand> getById(@PathVariable("id") Long id);

    @PostMapping("/brand/getIdByCode")
    MSResponse<Long> getIdByCode(@RequestBody MDBrand mdBrand);

    @PostMapping("/brand/getIdByName")
    MSResponse<Long> getIdByName(@RequestBody MDBrand mdBrand);

    @GetMapping("/brand/findAllList")
    MSResponse<List<MDBrand>> findAllList();

    @PostMapping("/brand/findList")
    MSResponse<MSPage<MDBrand>> findList(@RequestBody MDBrand mdBrand);

    @PostMapping("/brand/insert")
    MSResponse<Integer> insert(@RequestBody MDBrand mdBrand);

    @PutMapping("/brand/update")
    MSResponse<Integer> update(@RequestBody MDBrand mdBrand);

    @DeleteMapping("/brand/delete")
    MSResponse<Integer> delete(@RequestBody MDBrand mdBrand);
}

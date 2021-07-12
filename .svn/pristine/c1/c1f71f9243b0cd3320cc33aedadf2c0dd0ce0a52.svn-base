package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDProduct;
import com.kkl.kklplus.entity.md.MDProductMaterial;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.ms.providermd.fallback.MSProductFeignFallbackFactory;
import com.wolfking.jeesite.ms.providermd.fallback.MSProductVerSecondFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@FeignClient(name="provider-md", fallbackFactory = MSProductVerSecondFeignFallbackFactory.class)
public interface MSProductVerSecondFeign {
    /**
     * 根据id 获取产品名称
     * @param id
     * @return
     */
    @GetMapping("/productVerSecond/getNameByIdForMD")
    MSResponse<String> getNameByIdForMD(@RequestParam("id") Long id);

    /**
     * 获取所有产品id，name
     * @return
     */
    @PostMapping("/productVerSecond/finAllIdAndNameListForMD")
    MSResponse<List<NameValuePair<Long, String>>> finAllIdAndNameListForMD();

    /**
     * 根据id 获取产品
     * @param
     * @return id,name,product_category_id,model,pinYin,sort,flag,remarks
     */
    @GetMapping("/productVerSecond/getSpecColumnByIdForMD")
    MSResponse<MDProduct> getSpecColumnByIdForMD(@RequestParam("id") Long id);

    /**
     * 根据ids 获取产品
     * @param
     * @return id,name,product_category_id,model,pinYin,sort,flag,remarks
     */
    @GetMapping("/productVerSecond/findSpecListByProductIdsForMD")
    MSResponse<List<MDProduct>> findSpecListByProductIdsForMD(@RequestParam("ids") List<Long> ids);

    /**
     * 获取所有产品id，name
     * @return
     */
    @PostMapping("/productVerSecond/findListForMD")
    MSResponse<MSPage<MDProduct>> findListForMD(@RequestBody MDProduct mdProduct);

    /**
     * 获取全部已审核产品
     */
    @GetMapping("/productVerSecond/findAllProductListForMD")
    MSResponse<List<MDProduct>> findAllProductListForMD();

    /**
     * 添加产品
     * @param mdProduct
     * @return
     */
    @PostMapping("/productVerSecond/insertForMD")
    MSResponse<Integer> insertForMD(@RequestBody MDProduct mdProduct);

    /**
     * 更新产品信息
     * @param mdProduct
     * @return
     */
    @PutMapping("/productVerSecond/updateProductForMD")
    MSResponse<Integer> updateProductForMD(@RequestBody MDProduct mdProduct);

    /**
     * 更新产品排序
     * @param mdProductList
     * @return
     */
    @PutMapping("/productVerSecond/updateSortForMD")
    MSResponse<Integer> updateSortForMD(@RequestBody List<MDProduct> mdProductList);

    /**
     * 删除产品
     * @param mdProduct
     * @return
     */
    @DeleteMapping("/productVerSecond/deleteForMD")
    MSResponse<Integer> deleteForMD(@RequestBody MDProduct mdProduct);

    /**
     * 获取全部已审核产品
     */
    @GetMapping("/productVerSecond/getIdByNameForMD")
    MSResponse<Long> getIdByNameForMD(@RequestParam("name") String name);

    /**
     * 根据产品id判断是否有套组产品包含此产品
     */
    @GetMapping("/productVerSecond/getSetProductByProductIdForMD/{productId}")
    MSResponse<MDProduct> getSetProductByProductIdForMD(@PathVariable("productId") Long productId);

}

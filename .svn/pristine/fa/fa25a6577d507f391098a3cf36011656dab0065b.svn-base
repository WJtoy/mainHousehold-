package com.wolfking.jeesite.modules.servicepoint.ms.md;

import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpProductService {


    @Autowired
    private ProductService productService;


    /**
     * 从缓存读取产品信息，当缓存未命中则从数据库装载至缓存
     */
    public Product getProductByIdFromCache(Long id) {
        return productService.getProductByIdFromCache(id);
    }

    /**
     * 拆分套组产品，返回组成产品列表
     *
     * @param id 套组产品id
     */
    public List<Product> getProductListOfSet(Long id) {
        return productService.getProductListOfSet(id);
    }
}

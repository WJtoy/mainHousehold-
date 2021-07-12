package com.wolfking.jeesite.test.md;

import com.google.common.collect.Sets;
import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.apache.shiro.util.Assert;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;

/**
 * Created by Jeff on 2017/4/27.
 */
//@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ProductTest {

    @Autowired
    private ProductService productService;

    @Test
    public void ProductInsertTest(){
        Product product = new Product();
        ProductCategory productCategory = new ProductCategory();
        List<Material> materialList = Lists.newArrayList();
        for (int i = 1; i <= 100; i++){
            try {
                product.setId(null);
                product.setName("Test" + i);
                productCategory.setId(2L);
                product.setCategory(productCategory);
                materialList.add(new Material(5L));
                materialList.add(new Material(8L));
                product.setMaterialList(materialList);
                productService.save(product);
            }catch (Exception e){
                log.error("\n\n====================  "+e.getLocalizedMessage() + "\n\n");
            }
        }
    }

    @Test
    public void testGetProduct(){
        Product product = productService.getProductByIdFromCache(122l);
        Assert.notNull(product,"product is null");
        Assert.isTrue("diannuanqi".equals(product.getPinYin()),"拼音简称错误");
        System.out.printf("id:%d ,name:%s ,pinYin:%s",product.getId(),product.getName(),product.getPinYin());
    }


    @Test
    public void testRedis() {
        productService.getProductMap(Lists.newArrayList(103L, 104L, 75L, 72L,80L));
    }

    /**
     * 测试product的equals及toString()
     */
    @Test
    public void testProductEquals(){
        Set<Product> products = Sets.newHashSetWithExpectedSize(2);
        Product p1 = new Product(1L,"product1");
        System.out.println("p1 hash:" + p1.hashCode());
        products.add(p1);

        Product p2 = new Product(1L,"product2");
        System.out.println("p2 hash:" + p2.hashCode());
        products.add(p2);

        System.out.println("size:" + products.size());
        for(Product p:products){
            System.out.println("product:" + p.toString());
        }

        org.junit.Assert.assertTrue("不已存在相同产品",products.contains(p2));
    }
}

package com.wolfking.jeesite.test.md;

import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by lzx
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class BrandTest {
    @Autowired
    private BrandService brandService;

    @Test
    public void getBrandALl(){
       List<Brand> brandList= brandService.findAllList();
        log.error("brandService.size {}",brandList.size());
    }

    @Test
    public void getBrandFromCahe(){
        /*
        Brand brand=brandService.getFromCache(9);
        log.error("Brand:id {},name: {}",brand.getId(),brand.getName());
        */
    }
}

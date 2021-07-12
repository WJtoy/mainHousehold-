package com.wolfking.jeesite.test.md;

import com.wolfking.jeesite.modules.md.entity.ServicePointStation;
import com.wolfking.jeesite.modules.md.service.ServicePointStationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ServicePointStationTest {
    @Autowired
    private ServicePointStationService servicePointStationService;

    @Test
    public void insert() {
        ServicePointStation servicePointStation = ServicePointStation.builder()
                //.servicepointId(1L)
                .address("不知道那个地方")
                .longtitude(12.3444)
                .latitude(23.22222)
                //.areaId(100L)
                .build();
        servicePointStation.setRemarks("这只是测试一次而已.");
        servicePointStationService.insert(servicePointStation);
    }

    @Test
    public void update() {
        ServicePointStation servicePointStation = ServicePointStation.builder()
                .address("XXXXX")
                .longtitude(15.3444)
                .latitude(25.22222)
                .build();
        servicePointStation.setId(1L);

        //servicePointStationService.save(servicePointStation);
    }

    @Test
    public void delete() {
        ServicePointStation servicePointStation = ServicePointStation.builder()
                .build();
        servicePointStation.setDelFlag(1);
        servicePointStation.setId(1L);

        servicePointStationService.delete(servicePointStation);
    }
}

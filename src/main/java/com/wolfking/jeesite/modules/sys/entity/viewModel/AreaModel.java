package com.wolfking.jeesite.modules.sys.entity.viewModel;

import com.wolfking.jeesite.modules.sys.entity.Area;
import lombok.Data;

import java.util.List;

@Data
public class AreaModel extends Area {
    // 下级区域列表
    private List<Area> subAreas;
}

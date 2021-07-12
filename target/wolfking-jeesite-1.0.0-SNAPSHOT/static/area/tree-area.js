function TreeArea(contextPath){
    var ret = {};
    // ret.clearParentIds = clearParentIds;
    ret.obtainTownDataByDistrict = obtainTownDataByDistrict;
    ret.fillTownDataBySelectedDistrict = fillTownDataBySelectedDistrict;
    ret.fillTownDataByTownIds = fillTownDataByTownIds;
    ret.initTownDataBySelectedDistrict = initTownDataBySelectedDistrict;
    ret.obtainDistrictDataByCity = obtainDistrictDataByCity;
    ret.loadAreaDataByAreaId = loadAreaDataByAreaId;
    return ret;

    /**
     * 根据被选中的区域加载自己及兄弟节点
     * @param zTree
     * @param selectAreaId
     * @returns {boolean}
     */
    function loadAreaDataByAreaId(zTree, selectAreaId, areaLevel) {
        if (selectAreaId == null || selectAreaId <= 0) {
            return false;
        }
        var grandpaId;
        var parentId;
        var id;

        if (areaLevel == "2") {  // 县/区
            var areaDeffer = $.get(contextPath + '/sys/area/service/area/'+selectAreaId, function(data){
                if (data.success == false) {
                    layerError(data.message, "错误提示");
                    return false;
                } else {
                    if (data.data) {
                        parentId = data.data.parent.id;
                        id = data.data.id;
                    }
                };
            }, "json");

            $.when(areaDeffer).then(function(){
                var parentNode = tree.getNodeByParam("id", parentId);
                obtainDistrictDataByCity(zTree, parentNode, selectAreaId);
            });
        } else if (areaLevel == "3")  {  // 乡/镇/街道
            var areaDeffer = $.get(contextPath + '/sys/area/service/threeLevelArea/'+selectAreaId, function(data){
                if (data.success == false) {
                    layerError(data.message, "错误提示");
                    return false;
                } else {
                    if (data.data) {
                        //console.log(data.data);
                        id = data.data.id;
                        parentId = data.data.parentId;
                        grandpaId = data.data.grandpaId;
                    }
                };
            }, "json");

            $.when(areaDeffer).then(function(){
                var areaPromise = $.ajax({
                    type: "get",
                    url: contextPath + '/sys/area/service/twoGroupAreaList?parentType=4&grandpaId='+grandpaId+"&type=5&parentId="+parentId,
                    dataType: "json",
                    success: function (data) {
                        //console.log(data);
                        //console.log(data.data.parent);
                        //console.log(data.data.current);
                        if (data.success != true) {
                            return false;
                        }

                        var grandpaZNode = zTree.getNodeByParam("id", grandpaId, null);//获取指定祖父节点
                        var areaData = data.data.parent;
                        var townData = data.data.current;
                        //console.log(areaData);
                        //console.log(townData);
                        if (typeof(grandpaZNode.children) == "undefined") {
                            for (var i = 0; i < areaData.length; i++) {
                                var nodeData = {
                                    id: areaData[i].id + '',
                                    pid: areaData[i].parent.id + '',
                                    name: areaData[i].name,
                                    type: areaData[i].type
                                }
                                zTree.addNodes(grandpaZNode, nodeData, false);
                            }
                        }
                        var parentZNode = zTree.getNodeByParam("id", parentId, null); //获取指定父节点
                        for (var i = 0; i < townData.length; i++) {
                            var nodeData = {
                                id: townData[i].id + '',
                                pid: townData[i].parent.id + '',
                                name: townData[i].name,
                                type: townData[i].type
                            }
                            zTree.addNodes(parentZNode, nodeData, false);
                        }
                        if (selectAreaId) {
                            var localTownNode = zTree.getNodeByParam("id", selectAreaId);
                            if (localTownNode != null) {
                                zTree.selectNode(localTownNode, true);
                            }
                        }
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                        layerError("读取区/县,街道数据列表失败", "系统提示");
                    }
                });
            });
        }
    }

    /**
     * 通过市id获取区/县区域列表
     * @param zTree
     * @param districtNode
     */
    function obtainDistrictDataByCity(zTree,cityNode,selectNodeId) {
        if (cityNode.type == 3 && typeof(cityNode.children) == "undefined") {
            var type = 4;
            var parentId = cityNode.id;
            $.ajax({
                type: "get",
                url: contextPath + '/sys/area/service/arealist?type='+type+'&id='+parentId,
                dataType: "json",
                success: function (data) {
                    var parentZNode = zTree.getNodeByParam("id", parentId, null);//获取指定父节点
                    for (var i = 0; i < data.length; i++) {
                        var nodeData = {id:data[i].id+'',pid:data[i].parent.id+'',name:data[i].name,type:data[i].type}
                        zTree.addNodes(parentZNode,nodeData, false);
                    }
                    if(selectNodeId) {
                        var localCityNode = zTree.getNodeByParam("id", selectNodeId);
                        if (localCityNode != null) {
                            zTree.selectNode(localCityNode, true);
                        }
                    }
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    layerError("读取区/县列表失败", "系统提示");
                }
            });
        }
    }

    /**
     * 通过区/县id获取乡镇区域列表
     * @param zTree
     * @param districtNode
     */
    function obtainTownDataByDistrict(zTree,districtNode,selectNodeId) {
        //console.log("obtainTownDataByDistrict");
        if (districtNode.type == 4 && typeof(districtNode.children) == "undefined") {
            var type = 5;
            var parentId = districtNode.id;
            $.ajax({
                type: "get",
                url: contextPath + '/sys/area/service/arealist?type='+type+'&id='+parentId,
                dataType: "json",
                success: function (data) {
                    // console.log("获取4级区域的数据."+data);
                    var parentZNode = zTree.getNodeByParam("id", parentId, null);//获取指定父节点
                    for (var i = 0; i < data.length; i++) {
                        var nodeData = {id:data[i].id+'',pid:data[i].parent.id+'',name:data[i].name,type:data[i].type}
                        zTree.addNodes(parentZNode,nodeData, false);
                    }
                    if(selectNodeId) {
                        var districtNode = zTree.getNodeByParam("id", selectNodeId);
                        if (districtNode != null) {
                            zTree.selectNode(districtNode, true);
                        }
                    }
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    layerError("读取街道/乡镇列表失败", "系统提示");
                }
            });
        }
    }

    /**
     * 通过区/县id获取乡镇区域列表
     * @param zTree
     * @param districtNode
     * @param servicePointId
     */
    function initTownDataBySelectedDistrict(zTree, districtNode, servicePointId, checked) {
        //console.log("initTownDataBySelectedDistrict");
        if (districtNode.type == 4 && typeof(districtNode.children) == "undefined" && servicePointId) {
            var type = 5;
            var parentId = districtNode.id;
            var areaPromise = $.ajax({
                type: "get",
                url: contextPath + '/sys/area/service/arealist?type='+type+'&id='+parentId,
                dataType: "json"
            });

            var servicepointPromise = $.ajax({
                type: "get",
                url: contextPath + '/md/servicepointstation/arealistbyservicepointid?servicePointId='+servicePointId,
                dataType: "json"
            });

            $.when(areaPromise,servicepointPromise).then(function(areaReturn,servicpointReturn){
                // console.log("获取4级区域的数据.");
                // console.log(areaReturn);
                // console.log("获取到服务点的数据.");
                // console.log(servicpointReturn);
                var parentZNode = zTree.getNodeByParam("id", parentId, null);//获取指定父节点
                var jqXHR = areaReturn[2];
                var pointXHR = servicpointReturn[2];
                if (jqXHR.statusText == "success" && pointXHR.statusText == "success") {
                    var areaData = areaReturn[0];
                    var servicePointData = servicpointReturn[0].data;
                    var servicePointSucess = servicpointReturn[0].success;
                    for (var i = 0; i < areaData.length; i++) {
                        var nodeData = {id: areaData[i].id + '',pid: areaData[i].parent.id + '',name: areaData[i].name,type: areaData[i].type}
                        var addNode = zTree.addNodes(parentZNode, nodeData, false);
                        /*
                        if (addNode && servicePointData && servicePointSucess) {
                            for (var n in servicePointData) {
                                if (servicePointData[n].id == areaData[i].id) {
                                    console.log("添加的节点:");
                                    console.log(addNode);
                                    var selectNode = zTree.getNodeByParam("id", areaData[i].id + '', null);
                                    if (checked == "true") {
                                        try {
                                            zTree.checkNode(selectNode, true, true);
                                        } catch (e) {
                                        }
                                    } else {
                                        zTree.selectNode(selectNode, true);
                                    }
                                    break;
                                }
                            }
                        }
                        */
                    }

                    if (servicePointData && servicePointSucess) {
                        for (var n in servicePointData) {
                            if (servicePointData[n] != null) {
                                var selectNode = zTree.getNodeByParam("id", servicePointData[n].id + '', null);
                                if (checked == "true") {
                                    try {
                                        zTree.checkNode(selectNode, true, true);
                                    } catch (e) {
                                    }
                                } else {
                                    zTree.selectNode(selectNode, true);
                                }
                            }
                        }
                    }
                }
            },function(data1,data2){
                //console.log("错误1." + data1);
                //console.log("错误1." + data2);
                if (data1 != null) {
                    layerError("读取街道/乡镇列表失败", "系统提示");
                    return false;
                }
                if (data2 != null) {
                    layerError("读取服务点失败", "系统提示");
                    return false;
                }
            })

        }
    }

    /**
     * 通过区/县id获取乡镇区域列表
     * @param zTree
     * @param districtNode
     * @param servicePointId
     */
    function fillTownDataBySelectedDistrict(zTree, districtNode, subAreaIds, checked) {
        // 用来取代方法  initTownDataBySelectedDistrict
        //console.log("fillTownDataBySelectedDistrict");
        if (districtNode.type == 4 && typeof(districtNode.children) == "undefined") {
            var type = 5;
            var parentId = districtNode.id;
            var areaPromise = $.ajax({
                type: "get",
                url: contextPath + '/sys/area/service/arealist?type='+type+'&id='+parentId,
                dataType: "json"
            });

            $.when(areaPromise).then(function(areaData){
                //console.log("获取4级区域的数据.");
                //console.log(areaData);
                //console.log(subAreaIds);
                var parentZNode = zTree.getNodeByParam("id", parentId, null);  //获取指定父节点
                for (var i = 0; i < areaData.length; i++) {
                    var nodeData = {id: areaData[i].id + '',pid: areaData[i].parent.id + '',name: areaData[i].name,type: areaData[i].type}
                    var addNode = zTree.addNodes(parentZNode, nodeData, false);
                }

                if (subAreaIds) {
                    for (var n=0; n<subAreaIds.length;n++) {
                        var selectNode = zTree.getNodeByParam("id",  subAreaIds[n]+ '', null);
                        if (selectNode) {
                            if (checked == "true") {
                                try {
                                    zTree.checkNode(selectNode, true, true);
                                } catch (e) {
                                }
                            } else {
                                zTree.selectNode(selectNode, true);
                            }
                        }
                    }
                }
            },function(data){
                //console.log("错误1." + data1);
                if (data != null) {
                    layerError("读取街道/乡镇列表失败", "系统提示");
                    return false;
                }
            })

        }
    }

    function fillTownDataByTownIds(zTree,townIds,checked) {
        //console.log(townIds);
        if ( townIds.length > 0) {
            var townIdsStr = townIds.join(",");
            //console.log("传入ids为:" + townIdsStr);
            var areaDeffer = $.get(contextPath + '/sys/area/service/arealistbyids',{ids:townIdsStr},function(data){
                if (data.success == false) {
                    layerError(data.message,"错误提示");
                    return false;
                } else {
                    if (data.data && data.data.length >0) {
                        //console.log("从后台查询出来的数据:");
                        $.each(data.data,function(i,area){
                            //console.log(area);
                            var parentId = area.parent.id;
                            var parentNode = zTree.getNodeByParam("id", parentId,null);
                            var nodeData = {id:area.id+'',pid:area.parent.id+'',name:area.name,type:area.type}

                            zTree.addNodes(parentNode,nodeData, false);
                        });
                    }
                };
            },"json");
            $.when(areaDeffer).then(function(){
                $.each(townIds,function(i,value) {
                    var selectNode = zTree.getNodeByParam("id", value + '', null);

                    if (checked == "true") {
                        try {
                            zTree.checkNode(selectNode, true, true);
                        } catch (e) {
                        }
                        //zTree.selectNode(selectNode, false);
                    } else {
                        zTree.selectNode(selectNode, true);
                    }
                });
                //console.log("已执行完成");
            });
        }
    }
}
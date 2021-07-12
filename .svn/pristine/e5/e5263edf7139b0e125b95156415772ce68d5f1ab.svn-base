<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>服务区域</title>
    <meta name="decorator" content="default"/>

    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        var $btnSubmit = $("#btnSubmit");
        $(document).ready(function () {
            $("#searchForm").validate({
                submitHandler: function(form){
                    if (clickTag == 1) {
                        return false;
                    }

                    if ($btnSubmit.prop("disabled") == true) {
                        return false;
                    }

                    var serviceAreaLength = $(":checked[name^='service']").length;

                    if (serviceAreaLength == 0) {
                        layerError("至少要选择一个服务街道", "错误提示");
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                        return false;
                    }
                    var servicePointStationArray = [];
                    var servicePointId = $("#servicePointId").val();
                    $(":checked[name^='service']").each(function (index, domEle) {
                        var subAreaId = $(this).data("sub-area-id");
                        var areaId = $(this).data("area-id");
                        var servicePointStation = {};
                        servicePointStation.subAreaId = subAreaId;
                        servicePointStation.areaId = areaId;
                        servicePointStation.servicePointId = servicePointId;
                        servicePointStationArray.push(servicePointStation);
                    });

                    var servicePointAutoPlanArray = [];
                    $(":checked[name^='category']").each(function(index, domEle) {
                        var subAreaId = $(this).data("sub-area-id");
                        var areaId = $(this).data("area-id");
                        var productCategoryId = $(this).data("category-id");
                        var servicePointAutoPlan = {};
                        servicePointAutoPlan.areaId = areaId;
                        servicePointAutoPlan.subAreaId = subAreaId;
                        servicePointAutoPlan.servicePointId = servicePointId;
                        servicePointAutoPlan.productCategoryId = productCategoryId;
                        servicePointAutoPlan.autoPlanFlag = 1;

                        servicePointAutoPlanArray.push(servicePointAutoPlan);
                    });

                    var entity ={};
                    entity.servicePointStationList = servicePointStationArray;
                    entity.servicePointAutoPlanList = servicePointAutoPlanArray;

                    var loadingIndex = top.$.jBox.tip('正在提交,请稍候...', 'loading');
                    clickTag = 1;
                    $btnSubmit.attr('disabled', 'disabled');
                    $.ajax({
                        cache: false,
                        type: "POST",
                        url: "${ctx}/md/servicePointAutoPlan/batchSave",
                        data: JSON.stringify(entity),
                        dataType: 'json',
                        contentType: "application/json;charset=utf-8",
                        success: function (data) {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                //layerMsg("保存成功");
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if (pframe) {
                                    pframe.repage();
                                }
                                top.layer.close(this_index);   //关闭本身
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError("数据保存错误:" + data.message, "错误提示");
                            }
                        },
                        error: function (data) {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data, null, "数据保存错误，请重试!");
                        }
                    });
                },
                errorContainer: "#messageBox",
                errorPlacement: function(error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

            $(":checkbox[name='selectAll']").on("click", function () {
                var checkedValue = $(this).attr("checked") || "";
                if (checkedValue == "") {
                    $(":checked[name^='service']").removeAttr("checked");
                    $(":checked[name^='category']").removeAttr("checked");
                    $(":checked[name^='selectCategoryAll']").removeAttr("checked");
                    $(":checkbox[name^='category']").attr("disabled","disabled");
                } else {
                    $(":checkbox[name^='service']").attr("checked", "checked");
                    $(":checkbox[name^='category']").removeAttr("disabled");
                }
            });

            $(":checkbox[name^='selectCategoryAll']").on("click", function () {
                var productCategoryId = $(this).data("category-id");

                var checkedValue = $(this).attr("checked") || "";
                if (checkedValue == "") {
                    $(":checked[name='category-"+productCategoryId+"']:not(:disabled)").removeAttr("checked");
                } else {
                    $(":checkbox[name='category-"+productCategoryId+"']:not(:disabled)").attr("checked", "checked");
                }
            });

            $(":checkbox[name^='service']").on("click", function () {
                var checkedValue = $(this).attr("checked") || "";
                var subAreaId = $(this).data("sub-area-id") || "";
                if (checkedValue == "") {
                    $(":checkbox[name^='category'][data-sub-area-id='"+subAreaId+"']").removeAttr("checked").attr("disabled","disabled");
                    $(":checked[name^='selectCategoryAll']").removeAttr("checked");
                } else {
                    $(":checkbox[name^='category'][data-sub-area-id='"+subAreaId+"']").removeAttr("disabled");
                }

                if ($(":checkbox[name^='service']").length == $(":checked[name^='service']").length) {
                    $("#selectAll").attr("checked", "checked");
                } else {
                    $("#selectAll").removeAttr("checked");
                }
            });

            $(":checkbox[name^='category']").on("click", function () {
                var productCategoryId = $(this).data("category-id") || "";

                if ($(":checkbox[name^='category'][data-category-id='"+productCategoryId+"']").length == $(":checked[name^='category'][data-category-id='"+productCategoryId+"']").length) {
                    $("#selectCategoryAll-"+productCategoryId).attr("checked", "checked");
                } else {
                    $("#selectCategoryAll-"+productCategoryId).removeAttr("checked");
                }
            });
        });
    </script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .form-horizontal { margin-top:20px;}
        .form-horizontal .control-label {width: 65px; text-align: left}
        .form-horizontal .form-actions {text-align: right;}
        .control-group {border-bottom:0px;}
        .form-actions { margin:0px -20px 0px -20px;background-color: white;  padding-top:10px;padding-bottom: 10px;}
    </style>
</head>
<body>
<sys:message content="${message}"/>
<form:form id="searchForm"  modelAttribute="servicePoint" action="" method="post" class="form-horizontal">
    <form:hidden path="id" id="servicePointId"/>
    <div class="row-fluid">
        <div class="span3">
            <div class="control-group">
                <label class="control-label">网点编号:</label>
                <form:input path="servicePointNo" readonly="true" htmlEscape="false" class="input-small"/>
            </div>
        </div>
        <div class="span8">
            <div class="control-group">
                <label class="control-label">网点名称:</label>
                <form:input path="name" readonly="true" htmlEscape="false" class="input-xlarge" />
            </div>
        </div>
    </div>
    <div class="control-group" style="height:725px;overflow: auto;">
        <table id="contentTable" class="table table-bordered table-condensed table-hover" style="height: auto;">
            <thead>
            <tr>
                <th rowspan="2" style="width:80px;">市</th>
                <th rowspan="2" style="width:100px;">区/县</th>
                <th rowspan="2" style="width:100px;">乡镇/街道</th>
                <th rowspan="2" style="width:60px;"><label><input id="selectAll" name="selectAll" type="checkbox" value="0"/>服务街道</label></th>
                <th colspan="${fn:length(productCategoryList)}">自动派单</th>
            </tr>
            <tr>
                <c:forEach items="${productCategoryList}" var="productCategory">
                    <th style="width:60px;"><label><input id="selectCategoryAll-${productCategory.id}" name="selectCategoryAll-${productCategory.id}" type="checkbox"  data-category-id="${productCategory.id}" />${productCategory.name}</label></th>
                </c:forEach>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${list}" var="areaModel">
                <c:forEach items="${areaModel.subAreas}" var="area">
                    <tr>
                        <td>${areaModel.parent.name}</td>
                        <td>${areaModel.name}</td>
                        <td>
                            <%--<a href="javascript:" data-toggle="tooltip" data-tooltip="${area.name}" style="cursor: pointer;">${area.name}</a>--%>
                            ${area.name}
                        </td>
                        <td>
                            <input type="checkbox" name="service-${area.id}" data-id="${area.id}" data-area-id="${area.parentId}" data-sub-area-id="${area.id}">
                        </td>
                        <c:forEach items="${productCategoryList}" var="category">
                            <td><input name="category-${category.id}" type="checkbox" data-category-id="${category.id}" data-area-id="${area.parentId}" data-sub-area-id="${area.id}" /></td>
                        </c:forEach>
                    </tr>
                </c:forEach>
            </c:forEach>
            </tbody>
        </table>
    </div>
    <div class="form-actions">
        <shiro:hasPermission name="md:servicepointautoplan:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
        </shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="关闭"
               onclick="javascript:closeDialog();"/>
    </div>
</form:form>

<script type="text/javascript">
    function initCheckBoxNew(){
        var trLength = $("#contentTable tbody tr").length;// 获取当前表格中tr的个数
        if (trLength == 0) {
            return false;
        }

        $(":checkbox[name^='category']").attr("disabled","disabled");
        var autoPlanList = {};
        <c:if test="${not empty autoPlanList && !(autoPlanList eq null)}">
            autoPlanList = ${autoPlanList}
        </c:if>
        if (autoPlanList && autoPlanList.length >0) {
            for (var i in autoPlanList) {
                var categoryId = autoPlanList[i].productCategoryId;
                var subAreaId =  autoPlanList[i].subAreaId;

                var query = ":checkbox[name^='category'][data-category-id='"+categoryId+"'][data-sub-area-id='"+subAreaId+"']";
                $(query).attr("checked", "checked");
            }
        }

        var areaIdList = {};
        <c:if test="${not empty areaIdList && !(areaIdList eq null)}">
            areaIdList = ${areaIdList}
        </c:if>

        if (areaIdList && areaIdList.length >0) {
            for (var i in areaIdList) {
                var areaId = areaIdList[i];

                var query = ":checkbox[name^='service'][data-sub-area-id='"+areaId+"']";
                $(query).attr("checked", "checked");

                var autoPlanAreaList = ":checkbox[name^='category'][data-sub-area-id='"+areaId+"']";
                $(autoPlanAreaList).each(function(index, domEle) {
                    $(this).removeAttr("disabled");
                })
            }
        }
        // 判断服务街道是否需要全选
        if ($(":checkbox[name^='service']").length == $(":checked[name^='service']").length) {
            $("#selectAll").attr("checked", "checked");
        }

        // 判断品类是否需要全选
        <c:forEach items="${productCategoryList}" var="category">
            var categoryId = ${category.id};
            if ($(":checkbox[name='category-"+categoryId+"']").length == $(":checked[name='category-"+categoryId+"']").length) {
                $("#selectCategoryAll-"+categoryId).attr("checked", "checked");
            }
        </c:forEach>
    }
    function mergeCell(columnIndex) {
        tr = $("#contentTable tr").length;// 获取当前表格中tr的个数
        var mark = 0; //要合并的单元格数
        var index = 0; //起始行数
        /*
         *  要合并单元格，需要存储两个参数，
         * 1，开始合并的单元格的第一行的行数，
         * 2.要合并的单元格的个数
        **/
        //console.log(tr);
        //判断 若只有一行数据，则不做调整
        if(tr <= 2){
        } else {
            //var i=1  比较当前的tr和上一个tr的值
            for(var i=0;i < tr ;i++){
                var ford = $("#contentTable tr:gt(0):eq("+i+") td:eq("+columnIndex+")").text();
                //根据下标获取单元格的值
                // tr:gt(0)  从下标0 开始获取
                // tr:gt(0):eq( i ) :i 标识 当前行的下标 ，0 开始
                // td:eq(0) 当前行的第一个单元格，下标从0开始
                var behind = $("#contentTable tr:gt(0):eq("+(parseInt(i)+1)+") td:eq("+columnIndex+")").text();
                if(ford == behind){
                    $("#contentTable tr:gt(0):eq("+(parseInt(i)+1)+") td:eq("+columnIndex+")").hide();
                    mark = mark +1;
                } else if(ford != behind){
                    //如果值不匹配则遍历到不同种的分类,将旧分类隐藏
                    index = i-mark;
                    if (mark >0) {
                        $("#contentTable tr:gt(0):eq(" + index + ") td:eq(" + columnIndex + ")").attr("rowspan", mark + 1);//+1 操作标识，将当前的行加入到隐藏
                        //rowspan 列上横跨， colspan 行上横跨
                        //后面的参数，表示横跨的单元格个数，
                        //合并单元格就是将其他的单元格隐藏（hide）,或删除（remove）。
                        //将一个单元格的rowspan 或colsspan 加大
                        mark = 0;
                        $("#contentTable tr:gt(0):eq(" + (parseInt(i)) + ") td:eq(" + columnIndex + ")").hide();
                    }
                }
            }
        }
    }
    function closeDialog() {
        // console.log("closeDialog();")
        var pframe = getActiveTabIframe();//定义在jeesite.min.js中
        if (pframe) {
            pframe.repage();
        }
        top.layer.close(this_index);   //关闭本身
    }

    mergeCell(0);
    mergeCell(1);
    initCheckBoxNew();
</script>
</body>
</html>


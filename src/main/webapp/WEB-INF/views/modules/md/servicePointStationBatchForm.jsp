<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点服务点管理</title>
    <meta name="decorator" content="default"/>
    <c:set var="currentuser" value="${fns:getUser() }" />
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        var $btnSubmit = $("#btnSubmit");
        $(document).ready(function () {
            $("#selectAll").on("click",function(e){
                var checkedValue = $(this).attr("checked") || "";
                if (checkedValue == "") {
                    var autoplansize = $(":checked[name='autoplan']").length;
                    if (autoplansize > 0) {
                        layerError("先取消自动派单选项,才能取消服务网点!","错误提示");
                        return false;
                    }
                    $(":checkbox[name='autoplan']").attr("disabled","disabled");
                    $(":checked[name='points']").removeAttr("checked");
                } else {
                    $(":checkbox[name='autoplan']").removeAttr("disabled");
                    $(":checkbox[name='points']").attr("checked","checked");
                }
            });

            $(":checkbox[name='points']").on("click",function(e){
                var checkedValue = $(this).attr("checked") || "";
                var areaId = $(this).val();
                var query = $(":checkbox[name='autoplan'][value='"+areaId+"']");
                var autoPlanCheckedValue = query.attr("checked") || "";
                if (checkedValue == "") {
                    if (autoPlanCheckedValue != "") {
                        layerError("先取消自动派单选项,才能取消服务网点!", "错误提示");
                        return false;
                    } else {
                        query.attr("disabled","disabled");
                    }
                } else {
                    query.removeAttr("disabled");
                }

                if ($(":checkbox[name='points']").length != $(":checked[name='points']").length){
                    $("#selectAll").removeAttr("checked");
                } else {
                    $("#selectAll").attr("checked","checked");
                }

            });


            $("#allAutoPlan").on("click",function(e){
                var checkedValue = $(this).attr("checked") || "";
                $(":checkbox[name='autoplan']:not(:disabled)").each(function(i,value){
                    if (checkedValue == "") {
                        $(value).removeAttr("checked");
                    } else {
                        $(value).attr("checked", checkedValue);
                    }
                })
            });


            $("#btnSubmit").click(function () {
                var areas = [];

                areas = $(":checked[name='points']").map(function(){
                    return $(this).attr("value");
                })

                //console.log(areas);

                if (areas.length ==0 ){
                    layerError("至少要选择一个街道","错误提示");
                    return false;
                }

                var autoPlans = [];
                autoPlans = $(":checked[name='autoplan']").map(function(){
                    return $(this).attr("value");
                })

                if(!$("#inputForm").valid()){
                    return false;
                }
                $("#areaIds").val(areas.get().join(","));
                $("#autoPlanIds").val(autoPlans.get().join(","));
                $("#inputForm").submit();
            });

            $("#inputForm").validate({
                highlight : function(element) {
                    $(element).closest('.control-group').addClass('has-error');
                },
                success : function(label) {
                    label.closest('.form-group').removeClass('has-error');
                    label.remove();
                },
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $btnSubmit.removeAttr('disabled');
                    element.parent('div').append(error);
                },
                submitHandler: function (form) {

                    if($btnSubmit.prop("disabled") == true){
                        return false;
                    }

                    $btnSubmit.attr("disabled", "disabled");
                    layerLoading('正在提交，请稍等...',false);
                    form.submit();
                },
            });
        });

        function pointSelect_callback(data){
            $("[id^='servicePoint.servicePointNo']").val(data.servicePointNo);
            var eid = $("#id").val();
            loadArea(data.id,eid);
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/md/servicepoint/selectForStation">服务区域列表</a></li>
    <li class="active"><a href="javascript:;">服务区域<shiro:hasPermission name="md:servicepointstation:edit">${not empty servicePointStation.id?'修改':'添加'}</shiro:hasPermission></a></li>
    <%--
    <li><a href="${ctx}/md/servicepointstation/form?servicePoint.id=${servicePoint.id}">网点服务点<shiro:hasPermission name="md:servicepointstation:edit">${not empty servicePointStation.id?'修改':'添加'}</shiro:hasPermission></a></li>
    --%>
    <li><a href="${ctx}/md/servicepointstation/areaStationList">区域服务点列表</a></li>
    <li><a href="${ctx}/md/servicepointstation/amap">地图</a></li>
</ul>
<sys:message content="${message}"/>
<form:form id="inputForm" modelAttribute="servicePoint" action="${ctx}/md/servicepointstation/batchSave" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="delFlag"/>
    <input id="areaIds" name="areaIds" type="hidden" value=""/>
    <input id="autoPlanIds" name="autoPlanIds" type="hidden" value=""/>

    <legend>网点信息</legend>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">网点编号:</label>
                <div class="controls">
                    <form:input path="servicePointNo" readonly="true" htmlEscape="false" class="input-small required"/>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">网点名称:</label>
                <div class="controls">
                    <%--
                    <md:pointselectlayer id="servicePoint" name="servicePoint.id" value="${servicePoint.id}" labelName="servicePointNo.name" labelValue="${servicePoint.name}"
                                         width="1200" height="780" callbackmethod="pointSelect_callback" title="选择网点" areaId="" cssClass="required"/>
                                         --%>
                    <form:input path="name" readonly="true" htmlEscape="false" class="input-small required" />
                </div>
            </div>
        </div>
    </div>
    <legend>服务点信息</legend>
    <div class="control-group">
        <label class="control-label">服务区域:</label>
        <div class="controls">
            <table id="contentTable" class="table table-bordered table-condensed table-hover" style="width: 90%">
                <thead>
                    <tr>
                        <th style="width:60px">市</th>
                        <th style="width:110px">区/县</th>
                        <th width="100">乡镇/街道</th>
                        <th width="60"><input id="selectAll" type="checkbox" value="0"/>服务街道</th>
                        <th width="60"><input id="allAutoPlan" type="checkbox" value="0"/>自动派单</th>
                        <%--<th width="60">自动派单</th>--%>
                    </tr>
                </thead>
                <tbody>
                <c:forEach items="${list}" var="areaModel">
                    <c:forEach items="${areaModel.subAreas}" var="area">
                    <tr>
                        <td>${areaModel.parent.name}</td>
                        <td>${areaModel.name}</td>
                        <td>
                            <a href="javascript:" data-toggle="tooltip" data-tooltip="${area.name}" style="cursor: pointer;">${area.name}</a>
                        </td>
                        <td>
                            <input name="points" type="checkbox" value="${area.id}" />
                        </td>
                        <td>
                            <input name="autoplan" type="checkbox" value="${area.id}" />
                        </td>
                    </tr>
                    </c:forEach>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <div class="form-actions">
        <shiro:hasPermission name="md:servicepointstation:edit">
            <input id="btnSubmit" class="btn btn-primary" type="button" value="保 存"/>&nbsp;</shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
    </div>
</form:form>
<script class="removedscript" type="text/javascript">

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

    /*
    function initCheckBox(){
        var areaIdArray = {areaIdList};
        if (areaIdArray && areaIdArray.length >0) {
            for(var i =0;i<areaIdArray.length;i++) {
                var query = ":checkbox[name='points'][value="+areaIdArray[i]+"]";
                $(query).attr("checked","checked");
            }
            var tr = $("#contentTable tbody tr").length;// 获取当前表格中tr的个数
            if (tr == areaIdArray.length) {
                $("#selectAll").attr("checked","checked");
            }
        }
    }
    */

    function initCheckBoxNew(){
        var stationList = {};
        <c:if test="${not empty servicePointStationList && !(servicePointStationList eq null)}">
            stationList = ${servicePointStationList}
        </c:if>
        $(":checkbox[name='autoplan']").attr("disabled","disabled");
        if (stationList && stationList.length >0) {
            var n = 0;
            for(var i in stationList){
                var areaId = stationList[i].area.id;
                var query = ":checkbox[name='points'][value="+areaId+"]";
                $(query).attr("checked","checked");

                var apfQuery = ":checkbox[name='autoplan'][value=" + areaId + "]";
                $(apfQuery).removeAttr("disabled");

                var autoPlanFlag = stationList[i].autoPlanFlag;
                if (autoPlanFlag == 1) {
                    $(apfQuery).attr("checked","checked");
                    n++;
                }
            }
            var tr = $("#contentTable tbody tr").length;// 获取当前表格中tr的个数
            if (tr == stationList.length) {
                $("#selectAll").attr("checked","checked");
            }
            /*
            if (tr == n) {
                $("#allAutoPlan").attr("checked","checked");
            }
            */
        }
    }

    mergeCell(0);
    mergeCell(1);
    //initCheckBox();
    initCheckBoxNew();
</script>
</body>
</html>

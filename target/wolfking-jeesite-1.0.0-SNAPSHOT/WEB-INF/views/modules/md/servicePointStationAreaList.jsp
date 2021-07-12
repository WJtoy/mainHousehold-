<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域服务点管理</title>
    <meta name="decorator" content="default" />
    <%@include file="/WEB-INF/views/include/dialog.jsp"%>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        .form-search .ul-form li label {width: auto;}
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        $(document).ready(function() {
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});

            $("select[name='autoPlanFlagCtrl']").change(function(){
                // console.log("是否自动派单:"+$(this).attr("id")+':'+$(this).attr("value"));
                var servicePointId = $(this).attr("id");
                var autoPlanFlag = $(this).attr("value");
                $.ajax({
                    cache: false,
                    type: "POST",
                    url:"${ctx}/md/servicepoint/ajax/updateAutoPlanFlag?servicePointId=" + servicePointId + "&autoPlanFlag=" + (autoPlanFlag || ''),
                    dataType: 'json',
                    success: function (data) {
                        if (data.success ) {
                            layerMsg('保存成功');

                        }else{
                            layerError(data.message);
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"保存自动派单错误，请重试!");
                        btn.disabled=false;
                    }
                });
            })

            $("select[name='level']").change(function(){
                // console.log("网点等级:"+$(this).attr("id")+':'+$(this).attr("value"));

                var servicePointId = $(this).attr("id");
                var level = $(this).attr("value");
                $.ajax({
                    cache: false,
                    type: "POST",
                    url:"${ctx}/md/servicepoint/ajax/updateLevel?servicePointId=" + servicePointId + "&level=" + (level || ''),
                    dataType: 'json',
                    success: function (data) {
                        if (data.success ) {
                            layerMsg('保存成功');
                        }else{
                            layerError(data.message);
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"保存网点等级错误，请重试!");
                        btn.disabled=false;
                    }
                });
            });
            $("#servicePointId").on("change",function(e){
                if (e.target.value == ""){
                    $("[id^='servicePointNo']").val("");
                }
            });

            $("#btnSubmit").click(function () {
                var $btnSubmit = $("#btnSubmit");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                var areaId = $("#areaId").val();
                if (areaId == undefined || areaId.length == 0) {
                    layerInfo("请选择区域!", "信息提示");
                    return false;
                }

                layerLoading('正在查询，请稍等...',true);
                $("#btnSubmit").prop("disabled",true);

                $("#searchForm").attr("action", "${ctx}/md/servicepointstation/areaStationList");
                $("#searchForm").submit();
            });
        });

        function pointSelect_callback(data){
            $("[id^='servicePointNo']").val(data.servicePointNo);
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/md/servicepoint/selectForStation">服务区域列表</a></li>
    <%--
    <shiro:hasPermission name="md:servicepointstation:edit">
        <li><a href="${ctx}/md/servicepointstation/form?servicePoint.id=${servicePointStation.servicePoint.id}">网点服务点添加</a></li>
    </shiro:hasPermission>
    --%>
    <li class="active"><a href="javascript:;">区域服务点列表</a></li>
    <li><a href="${ctx}/md/servicepointstation/amap">地图</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<c:set var="isSystemUser" value="${currentuser.isSystemUser()}" />
<form:form id="searchForm" modelAttribute="servicePoint" action="${ctx}/md/servicepointstation/areaStationList" method="POST" class="breadcrumb form-search">
    <form:hidden path="firstSearch" />
    <ul class="ul-form">
        <li>
            <label style="margin-left: 40px">区域:</label>
            <%--
            <sys:treeselectarea id="area" name="area.id" value="${servicePoint.area.id}" labelName="area.name" labelValue="${servicePoint.area.name}" title="区域"
                                url="/sys/area/treeData" nodesLevel="2" nameLevel="3" cssStyle="width:140px;" cssClass="required" allowClear="true" notAllowSelectRoot="true"/>
             --%>
            <%--<sys:treeselect id="area" name="area.id" value="${servicePoint.area.id}" labelName="area.name" labelValue="${servicePoint.area.fullName}" title="区域"--%>
                            <%--url="/sys/area/treeData" nodesLevel="2" nameLevel="3" cssStyle="width:140px;" cssClass="required" allowClear="true" notAllowSelectRoot="true" notAllowSelectParent="false"/>--%>
            <sys:newtableareaselect id="area" name="area.id" value="${servicePoint.area.id}"
                                    labelValue="${servicePoint.area.fullName}" labelName="area.name"
                                    title="区域" mustSelectCounty="false" cssClass="required"> </sys:newtableareaselect>

        </li>
        <li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" /></li>
        <li class="clearfix"></li>
    </ul>
</form:form>
<sys:message content="${message}" />
<table id="contentTable" class="fancyTable datatable table table-bordered table-condensed" style="table-layout:fixed">
    <thead>
    <tr>
        <th width="200">区/县名称</th>
        <th width="200">乡/镇/街道名称</th>
        <th width="260">服务点名称</th>
        <th width="200">网点名称</th>
        <%--<th width="160">服务半径(米)</th>--%>
        <c:if test="${isSystemUser}">
            <th width="130">备注</th>
        </c:if>
        <%--<th width="160">操作</th>--%>
    </tr>
    </thead>
    <tbody>
        <c:forEach items="${list}" var="item">
            <tr>
                <td>${item.area.parent.name}</td>
                <td>${item.area.name}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.name}">${item.name}</a></td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.servicePoint.name}">${item.servicePoint.name}</a></td>
                <td>${item.remarks}</td>
                <%--<td></td>--%>
            </tr>
            <%--<tr>
                <td rowspan="${item.cityMaxRow}">
                    <a>${item.area.name}</a>
                </td>

                <c:forEach  var="i" begin="0" end="${item.maxRow-1}">
                    <c:if test="${i ne 0}">
                        <tr>
                        <td rowspan="${item.maxRow}">
                            <shiro:hasPermission name="md:servicepointstation:edit">
                                <a href="${ctx}/md/servicepointstation/areaForm?area.id=${item.area.id}" data-toggle="tooltip"  data-tooltip="点击可以添加服务点">${item.subArea.name}</a>
                            </shiro:hasPermission>
                        </td>
                    </c:if>
                    <c:choose>
                        <c:when test = "${i lt item.servicePointStationList.size()}">
                            <td>${item.servicePointStationList.get(i).name}</td>
                            <td>${item.servicePointStationList.get(i).servicePoint.name}</td>
                            <td>${item.servicePointStationList.get(i).remarks}</td>
                            <td style="text-align: left;">
                                <shiro:hasPermission name="md:servicepointstation:stop">
                                    <c:choose>
                                        <c:when test="${item.servicePointStationList.get(i).delFlag==0}">
                                            <a href="${ctx}/md/servicepointstation/delete?id=${item.servicePointStationList.get(i).id}&servicePointId=${item.servicePointStationList.get(i).servicePoint.id}" onclick="return layerConfirmx('确认要停用该服务点吗？', this.href)">停用</a>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${ctx}/md/servicepointstation/enable?id=${item.servicePointStationList.get(i).id}&servicePointId=${item.servicePointStationList.get(i).servicePoint.id}" onclick="return layerConfirmx('确认要启用该服务点吗？', this.href)">启用</a>
                                        </c:otherwise>
                                    </c:choose>
                                </shiro:hasPermission>
                            </td>
                        </c:when>
                        <c:otherwise>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${i eq 0}">
                        </tr>
                    </c:if>
                </c:forEach>
            </tr>--%>
        </c:forEach>
    </tbody>
</table>
</body>
<script  type="text/javascript">
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

    mergeCell(0);
    mergeCell(1);
</script>
</html>


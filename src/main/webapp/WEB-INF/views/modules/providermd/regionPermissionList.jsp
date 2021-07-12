<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域设置</title>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/area/Area-1.2.js" type="text/javascript"></script>
    <script src="${ctxStatic}/layui/layui.js"></script>
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <c:set var="currentuser" value="${fns:getUser() }" />
    <style type="text/css">
        table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .b-form{
            padding: 8px 15px;
            margin: 13px 0 13px;
            list-style: none;
            border-radius: 4px;
        }
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;

        function setPage(obj) {
            var cityId = $("#cityId").val();
            if (obj != 2) {
                if(cityId==null || cityId<=0){
                    layerError("请选择城市","错误提示");
                    return false;
                }
            }
            if(cityId!='' || cityId>0){
                layerLoading("查询中...", true);
                $("#searchForm").submit();
            }
        }

        function editSelectArea(cityId) {
            var type = $("#type").val();
            var text;
            if (type == 1) {
                text = "突击街道";
            } else {
                text = "远程街道";
            }
            var url = "${ctx}/provider/md/regionPermission/selectStreet?id="+cityId;
            top.layer.open({
                type: 2,
                id:"areaRemoteFee",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['1240px', '850px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                    // 获取子页面的iframe
                    var iframeWin = top[layero.find('iframe')[0]['name']];
                    var subCityId = $("#subCityId").val();
                    var areaId = $("#areaId").val();
                    var type = $("#type").val();
                    if(iframeWin != null){
                        var json = {
                            cityId : subCityId,
                            areaId : areaId,
                            type : type
                        };
                        iframeWin.child(json);
                    }
                },
                end:function(){
                }
            });
        }

        function selectStreet(type) {
            if (type == 'assault') {
                $("#assaultStreet").addClass('active');
                $("#remoteStreet").removeClass('active');
                $("#type").attr("value",1);
                setPage(2);
            } else {
                $("#assaultStreet").removeClass('active');
                $("#remoteStreet").addClass('active');
                $("#type").attr("value",2);
                setPage(2);
            }
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active" id="assaultStreet"><a href="#" onclick="selectStreet('assault')">突击街道</a></li>
    <li class="" id="remoteStreet"><a href="#" onclick="selectStreet('remote')">远程街道</a></li>
</ul>

<form:form id="searchForm" modelAttribute="regionPermission" action="${ctx}/provider/md/regionPermission/findRegionPermissionList" method="post" class="b-form form-search">
    <form:hidden path="type" />
    <label>区&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;域：</label>
        <md:arearemotselect name="cityId" id="city" value="${regionPermission.cityId}"
                                       labelValue="${fullName}" labelName="fullName" title=""
                                       mustSelectCounty="true" cssClass="required">
        </md:arearemotselect>&nbsp;
    &nbsp;<input id="btnSeach" class="btn btn-primary" type="button" onclick="setPage(1)" style="width: 70px" value="查询" />&nbsp;
    <div style="float: right;width: 320px;margin-top: 5px;">
        <div style="width: 150px;float: left;"><div style="width: 16px;height: 16px;background: #FFEEEE;float: left;"></div><label style="width: 120px;float: right">所有街道已开通</label></div>
        <div  style="width: 150px;float: right;"><div style="width: 16px;height: 16px;background: #E8F7FF;float: left;"></div><label style="width: 120px;float: right">所有街道未开通</label></div>
    </div>
</form:form>

<input type="hidden" value="${regionPermission.cityId}" id="subCityId">
<input type="hidden" value="${regionPermission.areaId}" id="areaId">
<sys:message content="${message}"/>
<c:if test="${regionPermission.cityId !=null and regionPermission.cityId > 0}">
    <table id="contentTable" class="layui-table" style="width: 99%">
        <thead>
        <tr>
            <th style="width:100px;text-align: center;" rowspan="2">省市</th>
            <th style="width:110px;text-align: center;" rowspan="2">区县 (街道数量)</th>
            <%--<th style="width: 100px;text-align: center;" rowspan="2" id="zongshu">总数</th>--%>
            <c:forEach items="${regionPermissionViewList}" var="view" varStatus="v">
                <c:choose>
                    <c:when test="${v.index == 0}">
                        <c:forEach items="${view.productCategoryList}" var="categroy">
                            <th colspan="2" style="width: 200px;text-align: center;">${categroy.productCategoryName}</th>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </tr>
        <tr>
            <c:forEach items="${regionPermissionViewList}" var="view" varStatus="v">
                <c:choose>
                    <c:when test="${v.index == 0}">
                        <c:forEach items="${view.productCategoryList}" var="categroy">
                            <th style="text-align: center">已开通</th>
                            <th style="text-align: center">未开通</th>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </tr>
        </thead>

        <tbody>
            <tr>
            <td rowspan="${regionPermissionViewList.size()>0?regionPermissionViewList.size():''}">
                <label onclick="editSelectArea('${regionPermission.cityId}')" style="color: #0096DA;">${fullName}</label>
            </td>
            <c:choose>
                <c:when test="${regionPermissionViewList.size()>0}">
                <c:forEach items="${regionPermissionViewList}" var="areaCounty" varStatus="i">
                    <c:choose>
                        <c:when test="${i.index == 0}">
                            <td>
                                ${areaCounty.areaName}
                                <c:choose>
                                    <c:when test="${areaCounty.count>0}">
                                        （${areaCounty.count}）
                                    </c:when>
                                    <c:otherwise>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <c:forEach items="${areaCounty.productCategoryList}" var="category">
                                <c:choose>
                                    <c:when test="${category.openingNum == 0}">
                                        <td style="color: #0096DA;background: #E8F7FF;">${category.openingNum}</td>
                                        <td style="color: #0096DA;background: #E8F7FF;">${category.noOpeningNum}</td>
                                    </c:when>
                                    <c:when test="${category.noOpeningNum == 0}">
                                        <td style="color: #F54142;background: #FFEEEE;">${category.openingNum}</td>
                                        <td style="color: #F54142;background: #FFEEEE;">${category.noOpeningNum}</td>
                                    </c:when>
                                    <c:otherwise>
                                        <td>${category.openingNum}</td>
                                        <td>${category.noOpeningNum}</td>
                                    </c:otherwise>
                                </c:choose>

                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td>
                                    ${areaCounty.areaName}
                                    <c:choose>
                                        <c:when test="${areaCounty.count>0}">
                                            （${areaCounty.count}）
                                        </c:when>
                                        <c:otherwise>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <c:forEach items="${areaCounty.productCategoryList}" var="category">
                                    <c:choose>
                                        <c:when test="${category.openingNum == 0}">
                                            <td style="color: #0096DA;background: #E8F7FF;">${category.openingNum}</td>
                                            <td style="color: #0096DA;background: #E8F7FF;">${category.noOpeningNum}</td>
                                        </c:when>
                                        <c:when test="${category.noOpeningNum == 0}">
                                            <td style="color: #F54142;background: #FFEEEE;">${category.openingNum}</td>
                                            <td style="color: #F54142;background: #FFEEEE;">${category.noOpeningNum}</td>
                                        </c:when>
                                        <c:otherwise>
                                            <td>${category.openingNum}</td>
                                            <td>${category.noOpeningNum}</td>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </tr>
                        </c:otherwise>
                    </c:choose>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <td></td>
            <td></td>
        </c:otherwise>
        </c:choose>
        </tr>
        </tbody>
    </table>
</c:if>
<form:form id="submitForm" ></form:form>
<script class="removedscript" type="text/javascript">
    $(document).ready(function () {
        var type = ${regionPermission.type};
        if (type == 1) {
            $("#assaultStreet").addClass('active');
            $("#remoteStreet").removeClass('active');
        } else {
            $("#assaultStreet").removeClass('active');
            $("#remoteStreet").addClass('active');
        }
    });
</script>
</body>
</html>

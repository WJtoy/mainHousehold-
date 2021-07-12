<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>偏远区域设置</title>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/area/AreaRegion.js" type="text/javascript"></script>
    <script src="${ctxStatic}/layui/layui.js"></script>
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <c:set var="currentuser" value="${fns:getUser() }" />
    <style type="text/css">
        .table thead th, .table tbody td{
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 32px;
        }
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;

        function setPage(obj) {
            var cityId = $("#cityId").val();
            if (obj != 2) {
                if(cityId==null || cityId<=0){
                    $("#cityId").attr("value", 0);
                    layerLoading("查询中...", true);
                    $("#searchForm").submit();
                }
            }
            if(cityId!='' || cityId>0){
                layerLoading("查询中...", true);
                $("#searchForm").submit();
            }
        }

        function editSelectArea(cityId, productCategoryId, productCategoryName) {
            var groupType = 3;
            var text = "偏远区域";
            var url = "${ctx}/provider/md/regionPermissionNew/selectStreetNew?id="+cityId+"&productCategoryId="+productCategoryId + "&groupType=" + groupType;
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

                    if(iframeWin != null){
                        var json = {
                            productCategoryName : productCategoryName
                        };
                        iframeWin.child(json);
                    }
                },
                end:function(){
                }
            });
        }

    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">偏远区域</a></li>
</ul>

<form:form id="searchForm" modelAttribute="regionPermission" action="${ctx}/provider/md/regionPermissionNew/regionPermissionRemoteNewList" method="post" class="b-form breadcrumb form-search">

    <label>区&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;域：</label>
        <md:newAreaRemotselect id="city" name="cityId" value="${regionPermission.cityId}"
                                labelValue="${regionPermission.cityName}" labelName="cityName"
                                title="区域" mustSelectCounty="false" cssClass="required">
        </md:newAreaRemotselect>&nbsp;&nbsp;
    <input type="checkbox" name="status" id="checkin" value="0"><label for="checkin" style="margin-right: 20px;">已开通</label>
    <shiro:hasPermission name="md:regionPermissionArea:view"><input id="btnSeach" class="btn btn-primary" type="button" onclick="setPage(1)" style="width: 70px" value="查询" />&nbsp;</shiro:hasPermission>
</form:form>

<input type="hidden" value="${regionPermission.cityId}" id="subCityId">
<input type="hidden" value="${regionPermission.areaId}" id="areaId">
<sys:message content="${message}"/>

<c:if test="${show eq true}">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="width: 100%">
        <thead>
        <tr>
            <%--update on 2020-06-16--%>
            <th style="width:331px;text-align: center;" rowspan="2">省</th>
            <th style="width:331px;text-align: center;" rowspan="2">市 (街道数量)</th>
            <c:forEach items="${datas}" var="view" varStatus="v">
                <c:choose>
                    <c:when test="${v.index == 0}">
                        <c:forEach items="${view.value}" var="regionPermissionView" varStatus="re">
                            <c:if test="${re.index == 0}">
                                <c:forEach items="${regionPermissionView.productCategoryList}" var="categroy">
                                    <th style="width: 200px;text-align: center;">${categroy.productCategoryName}</th>
                                </c:forEach>
                            </c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </tr>
        </thead>

        <tbody>

            <c:forEach items="${datas}" var="data">
                <tr>
                <td rowspan="${data.value.size()}">
                    <label>${data.key}</label>
                </td>
            <c:choose>
                <c:when test="${data.value.size()>0}">
                <c:forEach items="${data.value}" var="areaCounty" varStatus="i">
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

                            <%--update on 2020-06-16--%>
                            <c:forEach items="${areaCounty.productCategoryList}" var="category">
                                <td>
                                    <a style="<c:out value='${category.openingNum eq 0 ? "":"color:#F54142"}'/>" onclick="editSelectArea('${areaCounty.areaId}','${category.productCategoryId}','${category.productCategoryName}')">${category.openingNum}</a>
                                </td>
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

                                <%--update on 2020-06-16--%>
                                <c:forEach items="${areaCounty.productCategoryList}" var="category">
                                    <td>
                                        <a style="<c:out value='${category.openingNum eq 0 ? "":"color:#F54142"}'/>" onclick="editSelectArea('${areaCounty.areaId}','${category.productCategoryId}','${category.productCategoryName}')">${category.openingNum}</a>
                                    </td>
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
        </c:forEach>
        </tbody>
    </table>
</c:if>
<form:form id="submitForm" ></form:form>
<script class="removedscript" type="text/javascript">
    $(document).ready(function () {
        var status = ${regionPermission.status == null ? 0 : 1};
        if (status == 1) {
            $("input[type='checkbox']").attr("value", 1);
            $("input[type='checkbox']").attr("checked", true);
        } else {
            $("input[type='checkbox']").attr("checked", false);
        }

        $("#checkin").click(function(){
            var checked = $("input[type='checkbox']").prop("checked");
            if (checked) {
                $("#checkin").attr("value", 1);
            } else {
                $("#checkin").attr("value", 0);
            }
        })
    });
</script>
</body>
</html>

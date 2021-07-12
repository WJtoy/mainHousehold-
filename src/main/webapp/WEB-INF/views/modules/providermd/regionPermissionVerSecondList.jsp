<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域设置</title>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/area/AreaRegion.js" type="text/javascript"></script>
    <c:set var="currentuser" value="${fns:getUser() }" />
    <style type="text/css">
        .table thead th, .table tbody td {
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
            var groupType = 1;
            var text = "区域属性";
            var url = "${ctx}/provider/md/regionPermissionNew/selectStreet?id="+cityId+"&productCategoryId="+productCategoryId + "&groupType="+ groupType;
            top.layer.open({
                type: 2,
                id:"areaRemoteFee",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['1000px', '888px'],
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
    <li class="active"><a href="javascript:void(0);">区域属性</a></li>
    <li><a href="${ctx}/provider/md/regionPermissionNew/findAreaList">区域汇总</a></li>
</ul>

<form:form id="searchForm" modelAttribute="regionPermission" action="${ctx}/provider/md/regionPermissionNew/findRegionPermissionList" method="post" class="b-form form-search">
    <label>区&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;域：</label>
        <md:newAreaRemotselect id="city" name="cityId" value="${regionPermission.cityId}"
                                labelValue="${regionPermission.cityName}" labelName="cityName"
                                title="区域" mustSelectCounty="false" cssClass="required">
        </md:newAreaRemotselect>&nbsp;&nbsp;
    <input id="btnSeach" class="btn btn-primary" type="button" onclick="setPage(1)" style="width: 70px" value="查询" />&nbsp;
</form:form>

<input type="hidden" value="${regionPermission.cityId}" id="subCityId">
<input type="hidden" value="${regionPermission.areaId}" id="areaId">
<sys:message content="${message}"/>

<c:if test="${show eq true}">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <%--update on 2020-12-02--%>
            <th width="60" rowspan="2">省</th>
            <th width="60" rowspan="2">市</th>
            <c:forEach items="${datas}" var="view" varStatus="v">
                <c:choose>
                    <c:when test="${v.index == 0}">
                        <c:forEach items="${view.value}" var="regionPermissionView" varStatus="re">
                            <c:if test="${re.index == 0}">
                                <c:forEach items="${regionPermissionView.productCategoryList}" var="categroy">
                                    <th colspan="3">${categroy.productCategoryName}</th>
                                </c:forEach>
                            </c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </tr>
        <tr>
            <c:forEach items="${datas}" var="view" varStatus="v">
                <c:choose>
                    <c:when test="${v.index == 0}">
                        <c:forEach items="${view.value}" var="regionPermissionView" varStatus="re">
                            <c:if test="${re.index == 0}">
                                <c:forEach items="${regionPermissionView.productCategoryList}" var="categroy">
                                    <th width="50">突击</th>
                                    <th width="50">自动</th>
                                    <th width="50">大客服</th>
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
                            </td>

                            <%--update on 2020-12-02--%>
                            <c:forEach items="${areaCounty.productCategoryList}" var="category">
                                <td><a style="<c:out value='${category.rushNum eq 0 ? "color:#515A6E":"color:#F54142"}'/>" href="javascript:void(0);" onclick="editSelectArea('${areaCounty.areaId}','${category.productCategoryId}','${category.productCategoryName}')">${category.rushNum}</a></td>
                                <td><a style="color:#515A6E" href="javascript:void(0);" onclick="editSelectArea('${areaCounty.areaId}','${category.productCategoryId}','${category.productCategoryName}')">${category.autoNum}</a></td>
                                <td><a style="color:#515A6E" href="javascript:void(0);" onclick="editSelectArea('${areaCounty.areaId}','${category.productCategoryId}','${category.productCategoryName}')">${category.keFuNum}</a></td>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td>
                                    ${areaCounty.areaName}
                                </td>

                                    <%--update on 2020-12-02--%>
                                <c:forEach items="${areaCounty.productCategoryList}" var="category">
                                    <td><a style="<c:out value='${category.rushNum eq 0 ? "color:#515A6E":"color:#F54142"}'/>" href="javascript:void(0);" onclick="editSelectArea('${areaCounty.areaId}','${category.productCategoryId}','${category.productCategoryName}')">${category.rushNum}</a></td>
                                    <td><a style="color:#515A6E" href="javascript:void(0);" onclick="editSelectArea('${areaCounty.areaId}','${category.productCategoryId}','${category.productCategoryName}')">${category.autoNum}</a></td>
                                    <td><a style="color:#515A6E" href="javascript:void(0);" onclick="editSelectArea('${areaCounty.areaId}','${category.productCategoryId}','${category.productCategoryName}')">${category.keFuNum}</a></td>
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
</body>
</html>

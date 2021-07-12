<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域半径设定管理</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="${ctx}/md/planradius/list?area.id=${planRadius.area.id}">区域半径列表</a></li>
    <shiro:hasPermission name="md:planradius:edit">
        <li><a href="${ctx}/md/planradius/form?area.id=${planRadius.area.id}">区域半径添加</a></li>
    </shiro:hasPermission>
</ul>
<form:form id="searchForm" modelAttribute="planRadius" action="${ctx}/md/planradius/list" method="POST" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <input id="area.Id" name="area.id" type="hidden" value="${planRadius.area.id}" />
</form:form>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover table-hover">
    <thead>
    <tr>
    <tr>
        <th>序号</th>
        <th>区域名称</th>
        <th>第一半径(千米)</th>
        <th>第二半径(千米)</th>
        <th>第三半径(千米)</th>
        <th>备注</th>
        <shiro:hasPermission name="md:planradius:edit">
            <th>操作</th>
        </shiro:hasPermission></tr>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="model">
        <c:set var="index" value="${index+1}" />
        <tr id="${model.id}">
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${model.area.name}</td>
            <td>${model.radius1}</td>
            <td>${model.radius2}</td>
            <td>${model.radius3}</td>
            <td>${model.remarks}</td>
            <shiro:hasPermission name="md:planradius:edit">
                <td>
                    <a href="${ctx}/md/planradius/form?id=${model.id}&area.id=${model.area.id}">修改</a>
                    <c:choose>
                        <c:when test="${model.delFlag==0}">
                            <a href="${ctx}/md/planradius/stop?id=${model.id}"
                               onclick="return confirmx('要停用区域半径设置吗？', this.href)">停用</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${ctx}/md/planradius/enable?id=${model.id}"
                               onclick="return confirmx('要启用区域半径设置吗？', this.href)">启用</a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>

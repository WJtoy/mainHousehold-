<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点服务点管理</title>
    <meta name="decorator" content="default" />
    <%@include file="/WEB-INF/views/include/dialog.jsp"%>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <style type="text/css">
        .sort {color: #0663A2;cursor: pointer;}
        .form-horizontal .control-label {width: 70px;}
        .form-horizontal .controls { margin-left: 80px;}
        .form-search .ul-form li label {width: auto;}
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        top.layer.closeAll();
        $(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});
        });

        function pointSelect_callback(data){
            $("[id^='servicePoint.servicePointNo']").val(data.servicePointNo);
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:;">网点服务点列表</a></li>
    <shiro:hasPermission name="md:servicepointstation:edit">
        <li><a href="${ctx}/md/servicepointstation/form?servicePoint.id=${servicePointStation.servicePoint.id}">网点服务点添加</a></li>
    </shiro:hasPermission>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<c:set var="isSystemUser" value="${currentuser.isSystemUser()}" />
<form:form id="searchForm" modelAttribute="servicePointStation" action="${ctx}/md/servicepointstation" method="POST" class="breadcrumb form-search">
    <form:hidden path="firstSearch" />
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <input id="orderBy" name="orderBy" type="hidden" value="${servicepointstation.orderBy}" />
    <ul class="ul-form">
        <li>
            <label>网点编号：</label>
            <form:input path="servicePoint.servicePointNo" readonly="true" htmlEscape="false" class="input-small" maxlength="20" />
        </li>
        <li>
            <label>网点名称：</label>
            <c:choose>
                <c:when test="${isSystemUser}">
                    <md:pointselectlayer id="servicePoint" name="servicePoint.id" value="${servicePointStation.servicePoint.id}" labelName="servicePoint.name" labelValue="${servicePointStation.servicePoint.name}"
                                         width="1200" height="780" callbackmethod="pointSelect_callback" title="选择网点" areaId="" cssClass="required"/>
                </c:when>
                <c:otherwise>
                    <form:input path="servicePoint.name" readonly="true" htmlEscape="false" class="input-small" />
                    <form:hidden path="servicePoint.id" />
                </c:otherwise>
            </c:choose>
        </li>
        <li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" /></li>
        <li class="clearfix"></li>
    </ul>
</form:form>
<sys:message content="${message}" />
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover table-hover">
    <thead>
    <tr>
        <th width="55">序号</th>
        <th width="100">网点编号</th>
        <th>网点名称</th>
        <th width="260">服务点名称</th>
        <th width="160">区域</th>
        <th width="310">详细地址</th>
        <th width="70">经度</th>
        <th width="70">纬度</th>
        <c:if test="${isSystemUser}">
            <th width="130">备注</th>
        </c:if>
        <th width="160">操作</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="model">
        <c:set var="index" value="${index+1}" />
        <tr id="${model.id}">
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${model.servicePoint.servicePointNo}</td>
            <td>${model.servicePoint.name}</td>
            <td>${model.name}</td>
            <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${model.area.fullName}">${fns:abbr(model.area.fullName,25)}</a></td>
            <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${model.address}">${fns:abbr(model.address,30)}</a></td>
            <td>${model.longtitude}</td>
            <td>${model.latitude}</td>
            <c:if test="${isSystemUser}">
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${model.remarks}">${fns:abbr(model.remarks,20)}</a></td>
            </c:if>
            <td>
                <shiro:hasPermission name="md:servicepointstation:edit">
                    <a href="${ctx}/md/servicepointstation/form?id=${model.id}&servicePointId=${model.servicePoint.id}">修改</a>
                </shiro:hasPermission>
                <shiro:hasPermission name="md:servicepointstation:stop">
                    <c:choose>
                        <c:when test="${model.delFlag==0}">
                            <a href="${ctx}/md/servicepointstation/delete?id=${model.id}&servicePointId=${model.servicePoint.id}" onclick="return layerConfirmx('确认要停用该服务点吗？', this.href)">停用</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${ctx}/md/servicepointstation/enable?id=${model.id}&servicePointId=${model.servicePoint.id}" onclick="return layerConfirmx('确认要启用该服务点吗？', this.href)">启用</a>
                        </c:otherwise>
                    </c:choose>
                </shiro:hasPermission>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>


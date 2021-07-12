<%@ page import="com.kkl.kklplus.entity.b2b.common.B2BActionType" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>网点资料管理</title>
	<meta name="decorator" content="default" />
	<%@include file="/WEB-INF/views/include/dialog.jsp"%>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<%@include file="/WEB-INF/views/include/treeview.jsp"%>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<style type="text/css">
		.table thead th,.table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
		}
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
			top.layer.closeAll();
			$(document).ready(function() {
				 $('a[data-toggle=tooltip]').darkTooltip();
				 $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});
			});

		</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">网点资料管理</a></li>
        <li><a href="${ctx}/tmall/md/servicepoint/serviceStoreCoverServiceList">覆盖服务管理</a></li>
        <li><a href="${ctx}/tmall/md/servicepoint/serviceStoreCapacityList">网点容量管理</a></li>
        <li><a href="${ctx}/tmall/md/servicepoint/workerList">网点师傅管理</a></li>
		<li><a href="${ctx}/tmall/md/servicepoint/servicePointBatchProcessList">网点批处理</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="mdB2bTmall" action="${ctx}/tmall/md/servicepoint/serviceStoreList" method="POST" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
		<ul class="ul-form">
			<li>
				<label>网点编号：</label>
				<form:input path="servicePoint.servicePointNo" htmlEscape="false" maxlength="20"    class="input-small" />
			</li>
			<li>
				<label>网点名称：</label>
				<form:input path="servicePoint.name" htmlEscape="false" maxlength="30"	class="input-small" />
			</li>
			<li>
				<label>联系方式：</label>
				<form:input path="servicePoint.contactInfo1" htmlEscape="false" maxlength="20" class="input-small" />
			</li>
			<li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" /></li>
			<li class="clearfix"></li>
		</ul>
	</form:form>
	<sys:message content="${message}" />
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th>序号</th>
				<th>网点编号</th>
				<th>名称</th>
				<th>手机</th>
				<th>详细地址</th>
				<shiro:hasPermission name="md:servicepoint:edit">
					<th>网点资料管理</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
        <c:set var="ACTION_TYPE_CREATE" value="<%=B2BActionType.ACTION_TYPE_CREATE.value%>"/>
        <c:set var="ACTION_TYPE_UPDATE" value="<%=B2BActionType.ACTION_TYPE_UPDATE.value%>"/>
        <c:set var="ACTION_TYPE_DELETE" value="<%=B2BActionType.ACTION_TYPE_DELETE.value%>"/>
			<c:forEach items="${page.list}" var="item">
				<c:set var="index" value="${index+1}" />
				<tr id="${item.servicePoint.id}">
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<td>${item.servicePoint.servicePointNo}</td>
					<td>${item.servicePoint.name}</td>
					<td>${item.servicePoint.contactInfo1}</td>
					<td>${item.servicePoint.address}</td>
					<td>
						<a class="btn btn-primary" href="${ctx}/tmall/md/servicepoint/processServiceStore?id=${item.id}">同步 -
                            <c:choose>
                                <c:when test="${item.actionType == ACTION_TYPE_CREATE}">
                                    新增
                                </c:when>
                                <c:when test="${item.actionType == ACTION_TYPE_UPDATE}">
                                    更新
                                </c:when>
                                <c:when test="${item.actionType == ACTION_TYPE_DELETE}">
                                    删除
                                </c:when>
                            </c:choose>
                        </a>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>

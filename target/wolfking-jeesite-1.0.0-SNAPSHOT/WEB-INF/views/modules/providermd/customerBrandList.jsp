<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客户品牌</title>
	<meta about="客户品牌(微服务md)" />
	<meta name="decorator" content="default" />
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
</head>

<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:customerbrand:edit">
			<li><a href="${ctx}/provider/md/customerBrand/form">添加</a></li>
		</shiro:hasPermission>
	</ul>
	<c:set var="currentuser" value="${fns:getUser()}"/>
	<form:form id="searchForm" modelAttribute="customerBrand" action="${ctx}/provider/md/customerBrand/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />
		<c:choose>
			<c:when test="${currentuser.isCustomer()==true}">
			</c:when>
			<c:when test="${currentuser.isSaleman()==true}">
				&nbsp;
				<label>客户：</label>
				<select id="customerId" name="customerId" class="input-small" style="width:250px;">
					<c:forEach items="${fns:getMyCustomerList()}" var="dict">
						<option value="${dict.id}"
								<c:out value="${(customerBrand.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
					</c:forEach>
				</select>
			</c:when>
			<c:otherwise>
				&nbsp;
				<label>客户：</label>
				<select id="customerId" name="customerId" class="input-small" style="width:250px;">
					<option value=""
							<c:out value="${(empty customerBrand.customerId)?'selected=selected':''}" />>所有</option>
					<c:forEach items="${fns:getMyCustomerList()}" var="dict">
						<option value="${dict.id}"
								<c:out value="${(customerBrand.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
					</c:forEach>
				</select>
			</c:otherwise>
		</c:choose>
		&nbsp;
		<label>品牌:</label>
		<form:input path="brandName" htmlEscape="false" maxlength="50" class="input-small"/>
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}" />
	<table id="contentTable"
		class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="30">序号</th>
                <c:if test="${currentuser.isCustomer()==false}">
				<th width="250">客户</th>
				</c:if>
				<th width="150">品牌</th>
				<th>备注</th>
				<th width="100">排序</th>
				<shiro:hasPermission name="md:customerbrand:edit">
					<th width="200">操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
			<c:set var="index" value="0"></c:set>
			<c:forEach items="${page.list}" var="entity">
				<tr>
					<c:set var="index" value="${index+1}"></c:set>
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<c:if test="${currentuser.isCustomer()==false}">
					<td>${entity.customerName}</td>
					</c:if>
					<td>${entity.brandName}</td>
					<td>
					<c:choose>
						<c:when test="${fn:length(entity.remarks)>40}">
							<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,80)}</a>
						</c:when>
						<c:otherwise>
							${entity.remarks}
						</c:otherwise>
					</c:choose>
					</td>
					<td>${entity.sort}</td>
					<shiro:hasPermission name="md:customerbrand:edit">
						<td><a href="${ctx}/provider/md/customerBrand/form?id=${entity.id}&customerName=${fns:urlEncode(entity.customerName)}&brandName=${fns:urlEncode(entity.brandName)}">修改</a>
							<a style="margin-left: 6px;" href="${ctx}/provider/md/customerBrand/delete?id=${entity.id}&customerId=${entity.customerId}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
						</td>
					</shiro:hasPermission>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>

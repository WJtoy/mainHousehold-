<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>B2B对接系统客户与客户</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:b2bcustomer:edit"><li><a href="${ctx}/tmall/md/b2bcustomer/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="b2bCustomerMap" action="${ctx}/tmall/md/b2bcustomer" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>客户：</label>
		<form:select path="customerId" cssStyle="width: 200px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"></form:options>
		</form:select>
		&nbsp;
		<label>数据源客户ID：</label><form:input path="shopId" htmlEscape="false" maxlength="20" class="input-small"/>
		&nbsp;
		<label>数据源客户名称：</label><form:input path="shopName" htmlEscape="false" maxlength="20" class="input-large"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">数据源</th>
				<th width="100">数据源客户ID</th>
				<th width="200">数据源客户名称</th>
				<th width="300">客户名称</th>
				<th>备注</th>
				<shiro:hasPermission name="md:b2bproduct:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${fns:getDictLabelFromMS(entity.dataSource,'order_data_source','Unknow')}</td>
				<td>${entity.shopId}</td>
				<td>${entity.shopName}</td>
				<td>${entity.customerName}</td>
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
				<shiro:hasPermission name="md:b2bcustomer:edit"><td>
    				<a href="${ctx}/tmall/md/b2bcustomer/form?id=${entity.id}&customerName=${fns:urlEncode(entity.customerName)}">修改</a>
					<a href="${ctx}/tmall/md/b2bcustomer/delete?id=${entity.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
	<script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
        });
	</script>
  </body>
</html>

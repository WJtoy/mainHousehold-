<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>品牌</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">品牌列表</a></li>
		<shiro:hasPermission name="md:brand:edit"><li><a href="${ctx}/md/brand/form">品牌添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="brand" action="${ctx}/md/brand" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>编码：</label><form:input path="code" htmlEscape="false" maxlength="10" class="input-small"/>
		&nbsp;
		<label>名称 ：</label><form:input path="name" htmlEscape="false" maxlength="20" class="input-small"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">编码</th>
				<th width="300">名称</th>
				<th>描述</th>
				<shiro:hasPermission name="md:brand:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="brand">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${brand.code}</td>
				<td>
					<a href="${ctx}/md/brand/form?id=${brand.id}">${brand.name}</a>
				</td>
				<td>
					<c:choose>
						<c:when test="${fn:length(brand.remarks)>40}">
							<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${brand.remarks}">${fns:abbr(brand.remarks,80)}</a>
						</c:when>
						<c:otherwise>
							${brand.remarks}
						</c:otherwise>
					</c:choose>
				</td>
				<shiro:hasPermission name="md:brand:edit"><td>
    				<a href="${ctx}/md/brand/form?id=${brand.id}">修改</a>
					<a href="${ctx}/md/brand/delete?id=${brand.id}" onclick="return confirmx('确认要删除 [${brand.name}] 产品分类吗？', this.href)">删除</a>
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

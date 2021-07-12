<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
  <head>
   <title>客户跟踪项目</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/head.jsp" %>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
  </head>
  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">客户跟踪项目列表</a></li>
		<shiro:hasPermission name="md:trackingtype:edit"><li><a href="${ctx}/md/trackingtype/form">客户跟踪项目添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="dict" action="${ctx}/md/trackingtype" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>跟踪项目：</label><form:input path="label" htmlEscape="false" maxlength="50" class="input-small"/>&nbsp;
		<input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询"/>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead><tr><th>序号</th><th>跟踪项目</th><th>描述</th><th>排序</th><shiro:hasPermission name="md:trackingtype:edit"><th>操作</th></shiro:hasPermission></tr></thead>
		<tbody>
		<c:forEach items="${page.list}" var="dict">
			<c:set var="index" value="${index+1}" />
			<tr>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${dict.label}</td>
				<td>${dict.description}</td>
				<td>${dict.sort}</td>
				<shiro:hasPermission name="md:trackingtype:edit"><td>
    				<a href="${ctx}/md/trackingtype/form?id=${dict.id}">修改</a>
					<a href="${ctx}/md/trackingtype/delete?id=${dict.id}" onclick="return confirmx('确认要删除该客户跟踪项目吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>

<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品图片定义</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:productpic:edit"><li><a href="${ctx}/md/product/pic/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="entity" action="${ctx}/md/product/pic" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>产品名称 ：</label><form:input path="product.name" htmlEscape="false" maxlength="20" class="input-medium"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">产品名称</th>
				<th width="300"></th>
				<shiro:hasPermission name="md:productpic:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="vmodel">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>
					<a href="${ctx}/md/product/pic/form?id=${vmodel.id}&productId=${vmodel.product.id}">${vmodel.product.name}</a>
				</td>
				<td>
					<c:forEach items="${vmodel.items}" var="vitem" varStatus="i" begin="0">
						<input type="radio" checked="checked" name="rad-${index}-${i.index}" /> ${vitem.title}${vitem.mustFlag==1?'(必须上传)':'(可选)'} &nbsp;&nbsp;
					</c:forEach>
				</td>
				<shiro:hasPermission name="md:productpic:edit"><td>
    				<a href="${ctx}/md/product/pic/form?id=${vmodel.id}&productId=${vmodel.product.id}">修改</a>
					<a href="${ctx}/md/product/pic/delete?id=${vmodel.id}&productId=${vmodel.product.id}" onclick="return confirmx('确认要删除 [${vmodel.product.name}] 产品的配置吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>

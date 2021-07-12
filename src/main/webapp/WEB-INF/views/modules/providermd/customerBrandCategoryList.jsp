<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户品牌产品分类</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:customerbrandcategory:edit"><li><a href="${ctx}/provider/md/customerBrandCategory/form">添加</a></li></shiro:hasPermission>
	</ul>
	<c:set var="currentuser" value="${fns:getUser()}"/>
	<form:form id="searchForm" modelAttribute="customerBrandCategory" action="${ctx}/provider/md/customerBrandCategory/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<c:choose>
			<c:when test="${currentuser.isCustomer()==true}">
			</c:when>
			<c:when test="${currentuser.isSaleman()==true}">
				&nbsp;
				<label>客户：</label>
				<select id="customerId" name="customerId" class="input-small" style="width:250px;">
					<c:forEach items="${fns:getMyCustomerList()}" var="dict">
						<option value="${dict.id}"
								<c:out value="${(customerBrandCategory.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
					</c:forEach>
				</select>
			</c:when>
			<c:otherwise>
				&nbsp;
				<label>客户：</label>
				<select id="customerId" name="customerId" class="input-small" style="width:250px;">
					<option value=""
							<c:out value="${(empty customerBrandCategory.customerId)?'selected=selected':''}" />>所有</option>
					<c:forEach items="${fns:getMyCustomerList()}" var="dict">
						<option value="${dict.id}"
								<c:out value="${(customerBrandCategory.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
					</c:forEach>
				</select>
			</c:otherwise>
		</c:choose>
		&nbsp;
		<label>品牌：</label>
		<form:select path="brandId" cssClass="input-small" cssStyle="width:200px;">
			<form:option value="" label="所有"/>
			<form:options items="${customerBrandList}" itemLabel="brandName" itemValue="id" htmlEscape="false"/>
		</form:select>
		&nbsp;
		<label>产品：</label>
		<form:select path="productId" cssClass="input-small" cssStyle="width:200px;">
			<form:option value="" label="所有"/>
			<form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
		</form:select>
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<c:if test="${currentuser.isCustomer()==false}">
					<th width="250">客户</th>
				</c:if>
				<th>品牌名称</th>
				<th>产品名称</th>
				<shiro:hasPermission name="md:customerbrandcategory:edit">
                    <th width="100">操作</th>
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
				<td>
					<a href="${ctx}/provider/md/customerBrandCategory/form?id=${entity.id}&customerId=${entity.customerId}&customerName=${fns:urlEncode(entity.customerName)}&brandId=${entity.brandId}&brandName=${entity.brandName}">${entity.brandName}</a>
				</td>
				<td>${entity.productName}</td>
				<shiro:hasPermission name="md:customerbrandcategory:edit">
                <td>
					<a href="${ctx}/provider/md/customerBrandCategory/form?id=${entity.id}&customerId=${entity.customerId}&customerName=${fns:urlEncode(entity.customerName)}&brandId=${entity.brandId}&brandName=${entity.brandName}">修改</a>
                    <%--<a href="${ctx}/md/customerbrandcategory/form?categoryId=${entity.category.id}">修改</a>--%>
                    <a href="${ctx}/provider/md/customerBrandCategory/delete?id=${entity.id}&customerId=${entity.customerId}&productCategoryId=${entity.productCategoryId}&productId=${entity.productId}&brandId=${entity.brandId}" onclick="return confirmx('确认要删除吗？', this.href)" style="margin-left: 10px;">删除</a>
                </td>
                </shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>

<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>B2B客户料号</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

	  <script type="text/javascript">
          //覆盖分页前方法
          function beforePage() {
              var $btnSubmit = $("#btnSubmit");
              $btnSubmit.attr('disabled', 'disabled');
              $("#btnClearSearch").attr('disabled', 'disabled');
              layerLoading("查询中...", true);
          }

          var clicktag = 0;
          $(document).on("click", "#btnSubmit", function () {
              if (clicktag == 0) {
                  clicktag = 1;
                  beforePage();
                  setPage();
                  this.form.submit();
              }
          });

	  </script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:auxiliarymaterialitem:edit"><li><a href="${ctx}/provider/md/auxiliaryMaterialItem/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="auxiliaryMaterialItem" action="${ctx}/provider/md/auxiliaryMaterialItem/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>分类：</label>
		<form:select path="category.id" cssStyle="width: 200px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${auxiliaryMaterialCategoryList}" itemLabel="name" itemValue="id"></form:options>
		</form:select>
        &nbsp;
        <label>产品名称：</label>
        <form:select path="productId" cssStyle="width: 200px;">
            <form:option value="" label="请选择"></form:option>
            <form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id"></form:options>
        </form:select>
		&nbsp;
		<label>项目名称:</label><form:input path="name" htmlEscape="false" maxlength="30" class="input-small"/>&nbsp;
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="250">分类名称</th>
				<th width="200">项目名称</th>
				<th width="200">产品</th>
				<th width="100">金额</th>
				<th width="100">价格类别</th>
				<th width="100">单位</th>
				<th>备注</th>
				<shiro:hasPermission name="md:auxiliarymaterialitem:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${entity.category.name}</td>
				<td>
                    <a href="${ctx}/provider/md/auxiliaryMaterialItem/form?id=${entity.id}">${entity.name}</a>
                </td>
				<td>${entity.productName}</td>
				<td>${entity.price}</td>
				<c:choose>
					<c:when test="${entity.type==0}">
						<td>固定价格</td>
					</c:when>
					<c:when test="${entity.type==1}">
						<td>自定义价格</td>
					</c:when>
					<c:otherwise>
						<td>未知</td>
					</c:otherwise>
				</c:choose>
				<td>${entity.unit}</td>
				<td>
					<c:choose>
						<c:when test="${fn:length(entity.remarks)>20}">
							<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,28)}</a>
						</c:when>
						<c:otherwise>
							${entity.remarks}
						</c:otherwise>
					</c:choose>
				</td>
				<shiro:hasPermission name="md:auxiliarymaterialitem:edit"><td>
    				<a href="${ctx}/provider/md/auxiliaryMaterialItem/form?id=${entity.id}">修改</a>
					<a href="${ctx}/provider/md/auxiliaryMaterialItem/delete?id=${entity.id}&productId=${entity.productId}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
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

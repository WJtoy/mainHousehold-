<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户配件</title>
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
		<li class="active"><a href="javascript:void(0);">客户配件</a></li>
		<shiro:hasPermission name="md:materialcategory:edit"><li><a href="${ctx}/md/materialCategory/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="materialCategory" action="${ctx}/md/materialCategory/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>配件类别名称:</label><form:input path="name" htmlEscape="false" maxlength="30" class="input-small"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="200">配件类别名称</th>
				<th>描述</th>
				<shiro:hasPermission name="md:materialcategory:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>
					<a href="${ctx}/md/materialCategory/form?id=${entity.id}">${entity.name}</a>
				</td>
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
				<shiro:hasPermission name="md:materialcategory:edit"><td>
    				<a href="${ctx}/md/materialCategory/form?id=${entity.id}">修改</a>
					<a href="${ctx}/md/materialCategory/delete?id=${entity.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
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

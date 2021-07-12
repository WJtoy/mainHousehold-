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
		<shiro:hasPermission name="md:b2bservicefeecategory:edit"><li><a href="${ctx}/b2bcenter/md/serviceFeeCategory/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="serviceFeeCategory" action="${ctx}/b2bcenter/md/serviceFeeCategory/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <label>数据源:</label>
        <form:select path="dataSource" cssStyle="width: 220px;">
            <form:option value="" label="请选择"></form:option>
            <form:options items="${fns:getDictListFromMS('order_data_source')}"
                          itemLabel="label" itemValue="value" />
        </form:select> &nbsp;
		<label>分类名称:</label><form:input path="categoryName" htmlEscape="false" maxlength="30" class="input-small"/>&nbsp;
		<label>类型:</label>
	    <select name="type" style="width:225px;">
            <c:choose>
                <c:when test="${serviceFeeCategory.type==1}">
                    <option value="">请选择</option>
                    <option value="1" selected>关联产品</option>
                    <option value="2">关联工单</option>
                </c:when>
                <c:when test="${serviceFeeCategory.type==2}">
                    <option value="">请选择</option>
                    <option value="1">关联产品</option>
                    <option value="2" selected>关联工单</option>
                </c:when>
                <c:otherwise>
                    <option value="" selected>请选择</option>
                    <option value="1">关联产品</option>
                    <option value="2">关联工单</option>
                </c:otherwise>
            </c:choose>

		</select>

		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
                <th width="100">数据源</th>
				<th width="250">分类名称</th>
				<th width="200">类型</th>
				<th>备注</th>
				<shiro:hasPermission name="md:b2bservicefeecategory:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${fns:getDictLabelFromMS(entity.dataSource,'order_data_source','Unknow')}</td>
				<td>
                    <a href="${ctx}/b2bcenter/md/serviceFeeCategory/form?id=${entity.id}">${entity.categoryName}</a>
                </td>
				<c:choose>
					<c:when test="${entity.type==1}">
						<td>关联产品</td>
					</c:when>
					<c:when test="${entity.type==2}">
						<td>关联工单</td>
					</c:when>
					<c:otherwise>
						<td>未知类型</td>
					</c:otherwise>
				</c:choose>
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
				<shiro:hasPermission name="md:b2bservicefeecategory:edit"><td>
    				<a href="${ctx}/b2bcenter/md/serviceFeeCategory/form?id=${entity.id}">修改</a>
					<a href="${ctx}/b2bcenter/md/serviceFeeCategory/delete?id=${entity.id}&dataSource=${entity.dataSource}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
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

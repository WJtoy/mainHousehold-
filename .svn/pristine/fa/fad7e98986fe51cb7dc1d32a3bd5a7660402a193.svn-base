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
		<shiro:hasPermission name="md:b2bcanceltype:edit"><li><a href="${ctx}/b2bcenter/md/cancelType/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="cancelTypeMapping" action="${ctx}/b2bcenter/md/cancelType/getList" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>数据源:</label>
		<form:select path="dataSource" cssStyle="width: 220px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
						  itemLabel="label" itemValue="value" />
		</form:select> &nbsp;
		<label>快可立取消原因:</label>
		<form:select path="cancelCode" cssStyle="width: 220px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getDictExceptListFromMS('cancel_responsible',1)}"
						  itemLabel="label" itemValue="value" />
		</form:select>&nbsp;

		<label>B2B取消类型code:</label><form:input path="b2bCancelCode" htmlEscape="false" maxlength="50" class="input-small"/>

		<%--<label>B2B服务类型名称：</label><form:input path="b2bServiceTypeName" htmlEscape="false" maxlength="50" class="input-small"/>

		<label>B2B服务类型名称Code：</label><form:input path="b2bServiceTypeCode" htmlEscape="false" maxlength="50" class="input-small"/>
		&nbsp;
		<label>服务类型名称：</label>
		<form:select path="serviceTypeId" cssStyle="width: 200px;">
		   <form:option value="" label="请选择"></form:option>
		   <form:options items="${fns:getServiceTypes()}" itemLabel="name" itemValue="id"></form:options>
	    </form:select>--%>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">数据源</th>
				<th width="200">取消类型名称</th>
				<th width="200">B2B取消类型code</th>
				<th width="300">B2B取消类型名称</th>
				<th>备注</th>
				<shiro:hasPermission name="md:b2bcanceltype:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${fns:getDictLabelFromMS(entity.dataSource,'order_data_source','Unknow')}</td>
				<td>${fns:getDictLabelFromMS(entity.cancelCode,'cancel_responsible','Unknow')}</td>
				<td>${entity.b2bCancelCode}</td>
				<td>${entity.b2bCancelName}</td>
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
				<shiro:hasPermission name="md:b2bcanceltype:edit"><td>
    				<a href="${ctx}/b2bcenter/md/cancelType/form?id=${entity.id}">修改</a>
					<a href="${ctx}/b2bcenter/md/cancelType/delete?id=${entity.id}&dataSource=${entity.dataSource}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
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

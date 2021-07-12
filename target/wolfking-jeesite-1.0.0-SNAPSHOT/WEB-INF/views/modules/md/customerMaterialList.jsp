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
            var isSaleman = $("#idSale").val();
            if(isSaleman=="true"){
                var val = $("#customer\\.id").val();
                if (val == undefined || val.length == 0) {
                    layerInfo("请选择客户!", "信息提示");
                    return false;
                }
            }
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...", true);
            return true;
        }
        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                var result = beforePage();
                if(!result){
                    clicktag = 0;
                    return false;
                }
                setPage();
                this.form.submit();
            }
        });
	</script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">客户配件</a></li>
		<shiro:hasPermission name="md:customermaterial:edit"><li><a href="${ctx}/md/customerMaterial/form">客户配件添加</a></li></shiro:hasPermission>
	</ul>
	<c:set var="currentuser" value="${fns:getUser()}"/>
	<input type="hidden" id="idSale" value="${currentuser.isSaleman()}">
	<form:form id="searchForm" modelAttribute="customerMaterial" action="${ctx}/md/customerMaterial/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<c:if test="${currentuser.isCustomer() == false}">
			<label>客户：</label>
			<form:select path="customer.id" cssStyle="width: 200px;">
				<form:option value="" label="请选择"></form:option>
				<form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id"></form:options>
			</form:select>
			&nbsp;
		</c:if>
		<label>产品名称：</label>
		<form:select path="product.id" cssStyle="width: 200px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id"></form:options>
		</form:select>
		&nbsp;
		<label>配件名称：</label>
		<form:select path="material.id" cssStyle="width: 200px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${materialList}" itemLabel="name" itemValue="id"></form:options>
		</form:select>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
                <c:if test="${currentuser.isCustomer()==false}">
					<th width="200">客户名称</th>
				</c:if>
				<th width="160">产品名称</th>
				<th width="160">配件名称</th>
				<th width="80">是否反件</th>
				<th width="80">价格</th>
				<th>描述</th>
				<shiro:hasPermission name="md:customermaterial:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<c:if test="${currentuser.isCustomer()==false}">
					<td>${entity.customer.name}</td>
				</c:if>
				<td>${entity.product.name}</td>
				<td>
					<a href="${ctx}/md/customerMaterial/form?id=${entity.id}">${entity.material.name}</a>
				</td>
				<c:choose>
					<c:when test="${entity.isReturn==1}">
					   <td><span class="label label-important">是</span></td>
					</c:when>
					<c:otherwise>
						<td>否</td>
					</c:otherwise>
				</c:choose>
				<td>${entity.price}</td>
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
				<shiro:hasPermission name="md:customermaterial:edit"><td>
    				<a href="${ctx}/md/customerMaterial/form?id=${entity.id}">修改</a>
					<a href="${ctx}/md/customerMaterial/delete?id=${entity.id}&customer.id=${entity.customer.id}&product.id=${entity.product.id}&material.id=${entity.material.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
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

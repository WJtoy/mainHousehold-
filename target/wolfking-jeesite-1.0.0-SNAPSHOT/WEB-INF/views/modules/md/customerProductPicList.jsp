<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品图片定义</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$('a[data-toggle=tooltip]').darkTooltip();
			$(document).on('change',"#customer\\.id",function (e) {
				var customerId = $(this).val();
				if (customerId ==null || customerId =='' || customerId == '0') {
					$.ajax({
						url: "${ctx}/md/product/treeData",
						success:function (e) {
							$("#product\\.id").empty();
							var programme_sel=[];
							programme_sel.push('<option value="" selected="selected">请选择</option>')
							for(var i=0,len = e.length;i<len;i++){
								var programme = e[i];
								programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
							}
							$("#product\\.id").append(programme_sel.join(' '));
							$("#product\\.id").val("");
							$("#product\\.id").change();
						},
						error:function (e) {
							layerError("请求产品失败","错误提示");
						}
					});
				} else {
					$.ajax({
						url: "${ctx}/md/product/ajax/customerProductList",
						data: {customerId: customerId},
						success:function (e) {
							if(e.success){
								$("#product\\.id").empty();
								var programme_sel=[];
								programme_sel.push('<option value="" selected="selected">请选择</option>')
								for(var i=0,len = e.data.length;i<len;i++){
									var programme = e.data[i];
									programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
								}
								$("#product\\.id").append(programme_sel.join(' '));
								$("#product\\.id").val("");
								$("#product\\.id").change();
							}else {
								$("#product\\.id").html('<option value="" selected>请选择</option>');
								layerMsg('该客户还没有配置产品！');
							}
						},
						error:function (e) {
							layerError("请求产品失败","错误提示");
						}
					});
				}
			});
		});
	</script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:customerpic:edit"><li><a href="${ctx}/md/customer/pic/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="entity" action="${ctx}/md/customer/pic" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>客户:</label>
		<form:select path="customer.id" style="width:318px;">
			<form:option value="0" label="所有"/>
			<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"
						  htmlEscape="false"/>
		</form:select>
		<label>产品名称 ：</label>
		<form:select path="product.id" style="width:318px;">
			<form:option value="0" label="所有"/>
			<form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
	    </form:select>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">客户</th>
				<th width="100">产品名称</th>
				<th width="300"></th>
				<th width="50">是否上传条码</th>
				<shiro:hasPermission name="md:customerpic:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="vmodel">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${vmodel.customer.name}</td>
				<td>
					<a href="${ctx}/md/customer/pic/form?id=${vmodel.id}&productId=${vmodel.product.id}&customerId=${vmodel.customer.id}&barcodeMustFlag=${vmodel.barcodeMustFlag}">${vmodel.product.name}</a>
				</td>
				<td>
					<c:forEach items="${vmodel.items}" var="vitem" varStatus="i" begin="0">
						<input type="radio" checked="checked" name="rad-${index}-${i.index}" /> ${vitem.title}${vitem.mustFlag==1?'(必须上传)':'(可选)'} &nbsp;&nbsp;
					</c:forEach>
				</td>
				<td>
					<c:choose>
						<c:when test="${vmodel.barcodeMustFlag == 0}">
							否
						</c:when>
						<c:when test="${vmodel.barcodeMustFlag == 1}">
							是
						</c:when>
						<c:otherwise>
						</c:otherwise>
					</c:choose>
				</td>
				<shiro:hasPermission name="md:customerpic:edit">
					<td>
					<c:choose>
						<c:when test="${empty vmodel.id}">
							<a href="${ctx}/md/customer/pic/form?productId=${vmodel.product.id}&customerId=${vmodel.customer.id}&barcodeMustFlag=${vmodel.barcodeMustFlag}" style="color:red;">添加</a>
						</c:when>
						<c:otherwise>
							<a href="${ctx}/md/customer/pic/form?id=${vmodel.id}&productId=${vmodel.product.id}&customerId=${vmodel.customer.id}&barcodeMustFlag=${vmodel.barcodeMustFlag}">修改</a>
						</c:otherwise>
					</c:choose>
					<a href="${ctx}/md/customer/pic/delete?id=${vmodel.id}&productId=${vmodel.product.id}&customerId=${vmodel.customer.id}" onclick="return confirmx('确认要删除 [${vmodel.product.name}] 产品的配置吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>

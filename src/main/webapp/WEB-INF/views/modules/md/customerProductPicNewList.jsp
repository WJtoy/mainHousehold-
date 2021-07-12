<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <title>客户产品图片定义</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	  <style type="text/css">
		  .required{
		  	background-color: #F2FBFF;color: #0096DA;padding: 2px 4px;border-radius: 3px
		  }
		  .choosable{
			  background-color: #FDF5E9;color: #FF9502;padding: 2px 4px;border-radius: 3px
		  }
		  .table thead th, .table tbody td {
			  text-align: center;
			  vertical-align: middle;
			  BackColor: Transparent;
			  height: 32px;
		  }
	  </style>
	<script type="text/javascript">
		$(document).ready(function() {
			$('a[data-toggle=tooltip]').darkTooltip();
			$("#contentTable").treeTable({expandLevel : 5});

			//覆盖分页前方法
			function beforePage() {
				var val = $("#customer\\.id").val();
				console.log(val);
				if (val == undefined || val == 0) {
					layerInfo("请选择客户!", "信息提示");
					return false;
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

				$.ajax({
					url: "${ctx}/md/customer/pic/ajax/customerProductCategoryList",
					data: {customerId: customerId},
					success:function (e) {
						if(e.success){
							$("#productCategoryId").empty();
							var programme_sel=[];
							programme_sel.push('<option value="" selected="selected">请选择</option>')
							for(var i=0,len = e.data.length;i<len;i++){
								var programme = e.data[i];
								programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
							}
							$("#productCategoryId").append(programme_sel.join(' '));
							$("#productCategoryId").val("");
							$("#productCategoryId").change();
						}else {
							$("#productCategoryId").html('<option value="" selected>请选择</option>');
							layerMsg('该客户还没有配置产品品类！');
						}
					},
					error:function (e) {
						layerError("请求产品品类失败","错误提示");
					}
				});
			});

			$(document).on("change","#productCategoryId",function (e) {
				var productCategoryId = $(this).val();
				var customerId = $("#customer\\.id").val();
				if (customerId !='') {
					if (productCategoryId != "") {
						$.ajax({
							url: "${ctx}/md/customer/pic/ajax/getProductCategoryProductList",
							data: {customerId : customerId,productCategoryId: productCategoryId},
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
				}else {
					layerMsg("请先选择客户");
				}
			});
		});

		function editCompletePic(type,id,productId,customerId,barcodeMustFlag) {
			var text = "添加完工信息";
			var url = "${ctx}/md/customer/pic/form";
			var area = ['1000px', '658px'];
			if(type == 10){
				url = "${ctx}/md/customer/pic/form?productId=" + productId + "&customerId=" + customerId + "&barcodeMustFlag" + barcodeMustFlag;
			}
			if(type == 20){
				text = "修改完工信息";
				url = "${ctx}/md/customer/pic/form?id="+ id + "&productId=" + productId + "&customerId=" + customerId + "&barcodeMustFlag=" + barcodeMustFlag;
			}
			top.layer.open({
				type: 2,
				id:"completePic",
				zIndex:19,
				title:text,
				content: url,
				area: area,
				shade: 0.3,
				maxmin: false,
				success: function(layero,index){
				},
				end:function(){
				}
			});
		}
	</script>
  </head>

  <body>
  <ul class="nav nav-tabs">
	  <li class="active"><a href="javascript:void(0);">完工信息</a></li>
  </ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<form:form id="searchForm" modelAttribute="entity" action="${ctx}/md/customer/pic" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<c:choose>
			<c:when test="${currentuser.isCustomer()}">
				<li>
					<label style="margin-left: 0px"><span class=" red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
					<form:hidden path="customer.id"/>
					<form:input path="customer.name" readonly="true" style="width:280px;"/>
				</li>
			</c:when>
			<c:otherwise>
				<li>
					<label style="margin-left: 0px"><span class=" red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
					<form:select path="customer.id" class="input-large" style="width:280px;">
						<form:option value="0" label="请选择"/>
						<form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
					</form:select>
				</li>
			</c:otherwise>
		</c:choose>
		&nbsp&nbsp
		<label>产品品类：</label>
		<form:select path="productCategoryId" cssStyle="width: 200px;">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${productCategoryList}" itemLabel="name" itemValue="id"></form:options>
		</form:select>
		&nbsp&nbsp
		<label>产&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp品：</label>
		<form:select path="product.id" style="width:280px;">
			<form:option value="0" label="所有"/>
			<form:options items="${productList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
	    </form:select>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>

	<shiro:hasPermission name="md:customerpic:edit">
		<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 128px;height: 32px" onclick="editCompletePic()">
			<i class="icon-plus-sign"></i>&nbsp;添加完工信息
		</button></shiro:hasPermission>

	<sys:message content="${message}"/>
	<c:set value="" var="customerId"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50" >序号</th>
				<th width="280">产品</th>
				<th width="888">完工信息</th>
				<th width="120">上传条码</th>
				<shiro:hasPermission name="md:customerpic:edit"><th width="120" rowspan="2">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="vmodel">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>
					${vmodel.product.name}
				</td>
				<td style="text-align: left">
					<c:forEach items="${vmodel.items}" var="vitem" varStatus="i" begin="0">
						<input type="radio" checked="checked" name="rad-${index}-${i.index}" /> ${vitem.title}${vitem.mustFlag==1?'(必须上传)':'(可选)'} &nbsp;&nbsp;
					</c:forEach>
				</td>

					<c:choose>
						<c:when test="${vmodel.barcodeMustFlag == 0}">
				<td style="color: red">否</td>
						</c:when>
						<c:when test="${vmodel.barcodeMustFlag == 1}">
				<td>是</td>
						</c:when>
						<c:otherwise>
							<td></td>
						</c:otherwise>
					</c:choose>

				<shiro:hasPermission name="md:customerpic:edit">
					<td>
						<c:choose>
							<c:when test="${empty vmodel.id}">
								<a href="javascript:editCompletePic(10,null,'${vmodel.product.id}','${vmodel.customer.id}','${vmodel.barcodeMustFlag}')" style="color:red;">添加</a>
							</c:when>
							<c:otherwise>
								<a href="javascript:editCompletePic(20,'${vmodel.id}','${vmodel.product.id}','${vmodel.customer.id}','${vmodel.barcodeMustFlag}')">修改</a>
							</c:otherwise>
						</c:choose>
							&nbsp&nbsp
						<a href="${ctx}/md/customer/pic/delete?id=${vmodel.id}&productId=${vmodel.product.id}&customerId=${vmodel.customer.id}" onclick="return confirmx('确认要删除 [${vmodel.product.name}] 的完工信息吗？', this.href)">删除</a>
					</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>

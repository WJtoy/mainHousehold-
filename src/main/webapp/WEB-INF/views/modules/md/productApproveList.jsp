<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>产品</title>
	<meta name="decorator" content="default" />
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		$(document).ready(function()
		{
			$("#treeTable").treeTable(
			{
				expandLevel : 5
			});
		});

		function approve(id, name)
		{
			var submit = function(v, h, f)
			{
				if (v == 'ok')
				{
					top.$.jBox.tip('确认产品' + name + '审核通过', 'loading');

					$(this).attr("disabled", "disabled");

					var data =
					{
						id : id
					};
					$.ajax(
					{
						cache : false,
						type : "POST",
						url : "${ctx}/md/productapprove/approve",
						data : data,
						success : function(data)
						{
                            if(ajaxLogout(data)){
                                return false;
                            }
							if (data.success)
							{
								top.$.jBox.tip('产品' + name + '审核通过', 'success');
								location.href = "${ctx}/md/productapprove";
							} else
							{
								top.$.jBox.closeTip();
								top.$.jBox.error(data.message);
							}
							$(this).removeAttr('disabled');
						},
						error : function(xhr, ajaxOptions, thrownError)
						{
							top.$.jBox.closeTip();
							// top.$.jBox.error(thrownError.toString());
							$(this).removeAttr('disabled');
                            ajaxLogout(e.responseText,null,"产品审核错误，请重试!");
						}
					});
				} else if (v == 'cancel')
				{
					// 取消
				}

				return true; //close
			};

			top.$.jBox.confirm('确认产品' + name + '审核通过吗？', '产品审核', submit);
		}
	</script>
</head>

<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">产品审核列表</a>
		</li>
	</ul>
	<form:form id="searchForm" action="${ctx}/md/productapprove"
		method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />
		<label>客户：</label>
		</label>
		<%--<sys:treeselect id="customer" name="customerId" value="${customerId}"
			labelName="customerName" labelValue="${customerName}" title="客户"
			url="/md/customer/treeData" cssClass="input-small"
			cssStyle="width:150px;" allowClear="true" />--%>
		<select name="customerId" class="input-large">
			<option value="">所有</option>
			<c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
				<option value="${customer.id}"  <c:out value="${customer.id==customerId ?'selected':''}"/>>${customer.name}</option>
			</c:forEach>
		</select>
		&nbsp;
		<label>产品品类：</label>
		<sys:treeselect id="category" name="categoryId" value="${categoryId}" labelName="categoryName" labelValue="${categoryName}"
					title="产品分类" url="/md/productcategory/treeData" cssClass="required"/>
		<label>产品名称：</label>
		<input type="text" id="name" name="name" maxlength="50"
			value="${name }" class="input-small" style="width:80px;" />
		<label>品牌 ：</label>
		<input type="text" id="brand" name="brand" maxlength="50"
			value="${brand }" class="input-small" style="width:80px;" />
		&nbsp;
		<label>型号/规格 ：</label>
		<input type="text" id="model" name="model" maxlength="50"
			value="${model }" class="input-small" style="width:80px;" />&nbsp;
		<input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}" />
	<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50px">序号</th>
				<th>产品品类</th>
				<th>名称</th>
				<th>拼音简称</th>
				<th>型号/规格</th>
				<th>套组</th>
				<th>描述</th>
				<shiro:hasPermission name="md:productapprove:approve">
					<th>操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
			<c:set var="index" value="0"></c:set>
			<c:forEach items="${page.list}" var="product">
				<tr>
					<c:set var="index" value="${index+1}"></c:set>
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<td>${product.category.name}</td>
					<td>${product.name}</td>
					<td>${product.pinYin}</td>
					<td>${product.model}</td>
					<c:choose>
						<c:when test="${product.setFlag==0}">
							<td>否</td>
						</c:when>
						<c:otherwise>
							<td>是</td>
						</c:otherwise>
					</c:choose>
					<td><a href="javascript:void(0);" title="${product.remarks}">${fns:abbr(product.remarks,50)}</a></td>
					<shiro:hasPermission name="md:productapprove:approve">
						<td><input type="Button" id="${product.id}"
							class="btn btn-mini btn-success" value="审核"
							onclick="approve('${product.id}','${product.name}')" /></td>
					</shiro:hasPermission>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>

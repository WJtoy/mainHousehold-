<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品排序</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
	<script type="text/javascript">
		var lock = false;
		$(document).ready(function() {
			$("#value").focus();
			$("#inputForm").validate({
				submitHandler: function(form){
					// var ids = [], nodes = tree.getCheckedNodes(true);
					// for(var i=0; i<nodes.length; i++) {
					// 	ids.push(nodes[i].id);
					// }
					// $("#productIds").val(ids);
					if(lock){
					    return false;
					}
					lock = true;
					var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
					loading('正在提交，请稍等...');
                    //form.attr("action", "${ctx}/md/product/updateSort");
					form.submit();
					return false;
				},
				errorContainer: "#messageBox",
				errorPlacement: function(error, element) {
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
						error.appendTo(element.parent().parent());
					} else {
						error.insertAfter(element);
					}
				}
			});

		});
		
		function refreshProductTree(){
			if($("#setFlag").val()==1){
				$("#divProduct").show();
				$("#productTree").show();
			}else{
				$("#divProduct").hide();
				$("#productTree").hide();
			}
		}
	</script>
  </head>
  
  <body>
 
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/md/product">产品列表</a></li>
		<%--<li><a href="${ctx}/md/product/form">产品添加</a></li>--%>
		<shiro:hasPermission name="md:product:view"><li class="active"><a href="${ctx}/md/product/sort">产品排序</a></li></shiro:hasPermission>
	</ul><br/>
	
	<form:form id="listForm" method="post" modelAttribute="productSortModel" action="${ctx}/md/product/updateSort">
	<sys:message content="${message}"/>
	
	<table id="treeTable" class="table table-striped table-bordered table-condensed" style="width: 800px;">
		<thead>
			<tr>
				<th width="50px">序号</th>
				<th width="150px">产品分类</th>
				<th width="200px">名称</th>
				<th width="200px">型号/规格</th>
				<th width="100px" style="text-align:center;">排序</th>
			</tr>
		</thead>
		<tbody>

		<c:set var="index" value="0"></c:set>
		<c:forEach items="${productList}" var="product">
			<tr >
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index}</td>
				<td>${product.category.name}</td>
				<td>${product.name}</td>
				<td>${product.model}</td>
				<td style="text-align:center;">
						<input type="hidden" name="products[${index-1}].id" value="${product.id}"/>
						<input type="hidden" name="products[${index-1}].name" value="${product.name}"/>
						<input type="hidden" name="products[${index-1}].model" value="${product.model}"/>
						<input type="hidden" name="products[${index-1}].category.name" value="${product.category.name}"/>
						<input type="number" name="products[${index-1}].sort" value="${product.sort}" style="width:50px;margin:0;padding:0;text-align:center;"/>
						<%--<input type="hidden" name="ids" value="${product.id}"/>--%>
						<%--<input type="text" name="sorts"  value="${product.sort}" style="width:50px;margin:0;padding:0;text-align:center;">--%>
			</tr>
		</c:forEach>
		
		</tbody>
	</table>
	<shiro:hasPermission name="md:product:edit"><div class="form-actions pagination-left">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保存排序" />
		</div></shiro:hasPermission>
  </form:form> 
  

  </body>
</html>

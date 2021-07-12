<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客戶管理</title>
	<meta name="decorator" content="default"/>
	<style type="text/css">
		.table thead th,.table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
		}
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			 	//全选绑定
			$("#selectAll").change(function() {
				 var $check = $(this);
				 $("input:checkbox").each(function(){
					 if ($(this).val() != "on"){
						 if ($check.attr("checked") == "checked") {
							 $(this).attr("checked", true);

						 }
						 else{
							 $(this).attr("checked", false);
						 }
					 }
				 });
			});
		});

		var ids = [];
		function setIds()
		{
			ids = [];
			$("input:checkbox").each(function()
			{
				 if ($(this).attr("checked")){
				 	var temp=$(this).attr("id");
				 	if(temp!="selectAll")
				 	{
				 		temp=$(this).next().attr("value");
						if(temp!="")
						{
						 	ids.push($(this).val());
						 }
				 	}
				 }
			 });
		}

		$(document).on("click", "#btnApprove", function () {
			setIds();
	        if (ids.length == 0) {
	            top.$.jBox.error('请选择要审核的客户信息', '客户审核');
	            return;
	        }

	        var submit = function (v, h, f) {
	            if (v == 'ok') {
	                top.$.jBox.tip("正在审核...", "loading");
	    	 		$("#btnConfirm").attr("disabled", "disabled");
	    	        var data = {ids:ids.join(",")};
	    	        $.ajax({
	    	            cache: false,
	    	            type: "POST",
	    	            url: "${ctx}/md/customer/approve",
	    	            data: data,
	    	            success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
	    	            	if (data.success){
	    	            		top.$.jBox.tip("客户审核成功", "success");
	    	            		repage();
	    	            	}
	    	            	else{
	    	            		top.$.jBox.error(data.message);
	    	            	}
	    		            $("#btnConfirm").removeAttr("disabled");
	    	            	top.$.jBox.closeTip();
	    	            },
	    	            error: function (e) {
	    	                $("#btnConfirm").removeAttr("disabled");
	    	                top.$.jBox.closeTip();
	    	                //top.$.jBox.error(thrownError.toString());
                            ajaxLogout(e.responseText,null,"客户审核错误，请重试!");
	    	            }
	    	        });
	            }
	            else if (v == 'cancel') {
	                // 取消
	            }

	            return true; //close
	        };

	        top.$.jBox.confirm('确定审核所选的信息么？', '客户审核', submit);

	    });
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/md/customer">客户列表</a></li>
		<shiro:hasPermission name="md:customer:edit"><li><a href="${ctx}/md/customer/form?sort=10">客户添加</a></li></shiro:hasPermission>
		<shiro:hasPermission name="md:customer:edit"><li  class="active"><a href="javascript:void(0);">客户审核</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="customer" action="${ctx}/md/customer/approvelist" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
			<label>代码：</label>
			<form:input path="code" htmlEscape="false" maxlength="30" />
			<label>名称：</label>
			<form:input path="name" htmlEscape="false" maxlength="60" class="required"/>
		&nbsp;&nbsp;
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>
		&nbsp;&nbsp;<input id="btnApprove" class="btn btn-success" type="button" value="通过审核"/>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
			<th><input type="checkbox" id="selectAll" name="selectAll"/></th>
			<th>序号</th>
			<th>代码</th><th>名称</th><th>厂商全称</th>
			<th>手机</th><th>描述</th>
		<shiro:hasPermission name="md:customer:edit"><th>操作</th></shiro:hasPermission></tr></thead>
			</tr>
		</thead>
		<tbody>
		<% int i=0; %>
		<c:forEach items="${page.list}" var="customer">
			<tr>
				<% i++; %>
				<td><input type="checkbox" id="cbox<%=i%>" value="${customer.id}" name="checkedRecords"/></td>
				<td><%=i%></td>
				<td>${customer.code}</td>
				<td>${customer.name}</td>
				<td>${customer.fullName}</td>
				<td>${customer.phone}</td>
				<td>${customer.remarks}</td>
				<shiro:hasPermission name="md:customer:edit">
				<td>
    				<a href="${ctx}/md/customer/form?id=${customer.id}">修改</a>
					<a href="${ctx}/md/customer/delete?id=${customer.id}" onclick="return confirmx('确认要删除该客戶吗？', this.href)">删除</a>

				</td>
				</shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
  <head>
		<title>结算方式</title>
		<%@ include file="/WEB-INF/views/include/head.jsp" %>
		<meta name="decorator" content="default"/>
	  	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
		<script type="text/javascript">
			$(document).on("click", "#btnSeq", function(){
				$.ajax({
					type: "POST",
					url: "${ctx}/md/paymenttype/testseqno?t="+(new Date()).getTime(),
					data:"",
					success: function (data) {
					   if(data){
						   if(data && data.success == true){
							   top.$.jBox.info(data.message,"提示");
						   }
						   else if( data && data.message){
							   top.$.jBox.error(data.message,"错误提示");
						   }
						   else{
							   top.$.jBox.error("错误","错误提示");
						   }
					   }
					   return false;
					},
					error: function (e) {
						 top.$.jBox.error("错误:"+e,"错误提示");
					}
				});
			});
		</script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">结算方式列表</a></li>
		<shiro:hasPermission name="md:paymenttype:edit"><li><a href="${ctx}/md/paymenttype/form">结算方式添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="dict" action="${ctx}/md/paymenttype" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>名称 ：</label><form:input path="label" htmlEscape="false" maxlength="50" class="input-small"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>&nbsp;
<%--		<a id="btnSeq" class="btn btn-primary" href="javascript:">序列号测试</a>--%>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead><tr><th>序号</th><th>键值</th><th>名称</th><th>描述</th><th>排序</th><shiro:hasPermission name="md:paymenttype:edit"><th>操作</th></shiro:hasPermission></tr></thead>
		<tbody>
		<c:forEach items="${page.list}" var="dict">
			<c:set var="index" value="${index+1}" />
			<tr>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${dict.value}</td>
				<td>${dict.label}</td>
				<td>${dict.description}</td>
				<td>${dict.sort}</td>
				<shiro:hasPermission name="md:paymenttype:edit"><td>
    				<a href="${ctx}/md/paymenttype/form?id=${dict.id}">修改</a>
					<a href="${ctx}/md/paymenttype/delete?id=${dict.id}" onclick="return confirmx('确认要删除该结算方式吗？', this.href)">删除</a>
    				<a href="<c:url value='${fns:getAdminPath()}/md/paymenttype/form?type=${dict.type}'></c:url>">添加结算方式</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>

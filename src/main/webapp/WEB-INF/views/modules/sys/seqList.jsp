<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
	<title>单据编号管理</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<script type="text/javascript">
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").submit();
	    	return false;
	    }
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">单据编号规则列表</a></li>
		<shiro:hasPermission name="sys:sequence:edit"><li><a href="${ctx}/sys/sequence/form">单据编号规则添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="seq" action="${ctx}/sys/sequence" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>代码：</label><form:input path="code" htmlEscape="false" maxlength="50" class="input-small"/>
		&nbsp;&nbsp;<label>描述:</label><form:input path="remarks" htmlEscape="false" maxlength="50" class="input-small"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead><tr><th>序号</th><th>编号代码</th><th>描述<th>前缀</th><th>日期格式</th><th>日期分割符</th><th>顺序号长度</th><th>后缀</th><shiro:hasPermission name="sys:sequence:edit"><th>操作</th></shiro:hasPermission></tr></thead>
		<tbody>
		<c:forEach items="${page.list}" var="seq">
			<c:set var="index" value="${index+1}" />
			<tr>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${seq.code}</td>
				<td>${seq.remarks}</td>
				<td>${seq.prefix}</td>
				<td>${fns:getDictLabelFromMS(seq.dateFormat, 'sequence_dateformat', '')}</td>
				<td>${seq.dateSeparator}</td>
				<td>${seq.digitBit}</td>
				<td>${seq.suffix}</td>
				<shiro:hasPermission name="sys:sequence:edit"><td>
    				<a href="${ctx}/sys/sequence/form?id=${seq.id}">修改</a>
					<a href="${ctx}/sys/sequence/delete?id=${seq.id}" onclick="return confirmx('确认要删除该单据编号规则吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
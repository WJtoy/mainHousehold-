<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>选择安维人员</title>
	<meta name="decorator" content="default" />
	<script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
	<style type="text/css">
	.pagination {  margin: 10px 0;  }
	.td {  word-break: break-all;  }
	</style>
	<script type="text/javascript">
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").submit();
	    	return false;
	    }
	    var data = null;
        var index = top.layer.index;
		function setData(i){
			data = items[i];
            top.$("#layui-layer"+index).find(".layui-layer-btn0").trigger("click");
		}

		var items =[
					<c:forEach items="${page.list}" var="engineer">
						{
						    id:'${engineer.id}',
							name:'${engineer.name}',
							phone:'${engineer.contactInfo}',
							grade:${engineer.grade}
						},
		            </c:forEach>
				   ];
	</script>
</head>

<body>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<div style="margin-left:3px;margin-right:3px;">
		<form:form id="searchForm" modelAttribute="engineer"  action="${ctx}/md/engineerForSP/select" method="post" class="breadcrumb form-search">
			<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
			<input id="pageSize" name="pageSize" type="hidden"
				value="${page.pageSize}" />
				<form:hidden path="servicePoint.id" />
				<form:hidden path="area.id" />
				<form:hidden path="exceptId" />
			<div style="margin:10px 0px 10px 0px;">
				<label>姓名：</label>
				<form:input path="name" maxlength="50" class="input-mini;" cssStyle="width: 200px;" />
				&nbsp;<label>电话 ：</label>
				<form:input path="contactInfo" maxlength="50" class="input-mini" cssStyle="width: 200px;"/>
				&nbsp;
					<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" />
					
			</div>
		</form:form>
		<sys:message content="${message}" />
		<table id="contentTable" class="datatable table table-bordered table-condensed table-hover">
			<thead>
				<tr>
					<th width="45">序号</th>
					<th width="120">姓名</th>
					<th width="120">手机</th>
					<th width="80">派单数</th>
					<th width="80">完成单数</th>
					<th width="80">违约单数</th>
					<th width="55">评价分数</th>
					<th>地址</th>
				</tr>
			</thead>
			<tbody>
				<c:set var="index" value="0" />
				<c:forEach items="${page.list}" var="engineer">
					<c:set var="i" value="${i+1}" />
					<tr>
						<td>${i+(page.pageNo-1)*page.pageSize}</td>
						<td><a href="javascript:void(0);" onclick="setData(${index});"> ${engineer.name}</a></td>
						<td>${engineer.contactInfo}</td>
						<td><span class="label label-info">${engineer.planCount}</span>
						</td>
						<td><span class="label label-success">${engineer.orderCount}</span>
						</td>
						<td><span class="label label-important">${engineer.breakCount}</span>
						</td>
						<td>${engineer.grade}</td>
						<td>${engineer.address}</td>
					</tr>
					<c:set var="index" value="${index+1}" />
				</c:forEach>
			</tbody>
		</table>
		<div class="pagination">${page}</div>
	</div>
	<script type="text/javascript" language="javascript" class="init">
        $(document).ready(function() {
            $("td,th").css({"text-align":"center","vertical-align":"middle"});
        });
	</script>
</body>
</html>

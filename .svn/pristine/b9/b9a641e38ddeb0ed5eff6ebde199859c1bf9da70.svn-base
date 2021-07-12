<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
	<title>服务类型管理</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<style type="text/css">
		.table thead th,.table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
		}
	</style>
	<script type="text/javascript">
		function editServiceType(type,id) {
			var text = "添加服务类型";
			var url = "${ctx}/md/servicetype/form?sort=10";
			var area = ['788px', '688px'];
			if(type == 20){
				text = "修改服务类型";
				url = "${ctx}/md/servicetype/form?id=" + id;
			}
			top.layer.open({
				type: 2,
				id:"customer",
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
	<li class="active"><a href="${ctx}/md/servicetype">服务类型列表</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="serviceType" action="${ctx}/md/servicetype" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>代码：</label>
		<form:input path="code" htmlEscape="false" maxlength="30" />
		<label>名称：</label>
		<form:input path="name" htmlEscape="false" maxlength="60" class="required"/>
		&nbsp;&nbsp;
		&nbsp;<input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询"/>
	</form:form>
	<shiro:hasPermission name="md:servicetype:edit">
		<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 125px;height: 30px" onclick="editServiceType(10,null)">
			<i class="icon-plus-sign"></i>&nbsp;添加服务类型
		</button>
	</shiro:hasPermission>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
		<tr><th>序号</th>
			<th>代码</th>
			<th>名称</th>
			<th>客户可见</th>
			<th>质保</th>
			<th>工单类型</th>
			<th>是否自动客评</th>
			<th>是否自动对账</th>
			<th>关联故障类别</th>
			<th>是否扣点</th>
			<th>是否收取平台信息费</th>
			<th>是否扣质保金</th>
			<th>描述</th>
			<th>排序</th>
		<shiro:hasPermission name="md:servicetype:edit"><th>操作</th></shiro:hasPermission></tr></thead>
		<tbody>
		<c:forEach items="${page.list}" var="serviceType">
			<c:set var="index" value="${index+1}" />
			<tr>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${serviceType.code}</td>
				<td>${serviceType.name}</td>
				<c:choose>
					<c:when test="${serviceType.openForCustomer eq 1}">
						<td>是</td>
					</c:when>
					<c:otherwise>
						<td>否</td>
					</c:otherwise>
				</c:choose>
				<td>${serviceType.warrantyStatus.label}</td>
<%--				<td>${fns:getDictLabelFromMS(serviceType.orderServiceType,'order_service_type','')}</td>--%>
				<td>${serviceType.orderServiceTypeDict.label}</td>
				<c:choose>
					<c:when test="${serviceType.autoGradeFlag==0}">
						<td><span class="label status_100">否</span></td>
					</c:when>
					<c:when test="${serviceType.autoGradeFlag==1}">
						<td><span class="label status_10">是</span></td>
					</c:when>
					<c:otherwise>
						<td><span class="label status_30">未知</span></td>
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${serviceType.autoChargeFlag==0}">
						<td><span class="label status_100">否</span></td>
					</c:when>
					<c:when test="${serviceType.autoChargeFlag==1}">
						<td><span class="label status_10">是</span></td>
					</c:when>
					<c:otherwise>
						<td><span class="label status_30">未知</span></td>
					</c:otherwise>
				</c:choose>
				<td><span class="label status_${serviceType.relateErrorTypeFlag eq 1?"10":"100"}">${serviceType.relateErrorTypeFlag eq 1?"是":"否"}</span></td>
				<td><span class="label status_${serviceType.taxFeeFlag eq 1?"10":"100"}">${serviceType.taxFeeFlag eq 1?"是":"否"}</span></td>
				<td><span class="label status_${serviceType.infoFeeFlag eq 1?"10":"100"}">${serviceType.infoFeeFlag eq 1?"是":"否"}</span></td>
				<c:choose>
					<c:when test="${serviceType.depositFlag==0}">
						<td><span class="label status_100">否</span></td>
					</c:when>
					<c:when test="${serviceType.depositFlag==1}">
						<td><span class="label status_10">是</span></td>
					</c:when>
				</c:choose>
				<td>${serviceType.remarks}</td>
				<td>${serviceType.sort}</td>
				<shiro:hasPermission name="md:serviceType:edit">
				<td>
					<a href="javascript:editServiceType(20,'${serviceType.id}')">修改</a>
					<a href="${ctx}/md/servicetype/delete?id=${serviceType.id}" onclick="return confirmx('确认要删除该服务类型吗？', this.href)">删除</a>
				</td>
				</shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>

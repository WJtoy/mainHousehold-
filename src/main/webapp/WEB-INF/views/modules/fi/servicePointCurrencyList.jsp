<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<head>
	<title>网点流水表</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#treeTable").treeTable({expandLevel : 5});
		});

		function openjBox(url,title,width,height){
			top.$.jBox.open("iframe:" + url , title, width, height,{top:'10px',buttons:{}, loaded:function(h){$("#jbox-iframe",h).prop("height","98%");} });
		}
	</script>
</head>
<body>
<sys:message content="${message}"/>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">账目列表</a></li>
	</ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<c:set var="userId" value="${currentuser.getId()}"/>
	<c:set var="userName" value="${currentuser.getName()}"/>
	<form:form id="searchForm" modelAttribute="engineerCurrency" action="${ctx}/fi/servicepointcurrency/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<div>
			<c:choose>
				<c:when test="${currentuser.isEngineer()}">
					<input id="servicePointId" type="hidden" name="servicePointId" value="${userId}" maxlength="50" style="width:245px;" />
					<input id="servicePointName" type="hidden" name="servicePointNo.name" value="${userName}" maxlength="50" style="width:245px;" />
				</c:when>
				<c:otherwise>
					<label class="control-label">服务网点：</label>
					<%--<sd:servicePointSelect id="servicePoint" name="servicePoint.id" value="${engineerCurrency.servicePoint.id}"--%>
										 <%--labelName="servicePoint.name" labelValue="${engineerCurrency.servicePoint.name}"--%>
										 <%--width="1200" height="780" title="选择服务网点" areaId="" cssClass="required"--%>
										 <%--showArea="false" allowClear="true" callbackmethod="" />--%>
					<md:mdServicePointSelector id="servicePoint" name="servicePoint.id" value="${engineerCurrency.servicePoint.id}"
											   labelName="servicePoint.name" labelValue="${engineerCurrency.servicePoint.name}"
											   width="1200" height="700" noblackList="true" noSubEnginner="true" callbackmethod="" cssClass="required"/>
				</c:otherwise>
			</c:choose>
			&nbsp;&nbsp;

			<c:set var="actionTypeList" value="${fns:getDictExceptListFromMS('ServicePointActionType','10,30,40,60,70')}" /><%--切换为微服务--%>
			<label>变更类型</label>
			<select id="actionType" name="actionType" style="width:200px;">
				<option value="" <c:out value="${(empty engineerCurrency.actionType)?'selected=selected':''}" />>所有</option>
				<c:forEach items="${actionTypeList}" var="dict">
					<option value="${dict.value}" <c:out value="${(engineerCurrency.actionType eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
				</c:forEach>
			</select>

			<label>日期范围：</label>
			<input id="createDate" name="createDate" type="text" readonly="readonly" style="margin-left:4px" maxlength="20" class="input-date Wdate"
				   value="<fmt:formatDate value='${engineerCurrency.createDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>~</label>
			<input id="updateDate" name="updateDate" type="text" readonly="readonly" maxlength="20" class="input-date Wdate"
				   value="<fmt:formatDate value='${engineerCurrency.updateDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			&nbsp;&nbsp;

			<label>相关单号:</label>
			<form:input path="currencyNo" class="input-medium" maxlength="20" />
			&nbsp;&nbsp;
			<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>
		</div>
	</form:form>
	<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th>序号</th>
				<th>变更类型</th>
				<th>变更前余额</th>
				<th>金额</th>
				<th>变更后余额</th>
				<th>相关单号</th>
				<th>描述</th>
				<th>创建时间</th>
				<th>修改时间</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${page.list}" var="item">
			<c:set var="index" value="${index+1}" />
			<c:if test="${!(item.servicePoint.id eq servicePointId) }">
				<c:set var="servicePointId" value="${item.servicePoint.id}"/>
				<tr id="${item.servicePoint.id}" pId="">
					<td colspan="3">${item.servicePoint.servicePointNo}&nbsp;-&nbsp;${item.servicePoint.primary.name}
						(${item.servicePoint.finance.paymentType.label})</td><%--切换为微服务--%>
					<td colspan="5">${fns:formatNum(item.servicePoint.finance.balance)}</td>
					<td></td>
				</tr>
			</c:if>
			<tr id="${item.id}" pId="${servicePointId}">
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${item.actionTypeName}</td><%--切换为微服务--%>
				<td><fmt:formatNumber pattern="0.00">${item.beforeBalance}</fmt:formatNumber> </td>
				<td><fmt:formatNumber pattern="0.00">${item.amount}</fmt:formatNumber></td>
				<td><fmt:formatNumber pattern="0.00">${item.balance}</fmt:formatNumber></td>
				<td>${item.currencyNo}</td>
				<td>
					<c:if test="${item.actionType == 50}">
						${fns:substringBeforeLast(item.remarks, '结师傅付款').concat('结师傅付款')}
					</c:if>
					<c:if test="${item.actionType != 50}">
						${item.remarks}
					</c:if>
				</td>
				<td>${fns:formatDate(item.createDate, 'yyyy-MM-dd HH:mm:ss')}</td>
				<td>${fns:formatDate(item.updateDate, 'yyyy-MM-dd HH:mm:ss')}</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
<script type="text/javascript">
	$(document).ready(function() {
		$("th").css({"text-align":"center","vertical-align":"middle"});
	});
</script>
</html>

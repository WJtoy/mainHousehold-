<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>催单-客服回复窗口</title>
	<meta name="description" content="客服回复窗口">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/reminder/tpl/kefuProcessItems.html" %>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
		var this_index = top.layer.index;
		function closeme(){
			top.layer.close(this_index);
		};
		$(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip({gravity: 'north'});
		});

	</script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 120px;}
		.form-horizontal .controls {margin-left: 130px;}
		.myalert {padding: 2px 5px 2px 5px;margin-bottom: 2px;}
		.centerForm {margin:0 auto; width:800px; line-height:20px; border:0px solid}
		.reminder-status{
			padding: 4px 5px;border-radius:2px;background-color:#0096DA;color: white;margin-left: 5px;
		}
		.order-status{
			padding: 4px 5px;border-radius:2px;background-color:#0096DA;color: white;
		}
	</style>
</head>
<body>
	<div style="width: 90%;margin-left: 5%;margin-top: 24px">
		<table id="tbMaterial" width="100%" border="0" class="table table-striped table-bordered table-condensed">
			<thead>
			<tr>
				<th width="70px">催单次数</th>
				<th width="60px">催单状态</th>
				<th width="70px">催单类型</th>
				<th>备注</th>
				<th width="80px">催单人</th>
				<th width="100px">催单时间</th>
				<th >回复内容</th>
				<th width="80px">回复人</th>
				<th width="100px">回复时间</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${reminder.items}" var="item" varStatus="i" begin="0">
			<tr>
				<td>第${item.itemNo}次催单</td>
				<td>
					<c:choose>
						<c:when test="${item.processAt>0}">
							已回复
						</c:when>
						<c:otherwise>未回复</c:otherwise>
					</c:choose>
				</td>
				<td>
					<c:if test="${not empty item.reminderReason}">
						${item.reminderReason.value}
					</c:if>
				</td>
				<td>
					<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${item.createRemark}">
						${item.createRemark}
					</a>
				</td>
				<td>
					${item.createName}
					<c:if test="${not empty tem.creatorTypeName}">
						<br/>(${item.creatorTypeName})
					</c:if>
				</td>
				<td>
					<jsp:useBean id="dateValue" class="java.util.Date"/>
					<jsp:setProperty name="dateValue" property="time" value="${item.createAt}"/>
					<fmt:formatDate value="${dateValue}" pattern="yyyy-MM-dd HH:mm:ss"/>
				</td>
				<!-- 回复 -->
				<td>
					<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${item.processRemark}">
						${item.processRemark}
					</a>
				</td>
				<td>
					${item.processName}
					<c:if test="${not empty tem.processorTypeName}">
						<br/>(${item.processorTypeName})
					</c:if>
				</td>
				<td><c:if test="${item.processAt>0}">
					<jsp:useBean id="processAt" class="java.util.Date"/>
					<jsp:setProperty name="processAt" property="time" value="${item.processAt}"/>
					<fmt:formatDate value="${processAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
			        </c:if>
			     </td>
			</tr>
			</c:forEach>
			<tbody>
		</table>
		<div style="height: 60px;width: 100%"></div>
		<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
			<hr style="margin: 0px;"/>
			<div style="float: right;margin-top: 10px;margin-right: 100px">
				<input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 96px;height: 40px" onclick="closeme()"/>
			</div>
		</div>
</body>
</html>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>投诉单-查看</title>
	<meta name="description" content="查看完整投诉单">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<!-- image viewer -->
	<script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
	<link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<%@ include file="/WEB-INF/views/modules/sd/complain/tpl/attacheView.html" %>
    <%@ include file="/WEB-INF/views/modules/sd/complain/tpl/complainList.html" %>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
		var this_index = top.layer.index;
		function closeme(){
			top.layer.close(this_index);
		};
		
		 $(document).ready(function() {
		     //加载投日志列表
			Order.showComplainLogList('${complain.id}','${complain.quarter}');
			$('a[data-toggle=tooltip]').darkTooltip();
			$('a[data-toggle=tooltipnorth]').darkTooltip(
			{
				gravity : 'north'
			});
			$('a[data-toggle=tooltipeast]').darkTooltip(
			{
				gravity : 'east'
			});
		});
	</script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 100px;}
		.form-horizontal .controls {margin-left: 120px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="complain"  method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="quarter"/>
		<sys:message content="${message}"/>
	<fieldset>
		<legend>
				<p class="text-right" style="margin-right: 10px;<c:if test="${empty complain.complainNo}">margin-right: 115px;</c:if>">No. ${complain.complainNo}</p>
		</legend>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">工单号:</label>
					<div class="controls">
						<%--<form:input path="orderNo"  disabled="true" htmlEscape="false" cssClass="input-block-level " />--%>
						<a style="line-height: 30px" class="input-block-level"  href="javascript:void(0);" onclick="Order.showComplainOrderDetail('${complain.orderId}','${complain.quarter}');">${complain.orderNo}</a>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉方:</label>
					<div class="controls">
						<input type="text" name="complainType.label" disabled="disabled" cssClass="input-block-level" value="${fns:getDictLabelFromMS(complain.complainType.value,'complain_type','')}" />
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">状态:</label>
					<div class="controls">
						<form:input path="status.label"  htmlEscape="false" disabled="true" cssClass="input-block-level " maxlength="10" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉对象:</label>
					<div class="controls">
						<%--<form:input path="complainObjectLabels"  htmlEscape="false" disabled="true" cssClass="input-block-level"  />--%>
							<c:forEach var="item" items="${fn:split(complain.complainObjectLabels,',')}" >
								<span class="label label-important">${item}</span>
							</c:forEach>
					</div>
				</div>
			</div>
			<div class="span8">
				<div class="control-group">
					<label class="control-label">投诉项目:</label>
					<div class="controls">
						<%--<form:input path="complainItemLabels"  htmlEscape="false" disabled="true" cssClass="input-block-level" />--%>
						<c:forEach var="item" items="${fn:split(complain.complainItemLabels,',')}" >
							<span class="label label-important">${item}</span>
						</c:forEach>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉人:</label>
					<div class="controls">
						<form:input path="complainBy" disabled="true" htmlEscape="false" cssClass="input-block-level " maxlength="10" />
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">投诉日期:</label>
					<div class="controls">
						<input id="complainDate" name="complainDate"
							   type="text" readonly="readonly"
							   maxlength="10" class="input-block-level  Wdate"
							   value="${fns:formatDate(complain.complainDate,'yyyy-MM-dd')}" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">投诉描述:</label>
					<div class="controls">
						<form:textarea path="complainRemark" htmlEscape="false" readonly="true" disabled="true" rows="3" maxlength="490" class="input-block-level" cssStyle="resize: vertical" />
					</div>
				</div>
			</div>
		</div>
		<c:if test="${complain.attachmentQty>0}">
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">投诉附件:</label>
					<div class="controls">
						<div id="divUploadFile" class="upload_warp">
						</div>
					</div>
				</div>
			</div>
		</div>
		</c:if>
		<c:set var="complainStatus" value="${fns:stringToInteger(complain.status.value)}" />
        <c:if test="${(complainStatus == 2 or complainStatus == 3  or (complainStatus == 1 and complain.judgeObject > 0))}">
		<legend>投诉判责</legend>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">责任判定:</label>
					<div class="controls">
						<table class="table table-bordered table-striped">
							<c:set var="judgeObjects" value="${fns:getDictListFromMS('judge_object')}" /><%--切换为微服务--%>
							<c:forEach items="${judgeObjects}" var="dict">
								<spring:eval var="containsObject" expression="complain.judgeObjectsIds.contains(dict.value)" />
								<c:if test="${containsObject}">
									<tr>
									<td width="140px">
										<span>${dict.label}</span>
									</td>
									<td>
										<c:set var="dictType" value="judge_item_${dict.value}" />
										<c:set var="judgeItems" value="${fns:getDictListFromMS(dictType)}" />
										<c:forEach items="${judgeItems}" var="item">
											<spring:eval var="containsItem" expression="complain.judgeItemsIds.contains(item.value)" />
											<c:if test="${containsItem}">
												<span class="label label-important">${item.label}</span>
											</c:if>
										</c:forEach>
										<!-- 网点 -->
										<c:if test="${dict.value eq '1' and complain.servicePoint != null and complain.servicePoint.id >0}">
											<label>责任网点:</label>
											<span class="label label-info">${complain.servicePoint.servicePointNo} - ${complain.servicePoint.name}</span>
										</c:if>
									</td>
									</tr>
								</c:if>
							</c:forEach>
						</table>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">判责意见:</label>
					<div class="controls">
						<form:textarea path="judgeRemark" htmlEscape="false" readonly="true" disabled="true" rows="3" maxlength="490" class="input-block-level" cssStyle="resize: vertical" />
					</div>
				</div>
			</div>
		</div>
        <c:if test="${complain.judgeAttachmentQty >0}">
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">判责附件:</label>
						<div class="controls">
							<div id="divJudgeUploadFile" class="upload_warp">
							</div>
						</div>
					</div>
				</div>
			</div>
        </c:if>
        </c:if>
		<!-- log -->
		<div class="tabbable">
			<ul class="nav nav-tabs">
				<li id="liLoglist" class="active"><a href="#tabComplainLogList" data-toggle="tab" id="lnktabComplainLogList" >处理日志</a></li>
			</ul>
			<!-- tab content -->
			<div class="tab-content">
				<div class="tab-pane active" id="tabComplainLogList" >
				</div>
			</div>
		</div>

		<c:if test="${complainStatus == 2 or complainStatus == 3}">
		<!-- 结案 -->
		<legend>处理方案</legend>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">处理方案:</label>
					<div class="controls">
						<%--<form:input path="completeResultLabels"  htmlEscape="false" disabled="true" cssClass="input-block-level" />--%>
						<c:forEach var="item" items="${fn:split(complain.completeResultLabels,',')}" >
							<span class="label label-info">${item}</span>
						</c:forEach>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">处理意见:</label>
					<div class="controls">
						<form:textarea path="completeRemark" htmlEscape="false" readonly="true" disabled="true" rows="3" maxlength="490" class="input-block-level" cssStyle="resize: vertical" />
					</div>
				</div>
			</div>
		</div>
		<c:if test="${complain.compensateResult>0}">
			<legend>赔偿</legend>
				<c:if test="${complain.compensateResult == 1 || complain.compensateResult == 3}">
				<div class="row-fluid">
					<div class="span4">
						<div class="control-group">
							<label class="control-label">厂商:</label>
							<div class="controls">
								<form:input path="customer.name" disabled="true" htmlEscape="false" style="width:250px;" />
							</div>
						</div>
					</div>
					<div class="span4">
						<div class="control-group">
							<label class="control-label">金额:</label>
							<div class="controls">
								<form:input path="customerAmount" disabled="true" cssClass="number " />
							</div>
						</div>
					</div>
				</div>
				</c:if>
				<c:if test="${complain.compensateResult == 2 || complain.compensateResult == 3}">
					<div class="row-fluid">
						<div class="span4">
							<div class="control-group">
								<label class="control-label">用户:</label>
								<div class="controls">
									<form:input path="userName" disabled="true" htmlEscape="false" style="width:250px;" />
								</div>
							</div>
						</div>
						<div class="span4">
							<div class="control-group">
								<label class="control-label">金额:</label>
								<div class="controls">
									<form:input path="userAmount" disabled="true" cssClass="number" />
								</div>
							</div>
						</div>
					</div>
				</c:if>
		</c:if>
		<c:if test="${complain.amerceResult>0}">
			<legend>罚款</legend>
				<c:if test="${complain.amerceResult == 1 || complain.amerceResult == 3}">
					<div class="row-fluid">
						<div class="span4">
							<div class="control-group">
								<label class="control-label">网点:</label>
								<div class="controls">
									<input id="servicePoint" name="servicePoint" disabled="disabled" type="text" value="${complain.servicePoint.servicePointNo} - ${complain.servicePoint.name}" style="width:250px;">
								</div>
							</div>
						</div>
						<div class="span4">
							<div class="control-group">
								<label class="control-label">金额:</label>
								<div class="controls">
									<form:input path="servicePointAmount" disabled="true" cssClass="number " />
								</div>
							</div>
						</div>
					</div>
				</c:if>
				<c:if test="${complain.amerceResult == 2 || complain.amerceResult == 3}">
					<div class="row-fluid">
						<div class="span4">
							<div class="control-group">
								<label class="control-label">客服:</label>
								<div class="controls">
									<form:input path="kefu.name" disabled="true" htmlEscape="false" style="width:250px;" />
								</div>
							</div>
						</div>
						<div class="span4">
							<div class="control-group">
								<label class="control-label">金额:</label>
								<div class="controls">
									<form:input path="kefuAmount" disabled="true" cssClass="number " />
								</div>
							</div>
						</div>
					</div>
				</c:if>
		</c:if>
		</c:if>
		<div class="form-actions">
			<input id="btnCancel" name="btnCancel" class="btn" type="button" value="关 闭"  onclick="closeme();" />
		</div>
	</fieldset>
	</form:form>

</body>
<script type="text/javascript">
	<c:if test="${!empty complain.applyAttaches && complain.applyAttaches.size()>0}">
        var data = ${fns:toJson(complain.applyAttaches)};
        var tmpl = document.getElementById('tpl-upload-file-image-view').innerHTML;
        var doTtmpl = doT.template(tmpl);
        var html = doTtmpl(data);
        $("#divUploadFile").append(html);
    	$("#divUploadFile").viewer('destroy').viewer({ url: "data-original"});
    </c:if>

    <c:if test="${!empty complain.judgeAttaches && complain.judgeAttaches.size()>0}">
        var judgedata = ${fns:toJson(complain.judgeAttaches)};
        var judgetmpl = document.getElementById('tpl-upload-file-image-view').innerHTML;
        var judgedoTtmpl = doT.template(judgetmpl);
        var judgehtml = judgedoTtmpl(judgedata);
        $("#divJudgeUploadFile").append(judgehtml);
    	$("#divJudgeUploadFile").viewer('destroy').viewer({ url: "data-original"});
    </c:if>

</script>
</html>
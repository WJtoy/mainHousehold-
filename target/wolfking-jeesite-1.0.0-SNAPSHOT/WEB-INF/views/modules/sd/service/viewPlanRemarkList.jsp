<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>网点备注-历史列表</title>
	<meta name="description" content="网点备注-历史列表">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
		var this_index = top.layer.index;
		function closeme(){
			top.layer.close(this_index);
		};
		
		 $(document).ready(function() {
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
	<fieldset>
		<sys:message content="${message}"/>
		<!-- log -->
		<div class="tabbable" style="padding-left:10px;padding-top: 10px;">
			<%--<ul class="nav nav-tabs">
				<li id="liLoglist" class="active"><a href="#tabComplainLogList" data-toggle="tab" id="lnktabComplainLogList" >历史备注列表</a></li>
			</ul>--%>
			<!-- tab content -->
			<div class="tab-content">
				<div class="tab-pane active" id="tabComplainLogList" >
					<div class="row-fluid">
						<div class="span6">
							<div class="control-group">
								<label class="control-label">网点编号:</label>${servicePointNo}
							</div>
						</div>
						<div class="span6">
							<div class="control-group">
								<label class="control-label">网点名称:</label>${servicePointName}

							</div>
						</div>
					</div>

					<table id="contentTable" class="table table-bordered table-condensed table-hover">
						<thead>
						<tr>
							<th width="30">序号</th>
							<th width="100">备注人</th>
							<th width="150">备注日期</th>
							<th>备注内容</th>
						</tr>
						</thead>
						<tbody>
						<c:forEach items="${planRemarks}" var="entity">
							<c:set var="rowNumber" value="${rowNumber+1}"/>
							<tr>
								<td>${rowNumber}</td>
								<td>
									${entity.name}
								</td>
								<td>
									${entity.date}
								</td>
								<td>
									${entity.planRemark}
								</td>
							</tr>
						</c:forEach>
						</tbody>
					</table>

				</div>
			</div>
		</div>

		<div class="form-actions">
			<input id="btnCancel" name="btnCancel" class="btn" type="button" value="关 闭"  onclick="closeme();" />
		</div>
	</fieldset>

</body>
</html>
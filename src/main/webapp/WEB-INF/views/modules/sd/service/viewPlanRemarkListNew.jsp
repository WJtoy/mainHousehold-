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
		#editBtn {
			position: fixed;
			left: 0px;
			bottom: 3px;
			width: 100%;
			height: 55px;
			background: #fff;
			z-index: 10;
			border-top: 1px solid #ccc;
			border-top: 1px solid #e5e5e5;
			text-align: right;
		}
	</style>
</head>
<body>
	<fieldset>
		<sys:message content="${message}"/>
		<div class="tabbable" style="padding-left:10px;padding-top: 10px;">
			<div class="tab-content" style="padding: 15px;padding-top: 0px;">
				<div class="tab-pane active" id="tabComplainLogList" >
					<div class="row-fluid" style="margin-bottom: 10px;">
						<div class="span5">
							<div class="control-group">
								<label class="control-label">网点编号：</label>
								<input id="servicePointNo" name="servicePointNo" style="width:240px;margin-top: 8px" type="text" value="${servicePointNo}" readonly="readonly">
							</div>
						</div>
						<div class="span6">
							<div class="control-group">
								<label class="control-label">网点名称：</label>
								<input id="servicePointName" name="servicePointName" style="width:240px;margin-top: 8px" type="text" value="${servicePointName}" readonly="readonly">
							</div>
						</div>
					</div>

					<table id="contentTable" class="table table-bordered table-condensed table-hover">
						<thead>
						<tr>
							<th width="50">序号</th>
							<th width="120">备注人</th>
							<th width="180">备注时间</th>
							<th width="538">备注</th>
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

		<div id="editBtn">
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closeme();"
				   style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
		</div>
	</fieldset>

</body>
<script>
    $(document).ready(function() {
        $("th").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"text-align":"center","vertical-align":"middle"});
    });
</script>
</html>
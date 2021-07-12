<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客户加急等级</title>
	<meta name="decorator" content="default" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<style type="text/css">
		#editBtn {
			position: fixed;
			left: 0px;
			bottom: 0;
			width: 100%;
			height: 60px;
			background: #fff;
			z-index: 10;
			border-top: 1px solid #e5e5e5;
		}
	</style>
	<script type="text/javascript">
		var this_index = top.layer.index;
		// 关闭页面
		function cancel() {
			top.layer.close(this_index);// 关闭本身
		}
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
					var loadingIndex = layerLoading('正在提交，请稍候...');
					var $btnSubmit = $("#btnSubmit");
					if ($btnSubmit.prop("disabled") == true) {
						event.preventDefault();
						return false;
					}
					$btnSubmit.prop("disabled", true);
					$.ajax({
						url:"${ctx}/md/urgentcustomer/save",
						type:"POST",
						data:$(form).serialize(),
						dataType:"json",
						success: function(data){
							//提交后的回调函数
							if(loadingIndex) {
								top.layer.close(loadingIndex);
							}
							if(ajaxLogout(data)){
								setTimeout(function () {
									clickTag = 0;
									$btnSubmit.removeAttr('disabled');
								}, 2000);
								return false;
							}
							if (data.success) {
								layerMsg("保存成功");
								top.layer.close(this_index);//关闭本身
								var pframe = getActiveTabIframe();//定义在jeesite.min.js中
								if(pframe){
									pframe.repage();
								}
							}else{
								setTimeout(function () {
									clickTag = 0;
									$btnSubmit.removeAttr('disabled');
								}, 2000);
								top.layer.close(loadingIndex);
                                layerError("保存失败", "错误提示");
							}
							return false;
						},
						error: function (data)
						{
							if(loadingIndex) {
								layer.close(loadingIndex);
							}
							setTimeout(function () {
								clickTag = 0;
								$btnSubmit.removeAttr('disabled');
							}, 2000);
							top.layer.close(loadingIndex);
							ajaxLogout(data,null,"数据保存错误，请重试!");
							//var msg = eval(data);
                            top.layer.close(loadingIndex);
						},
						timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
					});
				},
					errorContainer : "#messageBox",
					errorPlacement : function(error, element)
					{
						$("#messageBox").text("输入有误，请先更正。");
						if (element.is(":checkbox")
								|| element.is(":radio")
								|| element.parent().is(
										".input-append"))
						{
							error.appendTo(element.parent()
									.parent());
						} else
						{
							error.insertAfter(element);
						}
			}});
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
		});
	</script>
	<style type="text/css">
		.form-horizontal .control-label{
			width: 180px;
		}
		.form-horizontal .controls{
			margin-left: 200px;
		}
		.form-horizontal .control-group{
			margin-bottom: 15px;
		}
	</style>
</head>

<body>
	<form:form id="inputForm" modelAttribute="urgentCustomer" action="${ctx}/md/urgentcustomer/save" method="post" class="form-horizontal">
		<sys:message content="${message}" />
		<c:if test="${canAction == true}">
		<div style="float: left;margin: 20px 0px 20px 0px">
			<label style="float: left;margin-top: 4px">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
			<div style="float: left">
				<c:choose>
					<c:when test="${urgentCustomer.customer.id > 0}">
						<form:hidden path="customer.id"></form:hidden>
						<form:input path="customer.name" readonly="true"></form:input>
					</c:when>
					<c:otherwise>
						<form:select path="customer.id" cssClass="input-small required" cssStyle="width:225px;">
							<form:option value="" label="请选择"/>
							<form:options items="${customerList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
						</form:select>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
			<table class="table table-striped table-bordered table-condensed table-hover">
				<thead>
				<tr>
					<th rowspan="2" width="100">省份</th>
					<c:forEach var="timelinessTitle" items="${urgentLevelList}" varStatus="j" begin="0">
						<th colspan="2" width="200">${timelinessTitle.remarks}</th>
					</c:forEach>
				</tr>
				<tr>
					<c:forEach items="${urgentLevelList}" varStatus="j" begin="0">
						<th width="100">收取</th>
						<th width="100">支付</th>
					</c:forEach>
				</tr>
				</thead>
				<tbody>
				<c:forEach var="areaModel" items="${urgentCustomer.list}" varStatus="i" begin="0">
					<tr>
						<td>${areaModel.area.name}</td>

						<c:forEach var="timeliness" items="${urgentLevelList}" varStatus="j" begin="0">
							<td>
								<form:hidden path="list[${i.index}].area.id"></form:hidden>
								<form:hidden path="list[${i.index}].list[${j.index}].urgentLevel.id"></form:hidden>
								<form:input path="list[${i.index}].list[${j.index}].chargeIn" class="input-small number" maxlength="7" min="0"></form:input>
							</td>
							<td>
								<form:input path="list[${i.index}].list[${j.index}].chargeOut" class="input-small number" maxlength="7" min="0"></form:input>
							</td>
						</c:forEach>
					</tr>
				</c:forEach>
				</tbody>
			</table>

		</c:if>
		<hr style="=border: 1px solid rgba(238, 238, 238, 1);margin-top: 20px"/>
		<div style="height: 20px"></div>
		<div id="editBtn" class="line-row">
			<shiro:hasPermission name="md:urgentcustomer:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 92px;height: 40px;margin-left: 84%;margin-top: 10px;margin-bottom: 10px"/>
				&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 92px;height: 40px;margin-top: 10px;margin-left: 19px;margin-bottom: 10px"/>
		</div>

	</form:form>
</body>
</html>

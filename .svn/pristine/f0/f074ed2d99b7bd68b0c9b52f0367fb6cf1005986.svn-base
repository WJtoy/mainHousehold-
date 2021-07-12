<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客户时效等级</title>
	<meta name="decorator" content="default" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<script type="text/javascript">
		$(document).ready(function()
		{
			$("#inputForm").validate(
				{
					submitHandler : function(form)
					{
						loading('正在提交，请稍等...');
                        var $btnSubmit = $("#btnSubmit");
                        if ($btnSubmit.prop("disabled") == true) {
                            event.preventDefault();
                            return false;
                        };
                        $btnSubmit.prop("disabled", true);
						form.submit();
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
	<ul class="nav nav-tabs">
		<li>
			<a href="${ctx}/md/customertimeliness/list">列表</a>
		</li>
		<li class="active">
			<a href="javascript:void(0);">添加</a>
		</li>
	</ul><br>
	<form:form id="inputForm" modelAttribute="customerTimeliness" action="${ctx}/md/customertimeliness/saveCustomerTimelinesss" method="post" class="form-horizontal">
		<sys:message content="${message}" />
		<c:if test="${canAction == true}">
		<div class="control-group">
			<label class="control-label">客户:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${customerTimeliness.customer.id > 0}">
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
						<th rowspan="2" width="200">省份</th>
						<c:forEach var="timelinessTitle" items="${timelineList}" varStatus="j" begin="0">
							<th colspan="2" width="200">${timelinessTitle.remarks}</th>
						</c:forEach>
					</tr>
					<tr>
						<c:forEach var="timelinessTitle" items="${timelineList}" varStatus="j" begin="0">
							<th width="100">收取</th>
							<th width="100">支付</th>
						</c:forEach>
					</tr>
				</thead>
				<tbody>
				<c:forEach var="areaModel" items="${customerTimeliness.areaTimelinessModelList}" varStatus="i" begin="0">
					<tr>
						<td>${areaModel.area.name}</td>

						<c:forEach var="timeliness" items="${timelineList}" varStatus="j" begin="0">
							<td>
								<form:hidden path="areaTimelinessModelList[${i.index}].area.id"></form:hidden>
								<form:hidden path="areaTimelinessModelList[${i.index}].list[${j.index}].timelinessLevel.id"></form:hidden>
								<form:input path="areaTimelinessModelList[${i.index}].list[${j.index}].chargeIn" class="input-small number" maxlength="7" min="0"></form:input>
							</td>
							<td>
								<form:input path="areaTimelinessModelList[${i.index}].list[${j.index}].chargeOut" class="input-small number" maxlength="7" min="0"></form:input>
							</td>
						</c:forEach>
					</tr>
				</c:forEach>
				</tbody>
			</table>
		</c:if>
		<div class="form-actions">
			<c:if test="${canAction == true}">
			<shiro:hasPermission name="md:urgentcustomer:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit"
					value="保 存" />&nbsp;</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="返 回"
				   onclick="history.go(-1)" />
		</div>
	</form:form>
</body>
</html>

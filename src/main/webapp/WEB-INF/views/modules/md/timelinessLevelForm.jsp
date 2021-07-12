<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>加急等级费用</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
					loading('正在提交，请稍等...')
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    };
                    $btnSubmit.prop("disabled", true);
					form.submit();
				},
				errorContainer: "#messageBox",
				errorPlacement: function(error, element) {
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
						error.appendTo(element.parent().parent());
					} else {
						error.insertAfter(element);
					}
				}
			});

		});
		
	</script>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/md/urgentlevel/list">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:timelinesslevel:edit">${not empty urgentLevel.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:timelinesslevel:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<sys:message content="${message}"/>
	<form:form id="inputForm" modelAttribute="timelinessLevel" action="${ctx}/md/timelinesslevel/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:input path="name" htmlEscape="false" minLength="2" maxlength="100" class="required" placeholder="(例：0~24)"/>(例：0~24)
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">收取:</label>
			<div class="controls">
				<form:input path="chargeIn" htmlEscape="false" maxlength="100" min="0" class="required number"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">支付:</label>
			<div class="controls">
				<form:input path="chargeOut" htmlEscape="false" maxlength="100" min="0" class="required number"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">排序:</label>
			<div class="controls">
				<form:input path="sort" htmlEscape="false" maxlength="100" min="0" class="required number"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="100" class="input-xlarge" cssStyle="min-height: 80px;max-height: 300px;min-width: 250px;max-width: 500px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:timelinesslevel:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>

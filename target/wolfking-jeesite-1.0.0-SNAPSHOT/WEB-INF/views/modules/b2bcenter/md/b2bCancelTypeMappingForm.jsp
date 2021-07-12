<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品分类</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
					loading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
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
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
	  </style>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/b2bcenter/md/cancelType/getList">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:b2bcanceltype:edit">${not empty cancelTypeMapping.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:b2bcanceltype:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="cancelTypeMapping" action="${ctx}/b2bcenter/md/cancelType/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">数据源:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${cancelTypeMapping.dataSource >0}">
						<form:hidden path="dataSource"></form:hidden>
						<input class="fromInput" readonly="true" value="${fns:getDictLabelFromMS(cancelTypeMapping.dataSource, 'order_data_source','Unknow' )}"></input>
					</c:when>
					<c:otherwise>
						<form:select disabled="${cancelTypeMapping.dataSource > 0?'true':'false'}" path="dataSource" cssClass="required input-medium" cssStyle="width: 220px;">
							<form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
										  itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">工单系统退单原因:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${cancelTypeMapping.cancelCode >0}">
						<form:hidden path="cancelCode"></form:hidden>
						<input class="fromInput" readonly="true" value="${fns:getDictLabelFromMS(cancelTypeMapping.cancelCode, 'cancel_responsible','Unknow' )}"></input>
					</c:when>
					<c:otherwise>
						<form:select disabled="${cancelTypeMapping.cancelCode > 0?'true':'false'}" path="cancelCode" cssClass="required input-medium" cssStyle="width: 220px;">
							<form:options items="${fns:getDictListFromMS('cancel_responsible')}"
										  itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</c:otherwise>
				</c:choose>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">B2B退单原因code:</label>
			<div class="controls">
				<form:input path="b2bCancelCode" htmlEscape="false" maxlength="9" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">B2B退单原因描述:</label>
			<div class="controls">
				<form:input path="b2bCancelName" htmlEscape="false" minLength="1" maxlength="100" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 260px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:b2bcanceltype:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>

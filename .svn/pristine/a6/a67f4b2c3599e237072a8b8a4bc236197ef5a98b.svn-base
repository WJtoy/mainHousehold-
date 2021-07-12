<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>保险价格配置</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
                rules: {
                    insurance: {min: 0},
                },
				submitHandler: function(form){
					loading('正在提交，请稍等...');
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
  </head>
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/md/insurancePrice/list">保险价格列表</a></li>
		<li class="active"><a href="javascript:void(0);">保险价格<shiro:hasPermission name="md:insuranceprice:edit">${not empty insurancePrice.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:insuranceprice:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="insurancePrice" action="${ctx}/md/insurancePrice/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<input id="oldProductCategoryId" name="oldProductCategoryId" type="hidden" value="${insurancePrice.category.id}"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">产品品类:</label>
			<div class="controls">
                <sys:treeselect id="category" name="category.id" value="${insurancePrice.category.id}" labelName="category.name" labelValue="${insurancePrice.category.name}"
					title="产品分类" url="/md/productcategory/treeData" cssClass="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">价格:</label>
			<div class="controls">
				<form:input path="insurance" htmlEscape="false" maxlength="10"  class="required number"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" class="input-xlarge" cssStyle="max-width: 1000px;max-height: 300px;min-width: 200px;min-height: 50px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:insuranceprice:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>

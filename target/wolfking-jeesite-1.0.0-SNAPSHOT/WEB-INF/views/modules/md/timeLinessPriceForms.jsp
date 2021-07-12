<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>产品分类时效奖励价格</title>
	<meta name="decorator" content="default" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<script type="text/javascript">
		$(document).ready(
		function()
		{
			$("#inputForm").validate(
				{
					submitHandler : function(form)
					{
						loading('正在提交，请稍等...');
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
					}
				});
		});
	</script>
</head>

<body>
	<ul class="nav nav-tabs">
		<li>
			<a href="${ctx}/md/timeLinessPrice/list">价格列表</a>
		</li>
		<li class="active">
			<a href="javascript:void(0);">添加</a>
		</li>
	</ul>
	<form:form id="inputForm" modelAttribute="timeLinessPrices" action="${ctx}/md/timeLinessPrice/saveTimeLinessPrices" method="post" class="form-horizontal">
		<sys:message content="${message}" />
		<c:if test="${canAction == true}">
		<div class="control-group">
			<label class="control-label">产品类别:</label>
			<div class="controls">
				<%--<sys:treeselect id="category" name="category.id" value="${timeLinessPrices.category.id}" labelName="category.name" labelValue="${timeLinessPrices.category.name}"
								title="产品分类" url="/md/productcategory/treeData" disabled="${(timeLinessPrices.category != null and timeLinessPrices.category.id != null)?'true':'false'}"
							cssClass="required"/>--%>
				<c:choose>
					<c:when test="${timeLinessPrices.category != null and timeLinessPrices.category.id != null}">
						<form:hidden path="category.id" readonly="true"></form:hidden>
						<form:input path="category.name" readonly="true"></form:input>
					</c:when>
					<c:otherwise>
						<sys:treeselect id="category" name="category.id" value="${timeLinessPrices.category.id}" labelName="category.name" labelValue="${timeLinessPrices.category.name}"
										title="产品分类" url="/md/productcategory/treeData" cssClass="required"/>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<c:forEach var="entity" items="${timeLinessPrices.list}" varStatus="i" begin="0">
			<div class="control-group">
				<label class="control-label">【${entity.timeLinessLevel.description}】:</label>
				<div class="controls">
					<form:hidden path="list[${i.index}].timeLinessLevel.value" htmlEscape="false"/>
					<form:input path="list[${i.index}].amount" htmlEscape="false" maxlength="10"  class="required number"/>
				</div>
			</div>
		</c:forEach>
		</c:if>
		<div class="form-actions">
			<c:if test="${canAction == true}">
			<shiro:hasPermission name="md:timelinessprice:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit"
					value="保 存" />&nbsp;</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="返 回"
				   onclick="history.go(-1)" />
		</div>
	</form:form>
</body>
</html>

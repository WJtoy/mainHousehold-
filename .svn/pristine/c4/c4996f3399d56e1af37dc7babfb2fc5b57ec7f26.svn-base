<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点付款失败确认</title>
	<meta name="decorator" content="default"/>
    <script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
	                $("#btnSubmit").attr("disabled", "disabled"); 
	                $("#btnCancel").attr("disabled", "disabled");
					loading('正在提交，请稍等...');
					var postUrl = "${ctx}/fi/servicepointwithdraw/issuesave";
					$.ajax({
						type: "POST",
						url: postUrl,
						data: {servicePointId:$("#servicePointId").val(),bankIssueValue:$("#bankIssue").val()},
						success: function (data) {
							if (data.success){
	    	            		top.$.jBox.tip('保存成功', 'success');
	    	            		parent.repage();
	    	            		parent.closethisfancybox();
					    	    parent.$.fancybox.close();
	    	            	}
	    	            	else{
	    	            		top.$.jBox.error(data.message);
	    	            	}
	                		$("#btnSubmit").removeAttr("disabled"); 
	                		$("#btnCancel").removeAttr("disabled"); 
	    	            	top.$.jBox.closeTip();
					        return false;
						},
						error: function (e) {
	                		$("#btnSubmit").removeAttr("disabled"); 
	                		$("#btnCancel").removeAttr("disabled"); 
	    	            	top.$.jBox.closeTip();
							top.$.jBox.error("付款错误:"+e,"错误提示");
						}
					});
					return false;
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
		
		function closethisfancybox(){
			parent.$.fancybox.close();
		}
	</script>
  </head>
  
  <body>
    <form:form id="inputForm" class="form-horizontal">
		<input id="servicePointId" name="servicePointId" type="hidden" value="${servicePointId}"/>
		<sys:message content="${message}"/>
		<legend>
			付款失败
		</legend>
		<div class="control-group">
			<label class="control-label">失败原因:</label>&nbsp;&nbsp;&nbsp;
			<c:set var="BankIssueTypeList" value="${fns:getDictListFromMS('BankIssueType')}" /><%-- 切换为微服务 --%>
			<select id="bankIssue" name="bankIssue" style="width:280px;">
				<c:forEach items="${BankIssueTypeList}" var="bankIssueDict">
				   <option value="${bankIssueDict.value}">${bankIssueDict.label}</option>
				</c:forEach>
			</select>
		</div>			
		<div class="form-actions">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();" />
		</div>
	</form:form>
  </body>
</html>

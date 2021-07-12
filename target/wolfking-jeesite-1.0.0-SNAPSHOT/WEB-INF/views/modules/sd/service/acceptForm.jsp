<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>接单</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
					loading('正在提交，请稍等...');
					$("#btnSubmit").prop("disabled", true);
					$.ajax({
						type: "POST",
						url: "${ctx}/sd/order/accept?"+ (new Date()).getTime(),
						data:$(form).serialize(),
						success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
					       if(data && data.success == true){
                               //回调父窗口方法
                               var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                               if(iframe != undefined){
                                   iframe.repage();
                               }
					    	   top.$.jBox.close();
					       }
					       else if( data && data.message){
					    	   top.$.jBox.error(data.message,"错误提示");
					       }
					       else{
					    	   top.$.jBox.error("接单错误","错误提示");
					       }
					       $('#btnSubmit').removeAttr('disabled');
					       return false;
						},
						error: function (e) {
							 // top.$.jBox.error("接单错误:"+e,"错误提示");
							$('#btnSubmit').removeAttr('disabled');
                            ajaxLogout(e.responseText,null,"接单错误，请重试!");
						}
					});
					top.$.jBox.closeTip();

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
			//parent.$.fancybox.close();
			top.$.jBox.close();
		}

	</script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
	</style>
</head>
<body >
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/accept" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group" style="margin-top:10px;">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" class="input-xxlarge" style="width:350px;"/>
			</div>
		</div>
		<div class="form-actions">
			<c:if test="${!empty order.id }">
			<shiro:hasPermission name="sd:order:accept"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="closethisfancybox();" />
		</div>
	</form:form>
</body>
</html>
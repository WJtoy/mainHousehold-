<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
	  <%@ include file="/WEB-INF/views/include/head.jsp" %>
	  <script src="${ctxStatic}/fancyBox/source/jquery.fancybox.js" type="text/javascript"></script>
	  <script src="${ctxStatic}/fancyBox/source/jquery.fancybox.pack.js" type="text/javascript"></script>
	  <link href="${ctxStatic}/fancyBox/source/jquery.fancybox.css" rel="stylesheet" />
	<title>对帐确认</title>
	<meta name="decorator" content="default"/>
    <script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
                    var auditType =$('input:radio[name="auditType"]:checked').val();
                    if(auditType == null || auditType == ''){
                        layerError("请勾选异常类型");
                        return false;
                    }
	                $("#btnSubmit").attr("disabled", "disabled"); 
	                $("#btnCancel").attr("disabled", "disabled"); 
					loading("正在提交，请稍等...");
					var postUrl = "";
					if ($("#processType").val() == "ChargeCreate"){
						postUrl = "${ctx}/fi/chargecreate/pending";
					}else if ($("#processType").val() == "CustomerCharge"){
						postUrl = "${ctx}/sd/customercharge/pendding";
					}else if ($("#processType").val() == "EngineerCharge"){
						postUrl = "${ctx}/sd/engineercharge/pendding";
					}
					$.ajax({
						type: "POST",
						url: postUrl,
						data: {ids:parent.ids.join(","), remarks:$("#remarks").val(),auditType:auditType},
						success: function (data) {
                            setTimeout(function() {
								if (data.success){
									top.$.jBox.tip("标记异常成功", "success");
									parent.repage();
									parent.$.fancybox.close();
									layerMsg('close');
								}
								else{
									top.$.jBox.error(data.message);
								}
								$("#btnSubmit").removeAttr("disabled");
								$("#btnCancel").removeAttr("disabled");
								top.$.jBox.closeTip();
                            }, 1500);
					        return false;
						},
						error: function (e) {
	                		$("#btnSubmit").removeAttr("disabled"); 
	                		$("#btnCancel").removeAttr("disabled");
	    	            	top.$.jBox.closeTip(); 
							top.$.jBox.error("标记异常错误:"+e,"错误提示");
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
    <form:form id="inputForm" modelAttribute="order" class="form-horizontal">
		<legend id="title">
				<c:choose>
					<c:when test="${processType eq 'ChargeCreate'}">
						<c:out value="标记异常订单确认" />
					</c:when>
					<c:when test="${processType eq 'CustomerCharge'}">
						<c:out value="客户对帐标记异常确认" />
					</c:when>
					<c:when test="${processType eq 'EngineerCharge'}">
						<c:out value="安维对帐标记异常确认" />
					</c:when>
			</c:choose>
		</legend>
		<input id="processType" name="processType" type="hidden" value="${processType}"/>
		<tags:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">异常类型:</label>
			<div class="controls">
				<div style="width: 100%">
					<c:forEach items="${fns:getDictListFromMS('fi_charge_audit_type')}" var="item">
						<input type="radio" id="auditType${item.value}" name="auditType" value="${item.value}">
						<label for="auditType${item.value}">${item.label}</label>&nbsp;&nbsp;
					</c:forEach>
				</div>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">问题描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" class="input-xxlarge" style="width:400px;"/>
			</div>
		</div>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();" />
		</div>
	</form:form>
  </body>
</html>

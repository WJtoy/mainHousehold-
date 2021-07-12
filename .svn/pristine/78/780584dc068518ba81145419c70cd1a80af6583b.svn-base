<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>对帐确认</title>
	<meta name="decorator" content="default"/>
    <script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
					var serviceChargeDiff = $("#serviceChargeDiff").val() == null || $("#serviceChargeDiff").val() ==  "" ? 0 : $("#serviceChargeDiff").val();
					var expressChargeDiff = $("#expressChargeDiff").val() == null || $("#expressChargeDiff").val() ==  "" ? 0 : $("#expressChargeDiff").val();
					var travelChargeDiff = $("#travelChargeDiff").val() == null || $("#travelChargeDiff").val() ==  "" ? 0 : $("#travelChargeDiff").val();
					var materialChargeDiff = $("#materialChargeDiff").val() == null || $("#materialChargeDiff").val() ==  ""  ? 0 : $("#materialChargeDiff").val();

					var timeLinessChargeDiff = $("#timeLinessChargeDiff").val() == null || $("#timeLinessChargeDiff").val() ==  ""  ? 0 : $("#timeLinessChargeDiff").val();
					var urgentChargeDiff = $("#urgentChargeDiff").val() == null || $("#urgentChargeDiff").val() ==  ""  ? 0 : $("#urgentChargeDiff").val();
					var praiseFeeDiff = $("#praiseFeeDiff").val() == null || $("#praiseFeeDiff").val() ==  ""  ? 0 : $("#praiseFeeDiff").val();

					var otherChargeDiff = $("#otherChargeDiff").val() == null || $("#otherChargeDiff").val() ==  ""  ? 0 : $("#otherChargeDiff").val();
					if (serviceChargeDiff == 0 && expressChargeDiff == 0 && travelChargeDiff == 0 && materialChargeDiff == 0 &&
							timeLinessChargeDiff == 0 && urgentChargeDiff == 0 && praiseFeeDiff == 0 && otherChargeDiff == 0){
   	            		top.$.jBox.error("您至少要输入其中一项的退补金额.");
   	            		return;
					}
	                $("#btnSubmit").attr("disabled", "disabled"); 
	                $("#btnCancel").attr("disabled", "disabled"); 
					loading('正在保存，请稍等...');
					var postUrl = "${ctx}/fi/customerchargewriteoff/writeoffsave";
					
					$.ajax({
						type: "POST",
						url: postUrl,
						data: {id:$("#id").val(), serviceCharge:serviceChargeDiff,expressCharge:expressChargeDiff,travelCharge:travelChargeDiff, materialCharge:materialChargeDiff,
													timeLinessCharge:timeLinessChargeDiff,urgentCharge:urgentChargeDiff,praiseFee:praiseFeeDiff,otherCharge:otherChargeDiff,
													remarks:$("#remarks").val()},
						success: function (data) {
							if (data.success){
	    	            		top.$.jBox.tip('数据保存成功', 'success');
	    	            		parent.repage();
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
							top.$.jBox.error("数据保存错误:"+e,"错误提示");
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
    <form:form id="inputForm" modelAttribute="customerCharge" class="form-horizontal">
		<form:input path="id" name="id" type="hidden"/>
		<sys:message content="${message}"/>
		<legend id="title">
			客户对帐异常退补处理
		</legend>
		<table style="width:100%;">
			<tr>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:90px;">服务费用:</label>
						<div class="controls" style="margin-left:110px;">
							<form:input path="serviceCharge" cssStyle="width:80px;" htmlEscape="false" maxlength="10" class="input-small" disabled="true"/>
							<input id="serviceChargeDiff" style="width:80px;" name="serviceChargeDiff" type="text" class="input-small number"/>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:90px;">时效费用:</label>
						<div class="controls" style="margin-left:110px;">
							<form:input path="timeLinessCharge" cssStyle="width:80px;" htmlEscape="false" maxlength="10" class="input-small" disabled="true"/>
							<input id="timeLinessChargeDiff" style="width:80px;" name="timeLinessChargeDiff" type="text" class="input-small number"/>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:90px;">快递费用:</label>
						<div class="controls" style="margin-left:110px;">
							<form:input path="expressCharge" cssStyle="width:80px;" htmlEscape="false" maxlength="10" class="input-small" disabled="true"/>
							<input id="expressChargeDiff" style="width:80px;" name="expressChargeDiff" type="text" class="input-small number"/>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:90px;">加急费用:</label>
						<div class="controls" style="margin-left:110px;">
							<form:input path="urgentCharge" cssStyle="width:80px;" htmlEscape="false" maxlength="10" class="input-small" disabled="true"/>
							<input id="urgentChargeDiff" style="width:80px;" name="urgentChargeDiff" type="text" class="input-small number"/>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:90px;">远程费用:</label>
						<div class="controls" style="margin-left:110px;">
							<form:input path="travelCharge" cssStyle="width:80px;" htmlEscape="false" maxlength="10" class="input-small" disabled="true"/>
							<input id="travelChargeDiff" style="width:80px;" name="travelChargeDiff" type="text" class="input-small number"/>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:90px;">好评费用:</label>
						<div class="controls" style="margin-left:110px;">
							<form:input path="praiseFee" cssStyle="width:80px;" htmlEscape="false" maxlength="10" class="input-small" disabled="true"/>
							<input id="praiseFeeDiff" style="width:80px;" name="praiseFeeDiff" type="text" class="input-small number"/>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:90px;">配件费用:</label>
						<div class="controls" style="margin-left:110px;">
							<form:input path="materialCharge" cssStyle="width:80px;" htmlEscape="false" maxlength="10" class="input-small" disabled="true"/>
							<input id="materialChargeDiff" style="width:80px;" name="materialChargeDiff" type="text" class="input-small number"/>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:90px;">其他费用:</label>
						<div class="controls" style="margin-left:110px;">
							<form:input path="otherCharge" cssStyle="width:80px;" htmlEscape="false" maxlength="10" class="input-small" disabled="true"/>
							<input id="otherChargeDiff" style="width:80px;" name="otherChargeDiff" type="text"  class="input-small number"/>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<div class="control-group">
						<label class="control-label" style="width:90px;">描述:</label>
						<div class="controls" style="margin-left:110px;">
							<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="180" class="input-xxlarge required" style="width:505px;"/>
						</div>
					</div>
				</td>
			</tr>
		</table>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();" />
		</div>
	</form:form>
  </body>
</html>

<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>结帐帐确认</title>
	<meta name="decorator" content="default"/>
    <script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
	                $("#btnSubmit").attr("disabled", "disabled"); 
	                $("#btnCancel").attr("disabled", "disabled"); 
					loading('正在提交，请稍等...');
					var postUrl = "${ctx}/fi/customerinvoice/save";
					$.ajax({
						type: "POST",
						url: postUrl,
						data: {ids:parent.ids.join(","), invoiceDate:$("#invoiceDate").val(),  remarks:$("#remarks").val()},
						async: false,
						success: function (data) {
							if (data.success){
	    	            		top.$.jBox.tip('结帐成功', 'success');
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
							top.$.jBox.error("结帐错误:"+e,"错误提示");
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
		<input id="customerId" name="customerId" type="hidden" value="${customerId}"/>
		<tags:message content="${message}"/>
		<legend>
			上游客户结帐确认
		</legend>
		<div class="control-group">
			<label class="control-label">结帐金额:</label>
			<div class="controls">
				<input id="totalCharge" name="totalCharge" type="text" readonly="readonly" value="${totalCharge}"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">结帐日期:</label>
			<div class="controls">
				<input id="invoiceDate" name="invoiceDate" type="text" readonly="readonly"  maxlength="20" class="Wdate required"
				onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" value="${currentDate}"/>
			</div> 
		</div> 
		<div class="control-group"> 
			<label class="control-label">结帐描述:</label> 
			<div class="controls"> 
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="180" class="input-xxlarge" style="height:59px;width:206px;"/> 
			</div> 
		</div> 
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();" />
		</div>
	</form:form>
  </body>
</html>

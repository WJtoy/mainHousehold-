<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
<title>充值</title>
<meta name="decorator" content="default" />
<%@ include file="/WEB-INF/views/include/head.jsp" %>
<script type="text/javascript">
    top.layer.closeAll();
    var clickTag = 0;
	$(document).ready(function(){
		$("#inputForm").validate({
			rules: {
				amount:{ min: 500, max : 10000000}
			},
            messages: {
                amount : { required: "请输入充值金额", min: "充值最小金额不能低于500.", max: "充值最大金额不能高于10000000."}
            },
			submitHandler : function(form)
			{
                if(clickTag == 1){
                    return false;
                }
                clickTag = 1;
                var $btnSubmit = $("#btnSubmit");
                $btnSubmit.attr('disabled', 'disabled');
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

    $(function () {
        $("input:radio[name=charge]").change(function () {

            var res= $(this).val();
            if(res=="0")
            {
                $("#customAmountDiv").show();
                $("#amount").val("500");
                $("#amount").select().focus();

            }else
            {
                $("#customAmountDiv").hide();
                $("#amount").val(res);
            }
        });
    });
</script>
</head>
<body>
	<br />
	<div style="padding-left: 400px;padding-top: 20px;">
		<sys:message content="${message}"/>
		<form:form id="inputForm" modelAttribute="customerCurrency" action="${ctx}/fi/customercurrency/chargeonline" method="post"
			class="form-horizontal">
			<div class="control-group">
				<label class="control-label">客户:</label>
				<div class="controls">
					<form:input path="customer.name" readonly="true" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">充值金额:</label>
				<div class="controls">
					<input name="charge"  type="radio" value="500" checked="checked">500
					<input name="charge"  type="radio" value="1000">1000
					<input name="charge"  type="radio" value="2000">2000
					<input name="charge"  type="radio" value="5000">5000
					<input name="charge"  type="radio" value="10000">10000
					<input name="charge"  type="radio" value="0">自定义金额
				</div>
			</div>
			<div id="customAmountDiv" style="display: none;" class="control-group">
				<label class="control-label">自定义金额:</label>
				<div class="controls">
					<input id="amount" name="amount" type="text" value="500" htmlEscape="false"  maxlength="10" class="required number"/>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">备注:</label>
				<div class="controls">
					<textarea style="text-align: left;" id="remarks" name="remarks"
						htmlEscape="false" rows="3" maxlength="200" class="input-xlarge"></textarea>
				</div>
			</div>
			<div class="control-group" style="padding-left: 50px;">
				<input checked="checked" type="radio" name="BC" value="ZFB">
				<img alt="ALipay" src="${ctxStatic}/images/bank/zhifubao.png">

				<input type="radio" name="BC" value="CCB"> <img alt="CCB"
					src="${ctxStatic}/images/bank/95533.png"> <input type="radio"
					name="BC" value="ICBCB2C"> <img alt="ICBC"
					src="${ctxStatic}/images/bank/95588.png">
			</div>
			<div class="control-group" style="padding-left: 50px;">
				<input type="radio" name="BC" value="ABC"> <img alt="ABC"
					src="${ctxStatic}/images/bank/95599.png"> <input type="radio"
					name="BC" value="BOCB2C"> <img alt="BOC"
					src="${ctxStatic}/images/bank/95566.png"> <input type="radio"
					name="BC" value="CMB"> <img alt="CMB"
					src="${ctxStatic}/images/bank/95555.png">
			</div>

			<div class="form-actions">
<%--				<c:if test="${canSave}">--%>
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="充值" />&nbsp;
<%--				</c:if>--%>
			</div>
		</form:form>
		
	</div>

</body>
</html>
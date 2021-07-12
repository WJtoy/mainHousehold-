<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>付款确认</title>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script type="text/javascript">
		$(document).ready(function() {
            jQuery.validator.addMethod("outOfDate", function(value, element){
                var inputDate = new Date(Date.parse(value.replace("-", "/")));
                var currentDate = new Date();
                return inputDate.getFullYear()*100+inputDate.getMonth() == currentDate.getFullYear()*100+currentDate.getMonth();
            }, "付款日期不能跨月");

            var needPay=parseFloat($("#hNeedPay").val()).toFixed(2);
            // var totalMinus=parseFloat($("#hTotalMinus").val()).toFixed(2);
            // totalMinus=0-totalMinus+1;
		    var canPay = $("#canPay").val();
		    if (canPay == 0) {
				top.$.jBox.error("该网点已经有付款在处理,请先确认上次付款结果.", "网点付款");
                parent.$.fancybox.close();
			}

            $("#applyAmount").val($("#totalAmount").val());
			calcRealAmount();

            $("#aIssue").fancybox({
                fitToView : false,
                width  : 700,
                height  : 330,
                autoSize : false,
                closeClick : false,
                type  : 'iframe',
                openEffect : 'none',
                closeEffect : 'none'
            });

			$(document).on("blur", "#applyAmount", function(){
				calcRealAmount();
			});
			
			$(document).on("click", "#btnSubmit", function(){
                var applyAmount=parseFloat($("#applyAmount").val()).toFixed(2);
				if(needPay == applyAmount) {
					$("#inputForm").submit();
	
				} else {

					var submit = function (v, h, f) {
						if (v == 'ok') {
							top.$.jBox.close();
							$("#inputForm").submit();

						}
						else if (v == 'cancel') {
						}
						return true; //close
					};

					top.$.jBox.confirm('银行实际付款和应付总额不一致,确认继续付款吗？', '网点付款', submit);
				
				}
			});
			
			$("#inputForm").validate({
				 rules:
				 {
					 applyAmount:{ required: true, range: [0.1,needPay]},
                     invoiceDate: { outOfDate: true}
				 },
				 messages: 
				 {
					 applyAmount:{ required: "请输入银行实际付款！", range: 0.1+"~"+needPay},
                     invoiceDate: { outOfDate: "付款日期不能跨月"}
				 },
				submitHandler: function(form){				
	                $("#btnSubmit").attr("disabled", "disabled"); 
	                $("#btnCancel").attr("disabled", "disabled"); 
					loading('正在提交，请稍等...');
					var postUrl = "${ctx}/fi/servicepointwithdraw/paysave";
					$.ajax({
						type: "POST",
						url: postUrl,
						data: {servicePointId:$("#servicePointId").val(), invoiceDate:$("#invoiceDate").val(), balance:0, debtsAmount:0,
                            realAmount:$("#realAmount").val(), paymentType:$("#paymentType").val(), bank:$("#hBank").val(),
                            branch:$("#branch").val(), bankNo:$("#bankNo").val(), bankOwner:$("#bankOwner").val(),
							bankOwnerIdNo:$("#hBankOwnerIdNo").val(), bankOwnerPhone:$("#hBankOwnerPhone").val(),
                            invoiceDate:$("#invoiceDate").val(), remarks:$("#remarks").val(), qYear:$("#qYear").val(),
							qMonth:$("#qMonth").val(), totalMinus:$("#hTotalMinus").val(), platformFee:$("#platformFee").val()},
						async: false,
						success: function (data) {
                            setTimeout(function() {
								if (data.success){
									top.$.jBox.tip('付款成功', 'success');
										parent.repage();
									parent.$.fancybox.close();
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
							top.$.jBox.error("付款错误:网络请求失败,请重试.","错误提示");
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

            $("#applyAmount").focus();
			
		});

        $(document).on("click", "#btnIssue", function () {
            $("#aIssue").attr("href", "${ctx}/fi/servicepointwithdraw/issueget?servicePointId="+$("#servicePointId").val());
            $("#aIssue").click();
        });

        function calcRealAmount() {
			var platformFee = myTofixed((parseFloat($("#applyAmount").val())*${platformFeeRate}), 2);
			var applyAmount = myTofixed(parseFloat($("#applyAmount").val()), 2);
			var realAmount = applyAmount - platformFee;
			$("#platformFee").val(platformFee);
			$("#realAmount").val(myTofixed(realAmount, 2));
		}
	    
	    function repage(){
	    	parent.repage();
	    }
		
		function closethisfancybox(){
			parent.$.fancybox.close();
		}

		var myTofixed =function(num,fractionDigits) {
			return Math.round(num*Math.pow(10,fractionDigits))/Math.pow(-10,fractionDigits);
		};
	</script>
  </head>
  
  <body>
    <form:form id="inputForm" class="form-horizontal">
		<input id="hBank" name="hBank" type="hidden" value="${bank}"/>
		<input id="paymentType" name="paymentType" type="hidden" value="${paymentType}"/>
		<input id="processType" name="processType" type="hidden" value="${processType}"/>
		<input id="servicePointId" name="servicePointId" type="hidden" value="${servicePointId}"/>
		<input id="canPay" name="canPay" type="hidden" value="${canPay}"/>
		<input id="branch" name="branch" type="hidden" value="${branch}"/>
		<input id="qYear" name="qYear" value="${qYear}" type="hidden"/>
		<input id="qMonth" name="qMonth" value="${qMonth}" type="hidden"/>
        <input id="hTotalAmount" name="hTotalAmount" value="${totalAmount}" type="hidden"/>
        <input id="hTotalMinus" name="hTotalMinus" value="${totalMinus}" type="hidden"/>
		<input id="hTotalDeductedAmount" name="hTotalDeductedAmount" value="${totalDeductedAmount}" type="hidden"/>
        <input id="hNeedPay" name="hNeedPay" value="${totalAmount+totalMinus+totalDeductedAmount}" type="hidden"/>
		<input id="hMinusInfo" name="hMinusInfo" value="${minusInfo}" type="hidden"/>
		<input id="hBankOwnerIdNo" name="hBankOwnerIdNo" value="${bankOwnerIdNo}" type="hidden"/>
		<input id="hBankOwnerPhone" name="hBankOwnerPhone" value="${bankOwnerPhone}" type="hidden"/>
		<a id="aIssue" type="hidden" href="" class="fancybox"  data-fancybox-type="iframe"></a>
		<sys:message content="${message}"/>
		<legend>
			网点及订单信息 <c:if test="${invoiceFlag == 1}"><span class="label status_Canceled">开票</span></c:if><c:if test="${discountFlag == 1}"><span class="label status_Canceled">扣点</span></c:if>
		</legend>
		<table style="width:100%;">
			<tr>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:60px;">网点编号:</label>
						<div class="controls" style="margin-left:70px;">
							<input id="servicePointNo" name="servicePointNo" type="text" readonly="readonly" value="${servicePointNo}" style="width:220px;"/>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:60px;">网点名称:</label>
						<div class="controls" style="margin-left:70px;">
							<input  id="servicePointName" name="servicePointName" type="text" readonly="readonly" value="${servicePointName}"
									<c:if test="${hBankIssue gt 0}">title="${fns:getDictLabelFromMS(hBankIssue, 'BankIssueType', '')}"</c:if><%-- 切换为微服务 --%>
									style="width:220px;
									<c:if test="${hBankIssue gt '0'}">background-color: #f2dede;</c:if>"/>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:60px;">联系电话:</label>
						<div class="controls" style="margin-left:70px;">
							<input id="contactPhone" name="contactPhone" type="text" readonly="readonly" value="${phone1} ${phone2}" style="width:220px;"/>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:60px;">开户银行:</label>
						<div class="controls" style="margin-left:70px;">
							<input id="bank" name="bank" type="text" readonly="readonly" value="${fns:getDictLabelFromMS(bank, 'banktype', '')}${branch}" style="width:220px;"/><%--切换为微服务--%>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:60px;">银行帐号:</label>
						<div class="controls" style="margin-left:70px;">
							<input id="bankNo" name="bankNo" type="text" readonly="readonly" value="${bankNo}" style="width:220px;"/>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:60px;">开户人:</label>
						<div class="controls" style="margin-left:70px;">
							<input id="bankOwner" name="bankOwner" type="text" readonly="readonly" value="${bankOwner}" style="width:220px;"/>
						</div>
					</div>
				</td>
			</tr>
		</table>
		<legend>
			网点付款信息
		</legend>
		<table style="width:100%;">
			<tr>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:60px;">应付总额:</label>
						<div class="controls" style="margin-left:70px;">
							<input id="totalAmount" name="totalAmount" type="text" value="${totalAmount+totalMinus+totalDeductedAmount}" style="width:220px; font-weight: bold; color: red;" readonly="readonly"/>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:60px;">实际付款:</label>
						<div class="controls" style="margin-left:70px;">
                            <input id="applyAmount" class="required number" name="applyAmount" type="text" value="" style="width:65px; color:blue;"/>
							<span style="font-size: 18px">-</span>
							<input id="platformFee" class="required number" name="platformFee" type="text" value="" style="width:65px;" readonly="readonly"/>
							<span style="font-size: 18px">=</span>
							<input id="realAmount" class="required number" name="realAmount" type="text" value="" style="width:65px;" readonly="readonly"/>
						</div>
					</div>
				</td>
				<td>
					<div class="control-group">
						<label class="control-label" style="width:60px;">付款日期:</label>
						<div class="controls" style="margin-left:70px;">
							<input id="invoiceDate" name="invoiceDate" type="text" readonly="readonly"  maxlength="20" class="Wdate required"
								   onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" value="${currentDate}" style="width: 220px;"/>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<td colspan="3">
					<div class="control-group">
						<label class="control-label" style="width:60px;">付款描述:</label>
						<div class="controls" style="margin-left:70px;">
							<input id="remarks" name="remarks" type="text" value="${remarks}" style="width:95%;"/>
						</div>
					</div>
				</td>
			</tr>
			<c:if test="${totalMinus != 0}">
			<tr>
				<td colspan="4" style="text-align: center; padding-top: 10px;">
                    <span class="alert alert-block" style="line-height: 30px;height: 30px;">
                        <span>待支付: ${totalAmount}, 待扣款：${minusInfo}</span>
                    </span>
				</td>
			</tr>
			</c:if>
			<tr>
				<td colspan="4">
					<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
						<input id="btnIssue" class="btn btn-danger" type="button" value="付款失败"/>&nbsp;&nbsp;
						<input id="btnSubmit" class="btn btn-primary" type="button" value="保 存"/>&nbsp;&nbsp;
						<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();" />
					</div>
				</td>
			</tr>
		</table>
	</form:form>
  </body>
</html>

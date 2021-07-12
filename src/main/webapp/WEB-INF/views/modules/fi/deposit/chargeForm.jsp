<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE HTML>
<head>
	<title>财务质保金线下充值</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<script type="text/javascript">
		var clickTag = 0;
		$(document).ready(function() {
			$('#remarks').keyup(function() {
				var len=this.value.length;
				$('#remarks_cnt').text(len);
			});

			$("#inputForm").validate({
				 rules: {
					 paymentType:{
						 required: true,
						 minlength: 1
						 },
                     amount:{
						 required: true
					 },
					 createDate:{
					 	required: true
					 }
				 },
				 messages: {
                     paymentTypeId:{ required: "请选择支付类型！",minlength: "请选择支付类型！"},
                     amount : { required: "请输入充值金额"},
					 createDate:{required: "请输入充值时间"}
				 },
				submitHandler: function(form){
					if(clickTag == 1){
						return false;
					}
					clickTag = 1;
					//check input
					var sid = $("#servicePointId").val();
					if(sid === '' || sid === '0'){
						layerMsg("请选择要充值的网点");
						clickTag = 0;
						return false;
					}
					var sname = $("#servicePointName").val();
					var paymentType = $("#paymentType").val();
					if(paymentType === null || paymentType === undefined){
						layerMsg("请选择支付方式");
						clickTag = 0;
						return false;
					}
					var amount = parseFloat($("#amount").val());
					if(amount === 0){
						layerMsg("请输入充值金额");
						clickTag = 0;
						return false;
					}
					var createDate = $("#createDate").val();
					if(createDate === ''){
						layerMsg("请输入充值时间");
						clickTag = 0;
						return false;
					}
					var currencyNo = $("#currencyNo").val();
					var msg = "是否确认充值质保金额 <span style='color: #2AA0DE'>"+ amount +" 元</span>？";
					var btnOkTitle = '确定';
					//是否达到要求
					var defineBalance = parseFloat($("#balance").val());
					var balance = parseFloat($("#beforeBalance").val());
					if(defineBalance < balance + amount){
						msg = "此次充值金额已超出<span style='color: #2AA0DE'>应缴金额</span>，是否保存？";
						btnOkTitle = '保存';
					}
					layer.confirm(
						msg, {
							title:'提示',
							btn: [btnOkTitle,'取消'] //按钮
						}, function(index){
							layer.close(index);
								var loadingIndex = layerLoading('正在提交，请稍等...');
								$("#btnSubmit").attr("disabled", "disabled");
								$("#btnCancel").attr("disabled", "disabled");
								var entity = {};
								entity['servicePoint.id'] = sid;
								entity['servicePoint.name'] = sname;
								entity['amount'] = amount;
								entity['balance'] = 0.00;
								entity['beforeBalance'] = 0.00;
								entity['paymentType'] = paymentType;
								entity['createDate'] = createDate;
								entity['currencyNo'] = currencyNo;
								entity['remarks'] = $("#remarks").val();
								$.ajax({
									url:"${ctx}/fi/servicepoint/deposit/chargeSubmit",
									type:"POST",
									data: entity,
									dataType:"json",
									success: function(data){
										//提交后的回调函数
										if(loadingIndex) {
											top.layer.close(loadingIndex);
										}
										if(ajaxLogout(data)){
											setTimeout(function () {
												clickTag = 0;
												$("#btnSubmit").removeAttr('disabled');
												$("#btnCancel").removeAttr('disabled');
											}, 2000);
											return false;
										}
										if (data.success) {
											layerMsg("网点质保金充值成功。");
											closeme();
										}else{
											setTimeout(function () {
												clickTag = 0;
												$("#btnSubmit").removeAttr('disabled');
												$("#btnCancel").removeAttr('disabled');
											}, 2000);
											layerError(data.message, "错误提示");
										}
										return false;
									},
									error: function (data)
									{
										if(loadingIndex) {
											top.layer.close(loadingIndex);
										}
										setTimeout(function () {
											clickTag = 0;
											$("#btnSubmit").removeAttr('disabled');
											$("#btnCancel").removeAttr('disabled');
										}, 2000);
										ajaxLogout(data,null,"数据保存错误，请重试!");
									}
								});
					},function(index){//cancel
							clickTag = 0;
					});

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

		function servicePointSelect_CallBack(data){
			$("#inputForm [id='servicePoint.id']").val(data.id);
			$("#inputForm [id='servicePoint.name']").val(data.name);
			$("#depositLevel").val(data.mdDepositLevel.name);
			$("#balance").val(data.deposit + "元");
			$("#beforeBalance").val(data.finance.deposit+"元");
		}

		var this_index = top.layer.index;
		function closeme(){
			top.layer.close(this_index);
		}
	</script>
</head>
<body>
	<form:form id="inputForm" modelAttribute="depositCurrency" method="post" class="form-horizontal">
		<sys:message content="${message}"/>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label"><span class="form-required">*</span>网点名称:</label>
					<div class="controls">
						<md:selectServicePointForDeposit id="servicePoint" name="servicePoint.id" value="${depositCurrency.servicePoint.id}"
														 labelName="servicePoint.name" labelValue="${depositCurrency.servicePoint.name}"
														 title="网点" allowClear="false" disabled="${depositCurrency.servicePoint != null && depositCurrency.servicePoint.id != null && depositCurrency.servicePoint.id > 0}"
														 callbackmethod="servicePointSelect_CallBack" width="1400" height="760" cssClass="required" cssStyle="width:185px;" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">质保等级:</label>
					<div class="controls">
						<input id="depositLevel" name="depositLevel" readonly="readonly" class="input-block-level" type="text" value="${depositCurrency.actionTypeName}" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">应缴金额:</label>
					<div class="controls">
						<input id="balance" name="balance" readonly="readonly" class="input-block-level" type="text" value="${depositCurrency.balance}元" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">已缴金额:</label>
					<div class="controls">
						<input id="beforeBalance" name="beforeBalance" readonly="readonly" class="input-block-level" type="text" value="${depositCurrency.beforeBalance}元" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label"><span class="form-required">*</span>充值类型:</label>
					<div class="controls">
						<form:select path="paymentType" cssClass="required input-block-level" >
							<form:options items="${paymentTypes}" itemLabel="name" itemValue="value" htmlEscape="false" />
						</form:select>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label"><span class="form-required">*</span>充值金额:</label>
					<div class="controls ">
						<div class="input-append input-block-level" style="margin-left:0px;">
						<input id="amount" name="amount" type="text" class="required number"  style="width:197px;" value="0"   />
						<span class="add-on">元</span>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label"><span class="form-required">*</span>充值时间:</label>
					<div class="controls">
						<input id="createDate" name="createDate" readonly="readonly" class="input-block-level Wdate" type="text" value="${fns:formatDate(depositCurrency.createDate,'yyyy-MM-dd HH:mm:ss')}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',maxDate:'%y-%M-%d %H:%m:%s',isShowClear:false});" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">相关单号:</label>
					<div class="controls">
						<input id="currencyNo" name="currencyNo" class="input-block-level" placeholder="输入转账单号" type="text" maxlength="36" value="" />
					</div>
				</div>
			</div>
		</div>

		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注:</label>
					<div class="controls remarks">
						<form:textarea path="remarks" rows="3" htmlEscape="false" cssStyle="overflow: hidden;resize:none;" cssClass="input-block-level" maxlength="200" />
						<span class="wordsNum"><span id="remarks_cnt">0</span>/200</span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid form-button-actions">
			<div class="span12" align="right" style="padding-bottom: 2px;">
			<input id="btnSubmit" class="btn btn-primary btn-save" type="submit" value="保 存"  />&nbsp;
			<input id="btnCancel" class="btn btn-cancel" type="button" value="取 消" onclick="closeme();"/>
			</div>
		</div>
	</form:form>
</body>
<style type="text/css">
	.form-required {
		color: #ee4247;
	}
	#inputForm{
		padding: 15px;
	}
	.form-button-actions {
		/*position: fixed;*/
		left: 0px;
		bottom: 0;
		width: 100%;
		height: 60px;
		background: #fff;
		z-index: 10;
		/*padding-left: 190px;*/
		border-top: 1px solid #e5e5e5;
		line-height: 40px;
		/*margin-left: 0px !important;*/
		padding-right: 20px;
		padding-bottom: 2px;
	}
	.form-button-actions .btn-save{
		width: 96px;height: 38px;margin-top: 12px;margin-right: 5px;
	}
	.form-button-actions .btn-cancel{
		width: 96px;height: 38px;margin-top: 12px;
	}
	.form-horizontal .control-label{
		width: 100px !important;
	}
	.form-horizontal .controls {
		margin-left: 120px !important;
	}
	.form-horizontal div.remarks {position: relative;}
	.form-horizontal .controls span.wordsNum {
		/*text-align: right;width: 100%;display: inline-block;position: inherit;padding-right: 0px;*/
		text-align: right;width: 100%;display: inline-block;position: absolute;right: 5px;bottom: 2px;
	}
</style>
</html>
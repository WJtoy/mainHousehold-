<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
<title>客户充值(微服务方式)</title>
<meta name="decorator" content="default" />
<%@ include file="/WEB-INF/views/include/head.jsp" %>
<script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
<link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
<script type="text/javascript">
    $(document).ready(function() {
        $("#tranferNoPic").viewer();
        $("#weChatTranferNoPic").viewer();
    });
    top.layer.closeAll();
    var clickTag = 0;
	$(document).ready(function(){
        $.validator.addMethod("levelLimit",function(value, element){
            var payType = $("input:radio[name='BC']:checked ").val();
            var amount = $("#amount").val();
            if(amount<500){
                $(element).data('error-msg','充值最小金额不能低于500.');
                return false;
            }else{
                if(payType=="ZFB" && amount>5000){
                    $(element).data('error-msg','支付宝线上充值金额大于5000,请使用线下充值');
                    return false;
                }else if(amount>10000000){
                    $(element).data('error-msg','充值金额不能大于10000000.');
                    return false;
                }
            }
            return true;
        },function(params, element){ return $(element).data('error-msg');});
		// 上线前，最小值调整为:500
		$("#inputForm").validate({
			rules: {
				/*amount:{ min: 500, max : 10000000}*/
                amount:{levelLimit:true}
			},
            messages: {
                /*amount : { required: "请输入充值金额", min: "充值最小金额不能低于500.", max: "充值最大金额不能高于10000000."}*/
                amount:{required: "请输入充值金额"}
            },
			submitHandler : function(form)
			{

                if(clickTag == 1){
                    return false;
                }
                var payType = $("input:radio[name='BC']:checked ").val();
                if(payType=="OFFL" || payType=="WeChat"){
                    var payContent ="";
                    var offlinePayType = 10;
                    var transferNo = $("#transferNo").val();
                    if(payType=="OFFL"){
                        offlinePayType = 10;
                        payContent='支付宝';
                        if(transferNo.length!=28){
                            clickTag = 0;
                            layerError(payContent+"交易单号长度不正确,长度必须为28位", "错误提示");
                            return false;
						}
						if(!isDate(transferNo)){
                            clickTag = 0;
                            layerError(payContent+"交易单号格式错误", "错误提示");
                            return false;
						}
					}else{
                        offlinePayType = 20;
                        payContent='微信';
                        if(transferNo.length!=32){
                            clickTag = 0;
                            layerError(payContent+"交易单号长度不正确,长度必须为32位", "错误提示");
                            return false;
                        }
					}
                    var phone = $("#phone").val();
                    /*if(transferNo==null || transferNo==''){
                        layerError(payContent+"交易单号不能为空", "错误提示");
                        clickTag = 0;
                        return false;
                    }*/
                    if(phone==null || phone==''){
                        layerMsg("手机号不能为空")
                        clickTag = 0;
                        return false;
                    }
                    layer.confirm("是否确认充值"+"<span style='color: #0096DA'>"+$("#amount").val()+"元</span>到账户？", {
                        title:'提示',
                        btn: ['确认','取消'] //按钮
                    }, function(index){
                        layer.close(index);
                        clickTag = 1;
                        var backAmount = 0.0;
                        if($("#backAmount").val()>0){
                            backAmount = $("#backAmount").val();
                        }
                        $("#btnSubmit").prop("disabled",true);
                        var loadingIndex = layerLoading('正在提交，请稍等...');
                        $.ajax({
                            url:"${ctx}/fi/customer/offline/recharge/save",
                            type:"POST",
                            data:{id:$("#id").val(),customerId:$("[id='customer.id']").val(),pendingAmount:$("#amount").val(),transferNo:transferNo,
								  phone:phone,remarks:$("#remarks").val(),backAmount:backAmount,payType:offlinePayType},
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
                                    }, 2000);
                                    return false;
                                }
                                if (data.success) {
                                    layerMsg("保存成功");
                                    setTimeout(function () {
                                        window.location.href="${ctx}/fi/customer/offline/recharge/successPage?customerId="+data.data.customerId +"&amount="+data.data.pendingAmount
                                    }, 2000);
                                }else{
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $("#btnSubmit").removeAttr('disabled');
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
                                }, 2000);
                                ajaxLogout(data,null,"数据保存错误，请重试!");
                                //var msg = eval(data);
                            }
                        });
                    });
				}else{
                    //$("#inputForm").attr("action","${ctx}/fi/recharge/customer/submitForm");
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
                    if(payType=="ZFB"){
                        if($("#amount").val()>5000){
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                            layerError("支付宝线上充值单笔金额不能超过5000元,请使用线下充值", "错误提示");
                            return false;
						}
					}
                    // loading('正在提交，请稍等...');
                    form.submit();
				}
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
        var amount = $("#amount").val();
        if(amount>0){
            getBackAmount();
		}
	});

    $(function () {
        $("input:radio[name=charge]").change(function () {
            var res= $(this).val();
            var payType = $("input:radio[name='BC']:checked ").val();
            if(res=="0")
            {
                $("#customAmountSpan").show();
                $("#amount").val("2000");
                $("#amount").removeAttr("readonly");
                $("#amount").select().focus();
                if(payType=='WeChat'){//微信
                    $("#payCode").attr("src","${ctxStatic}/images/recharge/weChat0.png");
				}
            }else {
                $("#customAmountSpan").hide();
                showPayCode(payType,res);
                $("#amount").val(res);
                $("#amount").attr("readonly","readonly");
            }
            getBackAmount();
         });
        
        $("input:radio[name='BC']").change(function () {
            var payType = $(this).val();
			if($(this).val()=='OFFL'){
			    $("#offlineDiv").show();
			    $("#transferNo").attr('placeholder','请输入支付宝交易单号');
			    $("#payTypeTitle").text("请使用支付宝扫码充值！")
                $("#payTypeTips").text("支付宝");
			    $("#fifthTip").show();
                $("#onlinePayAmount").hide();
                $("#manualAmount").hide();
                $("#offlineAliPay").show();
                $("#chargeSpan6").show();
                $("#chargeSpan12").hide();//隐藏3000的选项
                $("#charge5").css("margin-left","0px");
                $("#charge5").attr("checked",true);
                var res = $("input:radio[name='charge']:checked ").val();
                $("#amount").val(res)
                showPayCode('OFFL',res);
                getBackAmount()
			}else if($(this).val()=='WeChat'){
			    var beforePayType = $("#beforeSelect").val();
                $("#offlineDiv").show()
                $("#transferNo").attr('placeholder','请输入微信交易单号');
                $("#payTypeTitle").text("请使用微信扫码充值！")
                $("#payTypeTips").text("微信");
                $("#fifthTip").hide();
                $("#onlinePayAmount").show();
                $("#manualAmount").show();
                $("#offlineAliPay").hide();
                $("#chargeSpan6").show();
                $("#chargeSpan12").hide();//隐藏3000的选项
                $("#charge5").css("margin-left","50px");
                var beforeCharge = $("input:radio[name='charge']:checked ").val();
                if(beforePayType=="OFFL"){ //如果上次选中的是支付宝线下充值,才需要重新设置默认金额，计算返现
                    $("#charge4").attr("checked",true);
                    $("#amount").val('2000');
                    $("#customAmountSpan").hide();
                    getBackAmount();
				}else if(beforePayType=="ZFB" && beforeCharge==3000){ //如果上次选中的是线上支付宝,并且选中的是3000
                    $("#charge7").attr("checked",true); //设置选中自定义
                    $("#amount").val('3000');
                    $("#customAmountSpan").show();
                    $("#amount").removeAttr("readonly");
                }
                var res = $("input:radio[name='charge']:checked ").val();
                showPayCode('WeChat',res);
		    }else{
                $("#onlinePayAmount").show();
                $("#manualAmount").show();
                $("#offlineDiv").hide();
                $("#offlineAliPay").hide();
                $("#charge5").css("margin-left","50px");
                var beforePayType = $("#beforeSelect").val();
                var amount = $("#amount").val();
                var beforeCharge = $("input:radio[name='charge']:checked ").val();
                if(beforePayType=="OFFL"){
                    $("#charge4").attr("checked",true);
                    $("#customAmountSpan").hide();
                    $("#amount").val("2000")
                    getBackAmount();
                    if($(this).val()=='ZFB'){
                        $("#chargeSpan6").hide();
                        $("#chargeSpan12").show();//显示3000的选项
                    }else{
                        $("#chargeSpan6").show();
                        $("#chargeSpan12").hide();
                    }
				}else if(beforePayType=="ZFB" && beforeCharge==3000){ ////如果上次选中的是线上支付宝,并且选中的是3000
                    $("#charge7").attr("checked",true); //设置选中自定义
                    $("#amount").val('3000');
                    $("#customAmountSpan").show();
                    $("#amount").removeAttr("readonly");
                    $("#chargeSpan6").show(); //显示10000
                    $("#chargeSpan12").hide(); //隐藏3000
                }else if($(this).val()=='ZFB'){
                    if(amount>5000){ //如果当前选中的是支付宝,选中的金额大于5000,则将金额改为2000默认值
                        $("#charge4").attr("checked",true);
                        $("#customAmountSpan").hide();
                        $("#amount").val("2000")
                        getBackAmount();
                    }
                    $("#chargeSpan6").hide();
                    $("#chargeSpan12").show();
                }else{
                    $("#chargeSpan6").show();
                    $("#chargeSpan12").hide();
                }
			}
			//记录当前选中的支付方式
			$("#beforeSelect").val(payType);
        });

        $("#amount").on('input propertychange',function () {
            var amount = $("#amount").val();
            if(amount!=null && amount!=''){ //检验输入的是否是合法金额
                var fix_amountTest=/^(([1-9]\d*)|\d)(\.\d{1,3})?$/;
                if(fix_amountTest.test(amount)==false){
                    return false;
                }
			}
            if(amount>'0'){
                getBackAmount();
            }else{
                $("#cashBackDiv").css("top","14px");
                $("#backMoneySpan").hide();
                $("#backMoneySpan").text("0元");
				//微信
                $("#weChatCashBackDiv").css("top","14px");
                $("#weChatBackMoneySpan").hide();
                $("#weChatBackMoneySpan").text("0元");

                $("#backAmount").val("0");
            }
        });
    });

    function getBackAmount() {
        $.ajax({
            url:"${ctx}/fi/customer/offline/recharge/getMoneyBack?amount="+$("#amount").val(),
            type:"POST",
            dataType:"json",
            success: function(data){
                if (data.success) {
                    if(data.data>0){
                        $("#cashBackDiv").css("top","5px");
                        $("#backMoneySpan").show();
                        $("#backMoneySpan").text("返现"+data.data+"元");
						//微信
                        $("#weChatCashBackDiv").css("top","5px");
                        $("#weChatBackMoneySpan").show();
                        $("#weChatBackMoneySpan").text("返现"+data.data+"元");

                        $("#backAmount").val(data.data);

                    }else{
                        $("#cashBackDiv").css("top","13px");
                        $("#backMoneySpan").hide();
                        $("#backMoneySpan").text("0元");

						//微信
                        $("#weChatCashBackDiv").css("top","13px");
                        $("#weChatBackMoneySpan").hide();
                        $("#weChatBackMoneySpan").text("0元");

                        $("#backAmount").val("0");
                    }
                }else{
                    $("#cashBackDiv").css("top","13px");
                    $("#backMoneySpan").hide();
                    $("#backMoneySpan").text("0元");
                    //微信
                    $("#weChatCashBackDiv").css("top","13px");
                    $("#weChatBackMoneySpan").hide();
                    $("#weChatBackMoneySpan").text("0元");

                    $("#backAmount").val("0");
                    layerError(data.message, "错误提示");
                }
                return false;
            },
            error: function (data)
            {
                $("#cashBackDiv").css("top","13px");
                $("#backMoneySpan").hide();
                $("#backMoneySpan").text("0元");
				//微信
                $("#weChatCashBackDiv").css("top","13px");
                $("#weChatBackMoneySpan").hide();
                $("#weChatBackMoneySpan").text("0元");

                $("#backAmount").val("0");
                ajaxLogout(data,null,"读取返现比率报错!");
                //var msg = eval(data);
            }
        });
    }

    function getTransferNO() {
        var payType = $("input:radio[name='BC']:checked ").val();
        if(payType=='OFFL'){
            $("#tranferNoPic").click();
		}else if(payType=='WeChat'){
            $("#weChatTranferNoPic").click();
		}
    }

    function showPayCode(payType,res) {
        if(payType=='OFFL'){ //支付宝
			if(res=='5000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/ZFB5000.png");
			}else if(res=='10000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/ZFB10000.png");
			}else if(res=='20000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/ZFB20000.png");
			}else if(res=='30000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/ZFB30000.png");
			}else if(res=='40000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/ZFB40000.png");
            }else if(res=='50000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/ZFB50000.png");
			}
        }else if(payType=='WeChat'){
            if(res=='500'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/weChat500.png");
            }else if(res=='1000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/weChat1000.png");
            }else if(res=='2000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/weChat2000.png");
            }else if(res=='5000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/weChat5000.png");
            }else if(res=='10000'){
                $("#payCode").attr("src","${ctxStatic}/images/recharge/weChat10000.png");
            }else{
                $("#payCode").attr("src","${ctxStatic}/images/recharge/weChat0.png");
			}
        }
    }


    //判断是否是日期格式
    function isDate(sDate) {
        var year, month, day;
        year = sDate.substring(0, 4);
        month = sDate.substring(4, 6);
        day = sDate.substring(6, 8);
        var iaMonthDays = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
        if (year < 2020 || year > 2500) return false
        if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) iaMonthDays[1] = 29;
        if (month < 1 || month > 12) return false
        if (day < 1 || day > iaMonthDays[month - 1]) return false
        return true
    }


</script>
	<style type="text/css">
		body{ text-align:center}
		.divcss5{margin:0 auto;padding:10px 10px;width:1200px;background-color: white}
		.form-horizontal .form-actions {padding-left: 0px !important;}
		.fromInput {
			border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;width: 280px;
		}
		.bankSize{width: 152px;height: 45px;}
		.layui-layer-title{text-align: left}
	</style>
</head>
<body style="background-color: #F8F8F9">
	<br />
	<div class="divcss5">
        <legend style="text-align: left"><span>充值</span></legend>
		<div style="width: 980px;margin-left: 80px">
			<input type="hidden" id="beforeSelect" value="ZFB">
			<sys:message content="${message}"/>
			<form:form id="inputForm" modelAttribute="customerCurrency" action="${ctx}/fi/recharge/customer/submitForm" method="post" class="form-horizontal">
				<form:hidden path="id" htmlEscape="true" />
				<form:hidden path="customer.id" htmlEscape="true" />
				<div class="row-fluid">
					<div class="span6">
						<div class="control-group">
							<label class="control-label">账号名称：</label>
							<div class="controls">
								<form:input path="customer.name" readonly="true" cssClass="input-block-level"/>
							</div>
						</div>
					</div>
					<div class="span6">
						<div class="control-group">
							<label class="control-label">账户余额：</label>
							<div class="controls">
								<input readonly="true" value="${balanceAmount}" style="border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;" class="input-block-level">
							</div>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<div class="control-group">
							<label class="control-label">支付方式：</label>
							<div class="controls">
								<input id="bc_zfb" type="radio" name="BC" value="ZFB" checked="checked"/>
								<label for="bc_zfb"><img alt="ALipay" src="${ctxStatic}/images/bank/zhifubao.png" class="bankSize"/></label>

								<input type="radio" id="bc_offline" name="BC" value="OFFL" style="margin-left: 18px"/>
								<label for="bc_offline">
									<div style="position: relative;">
										<img src="${ctxStatic}/images/recharge/ZFB.png" class="bankSize"/>
										<div style="position: absolute;top: 13px;left: 50px" id="cashBackDiv">
											<span>线下充值</span>
											<div style="font-size: 11px;color: red;display: none" id="backMoneySpan">返现0元</div>
										</div>
									</div>
								</label>

                                <input type="radio" id="WeChat_offline" name="BC" value="WeChat" style="margin-left: 18px"/>
                                <label for="WeChat_offline">
                                    <div style="position: relative;">
                                        <img src="${ctxStatic}/images/recharge/weChat.png" class="bankSize"/>
                                        <div style="position: absolute;top: 13px;left: 50px" id="weChatCashBackDiv">
                                            <span>线下充值</span>
                                            <div style="font-size: 11px;color: red;display: none" id="weChatBackMoneySpan">返现0元</div>
                                            <input type="hidden" id="weChatBackAmount" value="0">
                                        </div>
                                    </div>
                                </label>

								<input type="radio" id="bc_abc" name="BC" value="ABC" style="margin-left: 18px"/>
								<label for="bc_abc"><img alt="ABC" src="${ctxStatic}/images/bank/95599.png" class="bankSize"/> </label>

							</div>
							<div class="controls" style="margin-top: 10px">
								<input type="radio" id="bc_bocb2c" name="BC" value="BOCB2C" />
								<label for="bc_bocb2c"><img alt="BOC" src="${ctxStatic}/images/bank/95566.png" class="bankSize"/> </label>

								<input type="radio" id="bc_cmb" name="BC" value="CMB" style="margin-left: 18px"/>
								<label for="bc_cmb"><img alt="CMB" src="${ctxStatic}/images/bank/95555.png" class="bankSize"/></label>

								<input type="radio" id="bc_ccb" name="BC" value="CCB" style="margin-left: 18px"/>
								<label for="bc_ccb"><img alt="CCB" src="${ctxStatic}/images/bank/95533.png" class="bankSize"/> </label>

								<input type="radio" id="bc_icbcb2c" name="BC" value="ICBCB2C" style="margin-left: 18px"/>
								<label for="bc_icbcb2c"><img alt="ICBC" src="${ctxStatic}/images/bank/95588.png" class="bankSize"/> </label>
							</div>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<div class="control-group">
							<label class="control-label">充值金额：</label>
							<div class="controls">
									<%--                    <input name="charge" id="charge1" type="radio" value="0.10" checked="checked"><label for="charge1">0.10</label>--%>
								<span id="onlinePayAmount">
                                     <input name="charge" id="charge2" type="radio" value="500"><label for="charge2">500</label>
									 <input name="charge" id="charge3" type="radio" value="1000" style="margin-left: 50px"><label for="charge3">1000</label>
								     <input name="charge" id="charge4" type="radio" value="2000" checked="checked" style="margin-left: 50px"><label for="charge4">2000</label>
								</span>
                                <span id="chargeSpan12">
	                                <input name="charge" id="charge12" type="radio" value="3000" style="margin-left: 50px"><label for="charge12">3000</label>
                                </span>
								<input name="charge" id="charge5" type="radio" value="5000" style="margin-left: 50px"><label for="charge5">5000</label>
                                <span style="display: none" id="chargeSpan6">
                                     <input name="charge" id="charge6" type="radio" value="10000" style="margin-left: 50px"><label for="charge6">10000</label>
                                </span>
								<span id="offlineAliPay" style="display: none">
									<input name="charge" id="charge8" type="radio" value="20000" style="margin-left: 50px"><label for="charge8">20000</label>
								    <input name="charge" id="charge9" type="radio" value="30000" style="margin-left: 50px"><label for="charge9">30000</label>
                                    <input name="charge" id="charge10" type="radio" value="40000" style="margin-left: 50px"><label for="charge10">40000</label>
									<input name="charge" id="charge11" type="radio" value="50000" style="margin-left: 50px"><label for="charge11">50000</label>
								</span>
								<span style="margin-left: 44px" id="manualAmount">
									<input name="charge" id="charge7" type="radio" value="0" ><label for="charge7">自定义金额</label>
									<span id="customAmountSpan" style="display: none">
										<input id="amount" name="amount" type="text" value="2000" htmlEscape="false"  maxlength="10" class="required number" style="width: 115px;" readonly="true" />
									    <span class="add-on">元</span>
									</span>
								</span>
							</div>
						</div>
					</div>
				</div>
				<div class="row-fluid" style="margin-top: 10px">
					<div class="span12">
						<div class="control-group">
							<label class="control-label">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
							<div class="controls">
						         <textarea style="text-align: left;" id="remarks" name="remarks"
								    htmlEscape="false" rows="3" maxlength="200" class="input-block-level" ></textarea>
							</div>
						</div>
					</div>
				</div>
				<div style="margin-left: 20px;display: none" id="offlineDiv">
					<input type="hidden" id="backAmount" value="0">
					<div class="row-fluid" style="margin-top: 20px">
						<div class="span4">
							<div style="text-align: left;margin-left: 60px">
								<img src="${ctxStatic}/images/recharge/ZFB500.png" style="width: 200px;height: 200px" id="payCode"/>
							</div>
						</div>
						<div class="span8">
							<div style="color: #17233D;font-family: SourceHanSansSC-bold;font-size: 18px;text-align: left" id="payTypeTitle">请使用支付宝扫码充值！</div>
							<div style="color:#F54142;text-align: left;margin-top: 24px">*您现在使用的是非实时到账充值业务，请注意：</div>
							<div style="text-align: left">1.您在线下充值成功后，<span style="color:#F54142;">需填写充值信息</span>，平台财务审核通过后将充值到您的账户。</div>
							<div style="text-align: left">2.到账时间：工作日4小时内到账，周末，节假日可能会顺延至工作日。</div>
                            <div style="text-align: left">3.每笔充值，对应提交一条充值信息。</div>
                            <div style="text-align: left">4.填写的手机号为到账提醒人手机号，可以为您本人或贵司业务联系人。</div>
                            <div style="text-align: left" id="fifthTip">5.超5万额度充值，请联系业务经理线下充值。</div>
							<div style="color: #17233D;font-family: SourceHanSansSC-bold;font-size: 18px;text-align: left;margin-top: 24px">填写充值信息</div>
							<div style="margin-top: 16px">
								<label class="control-label" style="width: 80px"><font style="color: red">*</font>交易单号：</label>
								<div class="controls" style="margin-left: 85px">
									<input id="transferNo" name="transferNo" value="" maxlength="50" class="fromInput" placeholder="输入支付宝交易单号" oninput="value=value.replace(/[^\d]/g,'')">
									<a href="javascript:getTransferNO()" style="margin-left: 5px">如何获取</a>
									<img src="${ctxStatic}/images/recharge/ZFBSketchMap.png" style="display: none" id="tranferNoPic">
									<img src="${ctxStatic}/images/recharge/weChatSketchMap.png" style="display: none" id="weChatTranferNoPic">
								</div>
							</div>
							<div style="margin-top: 16px">
								<label class="control-label" style="width: 80px"><font style="color: red">*</font>通知手机：</label>
								<div class="controls" style="margin-left: 85px">
									<input id="phone" name="phone" maxlength="11" value="" class="fromInput mobile">
								</div>
							</div>
						</div>
					</div>
				</div>
			</form:form>
		</div>
		<c:if test="${canRecharge != null && canRecharge == true}">
			<div style="bottom: 0; width: 100%;height: 60px;background-color: white;margin-top: 30px">
				<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
				<div style="float: right;margin-top: 10px;margin-right: 20px">
					<input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" onclick='$("#inputForm").submit()' value="确认充值"/>
				</div>
			</div>
		</c:if>
	</div>
</body>
</html>
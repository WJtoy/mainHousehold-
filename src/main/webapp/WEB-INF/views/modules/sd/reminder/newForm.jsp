<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>新建催单</title>
	<meta name="description" content="新建催单">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
		var parentIndex = getCookie('layer.parent.id');
        var clickTag = 0;
		var this_index = top.layer.index;
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
                    var reminderReason = $("input[name='reminderReason.name']:checked").val();
                    if(reminderReason==null || reminderReason==''){
                        layerMsg("请选择催单类型");
                        return false
                    }
					var context = $("#createRemark").val();
					var forbiddenArray = filterForbiddenStr(context);
					if(forbiddenArray != null){
						layerAlert("备注含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
						return false;
					}
                    if (clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var ajaxSuccess = 0;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
					var loadingIndex;
					var action = $("#action").val();
					$.ajax({
						async: false,
						cache: false,
						type: "POST",
						url: "${ctx}/sd/reminder/save?"+ (new Date()).getTime(),
						data:$(form).serialize(),
						beforeSend: function () {
							loadingIndex = layer.msg('正在提交，请稍等...', {
								icon: 16,
								time: 0,
								shade: 0.3
							});
						},
						complete: function () {
							if(loadingIndex) {
								layer.close(loadingIndex);
							}
							if(ajaxSuccess == 0) {
								setTimeout(function () {
									clickTag = 0;
									$btnSubmit.removeAttr('disabled');
								}, 2000);
							}
						},
						success: function (data) {
							if(ajaxLogout(data)){
								return false;
							}
						   if(data && data.success == true) {
                               ajaxSuccess = 1;
								if(action=='0') {
									var iframe = getActiveTabIframe();//定义在jeesite.min.js中
									if (iframe != undefined) {
										iframe.repage();
									}
                                    top.layer.close(this_index);
									layerMsg('提交成功');
								}else{
									//回调父窗口方法
									setTimeout(function() {
										var layero = $("#layui-layer" + parentIndex,top.document);
										var iframeWin = top[layero.find('iframe')[0]['name']];
										iframeWin.reloadReminder();
										top.layer.close(this_index);//关闭本窗口
										return false;
									}, 300);
									return false;
								}
						   }
						   else if( data && data.message){
							   layerError(data.message,"错误提示");
						   }
						   else {
							   layerError("保存错误", "错误提示");
						   }
						   return false;
						},
						error: function (e) {
							ajaxLogout(e.responseText,null,"保存错误，请重试!");
						}
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

			$('input[type="radio"][name="reminderReason.name"]').change(function(){
                var value = $(this).val();
                var test = $("#labelValue"+ value).text();
                if(test!=null && test!=''){
                    $("#createRemark").val(test);
                }
			});
		});
		
		function closeme(){
            layer.confirm(
                '取消后，填写的单据内容不保存，<br/>确定取消保存并关闭窗口吗？'
                ,{icon: 3, title:'系统确认',success: function(layro, index) {
                        $(document).on('keydown', layro, function(e) {
                            if (e.keyCode == 13) {
                                layro.find('a.layui-layer-btn0').trigger('click')
                            }else if(e.keyCode == 27){
                                layer.close(index);//关闭本身
                            }
                        })
                    }
                }
                ,function(index) {
                    layer.close(index);//关闭本身
                    top.layer.close(this_index);
                }
                ,function(index) {});
            return false;
		};

	</script>
	<style type="text/css">
		legend span {
			border-bottom: #0096DA 4px solid;
			padding-bottom: 6px;}
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 120px;}
		.form-horizontal .controls {margin-left: 130px;}
		.myalert {padding: 2px 5px 2px 5px;margin-bottom: 2px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="reminder" method="post" cssClass="form-horizontal">
		<form:hidden path="quarter"/>
		<form:hidden path="orderId" />
		<form:hidden path="action"/>
		<sys:message content="${message}"/>
		<c:if test="${canAction eq true}">
		<div style="width: 80%;margin-left: 7%;margin-top: 24px">
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group" style="margin-top: 7px">
						<label class="control-label" style="padding-top: 0px">工单单号：</label>
						<div class="controls">
							<form:hidden path="orderNo"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
							<a href="javascript:void(0);" onclick="Order.viewOrderDetail('${reminder.orderId}','${reminder.quarter}');">${reminder.orderNo}</a>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">工单来源：</label>
						<div class="controls">
							<form:input path="dataSourceName"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
						<div class="controls">
							<form:input path="customer.name"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">服务网点：</label>
						<div class="controls">
							<form:input path="servicePoint.name"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">用户姓名：</label>
						<div class="controls">
							<form:input path="userName"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">用户电话：</label>
						<div class="controls">
							<form:input path="userPhone"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">用户地址：</label>
						<div class="controls">
							<form:input path="userAddress"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<legend style="margin-top: 10px"><span>催单</span></legend>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;型：</label>
						<div class="controls">
							<c:forEach items="${fns:getDictListFromMS('reminder_reason')}" var="dict">
								<input type="radio" id="reminderReason${dict.value}" name="reminderReason.name" value="${dict.value}">
								<label for="reminderReason${dict.value}" id="labelValue${dict.value}">${dict.label}</label>&nbsp;&nbsp;
							</c:forEach>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label"><font color="red">*</font>备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
						<div class="controls">
							<form:textarea path="createRemark" htmlEscape="false" rows="4" maxlength="250" class="input-block-level required" />
						</div>
					</div>
				</div>
			</div>
		</div>
		<%--<fieldset>
			<div class="form-actions">
				<shiro:hasPermission name="sd:reminder:create"><input id="btnSave" class="btn btn-primary" type="submit" value="提 交"/>&nbsp;</shiro:hasPermission>
				<input id="btnCancel" class="btn" type="button" value="取 消"  onclick="closeme();" />
			</div>
		</fieldset>--%>
		</c:if>
	</form:form>
	<c:if test="${canAction eq true}">
		<div style="height: 60px;width: 100%"></div>
		<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
			<hr style="margin: 0px;"/>
			<div style="float: right;margin-top: 10px;margin-right: 20px">
				<shiro:hasPermission name="sd:reminder:create">
				    <input id="btnSave" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" onclick="$('#inputForm').submit()" value="保 存"/>
				</shiro:hasPermission>
				<input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 96px;height: 40px" onclick="closeme()"/>
			</div>
		</div>
    </c:if>
</body>
</html>
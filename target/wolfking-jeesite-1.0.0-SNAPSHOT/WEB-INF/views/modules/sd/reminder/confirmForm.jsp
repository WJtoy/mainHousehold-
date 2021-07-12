<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>催单-跟单确认</title>
	<meta name="description" content="跟单确认">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<!-- clipboard -->
	<script src="${ctxStatic}/common/clipboard.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/reminder/tpl/confirmItems.html" %>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
		Order.rootUrl = "${ctx}";
		var this_index = top.layer.index;
		function closeme(){
			top.layer.close(this_index);
		};
		var clickTag = 0;
		<c:if test="${canAction eq true}">
		$(document).ready(function() {
			$('#btnReject1').darkTooltip();
            $('#disabledConfirm').darkTooltip();
            $('#disabledReject').darkTooltip();
			//装载日志
			// loadReminderLogList();
			var items = ${fns:toJson(reminder.items)};
			var model = {
				orderNo:'${reminder.orderNo}',
				userName:'${reminder.userName}',
				userPhone:'${reminder.userPhone}',
				userAddress:'${reminder.userAddress}',
				items: items
			};
			Order.showReminderConfirmItems(model);

            $('input[type="radio"][name="reminderReason.name"]').change(function(){
                var value = $(this).val();
                var test = $("#labelValue"+ value).text();
                if(test!=null && test!=''){
                    $("#processRemark").val(test);
                }
            });
		});

        $(document).on("click", "#btnSave", function () {
            submit(1);
        });

        $(document).on("click", "#btnReject", function () {
            submit(2);
        });
        var $btnSave = $("#btnSave");
        var $btnReject = $("#btnReject");

        function resetButtons() {
            $btnSave.removeAttr('disabled');
            $btnReject.removeAttr('disabled');
            clickTag = 0;
        }

		function submit(actionCode){
			if(!actionCode){
				return false;
			}
			if(actionCode != 1 && actionCode != 2){
                layerError("提交类型错误", "错误提示");
				return false;
			}
			if (clickTag === 1) {
				return false;
			}
			clickTag = 1;
			$btnSave.attr('disabled', 'disabled');
			$btnReject.attr('disabled', 'disabled');

            var reminderReason = $("input[name='reminderReason.name']:checked").val();
            var itemId = $("#itemId").val();
			if(actionCode === 2){
                if(reminderReason==null || reminderReason==''){
                    layerMsg("请选择催单类型");
                    resetButtons();
                    return false
                }
			}
			var processRemark = $("#processRemark").val();
			if (actionCode === 2){
				if(Utils.isEmpty(processRemark)) {
					layerInfo("请输入催单意见.","提示");
					resetButtons();
					return false;
				}
			}
			if(!Utils.isEmpty(processRemark)) {
				var forbiddenArray = filterForbiddenStr(processRemark);
				if(forbiddenArray != null){
					layerAlert("催单意见含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
					resetButtons();
					return false;
				}
			}
			if(actionCode === 1){
				processRemark = "确认，催单已回复客户";
			}
            var confirmClickTag = 0;
			var actionName = "确定[<font color='red'>再次催单</font>]吗？";
			if(actionCode === 1){
			    actionName = "确定已回复客户吗？";
            }
            layer.confirm(actionName, {
                icon: 3
                ,title:'系统确认'
                ,cancel: function(index, layero){
                    resetButtons();
                }
            }, function(index,layero){
                if(confirmClickTag === 1){
                    return false;
                }
                var btn0 = $(".layui-layer-btn0",layero);
                if(btn0.hasClass("layui-btn-disabled")){
                    return false;
                }
                confirmClickTag = 1;
                btn0.addClass("layui-btn-disabled").attr("disabled","disabled");
                // do something
                var loadingIndex = layer.msg('正在提交，请稍等...', {
                    icon: 16,
                    time: 0,//不定时关闭
                    shade: 0.3
                });
                var ajaxSuccess = 0;
                top.layer.close(index);//关闭确认窗口本身
                $.ajax({
                    async: false,
                    cache: false,
                    type: "POST",
                    data: {
                        "id": "${reminder.id}",
                        "reminderNo": "${reminder.reminderNo}",
                        "quarter": "${reminder.quarter}",
                        "orderId": "${reminder.orderId}",
                        "status": ${reminder.status},
                        "processRemark": processRemark,
                        "action": actionCode,
						"reminderReason.name":reminderReason,
						"itemId":itemId
                    },
                    url: "${ctx}/sd/reminder/saveConfirm?_at=" + (new Date()).getTime(),
                    beforeSend: function () {
                        loadingIndex = layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if (loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        if(ajaxSuccess == 0) {
                            setTimeout(function () {
                                resetButtons();
                            }, 1000);
                        }
                    },
                    success: function (data) {
                        if (data && data.success === true) {
                            ajaxSuccess = 1;
                            layerMsg('提交成功');
                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if (iframe != undefined) {
                                iframe.repage();
                            }
                            top.layer.close(this_index);
                        }
                        else if (data && data.message) {
                            layerError(data.message, "错误提示");
                        }
                        else {
                            layerError("提交失败，请重试", "错误提示");
                        }
                        resetButtons();
                        return false;
                    },
                    error: function (e) {
                        layerError("提交失败:" + e, "错误提示");
                        resetButtons();
                    }
                });
                return false;
            },function(index) {//cancel
               resetButtons();
            });
			return false;
		}

		function loadReminderLogList() {
			Order.loadReminderLogList('${reminder.id}', '${reminder.quarter}');
		}
		</c:if>
	</script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 120px;}
		.form-horizontal .controls {margin-left: 130px;}
		.myalert {padding: 2px 5px 2px 5px;margin-bottom: 2px;}
		.centerForm {margin:0 auto; width:800px; line-height:20px; border:0px solid #F00}
		.reminder-status{
			padding: 4px 5px;border-radius:2px;background-color:#34C758;color: white;margin-left: 5px;
		}
		.order-status{
			padding: 4px 5px;border-radius:2px;background-color:#0096DA;color: white;
		}
		.btn-reject{
			border-radius: 4px;color: white;border: 1px solid rgba(255, 255, 255, 0);background-color: rgba(255, 149, 2, 1);
		}
		.btn-danger.disabled {
			color: rgb(255, 255, 255);
			background-color: rgba(255, 149, 2, 1) !important;
		}
	</style>
</head>
<body>
	<div class="centerForm">
	<form:form id="inputForm" modelAttribute="reminder" method="post" cssClass="form-horizontal" cssStyle="width:800px;margin-left: 0px;">
		<sys:message content="${message}"/>
		<c:if test="${canAction eq true}">
		<input type="hidden" id="itemId" value="${reminder.itemId}">
		<fieldset>
			<legend>
				<div class="row-fluid">
					<div class="span9">

					</div>
					<div class="span3">
						<p style="float: right;font-size: 14px">${reminder.reminderNo}<span class="reminder-status">${reminder.statusName}</span></p>
					</div>
				</div>
			</legend>
			<div class="row-fluid" style="margin-top: 5px">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">工单单号：</label>
						<div class="controls">
							<form:hidden path="orderNo"  htmlEscape="false" cssClass="input-block-level required" readonly="true"/>
							<a href="javascript:void(0);" onclick="Order.viewOrderDetail('${reminder.orderId}','${reminder.quarter}');">${reminder.orderNo}</a>
							<span class="order-status">${reminder.orderStatusName}</span>
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
			<div class="row-fluid" style="margin-top: 5px">
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
			<div class="row-fluid" style="margin-top: 5px">
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
			<div class="row-fluid" style="margin-top: 5px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">用户地址：</label>
						<div class="controls">
							<form:input path="userAddress"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<legend style="margin-top: 10px"><span style="border-bottom: #0096DA 4px solid;padding-bottom: 6px;">催单</span></legend>
			<div class="row-fluid" style="margin-top: 5px">
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
			<div class="row-fluid" style="margin-top: 5px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
						<div class="controls">
							<form:textarea path="processRemark" htmlEscape="false" rows="3" maxlength="250" class="input-block-level" />
						</div>
					</div>
				</div>
                <%--<div class="span1">
                    <shiro:hasPermission name="sd:reminder:complete">
                        <input id="btnSave" class="btn btn-primary" type="button" value="回复客户"/>
                        <c:choose>
                            <c:when test="${reminderCheckFlag == 1}">
                                <a id="btnReject1" href="javascript:void(0);" class="btn btn-danger disabled"  data-toggle="tooltip" data-tooltip="${reminderCheckMsg}" style="margin-top:5px;padding: 5px;width:70px;" >再次催单</a>
                            </c:when>
                            <c:otherwise>
                                <input id="btnReject" class="btn btn-danger" type="button" style="margin-top: 5px;" value="再次催单"/>&nbsp;
                            </c:otherwise>
                        </c:choose>
                    </shiro:hasPermission>
                </div>--%>
			</div>
			</fieldset>
		</c:if>
	</form:form>
	</div>
	<c:if test="${canAction eq true}">
	<div style="margin: 0 20px;margin-bottom: 20px">
		<legend style="margin-top: 10px"><span style="border-bottom: #0096DA 4px solid;padding-bottom: 6px;">跟踪进度</span></legend>
		<!-- 催单项目 -->
		<div id="divItems" class="tab-content" style="margin: 0 20px;margin-bottom: 20px">
			<div class="tab-pane active" id="tabItems"></div>
		</div>
	</div>

	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;"/>
		<div style="float: right;margin-top: 10px;margin-right: 20px">
			<shiro:hasPermission name="sd:reminder:process">
				<button id="btnSave" class="btn btn-primary" style="margin-right: 5px;width: 96px;height: 40px" type="button" onclick="saveReminderReply();"/>回复</button>
			</shiro:hasPermission>
			<shiro:hasPermission name="sd:reminder:complete">
				<c:choose>
					<%--<c:when test="${reminder.dataSource==11}">
						<a id="disabledConfirm" href="javascript:void(0);" class="btn btn-primary disabled" data-toggle="tooltip" data-tooltip="待商家处理" style="padding: 9px 18px" >回复客户</a>
						<a  id="disabledReject" href="javascript:void(0);" class="btn btn-danger disabled" data-toggle="tooltip" data-tooltip="待商家处理" style="padding: 9px 18px" >再次催单</a>
					</c:when>
					<c:otherwise>
						<input id="btnSave" class="btn btn-primary" style="margin-right: 5px;width: 96px;height: 40px" type="button" value="回复客户"/>
						<c:choose>
							<c:when test="${reminderCheckFlag == 1}">
								<a id="btnReject1" href="javascript:void(0);" class="btn btn-danger disabled" data-toggle="tooltip" data-tooltip="${reminderCheckMsg}" style="padding: 9px 18px" >再次催单</a>
							</c:when>
							<c:otherwise>
								<input id="btnReject" class="btn-reject" type="button" style="width: 96px;height: 40px;" value="再次催单"/>&nbsp;
							</c:otherwise>
						</c:choose>
					</c:otherwise>--%>
					<c:when test="${reminderCheckFlag == 1}">
						<a id="btnReject1" href="javascript:void(0);" class="btn btn-danger disabled" data-toggle="tooltip" data-tooltip="${reminderCheckMsg}" style="padding: 9px 18px" >再次催单</a>
					</c:when>
					<c:otherwise>
						<input id="btnReject" class="btn-reject" type="button" style="width: 96px;height: 40px;" value="再次催单"/>&nbsp;
					</c:otherwise>
				</c:choose>
			</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 96px;height: 40px" onclick="closeme()"/>
		</div>
	</div>
	</c:if>
</body>
<script type="text/javascript">
	$(document).ready(function() {
		$('a.zclip').each(function () {
			$_this = $(this);
			var msg = $_this.data('clipboard-text');
			msg = msg.replace(/\\n/g,'\n');
			$_this.attr('data-clipboard-text',msg);
			var id = $_this.attr('id');
			var clip_item = new ClipboardJS('#'+id);
			clip_item.on('success', function(e) {
				layerMsg("信息复制成功");
			});
		});
	});
</script>


</html>
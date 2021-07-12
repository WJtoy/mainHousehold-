<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>催单-客服回复窗口</title>
	<meta name="description" content="客服回复窗口">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/reminder/tpl/items.html" %>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
		var this_index = top.layer.index;
		function closeme(){
			top.layer.close(this_index);
		};
		var clickTag = 0;
		var reminderReplyTag = 0;
		<c:if test="${canAction eq true}">
		$(document).ready(function() {

			//装载日志 loadReminderLogList();
			//项目列表
			var items = ${fns:toJson(reminder.items)};
			Order.showReminderItems(items);
            var logClickTag = 0;
            $("#btnSaveLog").on("click", function () {
                var self = this;
                if (logClickTag === 1) {
                    return false;
                }
                logClickTag = 1;
                var $btnSubmit = $(self);
                $btnSubmit.attr('disabled', 'disabled');
                var logContent = $("#logContent").val();
                if (Utils.isEmpty(logContent)) {
                    layerError("请输入日志内容.", "错误提示");
                    $btnSubmit.removeAttr('disabled');
                    logClickTag = 0;
                    return false;
                }
                //日志简明增加操作名称
                logContent = "处理日志:" + logContent;
                var loadingIndex;
                var reminderId = '${reminder.id}';
                var quarter = '${reminder.quarter}';
                var status = '${reminder.status}';
                var visibilityFlag = $("input[name='visibilityFlag']:checked").val();
                $.ajax({
                    async: false,
                    cache: false,
                    type: "POST",
                    data: {
                        "reminderId": reminderId,
                        "quarter": quarter,
                        "status": status,
                        "logContent": logContent,
                        "visibilityFlag": visibilityFlag
                    },
                    url: "${ctx}/sd/reminder/log/saveLog?" + (new Date()).getTime(),
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
                        setTimeout(function () {
                            logClickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                            $('#btnSaveLog').removeAttr('disabled');
                        }, 2000);
                    },
                    success: function (data) {
                        if (data && data.success === true) {
                            $("#logContent").val("");
                            // loadReminderLogList();
							loadItems();
                        }
                        else if (data && data.message) {
                            layerError(data.message, "错误提示");
                        }
                        else {
                            layerError("保存处理日志错误", "错误提示");
                        }
                        return false;
                    },
                    error: function (e) {
                        layerError("保存处理日志错误:" + e, "错误提示");
                    }
                });
                return false;
            });

		});

		function saveReminderReply(){
			if(reminderReplyTag === 1){
				return false;
			}
			reminderReplyTag = 1;
			var $btn = $("#btnSave");
			var remarks =  $("#processRemark").val();
			if(Utils.isEmpty(remarks)){
				layerError("请输入回复内容.","错误提示");
				reminderReplyTag = 0;
				return false;
			}
			var forbiddenArray = filterForbiddenStr(remarks);
			if(forbiddenArray != null){
				layerAlert("回复内容含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
				reminderReplyTag = 0;
				return false;
			}
			$btn.attr('disabled', 'disabled');
			var confirmClickTag = 0;
			layer.confirm('确定提交催单回复内容吗?', {
				icon: 3
				,title:'系统确认'
				,cancel: function(index, layero){
					reminderReplyTag=0;
					$btn.removeAttr('disabled');
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
				$btn.attr('disabled', 'disabled');
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
					url: "${ctx}/sd/reminder/saveReply?"+ (new Date()).getTime(),
					data:$("#reminderReplyForm").serialize(),
					complete: function () {
						layer.close(loadingIndex);
						if(ajaxSuccess == 0) {
							setTimeout(function () {
								$btn.removeAttr('disabled');
								reminderReplyTag = 0;
							}, 2000);
						}
					},
					success: function (data) {
						$btn.removeAttr('disabled');
						if(data && data.success == true){
							ajaxSuccess = 1;
							//刷新列表
							var iframe = getActiveTabIframe();//定义在jeesite.min.js中
							if (iframe != undefined) {
								iframe.repage();
							}
						}
						else if( data && data.message){
							layerError(data.message,"错误提示");
						}
						else{
							layerError("回复催单错误","错误提示");
						}
						reminderReplyTag = 0;
						return false;
					},
					error: function (e) {
						$btn.removeAttr('disabled');
						reminderReplyTag = 0;
						layerError("回复催单错误:"+e,"错误提示");
					}
				});
				return false;
			},function(index) {//cancel
				$btn.removeAttr('disabled');
				reminderReplyTag = 0;
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
		.centerForm {margin:0 auto; width:800px; line-height:20px; border:0px solid}
		.reminder-status{
			padding: 4px 5px;border-radius:2px;background-color:#0096DA;color: white;margin-left: 5px;
		}
		.order-status{
			padding: 4px 5px;border-radius:2px;background-color:#0096DA;color: white;
		}
	</style>
</head>
<body>
	<div class="centerForm">
	<form:form id="reminderReplyForm" modelAttribute="reminder" method="post" cssClass="form-horizontal" cssStyle="width:800px;margin-left: 0px;">
		<sys:message content="${message}"/>
		<c:if test="${canAction eq true}">
			<input type="hidden" id="id" name="id" value="${reminder.id}" />
			<input type="hidden" id="orderId" name="orderId" value="${reminder.orderId}" />
			<input type="hidden" id="quarter" name="quarter" value="${reminder.quarter}" />
			<input type="hidden" id="reminderNo" name="reminderNo" value="${reminder.reminderNo}" />
            <input type="hidden" id="itemId" name="itemId" value="${reminder.itemId}" />
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
			<div class="row-fluid">
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
			<legend style="margin-top: 10px"><span style="border-bottom: #0096DA 4px solid;padding-bottom: 6px;">催单</span></legend>
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;型：</label>
						<div class="controls">
							<form:input path="reminderReason.value"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
						<div class="controls">
							<form:textarea path="createRemark" htmlEscape="false" rows="3" maxlength="250" readonly="true" class="input-block-level" />
						</div>
					</div>
				</div>
			</div>
			<%--<div class="row-fluid">
				<div class="span6">
					<div class="control-group">
						<label class="control-label">催单人:</label>
						<div class="controls">
							<form:input path="createName"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">创建日期:</label>
						<div class="controls">
							<input type="text" class="input-block-level" readonly="readonly" value="${reminder.createDate}" />
						</div>
					</div>
				</div>
			</div>--%>
			<legend><span style="border-bottom: #0096DA 4px solid;padding-bottom: 6px;">处理</span></legend>
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label"><font color="red">*</font>回复内容：</label>
						<div class="controls">
							<form:textarea path="processRemark" htmlEscape="false" rows="3" maxlength="250" class="input-block-level required" />
						</div>
					</div>
				</div>
				<%--<div class="span1">
					<shiro:hasPermission name="sd:reminder:process">
						<button id="btnSave" class="btn btn-small btn-primary" style="margin-top: 20px;" type="button" onclick="saveReminderReply();"/>回复</button>
					</shiro:hasPermission>
				</div>--%>
			</div>
		</fieldset>
		</c:if>
	</form:form>
	</div>
	<c:if test="${canAction eq true}">
	<!-- 催单项目 -->
	<div id="divItems" class="tab-content" style="margin: 0 20px;margin-bottom: 20px">
		<legend style="margin-top: 10px"><span style="border-bottom: #0096DA 4px solid;padding-bottom: 6px;">跟踪进度</span></legend>
		<div class="tab-pane active" id="tabItems"></div>
	</div>
	</c:if>
	<c:if test="${canAction eq true}">
		<div style="height: 60px;width: 100%"></div>
		<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
			<hr style="margin: 0px;"/>
			<div style="float: right;margin-top: 10px;margin-right: 20px">
				<shiro:hasPermission name="sd:reminder:process">
					<button id="btnSave" class="btn btn-primary" style="margin-right: 5px;width: 96px;height: 40px" type="button" onclick="saveReminderReply();"/>回复</button>
				</shiro:hasPermission>
				<input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 96px;height: 40px" onclick="closeme()"/>
			</div>
		</div>
	</c:if>
</body>
</html>
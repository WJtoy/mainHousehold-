<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>返件单跟踪进度</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 80px;}
		.form-horizontal .controls {margin-left: 100px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="materialReturn" action="" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="quarter"/>
		<sys:message content="${message}"/>
		<div class="control-group" style="margin-top:30px;">
			<label class="control-label">订单号:</label>
			<div class="controls">
				<form:input path="orderNo" readonly="true" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">返件单号:</label>
			<div class="controls">
				<form:input path="returnNo" readonly="true" />
			</div>
		</div>
		<c:if test="${empty canSave}">
		<div class="control-group" >
			<label class="control-label">进度:</label>
			<div class="controls">
				<form:select path="pendingType.value" class="required" cssStyle="width: 220px;">
					<form:options items="${fns:getDictListFromMS('material_pending_type')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
				</form:select>
				<span class="red">*</span>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">内容:</label>
			<div class="controls">
				<form:textarea path="pendingContent" htmlEscape="false" rows="4" maxlength="150" class="input-block-level required" style="width:92%;"/>
				<span class="red">*</span>
			</div>
		</div>
		</c:if>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<c:if test="${empty canSave}">
			<input id="btnSubmit" class="btn btn-primary" type="button" value="保 存"/>&nbsp;
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethislayer();" />
		</div>
	</form:form>
	<script type="text/javascript">
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        var clickTag = 0;
        <c:if test="${empty canSave}">
        $(document).ready(function() {
            $('#btnSubmit').off().on('click', function(){
                if (!$("#inputForm").valid()) {
                    return false;
                }
                if (clickTag == 1){
                    return false;
                }
                clickTag = 1;
                var $btnSubmit = $("#btnSubmit");
                $btnSubmit.attr("disabled", "disabled");
                var ajaxSuccess = 0;
                var materialMaster = {};
                materialMaster.id = $("#id").val();
                materialMaster.quarter = $("#quarter").val();
                var option = $("[id='pendingType.value'] option:selected");
                materialMaster["pendingType.label"] = option.text();
                materialMaster["pendingType.value"] = option.val();
                materialMaster.pendingContent = $("#pendingContent").val();
                var loadingIndex;
                var date = new Date();
                $.ajax({
                    async: false,
                    cache: false,
                    type: "POST",
                    url: "${ctx}/sd/material/return/pending?at="+ (new Date()).getTime(),
                    data: materialMaster,
                    beforeSend: function () {
                        loadingIndex = top.layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,//不定时关闭
                            shade: 0.3
                        });
                    },
                    complete: function () {
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
                        if(loadingIndex) {
                            top.layer.close(loadingIndex);
                        }
                        if(data && data.success == true){
                            top.layer.close(index);
                            //from order list
                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if(iframe != undefined){
                                materialMaster.pendingDate = DateFormat.format(date, 'yyyy-MM-dd hh:mm');
                                iframe.updatePendingInfo(materialMaster);
                            }

                            ajaxSuccess = 1;
                        }
                        else if( data && data.message){
                            layerError(data.message,"错误提示");
                        }
                        else{
                            layerError("设置跟踪进度错误","错误提示");
                        }
                        return false;
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"设置跟踪进度错误，请重试!");
                    }
                });
            });

            $("#inputForm").validate({
                submitHandler: function(form){
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

		</c:if>
        function closethislayer(){
            top.layer.close(index);
        }
	</script>
</body>
</html>
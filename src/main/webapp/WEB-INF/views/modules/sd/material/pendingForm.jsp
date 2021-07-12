<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>配件单跟踪进度</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 80px;}
		.form-horizontal .controls {margin-left: 85px;}
		.form-horizontal .control-group{border-bottom: none;margin-bottom: 8px;}
		.praise_status_1{background-color: #0096DA;}
		.praise_status_2{background-color: #FF9502;}
		.praise_status_3{background-color: #10B9B9;}
		.praise_status_4{background-color: #34C758;}
		.praise_status_5{background-color: #f54142;}
		.praise_status_6{background-color: #f54142;}
		legend span {
			border-bottom: #0096DA 4px solid;
			padding-bottom: 6px;}
	</style>
</head>
<body>
<fieldset style="width: 90%;margin-left: 5%">
	<form:form id="inputForm" modelAttribute="materialMaster" action="" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="quarter"/>
		<sys:message content="${message}"/>
		<div class="row-fluid" style="margin-top: 12px">
			<div class="span12">
				<div class="control-group">
					<div class="controls">
							<%--<span style="margin-left: 570px">${praise.praiseNo}</span>&nbsp;&nbsp;<span class="praise_status_${praise.status}">${praise.strStatus}</span>--%>
						<div style="float: right">
							<span>${materialMaster.masterNo}</span>
							&nbsp;&nbsp;<span class="label praise_status_${materialMaster.status.value}">${materialMaster.status.label}</span>
						</div>
					</div>
				</div>
			</div>
		</div>
		<legend></legend>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">工单单号：</label>
					<div class="controls">
						<form:input path="orderNo" readonly="true" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group" >
					<label class="control-label">跟踪内容：</label>
					<div class="controls">
						<form:select path="pendingType.value" class="required" cssStyle="width: 220px;">
							<form:options items="${fns:getDictListFromMS('material_pending_type')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
						</form:select>
					</div>
				</div>
			</div>
		</div>
		<c:if test="${empty canSave}">
		<%--<div class="control-group" >
			<label class="control-label">进度:</label>
			<div class="controls">
				<form:select path="pendingType.value" class="required" cssStyle="width: 220px;">
					<form:options items="${fns:getDictListFromMS('material_pending_type')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
				</form:select>
				<span class="red">*</span>
			</div>
		</div>--%>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">跟踪状态：</label>
					<div class="controls">
						<form:textarea path="pendingContent" htmlEscape="false" rows="4" maxlength="150" class="required" style="width:555px;"/>
					</div>
				</div>
			</div>
		</div>
		</c:if>
		<legend><span>跟踪进度</span></legend>
		<table id="treeTable" width="100%" class="table table-hover table-bordered table-condensed"
			   style="margin-bottom: 0px;table-layout: fixed;">
			<thead>
			<tr>
				<th width="80">序号</th>
				<th width="130">跟踪时间</th>
				<th width="*">跟踪内容</th>
				<th width="80">操作人</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${logs}" var="log" varStatus="i" begin="0">
				<tr>
					<td>${i.index+1}</td>
					<td><fmt:formatDate value="${log.createDate}" pattern="yyyy-MM-dd HH:mm"/></td>
					<td>${log.content}</td>
					<td>${log.createBy}</td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
	</form:form>
</fieldset>
<div style="height: 60px;width: 100%"></div>
<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
	<hr style="margin: 0px;"/>
	<div style="float: right;margin-top: 10px;">
		<c:if test="${empty canSave}">
			<input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="保 存" />
		</c:if>
		<input id="btnCancel" class="btn" type="button" value="取 消" style="width: 96px;height: 40px;margin-right: 10px" onclick="closethislayer()"/>
	</div>
</div>
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
                    url: "${ctx}/sd/material/pending?at="+ (new Date()).getTime(),
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
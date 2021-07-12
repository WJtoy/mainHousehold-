<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>关闭配件单</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<meta name="decorator" content="default"/>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 80px;}
		.form-horizontal .controls {margin-left: 100px;}
		.form-horizontal .control-group{border-bottom: none;margin-bottom: 8px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="materialMaster" action="" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="quarter"/>
		<sys:message content="${message}"/>
		<c:if test="${empty canSave}">
            <div class="control-group" style="margin-top: 24px">
                <label class="control-label">关闭类型:</label>
                <div class="controls">
                    <input name="status" type="radio" id="status4" value="4" checked="checked" /> <label for="status4" data-toggle="tooltip" data-tooltip="关闭配件单">关闭配件单</label>
                    <input name="status" type="radio" id="status5" value="5" /> <label for="status5" data-toggle="tooltip" data-tooltip="驳回配件及返件的申请">驳回申请</label>
                    <input name="status" type="radio" id="status6" value="6" /> <label for="status6" data-toggle="tooltip" data-tooltip="异常收件">异常收件</label>
                </div>
            </div>
<%--			<div class="control-group">--%>
<%--				<label class="control-label">是否返件:</label>--%>
<%--				<div class="controls">--%>
<%--					<input name="returnFlag" type="radio" id="returnFlag1" value="1" <c:if test="${materialMaster.returnFlag eq 1}">checked="checked"</c:if> /> <label for="returnFlag1" data-toggle="tooltip" data-tooltip="确认该配件需要返件处理" class="label label-warning">返件</label>--%>
<%--					<input name="returnFlag" type="radio" id="returnFlag0" value="0" <c:if test="${materialMaster.returnFlag eq 0}">checked="checked"</c:if> /> <label for="returnFlag0" data-toggle="tooltip" data-tooltip="该配件不需要返件处理，原返件单自动取消/驳回" class="label label-info">不返件</label>--%>
<%--				</div>--%>
<%--			</div>--%>
			<div id="divCloseType" class="control-group" style="display: none;">
				<label class="control-label">异常原因:</label>
				<div class="controls">
					<form:select path="closeType" class="input-small" style="width:125px;">
						<form:options items="${fns:getDictListFromMS('material_abnormal_close_type')}" itemLabel="label" itemValue="value"
									  htmlEscape="false"/>
					</form:select>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">关闭说明:</label>
				<div class="controls">
					<form:textarea path="closeRemark" htmlEscape="false" rows="4" maxlength="150" class="input-block-level" style="width:92%;"/>
				</div>
			</div>
		</c:if>
	</form:form>
	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;"/>
		<div style="float: right;margin-top: 10px;">
			<c:if test="${empty canSave}">
			    <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="确 定" />
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="取 消" style="width: 96px;height: 40px;margin-right: 10px" onclick="closethislayer()"/>
		</div>
	</div>
	<script type="text/javascript">
        var thisindex = parent.layer.getFrameIndex(window.name); //获取窗口索引
        var clickTag = 0;
        <c:if test="${empty canSave}">
        $(document).ready(function() {
            $('label[data-toggle=tooltip]').darkTooltip({gravity:'north'});

            $('#btnSubmit').off().on('click', function(){
                if (!$("#inputForm").valid()) {
                    return false;
                }
				layer.confirm('确定要 <font color="blue">关闭</font> 该配件申请单吗?', {icon: 3, title:'提示'}, function(index){
                    top.layer.close(index);
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
					materialMaster.closeRemark = $("#closeRemark").val();
					materialMaster.closeType = $("#closeType").val();
					materialMaster["status.value"] = $("input[name='status']:checked").val();
					materialMaster.returnFlag = $("input[name='returnFlag']:checked").val();
					var loadingIndex;
					$.ajax({
						async: false,
						cache: false,
						type: "POST",
						url: "${ctx}/sd/material/close?at="+ (new Date()).getTime(),
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
								layerMsg('配件单关闭成功!');
								//刷新父窗口
								var parentIndex = getCookie('layer.parent.id');
								if(parentIndex && parentIndex != 0) {
									var layero = $("#layui-layer" + parentIndex, top.document);
									var iframeCtl = layero.find('iframe');
									if (iframeCtl && iframeCtl.length > 0) {
										var iframeWin = top[iframeCtl[0]['name']];
										if (typeof iframeWin.reloadApproveList === "function" && iframeWin.reloadApproveList) {
											iframeWin.reloadApproveList();
										} else {
											iframeWin.location.reloadApproveList();
										}
										// var this_index = parent.layer.getFrameIndex(window.name);
										// parent.layer.close(this_index);//ie下报错
										top.layer.close(thisindex);
									} else {
										layerMsg('关闭成功,请手动关闭此页面!');
									}
								}else{
									layerMsg('关闭成功,请手动关闭此页面!');
								}

								ajaxSuccess = 1;
							}
							else if( data && data.message){
								layerError(data.message,"错误提示");
							}
							else{
								layerError("关闭配件单错误","错误提示");
							}
							return false;
						},
						error: function (e) {
							ajaxLogout(e.responseText,null,"关闭配件单错误，请重试!");
						}
					});
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
			$("input[name='status']").on('change', function () {
				var self = $(this);
				var statusVal = self.val();
				if(statusVal === "5" || statusVal === "6"){
					$("input[name='returnFlag'][value='0']").attr("checked",true);
				}
				if(statusVal === "6"){
					$("#divCloseType").show();
				}else{
					$("#divCloseType").hide();
				}
			});
        });

		</c:if>
        function closethislayer(){
            top.layer.close(thisindex);
        }
	</script>
</body>
</html>
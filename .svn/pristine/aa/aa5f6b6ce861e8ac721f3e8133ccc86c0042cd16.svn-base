<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>驳回配件申请单</title>
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
	<form:form id="inputForm" modelAttribute="material" action="" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="quarter"/>
		<sys:message content="${message}"/>
		<c:if test="${canAction}">
			<div id="divCloseType" class="control-group">
				<label class="control-label">驳回原因:</label>
				<div class="controls">
					<form:radiobuttons path="closeType" items="${fns:getDictListFromMS('material_reject_type')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">详细描述:</label>
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
			<c:if test="${canAction}">
			    <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="确 定" />
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="取 消" style="width: 96px;height: 40px;margin-right: 10px" onclick="closethislayer()"/>
		</div>
	</div>
	<script type="text/javascript">
        var thisindex = parent.layer.getFrameIndex(window.name); //获取窗口索引
        var clickTag = 0;
        <c:if test="${canAction}">
        $(document).ready(function() {
            $('label[data-toggle=tooltip]').darkTooltip({gravity:'north'});

            $('#btnSubmit').off().on('click', function(){
                if (!$("#inputForm").valid()) {
                    return false;
                }
				var closeType = $("[name='closeType']:radio:checked").val();
                if(!closeType){
					layerError("请选择驳回原因","错误提示");
					return false;
				}
				layer.confirm('确定要 <font color="blue">驳回</font> 该配件申请单吗?', {icon: 3, title:'提示'}, function(index){
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
					materialMaster.closeType = closeType;
					materialMaster.closeRemark = $("#closeRemark").val();
					var loadingIndex;
					$.ajax({
						async: false,
						cache: false,
						type: "POST",
						url: "${ctx}/sd/material/materialmasterreject?at="+ (new Date()).getTime(),
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
								layerMsg('配件单驳回成功!');
								//刷新父窗口
								var parentIndex = getCookie('layer.three.pid');
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
										top.layer.close(thisindex);
									} else {
										layerMsg('配件单驳回成功,请手动关闭此页面!');
									}
								}else{
									layerMsg('配件单驳回成功,请手动关闭此页面!');
								}

								ajaxSuccess = 1;
							}
							else if( data && data.message){
								layerError(data.message,"错误提示");
							}
							else{
								layerError("配件单驳回错误","错误提示");
							}
							return false;
						},
						error: function (e) {
							ajaxLogout(e.responseText,null,"配件单驳回错误，请重试!");
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
        });

		</c:if>
        function closethislayer(){
            top.layer.close(thisindex);
        }
	</script>
</body>
</html>
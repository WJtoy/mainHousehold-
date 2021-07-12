<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>关闭返件单</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
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
		<div class="alert alert-error">
			<strong>注意:</strong> 取消后，返件单状态变更为【驳回】
		</div>
		<c:if test="${empty canSave}">
			<div class="control-group">
				<label class="control-label">取消说明:</label>
				<div class="controls">
					<form:textarea path="closeRemark" htmlEscape="false" rows="4" maxlength="150" class="input-block-level required" style="width:92%;"/>
					<span class="red">*</span>
				</div>
			</div>
		</c:if>
		<div class="form-actions" style="text-align: center; padding: 10px 0px 10px 0px;">
			<c:if test="${empty canSave}">
			<input id="btnSubmit" class="btn btn-primary" type="button" value="保 存"/>&nbsp;
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethislayer();" />
		</div>
	</form:form>
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
				top.layer.confirm('确定要 <font color="red">关闭</font> 该返件申请单吗?', {icon: 3, title:'提示'}, function(index){
                    top.layer.close(index);
				    //do something
					if (clickTag == 1){
						return false;
					}
					clickTag = 1;
					var $btnSubmit = $("#btnSubmit");
					$btnSubmit.attr("disabled", "disabled");
					var ajaxSuccess = 0;
					var materialReturn = {};
					materialReturn.id = $("#id").val();
					materialReturn.quarter = $("#quarter").val();
					materialReturn.closeRemark = $("#closeRemark").val();
					var loadingIndex;
					$.ajax({
						async: false,
						cache: false,
						type: "POST",
						url: "${ctx}/sd/material/return/close?at="+ (new Date()).getTime(),
						data: materialReturn,
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
							}else{
                                top.layer.close(thisindex);
                                var parentIndex = getCookie('layer.parent.id');
                                if(parentIndex && parentIndex != 0){
                                    top.layer.close(parentIndex);
                                }
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
								layerMsg('取消返件单成功!');
								ajaxSuccess = 1;
							}
							else if( data && data.message){
								layerError(data.message,"错误提示");
							}
							else{
								layerError("取消返件单错误","错误提示");
							}
							return false;
						},
						error: function (e) {
							ajaxLogout(e.responseText,null,"取消返件单错误，请重试!");
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
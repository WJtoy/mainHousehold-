<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>投诉单申诉</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<style type="text/css">
	.form-horizontal{margin-top:10px;}
	</style>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        //var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var parentIndex = getCookie('layer.parent.id');
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
			$("#reContent").focus();
			$("#inputForm").validate({
				submitHandler: function(form){
					var appealContent = $("#content").val();
					var forbiddenArray = filterForbiddenStr(appealContent);
					if(forbiddenArray != null){
						layerAlert("申诉内容含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
						return false;
					}
                    if(clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
                    var ajaxSuccess = 0;
                    var loadingIndex;
					$.ajax({
                        async: false,
                        cache: false,
						type: "POST",
						url: "${ctx}/sales/sd/complain/ajax/appealSave?"+ (new Date()).getTime(),
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
					       if(data && data.success == true){
                               //回调父窗口方法
                               setTimeout(function() {
                                   var layero = $("#layui-layer" + parentIndex,top.document);
                                   var iframeWin = top[layero.find('iframe')[0]['name']];
                                   iframeWin.appealSuccessCallback(''||$("#complainId").val());
                                   //iframeWin.reloadComplain();
                                   top.layer.close(this_index);//关闭本窗口
								   ajaxSuccess = 1;
                                   return false;
                               }, 300);
                               return false;
					       }
					       else if( data && data.message){
                               layerError(data.message,"错误提示");
					       }
					       else{
                               layerError("申诉发生错误，请联系管理员","错误提示");
					       }
					       return false;
						},
						error: function (e) {
                            ajaxLogout(e.responseText,null,"保存申诉错误，请重试!");
						}
					});
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
			
			$("#title").focus();
		});
		
		function closethiswindow(){
            top.layer.close(this_index);
		}

	</script>
</head>
<body>
	<form:form id="inputForm" modelAttribute="complainlog" action="${ctx}/sales/sd/complain/saveAppeal" method="post" class="form-horizontal">
		<form:hidden path="complainId"/>
		<form:hidden path="quarter"/>
		<sys:message content="${message}"/>

		<div class="control-group">
			<label class="control-label">投诉单号:</label>
			<div class="controls">
					${complainNo}
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">申诉内容:</label>
			<div class="controls">
				<form:textarea path="content" htmlEscape="false" rows="6" maxlength="200" class="input-xxlarge required"/>
			</div>
		</div>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethiswindow()"/>
		</div>
	</form:form>
</body>
</html>
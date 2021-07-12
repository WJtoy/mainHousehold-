<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>测试按钮双击</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		var isSubmit = false;
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
                    // if(isSubmit){
				     //    return false;
					// }
					// isSubmit = true;
				    var $btnSubmit = $("#btnSubmit");
                    // if($btnSubmit.prop("disabled") == true){
                    //     return false;
                    // }
                    // $btnSubmit.prop("disabled", true);
                    loading('正在提交，请稍等...');
					form.submit();
                    // $('#btnSubmit').removeAttr('disabled');
                    //setTimeout("$('#btnSubmit').removeAttr('disabled');isSubmit=false;",100); //设置三秒后提交按钮 显示
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

        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function(){
            if (clicktag == 0) {
                clicktag = 1;
                this.disabled=true;
				this.form.submit();
                setTimeout(function () { clicktag = 0;$('#btnSubmit').removeAttr('disabled'); }, 1000);
            }
        });

        $(document).on("click", "#btnAjax", function(){
            var self = this;
            if(clicktag == 1){
                return false;
			}
			clicktag = 1;

            $.ajax({
                url: '${ctx}/sd/test/testButtonDbClickAjax',
                data: $("#inputForm").serialize(),
                type: 'post',
                beforeSend: function () {
                    // console.log('beforeSend');
                    //1.让提交按钮失效，以实现防止按钮重复点击
                    $(self).attr('disabled', 'disabled');
                },
                complete: function () {
                    // console.log('complete');
                    //2.让按钮重新有效
                    $(self).removeAttr('disabled');
                },
                success: function(msg){
                    if (msg.success == true) {
                        $("#id").val(msg.data);
                        alert('登录成功！');
                    } else {
                        alert(msg.message);
                    }
                    // console.log('success');
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    alert(XMLHttpRequest.status);
                    alert(XMLHttpRequest.responseText)
                    // console.log('error');
                }
            });

        });

	</script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
	</style>
</head>
<body >
	<form:form id="inputForm" action="${ctx}/sd/test/testButtonDbClick" method="post" class="form-horizontal">
		<sys:message content="${message}"/>
		<div class="control-group" style="margin-top:10px;">
			<input type="text" id="id" name="id" value="${id}" />
			<label class="control-label">备注:</label>
			<div class="controls">
				<input type="text" id="remarks" name="remarks" />
			</div>
		</div>
		<div class="form-actions">
			<%-- ok <input id="btnSubmit" class="btn btn-primary" onclick="this.disabled=true; this.form.submit();" type="submit" value="保 存"/>--%>
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>
			<input id="btnAjax" class="btn btn-primary" type="button" value="ajax"/>
		</div>
	</form:form>
</body>
</html>
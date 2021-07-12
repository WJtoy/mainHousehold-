<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>反馈分类</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            $("#inputForm").validate({
				submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);

                    $.ajax({
                        url:"${ctx}/provider/md/appFeedbackType/save",
                        type:"POST",
                        data:$(form).serialize(),
                        dataType:"json",
                        success: function(data){
                            //提交后的回调函数
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                                top.layer.close(this_index);//关闭本身
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data)
                        {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
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
		});

	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
	  </style>
  </head>
  
  <body>
	<form:form id="inputForm" modelAttribute="appFeedbackType" action="" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="parentId"/>
		<form:hidden path="hasChildren"/>
		<form:hidden path="isEffect"/>
		<form:hidden path="isAbnormaly"/>
		<form:hidden path="abnormalyOverTimes"/>
		<form:hidden path="actionType"/>
		<form:hidden path="name"/>
		<form:hidden path="userType"/>
		<form:hidden path="sumType"/>
		<sys:message content="${message}"/>
		<div class="control-group" style="margin-top: 20px">
			<label class="control-label">分类:</label>
			<div class="controls">
				<form:input path="label" htmlEscape="false" maxlength="20" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">反馈类型:</label>
			<div class="controls">
				<form:select disabled="${appFeedbackType.id > 0?'true':'false'}" path="feedbackType" cssClass="required input-medium" cssStyle="width: 220px;">
					<form:options items="${feedbackTypeEnumList}"
								  itemLabel="label" itemValue="value" htmlEscape="false" />
				</form:select>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label"><span style="color: red">*</span>数值:</label>
			<div class="controls">
				<form:input path="value" htmlEscape="false" maxlength="4" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">排序:</label>
			<div class="controls">
				<form:input path="sortBy" htmlEscape="false" maxlength="4" class="required"/>
			</div>
		</div>
	</form:form>
	<hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 15px"/>
	<shiro:hasPermission name="md:appfeedbacktype:edit">
		<input id="btnSubmit" class="btn btn-primary" type="submit" style="margin-left: 500px;margin-top:10px" onclick="$('#inputForm').submit()" value="保 存"/>
	</shiro:hasPermission>
  </body>
</html>

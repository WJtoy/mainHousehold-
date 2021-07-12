<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <title>线下充值审核无效</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            $("#btnSubmit").click(function () {
                var invalidType = $("input[name='invalidType']:checked").val();
                if(invalidType==null || invalidType==''){
                    layerError("请选中无效类型", "错误提示");
                    return false
                }
                if(invalidType==20 && ($("#invalidReason").val()==null || $("#invalidReason").val()=='')){
                    layerError("请输入无效原因", "错误提示");
                    return false
                }
                var $btnSubmit = $("#btnSubmit");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                $("#btnSubmit").prop("disabled",true);
                if (!$("#inputForm").valid()) {
                    $("#btnSubmit").prop("disabled",false);
                    return false;
                }
                $("#inputForm").submit();
            });

            $("#inputForm").validate({
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                submitHandler: function (form) {
                    //check
                    var loadingIndex = layerLoading('正在提交，请稍等...');
                    //form.submit();
                    $.ajax({
                        url:"${ctx}/fi/customer/offline/recharge/invalid",
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
                                    $("#btnSubmit").removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                                top.layer.close(this_index);
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $("#btnSubmit").removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data)
                        {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $("#btnSubmit").removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        }
                    });
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#btnSubmit").removeAttr('disabled');
                    $("#btnApprove").removeAttr('disabled');

                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });
        });

		//取消
		function cancel() {
            top.layer.close(this_index);//关闭本身
        }
	</script>
	  <style type="text/css">
		  .form-horizontal .control-label{
			  width: 90px;
		  }
		  .form-horizontal .controls{
			  margin-left: 100px;
		  }
	  </style>
  </head>
  
  <body>
    <sys:message content="${message}"/>
        <div style="width: 80%;padding-top: 30px;margin: 0 auto">
            <form:form id="inputForm" modelAttribute="customerOfflineRecharge" action="${ctx}/sd/order/crush/updateAddress" method="post" class="form-horizontal">
                <form:hidden path="id"/>
                <form:hidden path="phone"/>
                <form:hidden path="createAt"/>
                <form:hidden path="pendingAmount"/>
                <form:hidden path="payType"/>
                <div class="row-fluid">
                    <div class="span12">
                        <div class="control-group">
                            <label class="control-label">无效类型：</label>
                            <div class="controls">
                                <c:forEach items="${fns:getDictListFromMS('recharge_invalid_type')}" var="dict">
                                    <input type="radio" id="invalidType_${dict.value}" name="invalidType" value="${dict.value}">
                                    <label for="invalidType_${dict.value}">${dict.label}</label>&nbsp;&nbsp;
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row-fluid" style="margin-top: 10px">
                    <div class="span12">
                        <div class="control-group">
                            <label class="control-label">无效原因：</label>
                            <div class="controls">
                                <textarea name="invalidReason" id="invalidReason" rows="5" maxlength="255" class="input-block-level"></textarea>
                            </div>
                        </div>
                    </div>
                </div>
            </form:form>
        </div>
        <div style="height: 60px;width: 100%"></div>
        <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
            <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
            <div style="float: right;margin-top: 10px;margin-right: 20px">
                <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="保 存"/>
                <input id="btnCancel" class="btn" type="button" value="取 消" style="width: 96px;height: 40px" onclick="cancel()"/>
            </div>
        </div>
  </body>
</html>

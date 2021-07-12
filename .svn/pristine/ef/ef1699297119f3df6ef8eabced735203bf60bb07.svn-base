<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品</title>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
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
                        url:"${ctx}/md/servicepoint/updateTimeliness",
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

            switchEvent("#spanTimelinessFlag",function(){
                $("#timeLinessFlag").val(1)
            },function() {
                $("#timeLinessFlag").val(0)
            });

            switchEvent("#spanCustomerTimeLinessFlag",function(){
                $("#customerTimeLinessFlag").val(1)
            },function() {
                $("#customerTimeLinessFlag").val(0)
            });

		});

		//取消
		function cancel() {
            top.layer.close(this_index);//关闭本身
        }
	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
		  .form-horizontal {margin-top: 5px;}
		  .form-horizontal .control-label {width: 90px;}
		  .form-horizontal .controls {margin-left: 90px;}
	  </style>
  </head>
  
  <body>
   <c:choose>
	   <c:when test="${canSave==false}">
		   <sys:message content="${message}"/>
	   </c:when>
	   <c:otherwise>
		   <form:form id="inputForm" modelAttribute="servicePointDto" action="" method="post" class="form-horizontal">
			   <form:hidden path="id"/>
			   <sys:message content="${message}"/>
			   <div class="row-fluid" style="margin-top: 48px">
				   <div class="span6">
					   <div class="control-group">
						   <div class="controls">
							   <label class="control-label">网点编号：</label>
							   <div class="controls">
								   <form:input path="servicePointNo" readonly="true" cssStyle="width: 250px"></form:input>
							   </div>
						   </div>
					   </div>
				   </div>
			   </div>
			   <div class="row-fluid" style="margin-top: 10px">
				   <div class="span6">
					   <div class="control-group">
						   <div class="controls">
							   <label class="control-label">网点名称：</label>
							   <div class="controls">
								   <form:input path="name" readonly="true" cssStyle="width: 250px"></form:input>
							   </div>
						   </div>
					   </div>
				   </div>
			   </div>
			   <div class="row-fluid" style="margin-top: 10px">
				   <div class="span12">
					   <div class="control-group">
						   <div class="controls">
							   <label class="control-label">网点手机：</label>
							   <div class="controls">
								   <form:input path="contactInfo1" readonly="true" cssStyle="width: 250px"></form:input>
							   </div>
						   </div>
					   </div>
				   </div>
			   </div>
			   <div class="row-fluid" style="margin-top: 10px">
				   <div class="span12">
					   <div class="control-group">
						   <div class="controls">
							   <label class="control-label">快可立补贴：</label>
							   <div class="controls">
								   <c:choose>
									   <c:when test="${servicePointDto.timeLinessFlag==0}">
										   <span class="switch-off" id="spanTimelinessFlag"></span>
									   </c:when>
									   <c:otherwise>
										   <span class="switch-on" id="spanTimelinessFlag"></span>
									   </c:otherwise>
								   </c:choose>
								   <input type="hidden" value="${servicePointDto.timeLinessFlag}" name="timeLinessFlag" id="timeLinessFlag">
							   </div>
						   </div>
					   </div>
				   </div>
			   </div>
			   <div class="row-fluid" style="margin-top: 10px">
				   <div class="span12">
					   <div class="control-group">
						   <div class="controls">
							   <label class="control-label">客户时效：</label>
							   <div class="controls">
								   <c:choose>
									   <c:when test="${servicePointDto.customerTimeLinessFlag==0}">
										   <span class="switch-off" id="spanCustomerTimeLinessFlag"></span>
									   </c:when>
									   <c:otherwise>
										   <span class="switch-on" id="spanCustomerTimeLinessFlag"></span>
									   </c:otherwise>
								   </c:choose>
								   <input type="hidden" value="${servicePointDto.customerTimeLinessFlag}" name="customerTimeLinessFlag" id="customerTimeLinessFlag">
							   </div>
						   </div>
					   </div>
				   </div>
			   </div>
		   </form:form>
		   <div style="height: 60px;width: 100%"></div>
		   <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
			   <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
			   <input id="btnSubmit" class="btn btn-primary" type="button" onclick="$('#inputForm').submit()" value="保 存" style="margin-left: 370px;margin-top: 10px;width: 82px"/>&nbsp;&nbsp;
			   <input id="btnCancel" class="btn" type="button" value="关 闭" style="margin-top:10px;width: 82px"onclick="cancel()"/>
		   </div>
	   </c:otherwise>
   </c:choose>
  </body>
</html>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	 <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <title>修改白名单</title>
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
                        url:"${ctx}/provider/sys/userWhitelist/update",
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
                                top.layer.close(loadingIndex);
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
        function cancel() {
            top.layer.close(this_index);//关闭本身
        }
	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
		  .form-horizontal {margin-top: 5px;}
		  .form-horizontal .control-label {width: 80px;}
		  .form-horizontal .controls {margin-left: 90px;}
	  </style>
  </head>
  <body>
    <sys:message content="${message}"/>
	<c:if test="${canSave==true}">
		<form:form id="inputForm" modelAttribute="sysUserWhiteList" action="" method="post" class="form-horizontal">
			<form:hidden path="id" />
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label" style="width: 140px">用户姓名：</label>
						<div class="controls">
							<form:input path="userName" htmlEscape="false" minLength="2" maxlength="20" readonly="true"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label" style="width: 140px"><font style="color: red">*</font>到期日期：</label>
						<div class="controls">
							<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:200px;margin-left:4px"
								   maxlength="20" class="input-small Wdate required"
								   value="<fmt:formatDate value="${sysUserWhiteList.endDate}" pattern="yyyy-MM-dd"/>"
								   onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 10px">
				<div class="span12">
					<div class="control-group">
						<label class="control-label" style="width: 140px">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
						<div class="controls">
							<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="496" cssStyle="resize: vertical;"/>
						</div>
					</div>
				</div>
			</div>
		</form:form>
	</c:if>
	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
		<div style="float: right;margin-top: 10px;margin-right: 20px">
           <c:if test="${canSave==true}">
			   <shiro:hasPermission name="sys:whitelist:edit">
				   <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 76px;height: 35px" onclick="$('#inputForm').submit()" value="保 存"/>
			   </shiro:hasPermission>
	        </c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 76px;height: 35px" onclick="cancel()"/>
		</div>
	</div>
	<%--<shiro:hasPermission name="md:producttype:edit">
		<div style="background: white;height: 50px;position: absolute;bottom: 5px;width: 100%">
			<input id="btnSubmit" class="btn btn-primary" type="submit" style="margin-left: 570px;margin-top:10px" onclick="$('#inputForm').submit()" value="保 存"/>
		</div>
	</shiro:hasPermission>--%>
  </body>
</html>

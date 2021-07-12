<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>反馈原因</title>
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

            switchEvent("#isEffect",function(){
                $("#inputIsEffect").val(1)
			},function() {
                $("#inputIsEffect").val(0)
            });
        });

	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
		  .form-horizontal {margin-top: 5px;}
		  .form-horizontal .control-label {width: 100px;}
		  .form-horizontal .controls {margin-left: 110px;}
	  </style>
  </head>
  
  <body>
	<form:form id="inputForm" modelAttribute="appFeedbackType" action="" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="hasChildren" value="0"/>
		<sys:message content="${message}"/>
		<div class="row-fluid" style="margin-top: 7px">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">分类:</label>
					<div class="controls">
						<form:hidden path="parentId"/>
						<input value="${appFeedbackType.parentName}" class="fromInput" readonly="true" style="width: 220px">
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">反馈类型:</label>
					<div class="controls">
						<form:hidden path="feedbackType"/>
						<select class="input-small" style="width:225px;" disabled="true">
							<option value=""
							<c:forEach items="${feedbackTypeEnumList}" var="feedbackType">
								<option value="${feedbackType.value}"
										<c:out value="${(feedbackType.value eq appFeedbackType.feedbackType)?'selected=selected':''}" />>${feedbackType.label}</option>
							</c:forEach>
						</select>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">反馈原因:</label>
					<div class="controls">
						<form:input path="label" htmlEscape="false" maxlength="100" class="required" cssStyle="width: 600px"/>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">原因简称:</label>
					<div class="controls">
						<input name="name" value="${appFeedbackType.name}" class="fromInput required" style="width: 600px" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">反馈原因描述:</label>
					<div class="controls">
						<form:textarea path="remarks" htmlEscape="false" rows="5" maxlength="255" class="input-xlarge" cssStyle="width:600px;height: 64px"/>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">数值:</label>
					<div class="controls">
						<form:input path="value" htmlEscape="false" maxlength="4" class="required" cssStyle="width: 220px"/>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">使用方:</label>
					<div class="controls">
						<form:select path="userType" cssClass="required input-medium" cssStyle="width: 235px;">
							<form:options items="${userTypeList}"
										  itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">次数:</label>
					<div class="controls">
						<form:input path="abnormalyOverTimes" htmlEscape="false" maxlength="4" class="required" cssStyle="width: 220px"/>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">计次方式:</label>
					<div class="controls">
						<form:select path="sumType" cssClass="required input-medium" cssStyle="width: 235px;">
							<form:options items="${sumTypeList}"
										  itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">标记异常:</label>
					<div class="controls">
						<select name="isAbnormaly" class="required"  style="width: 235px">
							<option value="">请选择</option>
							<c:choose>
								<c:when test="${appFeedbackType.isAbnormaly==0}">
									<option value="0" selected>否</option>
									<option value="1">是</option>
								</c:when>
								<c:otherwise>
									<option value="0">否</option>
									<option value="1" selected>是</option>
								</c:otherwise>
							</c:choose>
						</select>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">处理方式:</label>
					<div class="controls">
						<form:select path="actionType" cssClass="required input-medium" cssStyle="width: 235px;">
							<form:options items="${actionTypeEnumList}"
										  itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">排序:</label>
					<div class="controls">
						<form:input path="sortBy" htmlEscape="false" maxlength="4" class="required" cssStyle="width: 220px"/>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">启用:</label>
					<div class="controls">
						<c:choose>
							<c:when test="${appFeedbackType.id !=null && appFeedbackType.id>0}">
                                <c:choose>
									<c:when test="${appFeedbackType.isEffect==0}">
									    <span class="switch-off" style="zoom: 0.7"  id="isEffect"></span>
									</c:when>
									<c:otherwise>
										<span class="switch-on" style="zoom: 0.7"  id="isEffect"></span>
									</c:otherwise>
								</c:choose>
								<input type="hidden" value="${appFeedbackType.isEffect}" name="isEffect" id="inputIsEffect">
							</c:when>
							<c:otherwise>
								<span class="switch-on" style="zoom: 0.7"  id="isEffect"></span>
								<input type="hidden" value="1" name="isEffect" id="inputIsEffect">
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
		</div>
		<%--<div class="form-actions">
			<shiro:hasPermission name="md:customerproduct:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
		</div>--%>
	</form:form>
	<hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 15px"/>
	<shiro:hasPermission name="md:appfeedbacktype:edit">
		<input id="btnSubmit" class="btn btn-primary" type="submit" style="margin-left: 700px;margin-top:10px" onclick="$('#inputForm').submit()" value="保 存"/>
	</shiro:hasPermission>
  </body>
</html>

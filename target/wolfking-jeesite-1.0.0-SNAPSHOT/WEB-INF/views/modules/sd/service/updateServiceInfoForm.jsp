<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>修改实际上门联系信息</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            $("[id='orderCondition.servicePhone']").select().focus();
			$("#inputForm").validate({
				submitHandler: function(form){
                    var $btnSubmit = $("#btnSubmit");
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sd/order/checkOrderKefuType?"+ (new Date()).getTime(),
                        data:$(form).serialize(),
                        success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            var confirmMsg = "确定要保存新的实际上门信息吗？"
                            if(data && data.success == true){
                                if(data.message){
                                    confirmMsg = data.message;
								}
                                top.layer.confirm(
                                    confirmMsg
                                    ,{zIndex:29991016,icon: 3,closeBtn: 0,title:'系统确认',success: function(layro, index) {
                                            $(document).on('keydown', layro, function(e) {
                                                if (e.keyCode == 13) {
                                                    layro.find('a.layui-layer-btn0').trigger('click')
                                                }else if(e.keyCode == 27){//esc
                                                    clickTag = 0;
                                                    $btnSubmit.removeAttr('disabled');
                                                    top.layer.close(index);//关闭本身
                                                }
                                            })
                                        }}
                                    ,function(index){
                                        if(clickTag == 1){
                                            return false;
                                        }
                                        clickTag = 1;
                                        $btnSubmit.attr('disabled', 'disabled');
                                        top.layer.close(index);
                                        var loadingIndex;
                                        var json = {};
                                        json.serviceAddress = $("[id='orderCondition.serviceAddress']").val();
                                        json.servicePhone = $("[id='orderCondition.servicePhone']").val();
                                        json.description = $("#description").val();
                                        json.areaName = $("#areaName").val();
                                        $.ajax({
                                            async: false,
                                            cache: false,
                                            type: "POST",
                                            url: "${ctx}/sd/order/updateUserServiceInfo?"+ (new Date()).getTime(),
                                            data:$(form).serialize(),
                                            beforeSend: function () {
                                                loadingIndex = top.layer.msg('正在提交，请稍等...', {
                                                    icon: 16,
                                                    time: 0,
                                                    shade: 0.3
                                                });
                                            },
                                            complete: function () {
                                                //console.log("" + new Date().getTime() + " [complete] clickTag:" + clickTag);
                                                if(loadingIndex) {
                                                    top.layer.close(loadingIndex);
                                                }
                                                setTimeout(function () {
                                                    clickTag = 0;
                                                    $btnSubmit.removeAttr('disabled');
                                                }, 2000);
                                            },
                                            success: function (data) {
                                                if(ajaxLogout(data)){
                                                    return false;
                                                }
                                                if(data && data.success == true){
                                                    //回调父窗口方法
                                                    var layero = $("#layui-layer" + parentIndex,top.document);
                                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                                    iframeWin.updateUserServiceInfo(json);
                                                    top.layer.close(this_index);//关闭本窗口
                                                    return false;
                                                }
                                                else if( data && data.message){
                                                    layerError(data.message,"错误提示");
                                                }
                                                else{
                                                    layerError("修改实际上门联系信息错误","错误提示");
                                                }
                                                return false;
                                            },
                                            error: function (e) {
                                                ajaxLogout(e.responseText,null,"修改实际上门联系信息错误，请重试!");
                                            }
                                        });//end ajax
                                    },function(index){
                                        //cancel
                                    });//end confirm
                            }
                            else if( data && data.success == false && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else{
                                layerError("修改实际上门联系信息错误","错误提示");
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"修改实际上门联系信息错误，请重试!");
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
			
		});


        function closethisfancybox() {
            top.layer.close(this_index);
//			top.$.jBox.close();
        }

        function areaIdCallback(){
        }

	</script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label{width:120px;}
		.form-horizontal .controls {margin-left: 130px;}
		i[class^="icon-"] {font-size:18px;}
	</style>	
</head>
<body>
	<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/updateUserServiceInfo" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="quarter"/>
		<sys:message content="${message}"/>
		<div style="margin-top: 30px;"></div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">下单电话:</label>
					<div class="controls">
						<form:input path="orderCondition.phone1" type="tel" htmlEscape="false" class="input-large" readonly="true" cssStyle="width: 150px;"/>
						<button id="btnCall" class="btn btn-success" type="button" onclick="javascript:StartDial('',false)"><i class="icon-phone-sign icon-white"></i> 拨号</button>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">联络电话:</label>
					<div class="controls">
						<form:input path="orderCondition.servicePhone" type="tel" htmlEscape="false" class="input-block-level phone required" maxlength="11" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">下单地址:</label>
					<div class="controls">
						<form:input path="orderCondition.address"  htmlEscape="false" class="input-block-level" readonly="true" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label"><span style="color: red">*</span>上门地址:</label>
					<div class="controls">
						<form:hidden path="orderCondition.subArea.id" id="subAreaId" />
						<sys:newareaselect name="orderCondition.area.id" id="area" value="${order.orderCondition.area.id}" labelValue="${order.orderCondition.area.name}" labelName="orderCondition.area.name"
										   title="" mustSelectCounty="true" cssClass="required" cssStyle="width: 220px;"
										   callback=''>
						</sys:newareaselect>

					</div>
				</div>
			</div>
			<div class="span6">
				<form:input  path="orderCondition.serviceAddress" cssClass="input-block-level required" maxlength="100"></form:input>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">服务描述:</label>
					<div class="controls">
						<form:textarea path="description" htmlEscape="true" rows="2" maxlength="250" class="input-block-level"/>
					</div>
				</div>
			</div>
		</div>
		<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
			<c:if test="${empty canSave || canSave ne false }">
				<shiro:hasPermission name="sd:order:plan"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/></shiro:hasPermission>
			</c:if>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();"/>
		</div>
	</form:form>
</body>
</html>
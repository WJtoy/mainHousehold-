<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户店铺</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            $("#inputForm").validate({
				submitHandler: function(form){
                    var dataSource = $("#dataSource").val();
                    var b2bCustomerCode = $("#b2bCustomerCode").val();
                    if(dataSource == 14){
                        if(b2bCustomerCode ==null || b2bCustomerCode ==''){
                            layerMsg('数据源是京东优易时,客户编码不能为空');
                            return false;
                        }
                    }
					// loading('正在提交，请稍候...');
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
					// form.submit();
                    $.ajax({
                        url:"${ctx}/b2bcenter/md/customer/save",
                        type:"POST",
                        data:$(form).serialize(),
                        dataType:"json",
                        success: function(data){
                            // 提交后的回调函数
                            if (loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if (ajaxLogout(data)) {
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
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
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
        // 关闭页面
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
	  </style>
  </head>
  
  <body>
  	<div id="addShop" style="margin-top: 40px;">

		<form:form id="inputForm" modelAttribute="b2BCustomerMapping" action="" method="post" class="form-horizontal">
			<form:hidden path="id"/>
			<sys:message content="${message}"/>
			<div class="control-group" style="margin-top: 20px">
				<label class="control-label">数据源:</label>
				<div class="controls">
					<form:hidden path="dataSource"></form:hidden>
					<input class="fromInput" readonly="true" value="${fns:getDictLabelFromMS(b2BCustomerMapping.dataSource, 'order_data_source','' )}"></input>
					<span class="red">*</span>
				</div>
			</div>

			<div class="control-group d-group" style="margin-bottom: 10px;">
				<label class="control-label">销售渠道:</label>
				<div class="controls">
					<form:select path="saleChannel" cssClass="required input-medium" cssStyle="width: 220px;">
						<form:option value="" label="请选择"/>
						<form:options items="${fns:getDictExceptListFromMS('sale_channel',0)}"
									  itemLabel="label" itemValue="value" htmlEscape="false" />
					</form:select>
					<span class="red">*</span>
				</div>
			</div>

			<div class="control-group">
				<label class="control-label">店铺ID:</label>
				<div class="controls">
					<form:input path="shopId" htmlEscape="false" minLength="2" maxlength="25" class="required"/>
					<span class="red">*</span>
					<span>(数据源客户ID)</span>
				</div>
			</div>

			<div class="control-group">
				<label class="control-label">店铺名称:</label>
				<div class="controls">
					<form:input path="shopName" htmlEscape="false" minLength="2" maxlength="50" class="required"/>
					<span class="red">*</span>
					<span>(数据源客户名称)</span>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">客户:</label>
				<div class="controls" >
					<form:select path="customerId" cssClass="input-small required" cssStyle="width:225px;">
						<form:option value="" label="请选择"/>
						<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
					</form:select>
					<span class="red">*</span>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">b2b客户编码:</label>
				<div class="controls">
					<form:input path="b2bCustomerCode" htmlEscape="false" minLength="2" maxlength="30"/>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">是否是默认店铺:</label>
				<div class="controls">
					<c:choose>
						<c:when test="${b2BCustomerMapping.defaultShopFlag==1}">
							<form:radiobutton path="defaultShopFlag" value="0"  label="否"/>
							<form:radiobutton path="defaultShopFlag" value="1" checked="checked" label="是"/>
						</c:when>
						<c:otherwise>
							<form:radiobutton path="defaultShopFlag" value="0"  checked="checked" label="否"/>
							<form:radiobutton path="defaultShopFlag" value="1"  label="是"/>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">描述:</label>
				<div class="controls">
					<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 260px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
				</div>
			</div>
			<div id="editbtn" style="margin-top: 50px;">
				<shiro:hasPermission name="md:b2bcustomer:edit">
					<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-left: 410px;margin-top: 2px;"/>
					&nbsp;
				</shiro:hasPermission>
				<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="cancel()"/>
			</div>
		</form:form>
	</div>
  </body>
</html>

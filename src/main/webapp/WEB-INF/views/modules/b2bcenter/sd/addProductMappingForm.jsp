<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>b2b系统客户和产品配置</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        var this_index = top.layer.index;
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
                    // form.submit();
                    $.ajax({
                        url:"${ctx}/b2bcenter/md/product/ajax/saveProductMapping",
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
            loadCustomerId();
            loadShopId();
		});


        function loadCustomerId() {
            var dataSource =$("#dataSource").val();
            var defaultCustomerId = $("#defaultCustomerId").val();
            if(dataSource==null || dataSource==''){
                return false;
            }
            $.ajax({
                url:"${ctx}/b2bcenter/md/customer/ajax/getCustomerListByDataSource?dataSource="+dataSource,
                async: false,
                success:function (e){
                    if(e.success){
                        $("#customerId").empty();
                        var customer_sel=[];
                        customer_sel.push('<option value="" selected="selected">请选择</option>')
                        for(var i=0,len=e.data.length;i<len;i++){
                            var programme = e.data[i];
                            if(programme.id==defaultCustomerId){
                                customer_sel.push('<option value="'+programme.id+'" selected>'+programme.name+'</option>')
							}else{
                                customer_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
							}
                        }
                        $("#customerId").append(customer_sel.join(' '));
                        $("#customerId").change();
                    }else{
                        layerError("获取客户错误:" + e.message, "错误提示");
                        $("#customerId").html('<option value="" selected>请选择</option>');
                        $("#customerId").change();
                    }
                },
                error:function (e) {
                    layerError("获取客户信息错误","错误提示");
                    $("#customerId").html('<option value="" selected>请选择</option>');
                    $("#customerId").change();
                }
            });
        }
        
        function loadShopId() {
            var customerId =$("#customerId").val();
            var dataSource = $("#dataSource").val();
            if(dataSource ==null || dataSource ==''){
                layerMsg('请先选择数据源！');
                return false;
            }
            if (customerId == "")
            {
                return false;
            }
            var defaultShopId = $("#defaultShopId").val();
            $.ajax({
                    url:"${ctx}/b2bcenter/md/product/ajax/getByCustomerId?customerId="+customerId +"&dataSource=" + dataSource,
                    success:function (e) {
                        if(e.success){
                            $("#productId").empty();
                            $("#shopId").empty();
                            var programme_sel=[];
                            var shopId_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            shopId_sel.push('<option value="" selected="selected">请选择</option>')
                            if(typeof(e.data.productList)!="undefined"){
                                for(var i=0,len=e.data.productList.length;i<len;i++){
                                    var programme = e.data.productList[i];
                                    programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                }
                            }
                            $("#productId").append(programme_sel.join(' '));
                            $("#productId").val("");
                            $("#productId").change();

                            if(typeof(e.data.customerMappingList)!="undefined"){
                                for(var i=0,len=e.data.customerMappingList.length;i<len;i++){
                                    var customerMapping = e.data.customerMappingList[i];
                                    if(customerMapping.shopId==defaultShopId){
                                        shopId_sel.push('<option value="'+customerMapping.shopId+'" selected>'+customerMapping.shopName+'</option>')
									}else{
                                        shopId_sel.push('<option value="'+customerMapping.shopId+'">'+customerMapping.shopName+'</option>')
									}
                                }
                            }
                            $("#shopId").append(shopId_sel.join(' '));
                            $("#shopId").change();


                        }else {
                            $("#productId").html('<option value="" selected>请选择</option>');
                            $("#productId").change();

                            $("#shopId").html('<option value="" selected>请选择</option>');
                            $("#shopId").change();
                        }
                    },
                    error:function (e) {
                        layerError("请求客户产品失败","错误提示");
                    }
                }
            );
        }
		function cancel() {
            top.layer.close(this_index);//关闭本身
        }

	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
	  </style>
  </head>
  
  <body>
	<input id="defaultCustomerId" type="hidden" value="${productMapping.customerId}">
	<input id="defaultShopId" type="hidden" value="${productMapping.shopId}">
	<form:form id="inputForm" modelAttribute="productMapping" action="${ctx}/b2bcenter/md/product/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<input type="hidden" id= "codeValue" value="${productMapping.customerProductModel}">
		<sys:message content="${message}"/>
		<div class="control-group" style="margin-top: 20px">
			<label class="control-label">数据源:</label>
			<div class="controls">
				<form:hidden path="dataSource"></form:hidden>
				<input class="fromInput required" readonly="true" value="${fns:getDictLabelFromMS(productMapping.dataSource, 'order_data_source','Unknow' )}"></input>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">客户:</label>
			<div class="controls">
				<select id="customerId" name="customerId" style="width:225px;" class="required" onchange="loadShopId()">
					<option value="">请选择</option>
				</select>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">店铺名称:</label>
			<div class="controls">
				<select id="shopId" name="shopId" style="width:225px;" class="required">
					<option value="" selected>请选择</option>
				</select>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">工单产品:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${productMapping.productId >0}">
						<form:hidden path="productId"></form:hidden>
						<input class="fromInput required" readonly="true" value="${productMapping.productName}" />
					</c:when>
					<c:otherwise>
						<select id="productId" name="productId" style="width:225px;" class="required">
							<option value="" selected>请选择</option>
						</select>
					</c:otherwise>
				</c:choose>
				<span class="add-on red">*</span>
			</div>
		</div>
		<div id="productDiv">
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">数据源料号:</label>
						<div class="controls">
							<input name="customerCategoryId" value="${productMapping.customerCategoryId}" class='fromInput required' maxlength="100" placeholder="数据源产品ID,必填"/>
							<span class="add-on red">*</span>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">数据源产品型号:</label>
			<div class="controls">
				<input name="productCode" value="${productMapping.productCode}" class='fromInput' maxlength="30" placeholder="数据源产品型号，选填"/>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<%--<div class="form-actions">
			<shiro:hasPermission name="md:b2bproduct:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="cancel()"/>
		</div>--%>
		<div id="editbtn" style="margin-top: 50px;">
			<shiro:hasPermission name="md:b2bproduct:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-left: 410px;margin-top: 2px;"/>
				&nbsp;
			</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="关 闭" onclick="cancel()"/>
		</div>
	</form:form>
  </body>
</html>

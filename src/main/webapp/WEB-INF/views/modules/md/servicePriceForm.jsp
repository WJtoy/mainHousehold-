<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<!-- 网点单个价格 -->
	<title>安维价格管理</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		var clickTag = 0;
        var $btnSubmit = $("#btnSubmit");
		$(document).ready(function() {
			$("#engineer").focus();
            $("#inputForm").validate({
                // rules: {
                //     price: {
                //         required:true,
					// 	number:true
                //     }
                // },
                // messages: {
                //     price: {
                //         required: "请输入价格",
					// 	number:"价格为数值类型"
                //     }
                // },
                highlight : function(element) {
                    $(element).closest('.control-group').addClass('has-error');
                },
                success : function(label) {
                    label.closest('.form-group').removeClass('has-error');
                    label.remove();
                },
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    element.parent('div').append(error);
                },
                submitHandler: function (form) {
                    if (clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var price = parseFloat($("#price").val());
                    var IW = $("[id='serviceType.warrantyStatus.value']").val();
                    var discountPrice = parseFloat($("#discountPrice").val());
                    //保外,优惠价应大于等于标准价，因保外价格都是小于等于0
                    if((IW == 'IW'  || IW == '' ) && discountPrice > price){
                        layerError('优惠价不能大于价格。', "错误提示");
                        clickTag = 0;
                        return false;
                    }
                    //保外,优惠价应大于等于标准价，因保外价格都是小于等于0
                    if(IW != 'IW' && discountPrice < price){
                        layerError('保外优惠价应大于等于价格。', "错误提示");
                        clickTag = 0;
                        return false;
                    }
                    $btnSubmit = $("#btnSubmit");
                    var ajaxSuccess = 0;
                    $btnSubmit.attr('disabled', 'disabled');
                    var loadingIndex;
                    var url = "${ctx}/md/serviceprice/save?qServicePointId=${servicePointId}&qServicePointName=${fns:urlEncode(servicePointName)}&qProductCategoryId=${productCategoryId}&qProductCategoryName=${fns:urlEncode(productCategoryName)} &qProductId=${productId}&qProductName=${fns:urlEncode(productName)}";
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: url +"&_t"+ (new Date()).getTime(),
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
                                ajaxSuccess = 1;
                                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                iframe.location=data.data;
                            }
                            else if( data && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else{
                                layerError("保存安维价格错误","错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"保存安维价格错误，请重试!");
                        }
                    });

                    return false;
                }
            });
            <c:if test="${servicePrice.id != null && servicePrice.id >0}">
            <c:if test="${servicePrice.serviceType.warrantyStatus.value eq 'IW'}">
            $("#price").rules("add",{min:0,messages:{min:"保内价格应大于等于0"}});
            $("#discountPrice").rules("remove");
            $("#discountPrice").rules("add",{min:0,nogt:"#price",messages:{min:"保内优惠价应大于等于0",nogt:"优惠价不能高于价格"}});
            </c:if>
            </c:if>
		});
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/md/serviceprice">安维价格列表</a></li>
		<li class="active"><a href="javascript:;">安维价格<shiro:hasPermission name="md:serviceprice:edit">${not empty servicePrice.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:serviceprice:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="servicePrice" action="${ctx}/md/serviceprice/save?qServicePointId=${servicePointId}&qServicePointName=${fns:urlEncode(servicePointName)}
											&qProductCategoryId=${productCategoryId}&qProductCategoryName=${fns:urlEncode(productCategoryName)}
											&qProductId=${productId}&qProductName=${fns:urlEncode(productName)}" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="servicePoint.id"/>
		<form:hidden path="product.id"/>
		<form:hidden path="serviceType.warrantyStatus.value"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">安维网点:</label>
			<div class="controls">
				<input type="text" id="servicePoint.name" name="servicePoint.name" value="${servicePrice.servicePoint.name}" readonly="readonly" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">产品:</label>
			<div class="controls">
				<input type="text" id="product.name" name="product.name" value="${servicePrice.product.name}" readonly="readonly" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">服务类型:</label>
			<div class="controls">
				<sys:treeselect id="serviceType" name="serviceType.id" value="${servicePrice.serviceType.id}" labelName="serviceType.name" labelValue="${servicePrice.serviceType.name}"
					title="服务类型" url="/md/servicetype/treeData" cssClass="required" disabled="true"/>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">价格:</label>
			<div class="controls">
				<form:input path="price" htmlEscape="false" maxlength="7"  class="required number"/>
				<c:forEach items="${productPriceList}" var="productPrice">
					${productPrice.priceTypeName}:${productPrice.standPrice}
				</c:forEach>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">优惠价:</label>
			<div class="controls">
				<form:input path="discountPrice" htmlEscape="false" maxlength="7" class="required number"/>
				<c:forEach items="${productPriceList}" var="productPrice">
					${productPrice.priceTypeName}:${productPrice.discountPrice}
				</c:forEach>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">是否启用:</label>
			<div class="controls">
				<form:radiobutton path="delFlag" value="0"></form:radiobutton>是
				<form:radiobutton path="delFlag" value="1"></form:radiobutton>否
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:serviceprice:edit"><input id="btnSubmit" name="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" name="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
	
	
</body>
</html>
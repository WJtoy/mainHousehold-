<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客户产品型号</title>
	<meta name="decorator" content="default" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${ctxStatic}/jquery-ztree/3.5.12/css/zTreeStyle/zTreeStyle.min.css" type="text/css">
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<script type="text/javascript" src="${ctxStatic}/jquery-ztree/3.5.12/js/jquery.ztree.all-3.5.min.js"></script>
	<script type="text/javascript">
        $(document).ready(function() {
            $("#inputForm").validate({
                    submitHandler : function(form)
                    {
                        loading('正在提交，请稍等...');
                        var $btnSubmit = $("#btnSubmit");
                        if ($btnSubmit.prop("disabled") == true) {
                            event.preventDefault();
                            return false;
                        };
                        $btnSubmit.prop("disabled", true);
                        form.submit();
                    },
                    errorContainer : "#messageBox",
                    errorPlacement : function(error, element)
                    {
                        $("#messageBox").text("输入有误，请先更正。");
                        if (element.is(":checkbox")
                            || element.is(":radio")
                            || element.parent().is(
                                ".input-append"))
                        {
                            error.appendTo(element.parent()
                                .parent());
                        } else
                        {
                            error.insertAfter(element);
                        }
			}});

            $(document).on('change','.selectCustomer',function(e){
                var customerId =$(this).val();
                if (customerId == "")
				{
				    return false;
				}
                $.ajax({
                        url:"${ctx}/md/customerproductmodel/ajax/customerProductList?customerId="+customerId,
                        success:function (e) {
                            if(e.success){
                                $("#productId").empty();
                                var programme_sel=[];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                }
                                $("#productId").append(programme_sel.join(' '));
                                $("#productId").val("");
								$("#productId").change();
                            }else {
                                $("#productId").html('<option value="" selected>请选择</option>');
                                layerMsg('该客户还没有关联商品！');
                            }
                        },
                        error:function (e) {
                            layerError("请求客户产品失败","错误提示");
                        }
                    }
                );
            });

            $("th").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });

	</script>
	<style type="text/css">
		.form-horizontal .control-label{
			width: 180px;
		}
		.form-horizontal .controls{
			margin-left: 200px;
		}
		.form-horizontal .control-group{
			margin-bottom: 15px;
		}
		.fromInput {
			border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		}

	</style>
</head>

<body>
<ul class="nav nav-tabs">
	<li>
		<a href="${ctx}/md/customerproductmodel/list">列表</a>
	</li>
	<li class="active">
		<a href="javascript:void(0);">添加</a>
	</li>
</ul><br>
<form:form id="inputForm" modelAttribute="customerProductModel" action="${ctx}/md/customerproductmodel/save" method="post" class="form-horizontal">
	<sys:message content="${message}" />
	<c:if test="${canAction == true}">
		<form:hidden path="id"></form:hidden>
		<div class="control-group">
			<label class="control-label">客户:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${customerProductModel.customer.id > 0}">
						<form:hidden path="customer.id"></form:hidden>
						<form:input path="customer.name" readonly="true"></form:input>
						<span class="add-on red">*</span>
					</c:when>
					<c:otherwise>
						<form:select id="customerId" path="customer.id" cssClass="input-small required selectCustomer" cssStyle="width:225px;">
							<form:option value="" label="请选择"/>
							<form:options items="${customerList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
						</form:select>
						<span class="add-on red">*</span>
					</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">产品:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${customerProductModel.customer.id > 0}">
						<form:hidden path="product.id"></form:hidden>
						<form:input path="product.name" readonly="true"></form:input>
						<span class="add-on red">*</span>
					</c:when>
					<c:otherwise>
						<select id="productId" name ="product.id" style="width:225px;" class="required">
							<option value="" selected>请选择</option>
						</select>
						<span class="add-on red">*</span>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<div id="productDiv">
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">型号:</label>
						<div class="controls">
							<input class="fromInput required" id="customerModel" name="customerModel" value="${customerProductModel.customerModel}" htmlEscape="false" maxlength="30"/>
							<span class="add-on red">*</span>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div id="productDiv">
			<div class="row-fluid">
				<div class="span12">
					<div class="control-group">
						<label class="control-label">名称:</label>
						<div class="controls">
							<input class="fromInput" name="customerProductName" value="${customerProductModel.customerProductName}" htmlEscape="false" maxlength="30"/>
						</div>
					</div>
				</div>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
	</c:if>
	<div id="formActions" class="form-actions">
		<c:if test="${canAction == true}">
			<shiro:hasPermission name="md:customerproductmodel:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit"
					   value="保 存" />&nbsp;</shiro:hasPermission>
		</c:if>
		<input id="btnCancel" class="btn" type="button" value="返 回"
			   onclick="history.go(-1)" />
	</div>
</form:form>
</body>
</html>

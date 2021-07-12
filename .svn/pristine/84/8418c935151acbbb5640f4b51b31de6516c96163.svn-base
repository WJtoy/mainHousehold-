<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>互助基金配置</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <c:set var="currentuser" value="${fns:getUser()}"/>
	<style type="text/css">
		#editBtn {
			position: fixed;
			left: 0px;
			bottom: 0;
			width: 100%;
			height: 60px;
			background: #fff;
			z-index: 10;
			border-top: 1px solid #e5e5e5;
		}
		.maxAmount{
			background-color: #fff;
			border: 1px solid #ccc;
			transition: border linear .2s,box-shadow linear .2s;
			display: inline-block;
			height: 20px;
			padding: 4px 6px;
			margin-bottom: 10px;
			font-size: 14px;
			line-height: 20px;
			color: #555;
			vertical-align: middle;
			border-radius: 4px;
		}
	</style>
    <script type="text/javascript">
        var this_index = top.layer.index;

        // 关闭页面
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }

        var clickTag = 0;

        $(document).ready(
            function () {
                $(document).ready(function () {
                    $("#inputForm").validate({
                        submitHandler: function (form) {
							var id = $("#id").val();
							var productCategoryId = $("#categoryId").val();
							var port = true;

							$.ajax({
								async: false,
								url:"${ctx}/md/insurancePrice/checkProductCategory?loginId="+ id + "&productCategoryId="+productCategoryId,
								success:function (e) {
									if(e.success){

									}else {
										port = false;
										layerMsg('该产品品类已配置过！');
									}
								},
								error:function (e) {
									layerError("验证产品品类失败","错误提示");
								}
							});
							if(!port){
								return false;
							}

                            var loadingIndex = layerLoading('正在提交，请稍候...');
                            var $btnSubmit = $("#btnSubmit");
                            if ($btnSubmit.prop("disabled") == true) {
                                event.preventDefault();
                                return false;
                            }
                            $btnSubmit.prop("disabled", true);
                            $.ajax({
                                url: "${ctx}/md/insurancePrice/ajaxSave",
                                type: "POST",
                                data: $(form).serialize(),
                                dataType: "json",
                                success: function (data) {
                                    //提交后的回调函数
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
                                        top.layer.close(this_index);//关闭本身
                                        var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                        if (pframe) {
                                            pframe.repage();
                                        }
                                    } else {
                                        setTimeout(function () {
                                            clickTag = 0;
                                            $btnSubmit.removeAttr('disabled');
                                        }, 2000);
                                        layerError(data.message, "错误提示");
                                    }
                                    return false;
                                },
                                error: function (data) {
                                    if (loadingIndex) {
                                        layer.close(loadingIndex);
                                    }
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                    }, 2000);
                                    ajaxLogout(data, null, "数据保存错误，请重试!");
                                    //var msg = eval(data);
                                },
                                timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                            });
                        },
                        errorContainer: "#messageBox",
                        errorPlacement: function (error, element) {
                        	$("#messageBox").text("输入有误，请先更正。");
							if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
								error.appendTo(element.parent().parent());
							} else if(element.parent().is(".depositLevel")){
								error.insertAfter(element);
							}else {
								var nspan = $(element.parent()).find("span");
								if(nspan){
									error.insertAfter(nspan);
								}else{
									error.insertAfter(element);
								}
							}
                        }
                    });

                });
            });

    </script>
</head>
<body>
<form:form id="inputForm" modelAttribute="insurancePrice" action="${ctx}/md/insurancePrice/ajaxSave" method="post"
           class="form-horizontal">
    <form:hidden path="id"/>
    <input id="oldProductCategoryId" name="oldProductCategoryId" type="hidden" value="${insurancePrice.category.id}"/>
    <sys:message content="${message}"/>
    <div class="control-group" style="margin-top: 45px">
        <label class="control-label">产品品类:</label>
		<div class="controls">
			<sys:treeselect id="category" name="category.id" value="${insurancePrice.category.id}" labelName="category.name" labelValue="${insurancePrice.category.name}"
							title="产品分类" url="/md/productcategory/treeData" cssClass="required"/>
		</div>
    </div>
    <div class="control-group">
        <label class="control-label">基金金额:</label>
        <div class="controls">
            <form:input path="insurance" htmlEscape="false" maxlength="10" class="required number"
                        placeholder="每单完工应扣除基金金额"/>
			<span class="add-on" style="margin-left: -5px">元</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">描述:</label>
        <div class="controls">
            <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" class="input-xlarge"
                           cssStyle="max-width: 1000px;max-height: 300px;min-width: 200px;min-height: 50px;"/>
        </div>
    </div>
    <div id="editBtn" class="line-row">

        <shiro:hasPermission name="md:insuranceprice:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"
                   style="width: 96px;height: 40px;margin-left: 70%;margin-top: 10px;margin-bottom: 10px"/>
        </shiro:hasPermission>

        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 96px;height: 40px;margin-top: 10px;margin-left: 13px;margin-bottom: 10px"/>
    </div>
</form:form>
</body>
</html>

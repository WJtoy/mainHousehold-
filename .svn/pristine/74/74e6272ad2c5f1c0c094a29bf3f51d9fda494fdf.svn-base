<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler: function(form){
                    var oEditor = CKEDITOR.instances.fixSpec;
                    var content = oEditor.getData();

                    if(isNull(content)){
                        layerMsg('请输入安装规范');
                        return false;
                    }

                    $("#fixSpec").text(content);

                    if(clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);

                    $.ajax({
                        url:"${ctx}/provider/md/customerProduct/save",
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

            $("#btnSubmit").css({'margin-left':'770px'});
        });

        $(document).on('change',"#customerId",function (e) {
            var customerId = $(this).val();
            if (customerId !=null || customerId !='' || customerId != '0') {
                $("#customerId-error").html("");
                $("#customerId-error").removeClass("error");
            }
        });

        $(document).on('change',"#productId",function (e) {
            var productId = $(this).val();
            var customerId = $("#customerId").val();
            if (productId !=null && productId !='' && productId != '0') {
                $("#productId-error").html("");
                $("#productId-error").removeClass("error");

                if (customerId != null && customerId !='' && customerId !='0') {
                    $.ajax({
                        url: "${ctx}/provider/md/customerProduct/ajax/form",
                        data: {customerId: customerId, productId: productId},
                        success:function (e) {
                            if(e.success){
                                setTimeout(function () {
                                    var id = e.data.id;
                                    var lhref = window.location.href+"?id="+ id;
                                    location.replace(lhref);
                                }, 200);
                            }
                        },
                        error:function (e) {
                            $("#id").val("");
                            layerError("请求产品失败","错误提示");
                        }
                    });
                } else {
                    //console.log("customerId为空");
                }
            }
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
        .form-horizontal {margin-top: 40px;}
        .form-horizontal .control-label {width: 77px;text-align: left;}
        .form-horizontal .controls {margin-left: 75px;}
        .form-horizontal .control-group {border-bottom: 0px;}

        textarea {padding:0px;}
    </style>
</head>

<body>
<form:form id="inputForm" modelAttribute="customerProduct" action="" method="post" class="form-horizontal">
    <c:set var="cuser" value="${fns:getUser()}" />
    <form:hidden path="id"/>
    <sys:message content="${message}"/>
    <div class="row-fluid" style="margin-top: 7px">
        <div class="span6">
            <div class="control-group">
                <label class="control-label"><span class=" red">*</span>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户:</label>
                <div class="controls">
                    <c:choose>
                        <c:when test="${customerProduct.customerDto.id> 0}">
                            <input type="hidden" name="customer.id" value="${customerProduct.customerDto.id}">
                            <form:input id = "customer.name" path="customerDto.name" readonly="true" cssStyle="width: 250px"></form:input>
                        </c:when>
                        <c:otherwise>
                            <select id="customerId" name="customer.id" class="input-small required selectCustomer" style="width:250px;">
                                <option value=""
                                        <c:out value="${(empty customerProduct.customerDto.id)?'selected=selected':''}" />>请选择</option>
                                <c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
                                    <option value="${customer.id}"
                                            <c:out value="${(customerProductModel.customerDto.id eq customer.id)?'selected=selected':''}" />>${customer.name}</option>
                                </c:forEach>
                            </select>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label"><span class=" red">*</span>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品:</label>
                <div class="controls">
                    <c:choose>
                        <c:when test="${customerProduct.productDto.id > 0}">
                            <input type="hidden" name="product.id" value="${customerProduct.productDto.id}">
                            <%--<form:hidden id="product.id" path="productDto.id"></form:hidden>--%>
                            <form:input path="productDto.name" readonly="true" cssStyle="width: 240px"></form:input>
                        </c:when>
                        <c:otherwise>

                            <select id="productId" name ="product.id" style="width:250px;" class="required">
                                <option value=""
                                        <c:out value="${(empty customerProduct.productDto.id)?'selected=selected':''}" />>请选择</option>
                                <c:forEach items="${fns:getProducts()}" var="product">
                                    <option value="${product.id}"
                                            <c:out value="${(customerProductModel.productDto.id eq product.id)?'selected=selected':''}" />>${product.name}</option>
                                </c:forEach>
                            </select>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
    <div class="control-group" style="margin-top: 10px;">
        <label class="control-label"><span class=" red">*</span>安装规范:</label>
        <div class="controls" style="width: 885px;height:552px;">
            <form:textarea path="fixSpec" htmlEscape="false" rows="10" maxlength="2000"  cssStyle="min-width: 280px;max-width: 875px;min-height: 70px;max-height: 552px;width: 875px;height: 552px" placeholder=""/>
            <sys:ckeditor replace="fixSpec" uploadPath="customerproduct" height="448" />
        </div>
    </div>


</form:form>
<hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 40px"/>
<shiro:hasPermission name="md:customerproduct:edit">
    <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="$('#inputForm').submit()" style="margin-top:15px;margin-left: 20px;width: 96px;height: 40px;" value="保 存"/>
    &nbsp;&nbsp;<input id="btnCancel" class="btn" type="button" value="取 消" style="margin-top:15px;margin-left: 0px;width: 96px;height: 40px;"onclick="cancel()"/>
</shiro:hasPermission>
<script type="text/javascript">
    function isNull(str){
        if ( str == "" ) return true;
        var regu = "^[ ]+$";
        var re = new RegExp(regu);
        return re.test(str);
    }
</script>
</body>
</html>

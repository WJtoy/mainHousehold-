<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户-品牌-产品分类配置</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <c:set var="currentuser" value="${fns:getUser()}"/>
	<style>
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 3px;
            width: 100%;
            height: 55px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #ccc;
            border-top: 1px solid #e5e5e5;
            text-align: right;
        }
    </style>
    <script type="text/javascript">
        var clickTag = 0;

        function checkAll(){
            var $check = $("#selectAll");
            $("input[type=checkbox]:enabled").each(function(){
                if ($(this).val() != "on"){
                    if ($check.prop("checked") == "checked" || $check.prop("checked") == true) {
                        $(this).prop("checked", true);
                    }
                    else{
                        $(this).prop("checked", false);
                    }
                }
            });
        }
        $(document).ready(function () {
            $("#inputForm").validate({
                submitHandler: function (form) {

                    //保存品牌信息
                    // loading('正在提交，请稍等...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    var productIds = [];
                    $("input[type='checkbox'][name='product']:checked").each(function(i,element){
                        productIds.push(this.value);
                    });
                    if (productIds.length == 0) {
                        layerMsg('请至少勾选一个产品');
                        return false;
                    }

                    $("#brandIds").val(productIds);
                    $btnSubmit.prop("disabled", true);
                    // form.submit();
                    var options = {
                        url: "${ctx}/provider/md/customerBrandCategory/save",
                        type: 'post',
                        dataType: 'json',
                        data:$(form).serialize(),
                        beforeSubmit: function(formData, jqForm, options){
                            loadingIndex = layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,
                                shade: 0.3
                            });
                            return true;
                        },// 提交前的回调函数
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg(data.message);
                                setTimeout(function () {
                                    cancel();
                                }, 1000);
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError("数据保存错误:" + data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                        },
                    };
                    $("#inputForm").ajaxSubmit(options);

                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });




            var products = "${customerBrandCategory.brandIds}".split(",");
            if (products.length > 0) {
               for (var j = 0; j < products.length; j++) {
                   $("input[type='checkbox'][name='product'][value=" + products[j] + "]").attr("checked", "checked");
               }

            }
            /**客户级联客户产品分类*/
            $(document).on('change', '.selectCustomer', function (e) {
                var customerId = $(this).val();
                if (customerId == "") {
                    $("#brandId").empty();
                    $("#brandId").append('<option value="" selected="selected">请选择</option>');
                    $("#brandId").change();
                    return false;
                }else {
                    $("[for='customerId']").remove();
                }
                $.ajax({
                        url: "${ctx}/provider/md/customerBrandCategory/ajax/getListByCustomer?customerId=" + customerId,
                        success: function (e) {
                            if (e && e.success == true) {
                                $("#brandId").empty();
                                var programme_sel = [];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for (var i = 0, len = e.data.customerBrands.length; i < len; i++) {
                                    var programme = e.data.customerBrands[i];
                                    programme_sel.push('<option value="' + programme.id + '">' + programme.brandName + '</option>')
                                }
                                $("#brandId").append(programme_sel.join(' '));
                                $("#brandId").val("");
                                $("#brandId").change();

                                $("#productTree").empty();
                                var brandNodes = [];
                                var products = e.data.products;
                                for (var i = 0, len = products.length; i < len; i++) {
                                    if(i == 0){
                                        brandNodes.push('<div style="width: 100%;margin-top: -9px;height: 40px;margin-bottom: 5px;"><input  id="selectAll" type="checkbox" style="zoom: 1.4;" onchange="checkAll()"><label for="selectAll" style="margin-top: 12px">全选</label></input></div>');
                                    }
                                    brandNodes.push('<div style="float: left;width: 180px;height: 40px"><input type="checkbox" style="zoom: 1.4" id="' + products[i].id +'" value="' + products[i].id +'" name="product"/><label for="' + products[i].id +'" style="padding: 3px;"> ' + products[i].name +'</label></div>');
                                }
                                $("#productTree").append(brandNodes);

                            } else if (e.success == false) {
                                $("#brandId").html('<option value="" selected>请选择</option>');
                                layerError(e.message, "错误提示");
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText, null, "请求品牌", "错误提示！");
                        }
                    }
                );
            });


            /**产品分类级联品牌信息*/
            $(document).on('change', '#brandId', function (e) {
                var brandId = $(this).val();
                var customerId = $("#customerId").val();
                if (brandId == null || brandId == "") {
                    return false;
                }else {
                    $("[for='brandId']").remove();
                }

                if (customerId == null || customerId == '') {
                    layerError("请先选择客户", "错误提示");
                    return false;
                }

                $("input[type=checkbox]:enabled").each(function(){
                    $(this).prop("checked", false);
                });
                $.ajax({
                        url: "${ctx}/provider/md/customerBrandCategory/ajax/findListByBrand?customerId=" + customerId + "&brandId=" + brandId,
                        success: function (e) {
                            if (e && e.success == true) {
                                //品牌信息
                                var products = e.data;
                                if (products.length > 0) {
                                    for (var j = 0; j < products.length; j++) {
                                        $("input[type='checkbox'][name='product'][value=" + products[j].productId + "]").attr("checked", "checked");
                                    }

                                }
                            } else if (e.success == false) {
                                layerError(e.message, "错误提示");
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText, null, "请求品牌失败", "错误提示！");
                        }
                    }
                );
            });


            if (${currentuser.isCustomer()==true && (customerBrandCategory.id ==null || customerBrandCategory.id<=0)}) {
                var customerId = $("#customerId").val();
                if (customerId == "") {
                    $("#brandId").empty();
                    $("#brandId").append('<option value="" selected="selected">请选择</option>');
                    $("#brandId").change();
                    return false;
                }
                $.ajax({
                        url: "${ctx}/provider/md/customerBrandCategory/ajax/getListByCustomer?customerId=" + customerId,
                        success: function (e) {
                            if (e && e.success == true) {
                                $("#brandId").empty();
                                var programme_sel = [];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for (var i = 0, len = e.data.customerBrands.length; i < len; i++) {
                                    var programme = e.data.customerBrands[i];
                                    programme_sel.push('<option value="' + programme.id + '">' + programme.brandName + '</option>')
                                }
                                $("#brandId").append(programme_sel.join(' '));
                                $("#brandId").val("");
                                $("#brandId").change();

                            } else if (e.success == false) {
                                $("#brandId").html('<option value="" selected>请选择</option>');
                                layerError(e.message, "错误提示");
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText, null, "请求品牌", "错误提示！");
                        }
                    }
                );
            }
        });
    </script>
</head>

<body>
<form:form id="inputForm" modelAttribute="customerBrandCategory" method="post" class="form-horizontal" cssStyle="padding: 25px;">
    <form:hidden path="id"/>
    <sys:message content="${message}"/>
    <c:if test="${canAction == true}">


        <div style="display: flex;flex-direction: column;margin-top: 20px">
            <div class="row-fluid">
                <c:choose>
                    <c:when test="${currentuser.isCustomer()==true}">
                        <form:hidden path="customerId"></form:hidden>
                    </c:when>
                    <c:otherwise>
                        <div style="margin-left: 10px;float: left;margin-right: 70px;width: 340px">
                            <div class="control-group" >
                                <label class="control-label" style="width: 80px;"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
                                <div class="controls" style="margin-left: 90px">
                                    <c:choose>
                                        <c:when test="${customerBrandCategory.customerId > 0}">
                                            <form:hidden path="customerId"></form:hidden>
                                            <form:input path="customerName" readonly="true" cssStyle="width: 236px"></form:input>
                                        </c:when>
                                        <c:otherwise>
                                            <select id="customerId" name="customerId" class="input-small required selectCustomer"
                                                    style="width:250px;">
                                                <option value=""
                                                        <c:out value="${(empty customerBrandCategory.customerId)?'selected=selected':''}"/>>
                                                    请选择
                                                </option>
                                                <c:forEach items="${fns:getMyCustomerList()}" var="customer">
                                                    <option value="${customer.id}"
                                                            <c:out value="${(customerBrandCategory.customerId eq customer.id)?'selected=selected':''}"/>>${customer.name}</option>
                                                </c:forEach>
                                            </select>

                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                    </c:otherwise>
                </c:choose>

                <div style="float: left;margin-left: 10px;width: 340px">
                    <label class="control-label" style="width: 80px;"><span class="red">*</span>品&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp牌：</label>
                    <div class="controls" style="margin-left: 90px;">
                        <c:choose>
                            <c:when test="${customerBrandCategory.brandId > 0}">
                                <form:hidden path="brandId"></form:hidden>
                                <form:input path="brandName" readonly="true" cssStyle="width: 236px"></form:input>
                            </c:when>
                            <c:otherwise>
                                <select id="brandId" name="brandId" style="width:250px;" class="required">
                                    <option value="" selected>请选择</option>
                                </select>

                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <div style="margin-top: 10px;">
                <label class="control-label" style="margin-left: 11px;width: 80px">产品信息：</label>
                <div class="controls" id="productTree" style="margin-left: 98px;height: 430px;overflow:auto;">

                    <c:if test="${products.size() > 0}">
                    <div style="width: 100%;margin-top: -9px;height: 40px;margin-bottom: 5px;">
                        <input  id="selectAll" type="checkbox" style="zoom: 1.4;" onchange="checkAll()"><label style="margin-top: 12px">全选</label></input>

                    </div>
                    <c:forEach items="${products}" var="product">
                        <div  style="float: left;width: 180px;height: 40px">

                                <input type="checkbox" style="zoom: 1.4" id="${product.id}" name="product" value="${product.id}">

                            <label for="${product.id}" style="padding: 3px;">
                                    ${product.name}
                            </label>

                        </div>
                    </c:forEach>
                    </c:if>

                </div>
                <form:hidden path="brandIds"/>
            </div>

        </div>
    </c:if>

    <div id="editBtn">
        <shiro:hasPermission name="md:customerbrandcategory:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"
                   style="margin-top: 10px;width: 85px;height: 35px;" onclick="$('#inputForm').submit()"/>&nbsp;
        </shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()"
               style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
    </div>
</form:form>
</body>
<script class="removedscript" type="text/javascript">
    function cancel() {
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        parent.layer.close(index);
    }

</script>
</html>

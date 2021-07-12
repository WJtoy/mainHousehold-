<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品价格</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <style>
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 5px;
            width: 100%;
            height: 50px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #e5e5e5;
            text-align: right;
        }
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function () {
                var $btnSubmit = $("#btnSubmit");
                $("#product").focus();
                $("#inputForm").validate({
                    submitHandler: function (form) {
                        // loading('正在提交，请稍等...');
                        // form.submit();
                        if(clickTag == 1){
                            return false;
                        }

                        clickTag = 1;
                        var loadingIndex;
                        $btnSubmit.attr('disabled', true);
                        var options = {
                            url: "${ctx}/md/productprice/saveproductpricesNew",
                            type: 'post',
                            dataType: 'json',
                            data: $(form).serialize(),
                            beforeSubmit: function (formData, jqForm, options) {
                                loadingIndex = layer.msg('正在提交，请稍等...', {
                                    icon: 16,
                                    time: 0,
                                    shade: 0.3
                                });
                                return true;
                            },// 提交前的回调函数
                            success: function (data) {
                                // 提交后的回调函数
                                if (loadingIndex) {
                                    layer.close(loadingIndex);
                                }
                                if (ajaxLogout(data)) {
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
                                    }, 2000);
                                    var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                    if (pframe) {
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
                                ajaxLogout(data, null, "数据保存错误，请重试!");
                            },
                        };
                        $("#inputForm").ajaxSubmit(options);
                    },
                    errorContainer: "#messageBox",
                    errorPlacement: function (error, element) {
                        $("#messageBox").text("输入有误，请先更正。");
                        if (element.is(":checkbox")
                            || element.is(":radio")
                            || element.parent().is(
                                ".input-append")) {
                            error.appendTo(element.parent()
                                .parent());
                        } else {
                            error.insertAfter(element);
                        }
                    }
                });
            });
    </script>
</head>

<body>
<form:form id="inputForm" modelAttribute="productPrices" action="" method="post"
           class="form-horizontal">
    <form:hidden path="id"/>
    <sys:message content="${message}"/>
    <c:set var="priceTypes" value="${fns:getDictExceptListFromMS('PriceType','40')}" /><%--切换为微服务--%>
    <c:set var="servicePriceFlag" value="0"/>
    <c:if test="${productPrices.priceType.value lt 40}">
        <c:set var="servicePriceFlag" value="1"/>
    </c:if>
    <div class="control-group" style="margin: 25px 0px 20px -90px;">
        <%--
        <label class="control-label">使用价格：</label>
        <form:select path="priceType.value" class="required" style="width:200px;" disabled="true">
            <form:option value="" label="请选择"/>
            <form:options items="${priceTypes}" itemLabel="label" itemValue="value" htmlEscape="false"/>
        </form:select>
        --%>
        <c:choose>
            <c:when test="${servicePriceFlag eq 1}">
                <label class="control-label">服务价格：</label>
                <form:select path="priceType.value" class="required" style="width:200px;">
                    <form:option value="" label="请选择"/>
                    <form:options items="${priceTypes}" itemLabel="label" itemValue="value" htmlEscape="false"/>
                </form:select>
                <label style="padding-left: 20px;">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
            </c:when>
            <c:otherwise>
                <form:hidden path="priceType.value" />
                <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
            </c:otherwise>
        </c:choose>

        <form:select path="product.id" class="required" style="width:280px;">
            <form:option value="" label="请选择"/>
            <form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
    </div>

    <div style="height: 530px;overflow: auto;">
    <table id="treeTable"
           class="table table-striped table-bordered table-condensed">
        <thead>
        <tr>
            <th>服务代码</th>
            <th>服务项目</th>
            <th width="128px">厂商标准价(元)</th>
            <th width="128px">厂商优惠价(元)</th>
            <th width="128px">网点标准价(元)</th>
            <th width="128px">网点优惠价(元)</th>
            <th>描述</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${productPrices.listProductPrice}"
                   var="productPrice" varStatus="i" begin="0">
            <tr>
                <td>${productPrice.serviceType.code}</td>
                <td>${productPrice.serviceType.name}
                    <form:hidden path="listProductPrice[${i.index}].serviceType.id" id="serviceType.id${i.index}" htmlEscape="false" maxlength="64" class="required"/>
                </td>
                <c:choose>
                    <c:when test="${productPrice.serviceType.warrantyStatus.value eq 'IW'}">
                        <td>
                            <form:input path="listProductPrice[${i.index}].customerStandardPrice" id="customerStandardPrice${i.index}" htmlEscape="false"
                                        maxlength="7" class="required number double" cssStyle="width: 70px;height: 22px;"/></td>
                        <td>
                            <form:input path="listProductPrice[${i.index}].customerDiscountPrice" id="customerDiscountPrice${i.index}" nogt="#customerStandardPrice${i.index}" htmlEscape="false"
                                        maxlength="7" class="required number double" cssStyle="width: 70px;height: 22px;"/></td>
                        <td>
                            <form:input path="listProductPrice[${i.index}].engineerStandardPrice" id="engineerStandardPrice${i.index}" htmlEscape="false"
                                        maxlength="7" class="required number double" cssStyle="width: 70px;height: 22px;"/>
                        </td>
                        <td>
                            <form:input path="listProductPrice[${i.index}].engineerDiscountPrice" id="engineerDiscountPrice${i.index}" nogt="#engineerStandardPrice${i.index}" htmlEscape="false"
                                        maxlength="7" class="required number double" cssStyle="width: 70px;height: 22px;"/>
                        </td>
                    </c:when>
                    <c:otherwise>
                        <td>
                            <form:input path="listProductPrice[${i.index}].customerStandardPrice" id="customerStandardPrice${i.index}" htmlEscape="false"
                                        maxlength="7" class="required number double" cssStyle="width: 70px;height: 22px;"/></td>
                        <td>
                            <form:input path="listProductPrice[${i.index}].customerDiscountPrice" id="customerDiscountPrice${i.index}" htmlEscape="false"
                                        maxlength="7" class="required number double" cssStyle="width: 70px;height: 22px;"/></td>
                        <td>
                            <form:input path="listProductPrice[${i.index}].engineerStandardPrice" id="engineerStandardPrice${i.index}" htmlEscape="false"
                                        maxlength="7" class="required number double" cssStyle="width: 70px;height: 22px;"/>
                        </td>
                        <td>
                            <form:input path="listProductPrice[${i.index}].engineerDiscountPrice" id="engineerDiscountPrice${i.index}" htmlEscape="false"
                                        maxlength="7" class="required number double" cssStyle="width: 70px;height: 22px;"/>
                        </td>
                    </c:otherwise>
                </c:choose>


                <td>
                    <form:input path="listProductPrice[${i.index}].remarks" id="remarks${i.index}" htmlEscape="false" maxlength="255" cssStyle="width: 443px;"/>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    </div>

    <div id="editBtn">
        <shiro:hasPermission name="md:customerPrice:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"
                   style="margin-top: 10px;width: 85px;height: 35px;"/>&nbsp;
        </shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()"
               style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
    </div>
</form:form>

<script class="removedscript" type="text/javascript">
    $(document).ready(function() {
        $("th").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"text-align":"center","vertical-align":"middle"});
    });
    // 关闭页面
    function cancel() {
        top.layer.close(this_index);// 关闭本身
    }

</script>
</body>
</html>

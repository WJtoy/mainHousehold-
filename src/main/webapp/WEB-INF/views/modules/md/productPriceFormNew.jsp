<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>服务价格</title>

    <meta name="decorator" content="default"/>
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

        .x {
            width: 50%;
            float: left;
        }

        .y {
            width: 50%;
            float: right;
        }

        .lineBot {
            margin-bottom: 10px;
        }

        input[type=text] {
            width: 250px;
        }

        #main {
            height: 80%;
            padding: 46px;
            margin-left: -90px;
            margin-top: -17px;
        }
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function () {
                var $btnSubmit = $("#btnSubmit");

                $("#value").focus();
                $("#inputForm").validate({
                    submitHandler: function (form) {
                        clickTag = 1;
                        var loadingIndex;

                        var customerStandardPrice = $("#customerStandardPrice").val();// 厂商标准价
                        var customerDiscountPrice = $("#customerDiscountPrice").val();// 厂商优惠价
                        var engineerStandardPrice = $("#engineerStandardPrice").val();// 网点标准价
                        var engineerDiscountPrice = $("#engineerDiscountPrice").val();// 网点优惠价

                        var warrantyStatus = $("#warrantyStatus").val();
                        if (warrantyStatus != 'OOT') {
                            if (parseInt(customerDiscountPrice) > parseInt(customerStandardPrice)) {
                                clickTag = 0;
                                layerError("厂商优惠价必须小于等于厂商标准价", "错误提示");
                                $("#customerDiscountPrice").focus();
                                return false;
                            }
                            if (parseInt(engineerDiscountPrice) > parseInt(engineerStandardPrice)) {
                                clickTag = 0;
                                layerError("网点优惠价必须小于等于网点标准价", "错误提示");
                                $("#engineerDiscountPrice").focus();
                                return false;
                            }
                        }
                        $btnSubmit.attr('disabled', true);
                        var options = {
                            url: "${ctx}/md/productprice/saveNew",
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
                                        // loading('同步中...');
                                    }, 1000);
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
<br/>
<form:form id="inputForm" modelAttribute="productPrice" action="" method="post"
           class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="product.id"/>
    <input id="priceType" name="priceType.value" type="hidden" value="${productPrice.priceType.value}"/>
    <sys:message content="${message}"/>
    <input type="hidden" id="warrantyStatus" value="${productPrice.serviceType.warrantyStatus.value}">

    <div id="main">
        <c:choose>
            <c:when test="${productPrice.priceType.value lt 40}">
                <div class="control-group x">
                    <label class="control-label">服务价格：</label>
                    <div class="controls lineBot">
                        <input type="text" readonly="readonly"
                               value="${fns:getDictLabelFromMS(productPrice.priceType.value, 'PriceType', '')}"/><%--切换为微服务--%>
                    </div>
                </div>

                <div class="control-group y">
                    <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
                    <div class="controls lineBot">
                            <%--<sys:treeselect id="product" name="product.id"--%>
                            <%--value="${productPrice.product.id}" labelName="product.name"--%>
                            <%--labelValue="${productPrice.product.name}" title="产品"--%>
                            <%--url="/md/product/treeData" cssClass="required" disabled="true"/>--%>
                        <input type="text" id="product" name="product.name" value="${productPrice.product.name}"
                               readonly="readonly"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">服务项目：</label>
                    <div class="controls lineBot">
                            <%--<sys:treeselect id="serviceType" name="serviceType.id"--%>
                            <%--value="${productPrice.serviceType.id}"--%>
                            <%--labelName="serviceType.name"--%>
                            <%--labelValue="${productPrice.serviceType.name}" title="服务类型"--%>
                            <%--url="/md/servicetype/treeData" cssClass="required" disabled="true"/>--%>
                        <input type="text" id="serviceType" name="serviceType.name" value="${productPrice.serviceType.name}"
                               readonly="readonly"/>
                        <input type="hidden" name="serviceType.id" value="${productPrice.serviceType.id}">
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="control-group x">
                    <label class="control-label">服务项目：</label>
                    <div class="controls lineBot">
                            <%--<sys:treeselect id="serviceType" name="serviceType.id"--%>
                            <%--value="${productPrice.serviceType.id}"--%>
                            <%--labelName="serviceType.name"--%>
                            <%--labelValue="${productPrice.serviceType.name}" title="服务类型"--%>
                            <%--url="/md/servicetype/treeData" cssClass="required" disabled="true"/>--%>
                        <input type="text" id="serviceType" name="serviceType.name" value="${productPrice.serviceType.name}"
                               readonly="readonly"/>
                        <input type="hidden" name="serviceType.id" value="${productPrice.serviceType.id}">
                    </div>
                </div>
                <div class="control-group y">
                    <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
                    <div class="controls lineBot">
                            <%--<sys:treeselect id="product" name="product.id"--%>
                            <%--value="${productPrice.product.id}" labelName="product.name"--%>
                            <%--labelValue="${productPrice.product.name}" title="产品"--%>
                            <%--url="/md/product/treeData" cssClass="required" disabled="true"/>--%>
                        <input type="text" id="product" name="product.name" value="${productPrice.product.name}"
                               readonly="readonly"/>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>


        <div class="control-group x">
            <label class="control-label">厂商标准价：</label>
            <div class="controls lineBot">
                <form:input path="customerStandardPrice" htmlEscape="false" maxlength="7"
                            class="required number  double" cssStyle="width: 226px;"/><span class="add-on">元</span>
            </div>
        </div>
        <div class="control-group y">
            <label class="control-label">厂商优惠价：</label>
            <div class="controls lineBot">
                <form:input path="customerDiscountPrice" htmlEscape="false"
                            maxlength="7" class="number  double" cssStyle="width: 226px;"/><span class="add-on">元</span>
            </div>
        </div>

        <div class="control-group x">
            <label class="control-label">网点标准价：</label>
            <div class="controls lineBot">
                <form:input path="engineerStandardPrice" htmlEscape="false" maxlength="7" class="number double"
                            cssStyle="width: 226px;"/><span class="add-on">元</span>
            </div>
        </div>
        <div class="control-group y">
            <label class="control-label">网点优惠价：</label>
            <div class="controls lineBot">
                <form:input path="engineerDiscountPrice" htmlEscape="false" maxlength="7" class="number double"
                            cssStyle="width: 226px;"/><span class="add-on">元</span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述：</label>
            <div class="controls lineBot">
                <form:textarea path="remarks" htmlEscape="false" rows="3"
                               maxlength="255" class="" cssStyle="width: 697px;height: 56px"/>
            </div>
        </div>
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
    // 关闭页面
    function cancel() {
        top.layer.close(this_index);// 关闭本身
    }

</script>
</body>
</html>

<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>客户价格管理</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <meta name="decorator" content="default"/>
    <style>
        #editBtn{
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
        #main{
            height: 80%;
            padding: 66px;
            margin-left: -110px;
            margin-top: -25px;
        }
        .lineBot{
            margin-bottom: 10px;
        }
        input[type=text]{
            width: 250px;
        }
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        var $btnSubmit = $("#btnSubmit");
        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler: function (form) {
                    clickTag = 1;
                    var loadingIndex;

                    var discountPrice = $("#discountPrice").val();
                    var price = $("#price").val();

                    var warrantyStatus = $("#warrantyStatus").val();
                    if (warrantyStatus != 'OOT') {
                        if(parseInt(discountPrice) > parseInt(price)){
                            clickTag = 0;
                            layerError("优惠价必须小于等于价格","错误提示");
                            $("#discountPrice").focus();
                            return false;
                        }
                    }

                    var options = {
                        url: "${ctx}/fi/md/customerPrice/save?qCustomerId=${customerId}&qCustomerName=${customerName}&qProductCategoryId=${productCategoryId}&qProductCategoryName=${productCategoryName}&qProductId=${productId}&qProductName=${productName}&qFirstSearch=${qFirstSearch}",
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
                                    // loading('同步中...');
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
                errorPlacement: function(error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });


            switchEvent("#spanDelFlag",function(){
                $("#delFlag").val(0)
            },function() {
                $("#delFlag").val(1)
            });
        });


    </script>
</head>
<body>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="inputForm" modelAttribute="customerPrice" action="" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="customer.id"/>
    <form:hidden path="product.id"/>
    <%--<form:hidden id="warrantyStatus" path="serviceType.warrantyStatus"/>--%>
    <sys:message content="${message}"/>
    <input type="hidden" value="${warrantyStatus}" id="warrantyStatus">
    <div id="main">

        <c:if test="${currentuser.isCustomer() == false}">
            <div class="control-group">
                <label class="control-label">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户:${params}</label>
                <div class="controls lineBot">
                    <input type="text" id="customer.name" name="customer.name" value="${customerPrice.customer.name}" readonly="readonly" />
                </div>
            </div>
        </c:if>
        <div class="control-group">
            <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品:</label>
            <div class="controls lineBot">
                <input type="text" id="product.name" name="product.name" value="${customerPrice.product.name}" readonly="readonly" />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">服务类型:</label>
            <div class="controls lineBot">
                    <%--<sys:treeselect id="serviceType" name="serviceType.id" value="${customerPrice.serviceType.id}" labelName="serviceType.name" labelValue="${customerPrice.serviceType.name}"--%>
                    <%--title="服务类型" url="/md/servicetype/treeData" cssClass="required" disabled="true"/>--%>
                <input type="text" id="serviceType" name="serviceType.name" value="${customerPrice.serviceType.name}" readonly="readonly" />
                <input type="hidden" name="serviceType.id" value="${customerPrice.serviceType.id}">
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">价&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;格:</label>
            <div class="controls lineBot">
                <form:input path="price" htmlEscape="false" maxlength="7"  class="required number" cssStyle="width: 226px;"/><span class="add-on">元</span>

            </div>
            <div class="controls lineBot" style="color:#999999">
                <c:forEach items="${productPriceList}" var="productPrice">
                    ${productPrice.priceTypeName}:${productPrice.standPrice}<%--切换为微服务--%>
                </c:forEach>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">优惠价格:</label>
            <div class="controls lineBot">
                <form:input path="discountPrice" htmlEscape="false" maxlength="7" class="required number" cssStyle="width: 226px;"/><span class="add-on">元</span>
            </div>
            <div class="controls lineBot" style="color:#999999">
                <c:forEach items="${productPriceList}" var="productPrice">
                    ${productPrice.priceTypeName}:${productPrice.discountPrice}<%--切换为微服务--%>
                </c:forEach>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">冻结金额:</label>
            <div class="controls lineBot">
                <form:input path="blockedPrice" htmlEscape="false" maxlength="7" class="required number" cssStyle="width: 226px;"/><span class="add-on">元</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述:</label>
            <div class="controls lineBot">
                <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge" cssStyle="width: 255px;height: 56px;"/>
            </div>
        </div>
        <c:if test="${customerPrice.id != null and customerPrice.id > 0}">
            <div class="control-group">
                <label class="control-label">启用价格:</label>
                <div class="controls">
                    <c:choose>
                        <c:when test="${customerPrice.id != null and customerPrice.id > 0}">
                            <c:choose>
                                <c:when test="${customerPrice.delFlag eq 1}">
                                    <span class="switch-off" id="spanDelFlag"></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="switch-on" id="spanDelFlag"></span>
                                </c:otherwise>
                            </c:choose>
                            <input type="hidden" value="${customerPrice.delFlag}" name="delFlag" id="delFlag">
                        </c:when>
                        <c:otherwise>
                            <span class="switch-on" id="spanDelFlag"></span>
                            <input type="hidden" value="0" name="delFlag" id="delFlag">
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
        </c:if>

    </div>

    <div id="editBtn">
        <c:if test="${canAction == true}">
            <shiro:hasPermission name="fi:md:customerprice:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;"/>&nbsp;
            </shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
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
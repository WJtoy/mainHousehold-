<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <!-- 网点单个产品的所有服务价格 -->
    <title>安维价格</title>
    <meta name="decorator" content="default" />
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
    </style>
    <script type="text/javascript">
        function child(obj) {
            var data = eval(obj);
            $("#servicePointId").val(data.servicePointId);
            $("#servicePointNo").val(data.servicePointNo);
            $("#serviceName").val(data.serviceName);
            $("#servicePointPrimaryName").val(data.servicePointPrimaryName);
            $("#contactInfo").val(data.contactInfo);
            $("#customizePriceFlag").val(data.customizePriceFlag);
            $("#useDefaultPrice").val(data.useDefaultPrice);
            $("#degree").val(data.degree);
            $("#serviceRemotePriceFlag").val(data.serviceRemotePriceFlag);
            $("#remotePriceFlag").val(data.remotePriceFlag);
            $("#remotePriceType").val(data.remotePriceType);
            $("#productId").val(data.productId);
            $("#productCategoryId").val(data.productCategoryId);
        }

        var this_index = top.layer.index;
        var clickTag = 0;
        var $btnSubmit = $("#btnSubmit");
        $(document).ready(function() {
            $("#inputForm :input").change(function(){
                $("#inputForm").data("changed",true);
            });

            $("#inputForm").validate({
                submitHandler: function (form) {
                    clickTag = 1;
                    var loadingIndex;

                    var newPriceLength = $("[name='newPrice']");
                    if (newPriceLength == undefined){
                        //console.log("没有数据");
                    } else {
                        //console.log(newPriceLength.length);
                        if (newPriceLength.length >0) {
                            $("#inputForm").data("changed", true);
                        }
                    }
                    if ($("#inputForm").data("changed")) {
                    } else {
                        layerError("当前无任何修改，请重新输入要修改的价格","错误提示");
                        return false;
                    }
                    var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
                    var options = {
                        url: "${ctx}/fi/md/servicePointPrice/saveProductPrices?qServicePointId=${servicePointId}&qServicePointName=${fns:urlEncode(servicePointName)}&qProductCategoryId=${productCategoryId}&qProductCategoryName=${fns:urlEncode(productCategoryName)}&qProductId=${productId}&qProductName=${fns:urlEncode(productName)}&serviceRemotePriceFlag=" + serviceRemotePriceFlag,
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
                                }, 2000);


                                setTimeout(function () {
                                    var servicePointId = $("#servicePointId").val();
                                    var servicePointNo = $("#servicePointNo").val();
                                    var serviceName = $("#serviceName").val();
                                    var servicePointPrimaryName = $("#servicePointPrimaryName").val();
                                    var contactInfo = $("#contactInfo").val();
                                    var customizePriceFlag = 1;
                                    var useDefaultPrice = $("#useDefaultPrice").val();
                                    var degree = $("#degree").val();
                                    var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
                                    var remotePriceFlag = 1;
                                    var remotePriceType = $("#remotePriceType").val();
                                    var selProductId = $("#productId").val();
                                    var selProductCategoryId = $("#productCategoryId").val();
                                    var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                    if (pframe) {
                                        pframe.reloadPrice(servicePointId, servicePointNo, serviceName, servicePointPrimaryName, contactInfo, customizePriceFlag,useDefaultPrice,degree,serviceRemotePriceFlag,remotePriceFlag,remotePriceType, selProductId,selProductCategoryId);
                                    }
                                },1000);
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
                    // form.submit();
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
            /*
             *自定义校验规则
             * phone  要验证规则名称
             * func  校验处理
             * value 获取输入的值
             * Element  当前的文本框
             * params 校验参数
             * */
            $.validator.addMethod('pricemin',function (value,element,params) {
                /*价格范围验证*/
                var num = parseFloat(value);
                if(num < parseFloat(params)){
                    return false;
                }
                return true;
            },'价格最小值不能小于-1000');
            jQuery.validator.addMethod("validDiscountPrice", function(b, a, c) {
                var bval = parseFloat(b);
                var cval = parseFloat($(c).val());
                if(bval<=0 && cval<0){
                    return Math.abs(bval) <= Math.abs(cval);
                }else {
                    return bval <= cval;
                }
                //return Math.abs(bval) <= Math.abs(cval);
                //return parseFloat(b) < parseFloat($(c).val())
            }, "优惠价不能高于标准价");

        });
        function delRow(idx){
            $("#tr_"+idx).hide();
            $("[id='prices[" + idx + "].delFlag']").val(1);
        }
    </script>
</head>
<body>
<input type="hidden" id="servicePointId">
<input type="hidden" id="servicePointNo">
<input type="hidden" id="serviceName">
<input type="hidden" id="servicePointPrimaryName">
<input type="hidden" id="contactInfo">
<input type="hidden" id="customizePriceFlag">
<input type="hidden" id="useDefaultPrice">
<input type="hidden" id="degree">
<input type="hidden" id="serviceRemotePriceFlag">
<input type="hidden" id="remotePriceFlag">
<input type="hidden" id="remotePriceType">
<input type="hidden" id="productId"/>
<input type="hidden" id="productCategoryId" />

<form:form id="inputForm" modelAttribute="servicePrices" action="" method="post" class="form-horizontal">
    <sys:message content="${message}" type="loading" />
    <c:if test="${canAction == true}">
        <div class="row-fluid">
            <div class="span4" style="display: none">
                <div class="control-group">
                    <label class="control-label">网点:</label>
                    <div class="controls">
                        <form:hidden path="servicePoint.id" />
                        <form:hidden path="servicePoint.name"/>
                        <input class="input-medium valid" style="width:250px;" readonly="readonly" type="text" value="${servicePrices.servicePoint.name}(${servicePrices.servicePoint.servicePointNo})" aria-invalid="false">
                    </div>
                </div>
            </div>
            <div class="span4" style="margin: 20px 0 10px 0;width: 25%;">
                <div class="control-group">
                    <label style="color: #999999;">产品：</label>
                    <form:hidden path="product.id" />
                    <input name="product.name" htmlEscape="false" type="text" disabled="disabled" value="${servicePrices.product.name}"/>
                </div>
            </div>
            <div class="span4" style="margin: 20px 0px 0 0;width: 27%">
                <div class="control-group">
                    <label style="color: #999999;">价格属性：</label>
                    <c:choose>
                        <c:when test="${servicePrices.servicePoint.customizePriceFlag eq 1}">
                            <input name="product.name" htmlEscape="false" type="text" disabled="disabled" value="自定义"></input>
                        </c:when>
                        <c:otherwise>
                            <input name="product.name" htmlEscape="false" type="text" disabled="disabled" value="标准价"></input>
                        </c:otherwise>
                    </c:choose>
                </div>

            </div>
            <c:if test="${serviceRemotePriceFlag == 0}">
                <div class="span4" style="margin: 20px 0px 0 0;">
                    <div class="control-group">
                        <label style="color: #999999;">价格轮次：</label>
                        <c:set var="priceInfo" value="${fns:getDictLabelFromMS(servicePrices.servicePoint.useDefaultPrice,'PriceType','')}" />
                        <input name="product.name" htmlEscape="false" type="text" disabled="disabled" value="${priceInfo}"></input>
                    </div>
                </div>
            </c:if>
        </div>

        <div id="divGrid" style="overflow: auto;height:560px;">
            <table id="contentTable" class="table table-striped table-bordered table-condensed"  width="100%">
                <c:choose>
                    <c:when test="${serviceRemotePriceFlag == 0}">
                        <c:set var="priceTurn" value="${fns:getDictInclueListFromMS('PriceType','10,20,30')}"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="priceTurn" value="${fns:getDictInclueListFromMS('PriceType','40')}"/>
                    </c:otherwise>
                </c:choose>
                <thead>
                <tr>
                    <th rowspan="2" style="text-align: center;vertical-align: middle;width: 128px;">服务项目</th>
                    <c:forEach items="${priceTurn}" var="dict">
                        <th colspan="2" style="text-align: center;vertical-align: middle;width: 144px;height: 40px;">${dict.label}</th>
                    </c:forEach>
                    <th colspan="2" style="text-align: center;vertical-align: middle;width: 144px;height: 40px;">自定义价格</th>
                    <th rowspan="2" style="text-align: center;vertical-align: middle;">描述</th>
                </tr>
                <tr>
                    <c:forEach items="${priceTurn}" var="dict">
                        <th width="70" style="text-align: center;vertical-align: middle;">价格</th>
                        <th width="70" style="text-align: center;vertical-align: middle;">优惠价</th>
                    </c:forEach>
                    <th width="70" style="text-align: center;vertical-align: middle;">价格</th>
                    <th width="70" style="text-align: center;vertical-align: middle;">优惠价</th>
                </tr>
                </thead>
                <tbody>

                    <%--服务项目--%>
                <c:forEach items="${servicePrices.prices}" var="price" varStatus="i" begin="0">
                    <c:set var="index" value="${i.index}" />
                    <tr id="tr_${index}" style="height: 56px;">
                            <%--服务项目列--%>
                        <td style="display: table-cell;vertical-align: middle;">${price.serviceType.name}&nbsp;
                            <c:if test="${empty price.id}"><span class="label label-info">新增</span><input type="hidden" name="newPrice"/></c:if>
                            <c:if test="${not empty price.id}"><span class="label label-warning">修改</span></c:if>
                                <%--<a href="javascript:;" onclick="delRow(${i.index});return false;" title="删除"><i class="icon-delete" style="margin-top: 0px;"></i></a>--%>
                            <input type="hidden" id="prices[${index}].id" name="prices[${index}].id" value="${price.id}" />
                            <input type="hidden" id="prices[${index}].serviceType.id" name="prices[${index}].serviceType.id"
                                   value="${price.serviceType.id}" />
                            <input type="hidden" id="prices[${index}].delFlag" name="prices[${index}].delFlag" value="${price.delFlag}" />
                            <input type="hidden" id="prices[${index}].priceType" name="prices[${index}].priceType" value="${price.priceType}" />
                        </td>

                        <c:set var="priceDifferent" value="false" />
                        <c:set var="discountPriceDifferent" value="false" />
                        <c:set var="remarks" value="" />
                        <c:set var="bExclude" value="false"/>
                        <c:set var="existsPriceType" value="false" />
                        <c:set var="lstCount" value="0" />

                        <c:forEach items="${priceTurn}" var="dict" varStatus="d">
                            <c:set var="dIndex" value="${d.index}" />
                            <c:set var="dValue" value="${dict.value}" />
                            <td style="text-align: center;vertical-align: middle;">
                                <c:set var="priceHint" value="" />
                                <c:set var="discountpriceHint" value="" />

                                <c:forEach items="${price.productPriceList}" var="productPrice" varStatus="p">
                                    <c:set var="pIndex" value="${p.index}" />
                                    <c:set var="pValue" value="${productPrice.priceType}" />
                                    <c:if test="${dValue == pValue}">
                                        <c:if test="${productPrice.priceType eq servicePrices.servicePoint.useDefaultPrice}">
                                            <c:set var="existsPriceType" value="true" />
                                            <c:if test="${productPrice.standPrice ne price.price}">
                                                <c:set var="priceDifferent" value="true" />
                                            </c:if>
                                            <c:if test="${productPrice.discountPrice ne price.discountPrice}">
                                                <c:set var="discountPriceDifferent" value="true" />
                                            </c:if>
                                        </c:if>
                                        <%--价格：0.0--%>
                                        <c:set var="priceHint" value="${priceHint}${productPrice.standPrice} " />
                                        <%--优惠价格：0.0--%>
                                        <c:set var="discountpriceHint" value="${discountpriceHint}${productPrice.discountPrice} " />
                                        <c:set var="bExclude" value="true"></c:set>
                                    </c:if>
                                    <c:if test="${productPrice.priceType eq servicePrices.servicePoint.useDefaultPrice}">
                                        <c:set var="bExclude" value="true"></c:set>
                                    </c:if>
                                    <c:set var="lstCount" value="${lstCount+1}" />
                                </c:forEach>

                                <c:if test="${bExclude eq false && existsPriceType eq false && lstCount gt 0}">
                                    <c:set var="remarks" value="${price.serviceType.name}参考价格没有维护" />
                                </c:if>
                                    <%--自定义价格--%>
                                    ${priceHint}
                            </td>
                            <%--展示优惠价--%>
                            <td style="text-align: center;vertical-align: middle;">
                                    ${discountpriceHint}
                            </td>
                        </c:forEach>
                            <%--使用标准价的返现网点--%>
                                <c:if test="${serviceRemotePriceFlag == 0}">
                                    <c:if test="${servicePrices.servicePoint.degree eq 30 && servicePrices.servicePoint.customizePriceFlag eq 0}">
                                        <c:forEach items="${price.productPriceList}" var="productPrice" varStatus="p">
                                            <c:if test="${servicePrices.servicePoint.useDefaultPrice == productPrice.priceType}">
                                                <c:set var="yh_price" value="${productPrice.standPrice}"></c:set>
                                                <c:set var="yh_discountPrice" value="${productPrice.discountPrice}"></c:set>
                                            </c:if>
                                        </c:forEach>
                                    </c:if>

                                </c:if>
                                <c:if test="${serviceRemotePriceFlag == 1}">
                                    <c:if test="${servicePrices.servicePoint.degree eq 30 && servicePrices.servicePoint.remotePriceFlag eq 0}">
                                        <c:forEach items="${price.productPriceList}" var="productPrice" varStatus="p">
                                            <c:if test="${servicePrices.servicePoint.remotePriceType == productPrice.priceType}">
                                                <c:set var="yh_price" value="${productPrice.standPrice}"></c:set>
                                                <c:set var="yh_discountPrice" value="${productPrice.discountPrice}"></c:set>
                                            </c:if>
                                        </c:forEach>
                                    </c:if>
                                </c:if>
                        <c:set var="c_price" value="${price.price}"></c:set>
                        <c:set var="c_discountPrice" value="${price.discountPrice}"></c:set>

                            <%--自定义价格--%>
                        <td style="text-align: center;vertical-align: middle;">
                            <input type="text" id="prices[${index}].price" name="prices[${index}].price" style="<c:out value='${not empty yh_price ? "" : priceDifferent eq true?"color:red;":""}'/>"
                                   maxlength="7" class="input-mini required number" pricemin="-1000" value="<c:out value="${not empty yh_price ? yh_price : c_price}"/>"/>
                        </td>
                        <td style="text-align: center;vertical-align: middle;">
                            <input type="text" id="prices[${index}].discountPrice" name="prices[${index}].discountPrice" style="<c:out value='${not empty yh_discountPrice ? "" : discountPriceDifferent eq true?"color:red;":""}'/>"
                                   maxlength="7" class="input-mini required number" validDiscountPrice="[id='prices[${index}].price']"
                                   value="<c:out value="${not empty yh_discountPrice ? yh_discountPrice : c_discountPrice}"/>"/>
                        </td>
                        <td style="display: table-cell;vertical-align: middle;">
                            <c:set var="textColor" value="" />
                            <c:if test="${not empty remarks}">
                                <c:set var="textColor" value="color:#FF0000;" />
                            </c:if>
                            <input type="text" id="prices[${index}].remarks" name="prices[${index}].remarks" maxlength="255" style="${textColor}width: 415px;" value="${remarks}"/>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <div id="editBtn">
        <c:if test="${canAction == true}">
            <shiro:hasPermission name="fi:md:servicepointprice:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;"/>&nbsp;
            </shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="cancel()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
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

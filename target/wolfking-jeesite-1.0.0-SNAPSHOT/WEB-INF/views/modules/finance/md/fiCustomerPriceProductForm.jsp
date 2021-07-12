<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <script src="${ctxStatic}/layui/layui.js"></script>
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <title>服务价格</title>
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
        .table td{
            height: 50px!important;
        }
        .use_price{
            margin-top: -4px;
            border-radius: 4px;
            border: 1px solid;
            border-color: #0096DA;
            background-color: #0096DA;
            width: 120px;
            height: 30px;
            color: #FFFFFF;
        }
        .price{
            text-align: center!important;
            width: 50px!important;

        }
        .error {
            background-color: #FEEEEE!important;
            text-align: center!important;
            vertical-align: middle!important;
            color: #F54142!important;
        }
        img{
            width: 21px;
            height: 21px;
        }

        #reminder{
            color: #F54142;
            animation: fade-in;
            animation-duration: 2s;
            -webkit-animation:fade-in 2s;/*针对webkit内核*/
        }
        @keyframes fade-in {
            0% {opacity: 0.1;}
            25% {opacity: 0.3;}
            50% {opacity: 0.6;}
            100% {opacity: 1;}
        }
        @-webkit-keyframes fade-in {/*针对webkit内核*/
            0% {opacity: 0.1;}
            25% {opacity: 0.3;}
            50% {opacity: 0.6;}
            100% {opacity: 1;}
        }
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        var $btnSubmit = $("#btnSubmit");
        $(document).ready(function() {
            var form;
            layui.use('form', function () {
                var form = layui.form,
                    $ = layui.$;

                form.render();

                form.on('checkbox(all)', function (data) {
                    if (data.elem.checked) {
                        $(":checkbox[name='serviceType_line']").attr("checked", "checked");
                        form.render();
                    } else {
                        $(":checkbox[name='serviceType_line']").removeAttr("checked");
                        form.render();
                    }
                });
            });

            $('.editImg').on({
                mouseenter:function(){
                    var that = this;
                    tips =layer.tips("<span style='color:#fff;'>修改</span>",that,{tips:[2,'#3E3E3E'],time:0,area: 'auto',maxWidth:500});
                },
                mouseleave:function(){
                    layer.close(tips);
                }
            });

            $("#inputForm :input").change(function() {
                $("#inputForm").data("changed",true);
            });


            $("#inputForm").validate({
                submitHandler: function (form) {
                    clickTag = 1;
                    var loadingIndex;

                    if ($("#inputForm").data("changed")) {
                        // submit the form
                        var options = {
                            url: "${ctx}/fi/md/customerPrice/saveProductPrices?qCustomerId=${customerId}&qCustomerName=${fns:urlEncode(customerName)}&qProductCategoryId=${productCategoryId}&qProductCategoryName=${fns:urlEncode(productCategoryName)}&qProductId=${productId}&qProductName=${fns:urlEncode(productName)}&qFirstSearch=${qFirstSearch}",
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
                                        top.layer.close(this_index);// 关闭本身
                                        loading('同步中...');
                                        var pframe = getActiveTabIframe();// 定义在jeesite.min.js中
                                        if(pframe){
                                            pframe.repage();
                                        }
                                    }, 2000);
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
                    } else {
                        // 表单未进行任何修改
                        layerError("当前无任何修改，请重新输入要修改的价格","错误提示");
                        return false;
                    }
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


            $(".priceBind").bind("input propertychange",function(event){
                var warrantyStatus = $(this).parent().parent().children(".warrantyStatus").val();
                var price = $(this).val();
                if (price <= 0 && warrantyStatus == 'IW') {
                    $("#reminder").show();
                } else {
                    $("#reminder").hide();
                }
            });
        });

        var newIndex = -1;
        function editImg(index, isNewPrice){
            newIndex++;
            $("#tr_"+index+" .editImg").hide();
            $("#tr_"+index+" .confirm").show();
            $("#tr_"+index+" .price").prop("readonly", false);

            if (isNewPrice) {
                var p_referPrice = $("#tr_"+index+" .p_referPrice").val();
                var p_referDiscountPrice = $("#tr_"+index+" .p_referDiscountPrice").val();
                $("#tr_"+index+" .p").attr("value", p_referPrice);

                $("#tr_"+index+" .d").attr("value", p_referDiscountPrice);

                $("#tr_"+index+" .b").attr("value", "0.0");
            }
            $("#tr_"+index+" .p").attr("name", "newPrices["+newIndex+"].price");
            $("#tr_"+index+" .d").attr("name", "newPrices["+newIndex+"].discountPrice");
            $("#tr_"+index+" .b").attr("name", "newPrices["+newIndex+"].blockedPrice");

            $("#tr_"+index+" #priceId").attr("name", "newPrices["+newIndex+"].id");
            $("#tr_"+index+" #serviceTypeId").attr("name", "newPrices["+newIndex+"].serviceType.id");
            $("#tr_"+index+" #serviceTypeName").attr("name", "newPrices["+newIndex+"].serviceType.name");
            $("#tr_"+index+" #serviceTypeWarrantyStatus").attr("name", "newPrices["+newIndex+"].serviceType.warrantyStatus.value");
            $("#tr_"+index+" #priceDelFlag").attr("name", "newPrices["+newIndex+"].delFlag");

            $("#inputForm #tr_"+index+" :input").change();

        }

        function disableOrEnable(obj, line){
            var newPriceName = $("#tr_"+line+" .p").attr("name");
            if (newPriceName.indexOf("newPrices") != -1) {
                returnSwitch(obj);
                layerError("编辑状态下不可停用价格","错误提示");
            } else {
                var serviceTypeName = $("#tr_"+line+" #serviceTypeName").val();
                var priceId = $("#tr_"+line+" #priceId").val();
                var flag = $("#tr_"+line+" .delFlag").val();
                var content;
                var url;
                var msg;
                var title;
                // 停用
                if (flag == 1) {
                    title = "启用";
                    content = '确认要启用【'+serviceTypeName+'】价格吗？';
                    url = "${ctx}/fi/md/customerPrice/active?id="+ priceId;
                    msg = '正在启用价格，请稍等...';
                } else {
                    title = "停用";
                    content = '确认要停用【'+serviceTypeName+'】价格吗？';
                    url = "${ctx}/fi/md/customerPrice/delete?id="+ priceId;
                    msg = '正在停用价格，请稍等...';

                }
                layer.confirm(
                    content,
                    {
                        btn: ['确定','取消'], //按钮
                        title:'提示',
                        cancel: function(index, layero){
                            // 右上角叉
                            returnSwitch(obj);
                        }
                    }, function(index){
                        layer.close(index);//关闭本身
                        var loadingIndex = top.layer.msg(msg, {
                            icon: 16,
                            time: 0,//不定时关闭
                            shade: 0.3
                        });
                        $.ajax({
                            url: url,
                            success:function (data) {
                                // 提交后的回调函数
                                if(loadingIndex) {
                                    setTimeout(function () {
                                        layer.close(loadingIndex);
                                    }, 2000);
                                }
                                if (data.success) {
                                    layerMsg(data.message);

                                    if (flag == 1) {
                                        // 停用切换为启用
                                        $("#tr_"+line+" .price").prop("readonly", true);
                                        $("#tr_"+line+" .editImg").show();
                                        $("#tr_"+line+" .price").css("color", "");
                                        $("#tr_"+line+" .label").attr("class","label label-warning");
                                        $("#tr_"+line+" .label").text("待审核");
                                        $("#tr_"+line+" .delFlag").attr("value", 2);
                                        $("#tr_"+line+" .switch-on").hide();


                                        $("#tr_"+line).find("input[type=checkbox]").prop("disabled", false);
                                        $("#tr_"+line).find("input[type=checkbox]").attr("name", "serviceType_line");

                                        $("#tr_"+line+" .p").attr("name", "prices["+newIndex+"].price");
                                        $("#tr_"+line+" .d").attr("name", "prices["+newIndex+"].discountPrice");
                                        $("#tr_"+line+" .b").attr("name", "prices["+newIndex+"].blockedPrice");

                                        viewRendering();
                                    } else {
                                        // 启用切换为停用
                                        $("#tr_"+line+" .price").prop("readonly", true);
                                        $("#tr_"+line+" .price").css("color", "#C5C8CE");
                                        $("#tr_"+line+" .editImg").hide();
                                        $("#tr_"+line+" .confirm").hide();
                                        $("#tr_"+line+" .label").attr("class","label");
                                        $("#tr_"+line+" .label").text("停用");
                                        $("#tr_"+line+" .delFlag").attr("value", 1);

                                        $("#tr_"+line).find("input[type=checkbox]").prop("disabled", true);
                                        $("#tr_"+line).find("input[type=checkbox]").attr("name", "serviceType_line_stop");
                                        $("#tr_"+line).find("input[type=checkbox]").removeAttr("checked");

                                        $("#tr_"+line+" .p").attr("name", "stop"+line+".price");
                                        $("#tr_"+line+" .d").attr("name", "stop"+line+".discountPrice");
                                        $("#tr_"+line+" .b").attr("name", "stop"+line+".blockedPrice");
                                        viewRendering();
                                    }
                                } else {
                                    layerError("服务价格"+title+"失败:" + data.message, "错误提示");
                                    // 取消操作
                                    returnSwitch(obj);
                                }
                                return false;
                            },
                            error: function (data) {
                                ajaxLogout(data,null,"数据保存错误，请重试!");
                                // 取消操作
                                returnSwitch(obj);
                            },
                        });
                        return false;
                    }, function(){
                        // 取消操作
                        returnSwitch(obj);
                    });
            }
        }

        function useStandardPrice(){
            var serviceType_checked = $("input[type='checkbox'][name='serviceType_line']:checked").length;
            if (serviceType_checked == 0) {
                layerError("请先选中您要修改的服务项目", "错误提示");
                return false;
            }
            var v = $("#v").val();
            var confirmText;
            if (v != '') {
                confirmText = '确认所选服务项目使用标准价【'+ v +'】吗？';
            } else {
                confirmText = '确认所选服务项目使用产品标准价吗？';
            }

            var customerId = $("#customerId").val();
            var productId = $("#productId").val();
            var serviceTypeIdArr = [];
            $("input[type='checkbox'][name='serviceType_line']:checked").each(function (index, element) {
                serviceTypeIdArr.push(parseInt($(this).val()));
            });

            var model = {};
            model['customerId'] = customerId;
            model['productId'] = productId;
            model['serviceTypeId'] = serviceTypeIdArr;
            layer.confirm(
                confirmText,
                {
                    btn: ['确定','取消'], //按钮
                    title:'使用标准价',
                    area: ['395px', '180px']
                }, function(index){
                    layer.close(index);//关闭本身
                    var loadingIndex = top.layer.msg("正在恢复... 请稍等", {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });

                    $.ajax({
                        url: "${ctx}/fi/md/customerPrice/updateCustomizePriceFlag",
                        data : model,
                        type : "POST",
                        dataType: "JSON",
                        traditional:true,
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                setTimeout(function () {
                                    layer.close(loadingIndex);
                                }, 2000);
                            }
                            if (data.success) {
                                layerMsg(data.message);
                                setTimeout(function () {
                                    cancel();
                                    loading('同步中...');
                                }, 2000);
                            } else {
                                layerError("保存失败:" + data.message, "错误提示");
                            }
                        },
                        error: function (data) {
                            layer.close(loadingIndex);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                        },
                    });
                    return false;
                }, function(){
                    // 取消操作
                });
        }

        // 渲染
        function viewRendering(){
            layui.use('form', function () {
                var form = layui.form,
                    $ = layui.$;

                form.render();
            });
        }

        function returnSwitch(obj){
            if ($(obj).attr("class") == 'switch-off') {
                honeySwitch.showOn(obj);
            } else {
                honeySwitch.showOff(obj);
            }
        }
    </script>
</head>
<body>

<input type="hidden" value="${useDefaultPrice}" id="priceType">
<input type="hidden" value="${fns:getDictLabelFromMS(useDefaultPrice,'PriceType','')}" id="v">
<c:set value="${customerPrices.customer.customizePriceFlag eq 0}" var="isStandardPrice"/>
<form:form id="inputForm" modelAttribute="customerPrices" action="" method="post" class="form-horizontal">
    <sys:message content="${message}" />
    <c:if test="${canAction == true}">
        <div class="row-fluid" style="margin: 20px 0px 10px -91px;">
            <div class="span4">
                <div class="control-group">
                    <label class="control-label">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
                    <div class="controls" style="margin-left: 160px;">
                        <form:hidden path="customer.id" id="customerId"/>
                        <form:input path="customer.name" id="customerName" readonly="true" cssClass="input-medium" cssStyle="width:250px;"/>
                    </div>
                </div>
            </div>
            <div class="span4">
                <div class="control-group" style="margin-left: -15px;">
                    <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
                    <div class="controls" style="margin-left: 160px;">
                        <form:hidden path="product.id" id="productId"/>
                        <form:input path="product.name" id="productName" readonly="true" cssClass="input-medium"  cssStyle="width:250px;"/>
                    </div>
                </div>
            </div>

            <div class="span4">
                <div class="control-group">
                    <label class="control-label">价格轮次：</label>
                    <div class="controls">
                        <form:hidden path="customer.useDefaultPrice" />
                        <input class="input-medium valid" style="width:250px;" readonly="readonly" type="text" value="${fns:getDictLabelFromMS(useDefaultPrice,'PriceType','')}" aria-invalid="false">
                    </div>
                </div>
            </div>
        </div>

        <div class="row-fluid" style="margin-left: -55px;">
            <div class="span6" style="width: 33%;">
                <div class="control-group" style="margin-left: -35px;">
                    <label class="control-label">价格属性：</label>
                    <form:hidden path="customer.customizePriceFlag" />
                    <input class="input-medium valid" style="width:250px;" readonly="readonly" type="text" value="${customerPrices.customer.customizePriceFlag == 0 ? '标准价':'自定义'}" aria-invalid="false">
                </div>
            </div>

                <%--有新增价格且是标准价--%>
            <c:if test="${isPresent eq true && isStandardPrice}">
                <div class="span3" style="margin-top: 4px;">
                    <div>
                        <button class="use_price" id="use_price" type="button" onclick="useStandardPrice()">
                            <img src="${ctxStatic}/images/price01.png">&nbsp;&nbsp;使用标准价
                        </button>
                    </div>
                </div>
            </c:if>
        </div>



        <div id="reminder" style="display: none;"><i class="icon-exclamation-sign"></i>&nbsp;&nbsp;提示：保内服务价格或优惠价不能小于等于0</div>
        <div id="divGrid" class="layui-form" style="overflow: auto;height:551px;margin-top: 15px;">
            <table id="contentTable" class="table table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
                <thead>
                <tr>
                        <%--如果存在新增价格且是使用标准价--%>
                    <c:if test="${isPresent && isStandardPrice}">
                        <th rowspan="2" style="width: 45px">
                            <input type="checkbox" name="all" lay-filter="all" lay-skin='primary' title="">
                        </th>
                    </c:if>
                    <th rowspan="2" style="width: 115px;">服务项目</th>
                    <th rowspan="2" style="width: 50px;">状态</th>
                    <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict">
                        <c:if test="${dict.value eq customerPrices.customer.useDefaultPrice}">
                            <th colspan="2" style="width: 135px;height: 40px;">${dict.label}</th>
                        </c:if>
                    </c:forEach>

                    <th colspan="<c:out value='${isStandardPrice ? 3 : 4}'/>" style="width: 266px;height: 40px;">服务价格</th>
                    <c:if test="${!isStandardPrice}">
                        <th rowspan="4" style="width: 100px;">启用</th>
                    </c:if>
                    <th rowspan="2" style="width: 70px;">修改人</th>
                    <th rowspan="2" style="width: 70px;">修改时间</th>
                </tr>

                <tr>
                    <c:if test="${customerPrices.customer.useDefaultPrice ne 0}">
                        <th width="70">价格</th>
                        <th width="70">优惠价</th>
                    </c:if>
                    <th width="70">价格</th>
                    <th width="70">优惠价</th>
                    <th width="70">冻结金额</th>
                    <c:if test="${!isStandardPrice}">
                        <th width="70">操作</th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${customerPrices.prices}" var="price" varStatus="i" begin="0">
                    <c:set var="index" value="${i.index}" />
                    <c:set var="flag" value="${price.delFlag}"/>
                    <%--是否新增标识--%>
                    <c:set var="isNewPrice" value="${empty price.id}"/>

                    <%--<c:set var="notMaintained" value="false"/>--%>
                    <tr id="tr_${index}">
                        <input type="hidden" value="${price.id}" id="priceId">
                        <input type="hidden" value="${price.serviceType.id}" id="serviceTypeId">
                        <input type="hidden" value="${price.serviceType.name}" id="serviceTypeName">
                        <input type="hidden" value="${price.serviceType.warrantyStatus.value}" id="serviceTypeWarrantyStatus"/>
                        <input type="hidden" value="${price.delFlag}" id="priceDelFlag"/>

                            <%--多选框--%>
                        <c:if test="${isPresent && isStandardPrice}">
                            <td>
                                <c:choose>
                                    <%--新增的需要手动使用标准价，其余的为不可勾选状态--%>
                                    <c:when test="${!isNewPrice}">
                                        <input type="checkbox" name="serviceType_line_stop" lay-filter="serviceType_line_stop" value="${price.serviceType.id}" disabled='disabled' lay-skin='primary' title="">
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${price.productPriceList.size() != 0}">
                                            <input type="checkbox" name="serviceType_line" lay-filter="serviceType_line" value="${price.serviceType.id}" lay-skin='primary' title="">
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </c:if>

                            <%--服务名称--%>
                        <c:choose>
                            <%--停用项--%>
                            <c:when test="${price.delFlag==1}">
                                <td>${price.serviceType.name}&nbsp;</td>
                                <td><span class="label">停用</span></td>
                            </c:when>
                            <c:otherwise>
                                <td>${price.serviceType.name}&nbsp;
                                    <input type="hidden" id="prices[${index}].id" name="prices[${index}].id" value="${price.id}" />
                                    <input type="hidden" id="prices[${index}].serviceType.id" name="prices[${index}].serviceType.id"
                                           value="${price.serviceType.id}" />
                                    <input type="hidden" id="prices[${index}].serviceType.name" name="prices[${index}].serviceType.name"
                                           value="${price.serviceType.name}" />
                                    <input type="hidden" id="prices[${index}].serviceType.warrantyStatus.value" name="prices[${index}].serviceType.warrantyStatus.value"
                                           value="${price.serviceType.warrantyStatus.value}" />
                                    <input type="hidden" id="prices[${index}].delFlag" name="prices[${index}].delFlag" value="${price.delFlag}" />
                                </td>
                                <%--状态--%>
                                <td>
                                    <c:if test="${not empty price.id && price.delFlag==0}"><span class="label label-success">已审核</span></c:if>
                                    <c:if test="${not empty price.id && price.delFlag==2}"><span class="label label-warning">待审核</span></c:if>
                                    <c:choose>
                                        <%--价格既是新增且是标准价还得没维护 -.- --%>
                                        <c:when test="${empty price.id && isStandardPrice && price.productPriceList.size() == 0}">
                                            <span>-</span>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${empty price.id}"><span class="label label-info">新增</span></c:if>
                                        </c:otherwise>
                                    </c:choose>

                                </td>
                            </c:otherwise>
                        </c:choose>

                        <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict" varStatus="d">
                            <c:if test="${dict.value eq customerPrices.customer.useDefaultPrice}">
                                <c:set var="dValue" value="${dict.value}" />
                                <%--轮次参考价格--%>
                                <c:set var="priceHint" value="" />
                                <c:set var="discountpriceHint" value="" />

                                <c:forEach items="${price.productPriceList}" var="productPrice">
                                    <c:set var="pValue" value="${productPrice.priceType}" />
                                    <c:if test="${dValue == pValue}">
                                        <%--价格：0.0--%>
                                        <c:set var="priceHint" value="${priceHint}${productPrice.standPrice} " />
                                        <%--优惠价格：0.0--%>
                                        <c:set var="discountpriceHint" value="${discountpriceHint}${productPrice.discountPrice} " />
                                    </c:if>
                                </c:forEach>
                                <c:choose>
                                    <c:when test="${priceHint == '' && discountpriceHint == ''}">
                                        <td class="error" colspan="2">参考价格未维护</td>
                                    </c:when>
                                    <c:otherwise>
                                        <td style="text-align: center;vertical-align: middle;">
                                                <%--价格--%>
                                                ${priceHint == '' ? '-' : priceHint}
                                        </td>
                                        <%--展示优惠价--%>
                                        <td style="text-align: center;vertical-align: middle;">
                                                ${discountpriceHint == '' ? '-' : discountpriceHint}
                                        </td>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:forEach>

                        <c:choose>
                            <%--停用--%>
                            <c:when test="${price.delFlag==1}">
                                <td>
                                    <input type="text" id="stop${index}.price" name="stop${index}.price"}
                                           class="input-mini number price p" value="${price.price}" readonly="true" style="text-align: center;width: 50px;color: #C5C8CE" />
                                </td>
                                <td >
                                    <input type="text" id="stop${index}.discountPrice" name="stop${index}.discountPrice"
                                           maxlength="7" class="input-mini number price d" value="${price.discountPrice}" readonly="true" style="text-align: center;width: 50px;color: #C5C8CE" />
                                </td>
                                <td >
                                    <input type="text" id="stop${index}.blockedPrice" name="stop${index}.blockedPrice"
                                           class="input-mini number price b" value="${price.blockedPrice}" readonly="true" style="text-align: center;width: 50px;color: #C5C8CE" />
                                </td>

                                <c:if test="${!isStandardPrice}">
                                    <td>
                                        <img src="${ctxStatic}/images/price05.png" style="display: none" onclick="editImg('${index}', ${isNewPrice})" class="editImg">
                                        <img src="${ctxStatic}/images/price06.png" style="display: none" class="confirm">
                                    </td>

                                    <td style="display:table-cell; vertical-align:middle">
                                        <span class="switch-off" style="zoom: 0.7" onclick="disableOrEnable(this,'${index}')"></span>
                                        <input type="hidden" value="1" class="delFlag">
                                    </td>
                                </c:if>


                            </c:when>
                            <%--启用--%>
                            <c:otherwise>

                                <c:set var="p_referPrice" value="${price.referPrice}"/>
                                <c:set var="p_referDiscountPrice" value="${price.referDiscountPrice}"/>
                                <c:set var="p_price" value="${price.price}"/>
                                <c:set var="p_discountPrice" value="${price.discountPrice}"/>
                                <input type="hidden" value="${p_referPrice}" class="p_referPrice">
                                <input type="hidden" value="${p_referDiscountPrice}" class="p_referDiscountPrice">

                                <c:choose>
                                    <c:when test="${price.serviceType.warrantyStatus.value eq 'IW'}">
                                        <input type="hidden" class="warrantyStatus" value="${price.serviceType.warrantyStatus.value}">
                                        <c:choose>
                                            <%--价格服务项未维护且是标准价--%>
                                            <c:when test="${price.productPriceList.size() == 0 && isStandardPrice}">
                                                <td></td>
                                                <td></td>
                                                <td></td>
                                            </c:when>
                                            <c:otherwise>
                                                <td>
                                                    <input type="text" id="prices[${index}].price"
                                                            <c:if test="${!isNewPrice}">
                                                                name="prices[${index}].price"
                                                            </c:if>
                                                           readonly="true" style="<c:out value='${p_referPrice != p_price ? "color:#F54142":""}'/>"
                                                           maxlength="7"
                                                           class="<c:out value="${isNewPrice ? '' : 'input-mini required number'}"/>  price p priceBind"
                                                            <c:out value="${isNewPrice ? '' : 'min=0.0'}"/>
                                                           value="<c:out value="${isNewPrice ? '-' : price.price}"/>" />
                                                </td>
                                                <td >
                                                    <input type="text" id="prices[${index}].discountPrice"
                                                            <c:if test="${!isNewPrice}">
                                                                name="prices[${index}].discountPrice"
                                                            </c:if>
                                                           readonly="true" style="<c:out value='${p_referDiscountPrice != p_discountPrice ? "color:#F54142":""}'/>"
                                                           maxlength="7"
                                                           class="<c:out value="${isNewPrice ? '' : 'input-mini required number'}"/>  price d priceBind"
                                                            <c:out value="${isNewPrice ? '' : 'min=0.0'}"/>
                                                        <%--<c:if test="${!isNewPrice}">--%>
                                                           comparePrice="[id='prices[${index}].price']"
                                                        <%--</c:if>--%>
                                                           value="<c:out value="${isNewPrice ? '-' : price.discountPrice}"/>"/>
                                                </td>
                                                <td >
                                                    <input type="text" id="prices[${index}].blockedPrice"
                                                            <c:if test="${!isNewPrice}">
                                                                name="prices[${index}].blockedPrice"
                                                            </c:if>
                                                           readonly="true"
                                                           maxlength="7"
                                                           class="<c:out value="${isNewPrice ? '' : 'input-mini required number'}"/>  price b priceBind"
                                                            <c:out value="${isNewPrice ? '' : 'min=0.0'}"/>
                                                           value="<c:out value="${isNewPrice ? '-' : price.blockedPrice}"/>"/>
                                                </td>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:if test="${!isStandardPrice}">
                                            <td>
                                                <label>
                                                    <img src="${ctxStatic}/images/price05.png" onclick="editImg('${index}', ${isNewPrice})" class="editImg">
                                                    <img src="${ctxStatic}/images/price06.png" style="display: none" class="confirm">
                                                </label>
                                            </td>

                                            <td style="display:table-cell; vertical-align:middle">
                                                <c:if test="${price.delFlag!=2 && !isNewPrice}">
                                                    <span class="switch-on"  style="zoom: 0.7" onclick="disableOrEnable(this,'${index}')"></span>
                                                    <input type="hidden" value="0" class="delFlag">
                                                </c:if>
                                            </td>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" class="warrantyStatus" value="${price.serviceType.warrantyStatus.value}">
                                        <c:choose>
                                            <c:when test="${price.productPriceList.size() == 0 && isStandardPrice}">
                                                <td></td>
                                                <td></td>
                                                <td></td>
                                            </c:when>
                                            <c:otherwise>
                                                <td>
                                                    <input type="text" id="prices[${index}].price"
                                                            <c:if test="${!isNewPrice}">
                                                                name="prices[${index}].price"
                                                            </c:if>
                                                           readonly="true" style="<c:out value='${p_referPrice != p_price ? "color:#F54142":""}'/>"
                                                           maxlength="7"
                                                           class="<c:out value="${isNewPrice ? '' : 'input-mini required number'}"/>  price p priceBind"
                                                            <c:out value="${isNewPrice ? '' : 'min=0.0'}"/>
                                                           value="<c:out value="${isNewPrice ? '-' : price.price}"/>"/>
                                                </td>
                                                <td>
                                                    <input type="text" id="prices[${index}].discountPrice"
                                                            <c:if test="${!isNewPrice}">
                                                                name="prices[${index}].discountPrice"
                                                            </c:if>
                                                           readonly="true" style="<c:out value='${p_referDiscountPrice != p_discountPrice ? "color:#F54142":""}'/>"
                                                           maxlength="7"
                                                           class="<c:out value="${isNewPrice ? '' : 'input-mini required number'}"/>  price d priceBind"
                                                            <c:out value="${isNewPrice ? '' : 'min=0.0'}"/>
                                                           value="<c:out value="${isNewPrice ? '-' : price.discountPrice}"/>"/>
                                                </td>
                                                <td>
                                                    <input type="text" id="prices[${index}].blockedPrice"
                                                            <c:if test="${!isNewPrice}">
                                                                name="prices[${index}].blockedPrice"
                                                            </c:if>
                                                           readonly="true"
                                                           maxlength="7"
                                                           class="<c:out value="${isNewPrice ? '' : 'input-mini required number'}"/>  price b priceBind"
                                                            <c:out value="${isNewPrice ? '' : 'min=0.0'}"/>
                                                           value="<c:out value="${isNewPrice ? '-' : price.blockedPrice}"/>"/>
                                                </td>
                                            </c:otherwise>
                                        </c:choose>

                                        <%--自定义价格方可操作--%>
                                        <c:if test="${!isStandardPrice}">
                                            <td>
                                                <label>
                                                    <img src="${ctxStatic}/images/price05.png" onclick="editImg('${index}', ${isNewPrice})" class="editImg">
                                                    <img src="${ctxStatic}/images/price06.png" style="display: none" class="confirm">
                                                </label>
                                            </td>

                                            <td style="display:table-cell; vertical-align:middle">
                                                <c:if test="${price.delFlag!=2 && !isNewPrice}">
                                                    <span class="switch-on"  style="zoom: 0.7" onclick="disableOrEnable(this,'${index}')"></span>
                                                    <input type="hidden" value="0" class="delFlag">
                                                </c:if>
                                            </td>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>

                            </c:otherwise>
                        </c:choose>
                        <td>${price.updateBy.name}</td>
                        <td><fmt:formatDate value="${price.updateDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <div id="editBtn">
        <c:if test="${!isStandardPrice}">
            <shiro:hasPermission name="fi:md:customerprice:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;"/>&nbsp;
            </shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="cancel()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
    </div>

</form:form>

<script class="removedscript" type="text/javascript">
    $(document).ready(function() {
        $("th").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"text-align":"center","vertical-align":"middle"});
    });
    // 关闭页面
    function cancel() {
        if ($("#inputForm").data("changed")) {
            layer.confirm(
                '确定要放弃此次编辑？',
                {
                    btn: ['确定','取消'], //按钮
                    title:'提示',
                    cancel: function(index, layero){
                    }
                }, function(index){
                    layer.close(index);

                    top.layer.close(this_index);
                }, function(){

                });
        } else {
            top.layer.close(this_index);// 关闭本身
            loading('同步中...');
            var pframe = getActiveTabIframe();// 定义在jeesite.min.js中
            if(pframe){
                pframe.repage();
            }
        }
    }

</script>
</body>
</html>

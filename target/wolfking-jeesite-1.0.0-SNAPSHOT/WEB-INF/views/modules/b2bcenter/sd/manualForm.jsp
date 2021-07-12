<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>B2B订单人工处理</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/modules/b2bcenter/sd/tpl/orderEditForm.html" %>
    <script type="text/javascript">
        var this_index = top.layer.index;
        $(document).ready(function () {
            $("#inputForm").validate({
                submitHandler: function (form) {
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        return false;
                    }

                    if ($("#subAreaId").val() == "") {
                        //console.log("手工只选择到3级就保存了。");
                        $("#subAreaId").val("1");
                    }

                    if (Utils.isEmpty($("[id='customer.id']").val())) {
                        layerAlert("未关联客户，请联系管理员。", "提示");
                        return;
                    }
                    if ($("#productTable tr:visible").length <= 1) {
                        layerAlert("订单下未添加产品详细清单。", "提示");
                        return;
                    }
                    $btnSubmit.attr("disabled", "disabled");

                    //check repeate create order
                    var phone1 = $("#phone1").val();
                    var phone2 = $("#phone2").val();
                    if (Utils.isEmpty(phone1) && Utils.isEmpty(phone2)) {
                        layerInfo("请输入用户电话。", "信息提示");
                        $btnSubmit.removeAttr('disabled');
                        return false;
                    }

                    layer.confirm(
                        '确定保存订单吗？'
                        , {
                            icon: 3, title: '系统确认', success: function (layro, index) {
                                $(document).on('keydown', layro, function (e) {
                                    if (e.keyCode == 13) {
                                        layro.find('a.layui-layer-btn0').trigger('click')
                                    } else if (e.keyCode == 27) {
                                        $btnSubmit.removeAttr('disabled');
                                        layer.close(index);//关闭本身
                                    }
                                })
                            }
                        }
                        , function (index) {
                            layer.close(index);//关闭本身
                            var loadingIndex = layer.msg('正在提交订单，请稍等...', {
                                icon: 16,
                                time: 0,//不定时关闭
                                shade: 0.3
                            });
                            $.ajax({
                                cache: false,
                                type: "POST",
                                url: "${ctx}/b2b/b2bcenter/order/manual?_" + (new Date()).getTime(),
                                data: $(form).serialize(),
                                success: function (data) {
                                    layer.close(loadingIndex);
                                    if (ajaxLogout(data)) {
                                        return false;
                                    }
                                    if (data.success) {
                                        layerMsg('订单转换成功!');
                                        var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                        if (iframe != undefined) {
                                            iframe.repage();
                                        }
                                        //延时方法
                                        setTimeout(function () {
                                            top.layer.close(this_index);//关闭本窗口
                                        }, 300);
                                    }
                                    else {
                                        layerError(data.message, '错误提示');
                                    }
                                    $btnSubmit.removeAttr('disabled');
                                },
                                error: function (e) {
                                    layer.close(loadingIndex);
                                    $btnSubmit.removeAttr('disabled');
                                    ajaxLogout(e.responseText, null, "订单转换错误，请重新查询并转换!");
                                }
                            });//end ajax
                        }
                        , function (index) {
                            $btnSubmit.removeAttr('disabled');
                        });
                    return false;

                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    }
                    if (element.parent().is(".input-td")) {
                        error.appendTo(element.parent());
                    } else {
                        var nspan = $(element.parent()).find("span");
                        if (nspan) {
                            error.insertAfter(nspan);
                        } else {
                            error.insertAfter(element);
                        }
                    }
                }
            });
        });

        var ma = false;
        $(document).on("click", "#btnMatch", function () {
            var addr = $("#txtAddress").val();
            if (addr.length == 0) {
                return false;
            }
            if (ma) {
                return false;
            }
            if ($("#btnMatch").prop("disabled") == true) {
                return false;
            }
            $("#btnMatch").prop("disabled", true);
            restoreLocation('','');
            var dadata = {fullAddress: addr};
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/sys/area/new_da",
                data: dadata,
                success: function (data) {
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success) {
                        if (data.data && data.data.length !== 0) {
                            /*
                            $("#areaId").val(data.data[0]);
                            $("#areaName").val(data.data[1]);
                            $("#address").val(data.data[2]);
                            if (data.data[3] == "0") {
                                layerInfo("系统识别地址与填写地址不一致,请确认", "信息提示");
                            }else{
                                //2019-04-15 经纬度
                                if(data.data.length === 8){
                                    $("#longitude").val(data.data[6]);
                                    $("#latitude").val(data.data[7]);
                                }
                            }
                            */
                            $("#areaId").val(data.data[0]);
                            $("#subAreaId").val(data.data[1]); //街道
                            $("#areaName").val(data.data[2]);
                            $("#address").val(data.data[3]);
                            if (data.data[4] == "0"){
                                layerInfo("系统识别地址与填写地址不一致,请确认","信息提示");
                            }else{
                                //2019-04-15 经纬度
                                if(data.data.length === 9){
                                    $("#longitude").val(data.data[7]);
                                    $("#latitude").val(data.data[8]);
                                }
                            }
                        } else {
                            $("#areaId").val(null);
                            $("#subAreaId").val(null);
                            $("#areaName").val("");
                            layerInfo("该信息暂时无法识别,请手动选择");
                        }
                    } else {
                        $("#areaId").val(null);
                        $("#subAreaId").val(null);
                        $("#areaName").val("");
                        layerInfo("该信息暂时无法识别,请手动选择");
                    }
                    ma = true;
                },
                error: function (e) {
                    //layerInfo("该信息暂时无法识别,请手动选择");
                    ajaxLogout(e.responseText, null, "该信息暂时无法识别,请手动选择!");
                }
            });

            $("#btnMatch").prop("disabled", false);
        });

        function closeme() {
            top.layer.close(this_index);
        }

        $(document).on("change", "#txtAddress", function () {
            ma = false;
        });

        //变更地址，经纬度座标还原
        function restoreLocation(id, name){
            $("#longitude").val(0);
            $("#latitude").val(0);
        }
    </script>
    <style type="text/css">
        .form-horizontal .control-label{width:120px}
        .form-horizontal .controls{margin-left:140px}
        .row-fluid .span4{width:400px}
        .row-fluid .span6{width:600px}
        .row-fluid .span7{width:600px}
        .row-fluid .span8{width:760px}
        .tooltip.bottom .tooltip-arrow{border-bottom-color:#EEADAD}
        .tooltip.right .tooltip-arrow{border-right-color:#EEADAD}
        .tooltip.top .tooltip-arrow{border-top-color:#EEADAD}
        .tooltip.left .tooltip-arrow{border-left-color:#EEADAD}
        .tooltip-inner{background-color:#EEADAD;color:#b94a48}
    </style>
</head>
<body>
<sys:message content="${message}"/>
<form:form id="inputForm" modelAttribute="order" action="${ctx}/b2b/canbo/order/manual" method="post"
           class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="b2bOrderNo"/>
    <form:hidden path="parentBizOrderId"/>
    <form:hidden path="quarter"/>
    <form:hidden path="dataSource.value"/>
    <form:hidden path="customer.id"/>
    <form:hidden path="status"/>
    <form:hidden path="repeateNo"/>
    <form:hidden path="orderType.value"/>
    <form:hidden path="customerUrgentCharge"/>
    <form:hidden path="engineerUrgentCharge"/>
    <form:hidden path="urgentLevel.id"/>
    <form:hidden path="customerOwner"/>
    <form:hidden path="estimatedReceiveDate"/>
    <form:hidden path="buyDate"/>
    <form:hidden path="expectServiceTime"/>
    <form:hidden path="siteCode"/>
    <form:hidden path="siteName"/>
    <form:hidden path="engineerName"/>
    <form:hidden path="engineerMobile"/>
    <form:hidden path="longitude" />
    <form:hidden path="latitude" />
    <form:hidden path="subArea.id" id="subAreaId" />
    <form:hidden path="b2bOrderId"/>
    <form:hidden path="b2bQuarter"/>
    <form:hidden path="category.id" />
    <form:hidden path="createById"/>
    <form:hidden path="customerMapping.saleChannel"/>
    <form:hidden path="createDt"/>
    <form:hidden path="orderNo"/>
    <form:hidden path="orderDataSource"/>
    <c:if test="${canCreateOrder}">
        <c:set var="currentuser" value="${fns:getUser() }"/>
        <!-- shop -->
        <legend>店铺信息</legend>
        <div class="row-fluid">
            <div class="span4">
                <div class="control-group">
                    <label class="control-label">店铺id:</label>
                    <div class="controls">
                        <form:input path="customerMapping.shopId" readonly="true" htmlEscape="false"
                                    data-placement="right" maxlength="100" class="required "/><span
                            class="add-on red">*</span>
                        <form:hidden path="customerMapping.saleChannel" />
                    </div>
                </div>
            </div>
            <div class="span4">
                <div class="control-group">
                    <label class="control-label">店铺名:</label>
                    <div class="controls">
                        <form:input path="customerMapping.shopName" readonly="true" htmlEscape="false"
                                    data-placement="right" maxlength="100" class="required "/><span
                            class="add-on red">*</span>
                    </div>
                </div>
            </div>
        </div>
        <!-- customer head -->
        <legend>客户信息</legend>
        <div class="row-fluid">
            <div class="span4">
                <div class="control-group">
                    <label class="control-label">联系人:</label>
                    <div class="controls">
                        <form:input path="userName" htmlEscape="false" data-placement="right" maxlength="100"
                                    class="required userName"/><span class="add-on red">*</span>
                    </div>
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label">可下单金额:</label>
                    <div class="controls">
                        <input type="text" id="balance" name="balance" class="input-mini" readonly="readonly"
                               style="border-color:#b94a48;color:#b94a48;"
                               value="${(empty order.customerBalance) ? 0 : order.customerBalance }"/>
                        <label>信用额度:</label>
                        <input type="text" id="credit" name="credit" class="input-mini" readonly="readonly"
                               style="border-color:#b94a48;color:#b94a48;" value="${order.customerCredit}"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span4">
                <div class="control-group">
                    <label class="control-label">手机:</label>
                    <div class="controls">
                        <form:input path="phone1" type="tel" htmlEscape="false" data-placement="right" maxlength="11"
                                    placeholder="第一联系电话，必填" class="required mobile "/><span
                            class="add-on red">*</span>
                    </div>
                </div>
            </div>
            <div class="span6">
                <div class="control-group">
                    <label class="control-label">座机:</label>
                    <div class="controls">
                        <form:input path="phone2" htmlEscape="false" maxlength="16" placeholder="第二联系电话，可选"/>
                    </div>
                </div>
            </div>

        </div>
        <div class="row-fluid">
            <div class="span4" style="width: auto;">
                <div class="control-group">
                    <label class="control-label">地址:</label>
                    <div class="controls">
                        <sys:newareaselect name="area.id" id="area" value="${order.area.id}"
                                        labelValue="${order.area.fullName}" labelName="area.name"
                                        title="" mustSelectCounty="true" cssClass="required" callback="restoreLocation"></sys:newareaselect>
                        <span class="add-on red">*</span>
                    </div>
                </div>
            </div>
            <div class="span7" style="margin-left:5px;">
                <div class="control-group">
                    <div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
                        <form:input path="address" htmlEscape="false" maxlength="150" data-placement="bottom"
                                    class="required" style="width:340px;"/>
                        <span class="add-on red">* 详细地址不包含省、市、区县</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span9">
                <div class="control-group">
                    <label class="control-label">智能填写:</label>
                    <div class="controls">
                        <input type="text" id="txtAddress" class="input-xlarge" placeholder="粘贴完整地址,点击识别,系统自动识别省市区"
                               style="width: 668px;" value="${order.fullAddress}"/>
                        <input type="button" id="btnMatch" value="识别" class="btn btn-primary" style="width: 60px;"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span8">
                <div class="control-group">
                    <label class="control-label">服务描述:</label>
                    <div class="controls">
                        <form:textarea path="description" htmlEscape="false" rows="2" maxlength="255"
                                       class="input-xlarge" style="width:100%;"/>
                    </div>
                </div>
            </div>
        </div>
        <!-- order body -->
        <legend>产品详细清单</legend>
        <div class="row-fluid">
            <div class="control-group">
                <table id="productTable" class="table table-striped table-bordered table-condensed"
                       style="margin-bottom: 0px;" >
                    <thead>
                    <tr>
                        <th width="30px">序号</th>
                        <th>产品</th>
                        <th>服务类型</th>
                        <th>数量</th>
                        <th>品牌</th>
                        <th>型号/规格</th>
                        <th>B2B产品编码</th>
                        <th>快递公司</th>
                        <th>快递单号</th>
                        <th>服务金额</th>
                        <th>冻结金额</th>
                        <th>备注</th>
                        <th></th>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </div>
            <div class="control-group">
                <table class="table table-bordered table-condensed">
                    <c:if test="${canCreateOrder == true}">
                        <tr>
                            <td colspan="2">
                                <a id="orderForm_btn_add" class="" href="#"
                                   onclick="Order.editForm_loadProductsForB2B('${order.customer.id}', '${order.dataSource.value}',true);return false;"
                                   style="margin-right:50px;" title="添加产品"><i class="icon-add"
                                                                              style="margin-top: 0px;"></i></a>
                            </td>
                        </tr>
                    </c:if>
                    <tr>
                        <td style="text-align:right;">服务金额</td>
                        <td width="200px" style="align:left;"><label
                                id="lblassignedCharge">${order.expectCharge }</label></td>
                    </tr>
                    <tr>
                        <td style="text-align:right;">冻结金额</td>
                        <td style="align:left;"><label id="lblblockedCharge">${order.blockedCharge }</label></td>
                    </tr>
                    <tr>
                        <td style="text-align:right;">数量总计</td>
                        <td style="align:left;"><label id="lbltotalQty">${order.totalQty}</label></td>
                    </tr>
                    <tr>
                        <td style="text-align:right;">总计</td>
                        <td style="align:left;"><label id="lbltotalCharge"
                                                       class="alert ${(order.expectCharge+order.blockedCharge)>(order.customerBalance+order.customerCredit)?'alert-error':'alert-success'}">${fns:formatDouble(order.expectCharge+order.blockedCharge,2)}</label>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </c:if>
    <div class="control-group">
        <div class="controls">
            <shiro:hasPermission name="sd:order:add">
                <c:if test="${canCreateOrder == true}">
                    <input id="btnSubmit" name="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
                </c:if>
            </shiro:hasPermission>
            <input id="btnCancel" class="btn" type="button" value="返回" onclick="closeme();"/>
        </div>
    </div>
</form:form>
<script class="removedscript" id="sc_AddItemRows" type="text/javascript">
    $(document).ready(function () {
        Order.rootUrl = "${ctx}";
        Order.reload = true;
        //load items
        Order.editForm_addItemRows(${fns:toGson(order)});
    });
</script>
</body>
</html>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>派单</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function () {
            $("#inputForm").validate({
                rules: {
                    'orderFee.planOtherCharge':{ min: 0, max : 1000},
                    'orderFee.planDistance':{min:0,max:999},
                    'orderFee.planTravelCharge':{min:0,max:300},
                    'orderFee.customerPlanTravelCharge':{min:0,max:500},
                    'orderFee.customerPlanOtherCharge':{min:0,max:1000}
                },
                messages: {
                    'orderFee.planOtherCharge' : {min: "不能小于0", max: "网点其它费用不能超过1000元"},
                    'orderFee.planDistance':{min:"不能小于0",max:"上门距离不能超过999公里"},
                    'orderFee.planTravelCharge':{min:"不能小于0",max:"网点远程费不能超过300元"},
                    'orderFee.customerPlanTravelCharge':{min:"不能小于0",max:"厂商远程费不能超过500元"},
                    'orderFee.customerPlanOtherCharge':{min:"不能小于0",max:"厂商其他费用不能超过1000元"}
                },
                submitHandler: function (form) {
                    if(clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
                    // var strPlanTotalCharge = $("[id='orderFee.planTotalCharge']").val();
                    // planTotalCharge = parseFloat(strPlanTotalCharge);
                    var planTravelCharge = parseFloat($("[id='orderFee.planTravelCharge']").val());
                    var planOtherCharge =  parseFloat($("[id='orderFee.planOtherCharge']").val());
                    var planTotalCharge = planTravelCharge + planOtherCharge;
                    if(planTotalCharge > 1300){
                        layerError("网点费用合计已超出1300元","错误提示");
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                        return false;
                    }
                    <c:if test="${limitRemoteDict != null && areaRemoteFee !=null && areaRemoteFee ==1}">
                    var limitTotalCharge = ${limitRemoteDict.sort};
                    if(limitTotalCharge<0){
                        limitTotalCharge = 0;
                    }
                    if(planTotalCharge > limitTotalCharge){
                        layerError("远程费用和其他费用合计已超过" + limitTotalCharge + "元，不允许派单!<br/>请确认是否操作退单!","错误提示");
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                        return false;
                    }
                    $("[id='orderFee.customerPlanTravelCharge']").val("0");
                    $("[id='orderFee.customerPlanOtherCharge']").val("0");
                    </c:if>
                    var loadingIndex;
                    var ajaxSuccess = 0;
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sd/order/plan?" + (new Date()).getTime(),
                        data: $(form).serialize(),
                        beforeSend: function () {
                            loadingIndex = layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,
                                shade: 0.3
                            });
                        },
                        complete: function () {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            if(ajaxSuccess == 0) {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                            }
                        },
                        success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            if (data && data.success == true) {
                                //回调父窗口方法
                                setTimeout(function() {
                                    var layero = $("#layui-layer" + parentIndex,top.document);
                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                    iframeWin.updateService();
                                    ajaxSuccess = 1;
                                    clickTag = 0;//还原标记
                                    top.layer.close(this_index);//关闭本窗口
                                    return false;
                                }, 300);
                                ajaxSuccess = 1;
                                return false;
                            }
                            else if (data && data.message) {
                                layerError(data.message,"错误提示");
                            }
                            else {
                                layerError("派单错误", "错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"派单错误，请重试!");
                        }
                    });
                    return false;
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

            $("[id='orderFee.planDistance']").on("blur",function(){
                var standDistance = ${freeDistance};
                var price = ${price};
                var distance = parseFloat($(this).val());

                var otherCharge = parseFloat($("[id='orderFee.planOtherCharge']").val());
                var travelCharge = 0;
                if(distance>standDistance) {
                    travelCharge = price*(distance - standDistance);
                    travelCharge = parseFloat(travelCharge.toFixed(1));
                    travelCharge = (travelCharge>300?300:travelCharge);//远程费不超过300
                    $("[id='orderFee.planTravelCharge']").val(travelCharge);
                    $("[id='orderFee.planTotalCharge']").val(travelCharge + otherCharge);
                }else{
                    $("[id='orderFee.planTravelCharge']").val("0");
                    $("[id='orderFee.planTotalCharge']").val(otherCharge);
                }
                <c:if test="${limitRemoteDict != null && areaRemoteFee !=null && areaRemoteFee ==1}">
                var limitTotalCharge = ${limitRemoteDict.sort};
                if(limitTotalCharge<0){
                    limitTotalCharge = 0;
                }
                var totalCharge = travelCharge + otherCharge;
                if(totalCharge > limitTotalCharge){
                    $("#btnSubmit").attr('disabled', 'disabled');
                    layerAlert("远程费用和其他费用合计已超过" + limitTotalCharge + "元，不允许派单!<br/>请确认是否操作退单!");
                }else{
                    $("#btnSubmit").removeAttr('disabled');
                    $("[id='orderFee.customerPlanTravelCharge']").val("0");
                    $("[id='orderFee.customerPlanOtherCharge']").val("0");
                }
                </c:if>
            });

            $("[id='orderFee.planOtherCharge']").on("blur", function(){
               var travelCharge = parseFloat($("[id='orderFee.planTravelCharge']").val());
               var otherCharge = parseFloat($("[id='orderFee.planOtherCharge']").val());
                $("[id='orderFee.planTotalCharge']").val(travelCharge + otherCharge);
                <c:if test="${limitRemoteDict != null && areaRemoteFee !=null && areaRemoteFee ==1}">
                var limitTotalCharge = ${limitRemoteDict.sort};
                if(limitTotalCharge<0){
                    limitTotalCharge = 0;
                }
                var totalCharge = travelCharge + otherCharge;
                if(totalCharge > limitTotalCharge){
                    $("#btnSubmit").attr('disabled', 'disabled');
                    layerAlert("远程费用和其他费用合计已超过" + limitTotalCharge + "元，不允许派单!<br/>请确认是否操作退单!");
                }else{
                    $("#btnSubmit").removeAttr('disabled');
                    $("[id='orderFee.customerPlanTravelCharge']").val("0");
                    $("[id='orderFee.customerPlanOtherCharge']").val("0");
                }
                </c:if>
            });

        });

        function selEngineerChange(data){
            if(data){
                $("#planedEngineerId").val(data.primary.id ||'');
                $("#planedEngineerName").val(data.primary.name ||'');
                // $("input[name='orderFee.engineerPaymentType.value'][value=" + data.paymentType.value +"]").prop("checked",true);
                $("[id='orderFee.engineerPaymentType.value']").val(data.paymentType.value);
                $("[id='orderFee.engineerPaymentType.label']").val(data.paymentType.label);
                $("#sendUserMessageFlag").prop('checked', true);
                if(data.appFlag == 0){
                    //无app接单权限，默认勾选
                    $("#sendEngineerMessageFlag").prop('checked', true);
                }else{
                    $("#sendEngineerMessageFlag").prop('checked', false);
                }
                $("[id='orderCondition.serviceAddress']").focus();
            }
        }

        function closethisfancybox() {
            top.layer.close(this_index);
        }

        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip(
                {
                    gravity: 'north'
                });
            $('a[data-toggle=tooltipeast]').darkTooltip(
                {
                    gravity: 'east'
                });
        });
    </script>

    <script type="text/javascript">
        <!-- 拨号插件 -->
        function plugin0() {
            return document.getElementById('plugin0');
        }
        plugin = plugin0;
        function addEvent(obj, name, func) {
            if (window.addEventListener) {
                obj.addEventListener(name, func, false);
            } else {
                obj.attachEvent("on" + name, func);
            }
        }

        function load() {
            addEvent(plugin(), 'OnDeviceConnect', function (number) {
                alert('设备连接事件：' + number);
            });
            addEvent(plugin(), 'OnDeviceDisconnect', function (number) {
                alert('设备断开事件：' + number);
            });
            addEvent(plugin(), 'OnCallOut', function (teleNo) {
                alert('呼叫事件：' + teleNo);
            });
            addEvent(plugin(), 'OnCallIn', function (teleNo) {
                alert('来电事件：' + teleNo);
            });
            addEvent(plugin(), 'OnHangUp', function (teleNo) {
                alert('挂起事件：' + teleNo);
            });
            addEvent(plugin(), 'OnAnswer', function (teleNo) {
                alert('应答事件：' + teleNo);
            });
        }
        function pluginLoaded() {
            //alert("Plugin loaded!");
        }

        //摘机
        function OffHookCtrl() {
            if (!plugin().OffHookCtrl())
                alert("OffHookCtrl Fail");
        }

        //拔号
        function StartDial(teleNo, bRecord) {
            teleNo = "00" + $("[id='orderCondition.servicePhone']").val();
//        	teleNo="00"+$("#servicePhone").val();
//			alert(teleNo);
            if (!plugin().StartDial(teleNo, bRecord))
                alert("StartDial Fail");
        }

        //挂机或挂断
        function HangUpCtrl() {
            if (!plugin().HangUpCtrl())
                alert("HangUpCtrl Fail");
        }
        //上传录音
        function HangUpCtrl() {
            alert(plugin().UploadRecord());
        }
        function testEvent() {
            plugin().testEvent();
        }

        function pluginValid() {
            if (plugin().valid) {
                alert(plugin().echo("This plugin seems to be working!"));
            } else {
                alert("Plugin is not working :(");
            }
        }
    </script>
    <style type="text/css">
        legend span {
            border-bottom: #0096DA 4px solid;
            padding-bottom: 6px;}
        .form-horizontal {margin-top: 5px;}
        i[class^="icon-"] {font-size:18px;}
        .form-horizontal .control-label {width: 100px;}
        .form-horizontal .controls {margin-left: 120px;}
    </style>
</head>
<body onload="load()">
<form:form id="inputForm" modelAttribute="order" action="${ctx}/sd/order/plan" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="quarter"/>
    <form:hidden path="orderFee.engineerPaymentType.value" />
    <form:hidden path="crushPlanFlag"/>
    <input type="hidden" id="areaRemoteFee" name="areaRemoteFee" value="${areaRemoteFee}" />
    <sys:message content="${message}"/>
    <legend><span>网点信息</span></legend>
    <c:if test="${limitRemoteDict != null && areaRemoteFee != null && areaRemoteFee == 1}">
        <div class="row-fluid">
            <div class="alert">
                <icon class="icon-warning-sign" style="margin-right:10px;"></icon>
                <c:choose>
                    <c:when test="${limitRemoteDict.sort <=0}">
                        提示: 该客户订单不允许加远程费及其他费用，请确认是否操作退单！
                    </c:when>
                    <c:otherwise>
                         提示: 远程费用和其他费用合计不能超过${limitRemoteDict.sort<0?0:limitRemoteDict.sort}元，否则将无法派单! 请确认是否操作退单!
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:if>
    <c:if test="${order.dataSource.value == '22'}">
        <div class="row-fluid">
            <div class="alert">
                <icon class="icon-warning-sign" style="margin-right:10px;"></icon>
                提示: 京东优易+订单，务必派单给网点 [ <b>${order.orderAdditionalInfo.siteName} ${order.orderAdditionalInfo.engineerName} ${order.orderAdditionalInfo.engineerMobile}</b> ]
            </div>
        </div>
    </c:if>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">服务网点:</label>
                <div class="controls">
                    <md:pointselectforplannew id="servicePoint" name="orderCondition.servicePoint.id" value="${order.orderCondition.servicePoint.id}" labelName="orderCondition.servicePoint.name" labelValue="${order.orderCondition.servicePoint.name}"
                                           width="1550" height="760" noSubEnginner="true" noblackList="true"
                                           callbackmethod="selEngineerChange" showArea = "true" allowClear="true" noModal="true"
                                           title="选择服务网点" areaId="${order.orderCondition.area.id}" subAreaId="${order.orderCondition.subArea.id}"
                                              productCategoryId="${order.orderCondition.productCategoryId}" serviceAddress="${order.orderCondition.area.name}${order.orderCondition.serviceAddress}"
                                           cssClass="required" cssStyle="width: 245px;"/>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">网点师傅:</label>
                <div class="controls">
                    <input type="text" id="planedEngineerName" name="orderCondition.engineer.name" class="input-block-level" value="${order.orderCondition.engineer.name}" readonly="readonly" />
                    <input type="hidden" id="planedEngineerId" name="planedEngineerId" value="${order.orderCondition.engineer.id}" />
                </div>
            </div>
        </div>
    </div>

    <div class="row-fluid">
        <c:if test="${areaRemoteFee !=null && areaRemoteFee ==1}">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label">上门距离:</label>
                    <div class="controls">
                        <input type="number" id="orderFee.planDistance" name="orderFee.planDistance" value="${fns:formatDouble(order.orderFee.planDistance,0)}" class="input-large required digits" />&nbsp;&nbsp;公里
                    </div>
                </div>
            </div>
        </c:if>
        <div class="span6" style="display: none">
            <div class="control-group">
                <label class="control-label">结算方式:</label>
                <div class="controls">
                    <form:input path="orderFee.engineerPaymentType.label" cssClass="input-block-level" readonly="true" />
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <c:choose>
            <c:when test="${areaRemoteFee !=null && areaRemoteFee ==1}">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">网点远程费:</label>
                        <div class="controls">
                            <form:input path="orderFee.planTravelCharge" readonly="true"  htmlEscape="false" maxlength="11" cssClass="input-block-level required number"/>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">网点其它费用:</label>
                        <div class="controls">
                            <form:input path="orderFee.planOtherCharge" type="number"  htmlEscape="false" cssClass="input-block-level required number"/>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">网点远程费:</label>
                        <div class="controls">
                            <form:input path="orderFee.planTravelCharge" readonly="true"  htmlEscape="false" maxlength="11" cssClass="input-block-level required number" cssStyle="border-color: red;background-color: rgb(255, 250, 250);width: 310px"/>
                            <a data-toggle="tooltip" data-tooltip="该区域未设置远程费用"><i class="icon-question-sign" style="color: red"></i></a>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">网点其它费用:</label>
                        <div class="controls">
                            <form:input path="orderFee.planOtherCharge" type="number"  readonly="true" htmlEscape="false" cssClass="input-block-level required number" cssStyle="border-color: red;background-color: rgb(255, 250, 250);width: 310px"/>
                            <a data-toggle="tooltip" data-tooltip="该区域未设置远程费用"><i class="icon-question-sign" style="color: red"></i></a>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">网点费用合计:</label>
                <div class="controls">
                    <input id="orderFee.planTotalCharge" name="orderFee.planTotalCharge" value="${order.orderFee.planOtherCharge + order.orderFee.planTravelCharge}" class="input-block-level required number" readonly="readonly" type="text" value="0.0" maxlength="7"/>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">审批单号</label>
                <div class="controls">
                    <form:input path="orderFee.planTravelNo" htmlEscape="false" maxlength="11" cssClass="input-block-level"/>

                </div>
            </div>
        </div>
    </div>
    <legend><span>厂商费用</span></legend>
    <div class="row-fluid">
        <c:choose>
            <c:when test="${limitRemoteDict != null && areaRemoteFee != null && areaRemoteFee == 1}">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">厂商远程费:</label>
                        <div class="controls">
                            <form:input path="orderFee.customerPlanTravelCharge" htmlEscape="false" maxlength="11" readonly="true" cssClass="input-block-level required number" cssStyle="border-color: red;background-color: rgb(255, 250, 250);width: 310px"/>
                            <a data-toggle="tooltip" data-tooltip="管控品类无远程费"><i class="icon-question-sign" style="color: red"></i></a>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">厂商其他费用:</label>
                        <div class="controls">
                            <form:input path="orderFee.customerPlanOtherCharge" htmlEscape="false" maxlength="11" readonly="true" cssClass="input-block-level required number" cssStyle="border-color: red;background-color: rgb(255, 250, 250);width: 310px"/>
                            <a data-toggle="tooltip" data-tooltip="管控品类无远程费"><i class="icon-question-sign" style="color: red"></i></a>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:when test="${customerRemoteFee ==null || customerRemoteFee==1}">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">厂商远程费:</label>
                        <div class="controls">
                            <form:input path="orderFee.customerPlanTravelCharge" htmlEscape="false" maxlength="11" cssClass="input-block-level required number"/>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">厂商其他费用:</label>
                        <div class="controls">
                            <form:input path="orderFee.customerPlanOtherCharge" htmlEscape="false" maxlength="11" cssClass="input-block-level required number"/>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">厂商远程费:</label>
                        <div class="controls">
                            <form:input path="orderFee.customerPlanTravelCharge" htmlEscape="false" maxlength="11" readonly="true" cssClass="input-block-level required number" cssStyle="border-color: red;background-color: rgb(255, 250, 250);width: 310px"/>
                            <a data-toggle="tooltip" data-tooltip="厂商未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">厂商其他费用:</label>
                        <div class="controls">
                            <form:input path="orderFee.customerPlanOtherCharge" htmlEscape="false" maxlength="11" readonly="true" cssClass="input-block-level required number" cssStyle="border-color: red;background-color: rgb(255, 250, 250);width: 310px"/>
                            <a data-toggle="tooltip" data-tooltip="厂商未设置远程费"><i class="icon-question-sign" style="color: red"></i></a>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    <legend><span>派单操作</span></legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="control-group">
                <label class="control-label">短信发送:</label>
                <div class="controls">
                    <form:checkbox path="sendUserMessageFlag" id="sendUserMessageFlag" value="1"/><label for="sendUserMessageFlag">用户短信</label>
                    <form:checkbox path="sendEngineerMessageFlag" id="sendEngineerMessageFlag" value="1"/><label for="sendEngineerMessageFlag">师傅短信</label>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="control-group">
                <label class="control-label">派单说明:</label>
                <div class="controls">
                    <form:textarea path="remarks" htmlEscape="false" rows="2" maxlength="200" class="input-block-level"/>
                </div>
            </div>
        </div>
    </div>
    <%--<div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
        <c:if test="${empty canSave || canSave ne false }">
            <shiro:hasPermission name="sd:order:plan">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保存并发送"/>&nbsp;</shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="　关　闭　" onclick="closethisfancybox();"/>
    </div>--%>
</form:form>
<div style="height: 60px;width: 100%"></div>
    <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
        <hr style="margin: 0px;"/>
        <div style="float: right;margin-top: 10px;margin-right: 20px">
            <c:if test="${empty canSave || canSave ne false }">
                <shiro:hasPermission name="sd:order:plan">
                    <input id="btnSubmit" class="btn btn-primary" type="button" value="派  单" onclick="$('#inputForm').submit()" style="margin-right: 5px;width: 96px;height: 40px"/>&nbsp;</shiro:hasPermission>
            </c:if>
            <input id="btnCancel" class="btn" type="button" value="　关　闭　" style="width: 96px;height: 40px" onclick="closethisfancybox();"/>
        </div>
    </div>
</div>

<object id="plugin0" type="application/x-nyteleactivex" width="0" height="0">
    <param name="onload" value="pluginLoaded"/>
    <param name="install-url" value="${ctxPlugin}/npNYTeleActiveX.dll"/>
</object>
</body>
</html>
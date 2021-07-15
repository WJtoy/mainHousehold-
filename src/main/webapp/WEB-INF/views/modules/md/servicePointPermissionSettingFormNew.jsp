<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <title>网点权限设定管理</title>
    <meta name="decorator" content="default"/>
    <c:set var="user" value="${fns:getUser()}"/>
    <script type="text/javascript">
        $(document).ready(function () {
            $("[id^='level.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if (optionSelected.val() == "0") {
                    $("[id^='level.label']").val("");
                } else {
                    $("[id^='level.label']").val(optionSelected.text());
                }
            });

            $("[id^='finance.paymentType.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if (optionSelected.val() == "0") {
                    $("[id^='finance.paymentType.label']").val("");
                } else {
                    $("[id^='finance.paymentType.label']").val(optionSelected.text());
                }
            });

            $("#inputForm").validate({
                rules: {
                    servicePointNo: {remote: "${ctx}/md/servicepoint/checkNo?id=${servicePoint.id}"},
                    contactInfo1: {remote: "${ctx}/md/servicepoint/checkContact?id=${servicePoint.id}"},
                    "primary.contactInfo": {remote: "${ctx}/md/servicepoint/checkEngineerMobile?id=${servicePoint.primary.id}"},
                    "finance.bankNo": {remote: "${ctx}/md/servicepoint/checkBankNo?id=${servicePoint.id}"}
                },
                messages: {
                    servicePointNo: {remote: "服务网点编号已存在"},
                    contactInfo1: {remote: "服务网点编号已存在"},
                    "primary.contactInfo": {remote: "手机号已注册"},
                    "finance.bankNo": {remote: "服务网点银行卡号已存在"}
                },
                onfocusout: function (element) {
                    $(element).valid();//失去焦点时再验证
                },
                submitHandler: function (form) {
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        return false;
                    }
                    var appFlag = $("[name='appFlag']:checked").val();
                    var primaryAppFlag = $("[name='primary.appFlag']:checked").val();

                    // /////
                    // var remotePriceEnabledFlag = $("#remotePriceEnabledFlag").val();
                    // var remotePriceFlag = $("[name=remotePriceFlag]:checked").val();
                    // var remotePriceReference = $("#remotePriceReference").val();
                    // var oldRemotePriceEnabledFlag = $("#oldRemotePriceEnabledFlag").val();
                    // var oldRemotePriceFlag = $("#oldRemotePriceFlag").val();
                    // var resetRemotePrice = $("#resetPrice_2").prop("checked");
                    // if (oldRemotePriceEnabledFlag == 0 && remotePriceEnabledFlag ==1) {
                    //     if (oldRemotePriceFlag == 0 && remotePriceFlag == 1 && remotePriceReference == 0) {
                    //         layerError("请为自定义偏远价格选择参考价格.", "错误提示");
                    //         $("#btnSubmit").prop("disabled", false);
                    //         return false;
                    //     }
                    // }
                    // if (oldRemotePriceEnabledFlag == 1 && remotePriceEnabledFlag ==1) {
                    //     if (oldRemotePriceFlag == 0 && remotePriceFlag == 1 && remotePriceReference == 0) {
                    //         layerError("偏远价格由标准价变更为自定义价时，请选择参考价格.", "错误提示");
                    //         $("#btnSubmit").prop("disabled", false);
                    //         return false;
                    //     }
                    //     if (remotePriceFlag == 1 && remotePriceReference == 0 && resetRemotePrice == true) {
                    //         layerError("重置偏远价格时,请为自定义偏远价格选择参考价格.", "错误提示");
                    //         $("#btnSubmit").prop("disabled", false);
                    //         return false;
                    //     }
                    // }
                    // ///////

                    if($("input[type='checkbox'][name='resetPrices']:checked").length == 0){
                       $("#resetPrice").val(0);
                    }else if($("input[type='checkbox'][name='resetPrices']:checked").length == 1){
                        $("input[type='checkbox'][name='resetPrices']:checkbox:checked").each(function(i,element){
                            $("#resetPrice").val(this.value);
                        });
                    }else {
                        $("#resetPrice").val(3);
                    }

                    if (appFlag == 0 && primaryAppFlag != 0) {
                        layerError("请注意，网点允许手机接单,主帐号才能允许手机接单.", "错误提示");
                        return false;
                    }

                    $("#btnSubmit").prop("disabled", true);
                    if (!$("#inputForm").valid()) {
                        $("#btnSubmit").prop("disabled", false);
                        return false;
                    }

                    var options = {
                        url: "${ctx}/provider/md/servicePointNew/psSave",
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
                    $("#btnSubmit").removeAttr('disabled');

                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                    if (element.context.name == 'primary.contactInfo') {
                        error.css("margin-left", "190px");
                        element.parent('div').parent('div').append(error);
                    }
                }
            });
        });

        function showContactInfo() {
            top.layer.open({
                type: 2,
                id: 'layer_searchServicePointContactInfo',
                zIndex: 19891015,
                title: '查询',
                content: "${ctx}/md/servicepoint/findUserListByContactInfo",
                area: ['800px', '640px'],
                shade: 0.3,
                shadeClose: true,
                maxmin: false,
                success: function (layero, index) {
                }
            });
        }

        // 开关控制
        function switchControl(obj) {
            var flag = $("." + obj).val();
            if (flag == 0) {
                $("." + obj).attr("value", 1);
                if (obj == "discountFlag") {
                    $("#finance\\.discount").attr("value", ${fns:getDictLabelFromMS("10","ServicePointDiscount","4")});
                }
            } else {
                $("." + obj).attr("value", 0);
            }
        }

        function switchRemotePriceEnabledFlag() {
            var flag = $("#remotePriceEnabledFlag").val();
            if (flag == 0) {
                $("#remotePriceEnabledFlag").val(1);
            } else {
                $("#remotePriceEnabledFlag").val(0);
            }
            editRemotePriceEnabledFlag();
        }
        function editRemotePriceEnabledFlag() {
            var flag = $("#remotePriceEnabledFlag").val();
            if(flag == 1){
                $("[name=remotePriceFlag]").removeAttr("disabled");
                // $("#remotePriceReference").removeAttr("disabled");
            }else {
                $("[name=remotePriceFlag]").attr("disabled", true);
                // $("#remotePriceReference").attr("disabled", true);
            }
        }

        function switchInsuranceFlag(obj) {
            var title;
            var insuranceFlag = $("#insuranceFlag").val();
            if(insuranceFlag == 0){
                title = "开启";
                insuranceFlag = 1;
            }else {
                insuranceFlag = 0;
                title = "关闭";
            }
            var id = $("#id").val();

            if(id == null || id == ''){
                if (insuranceFlag == 1) {
                    $("#insuranceFlag").val(1);
                } else {
                    $("#insuranceFlag").val(0);
                }
            }else {
                layer.confirm(
                    '确认要'+ title+'互助基金吗？',
                    {
                        btn: ['确定','取消'], //按钮
                        title:'提示',
                        cancel: function(index, layero){
                            // 右上角叉
                            if ($(obj).attr("class") == 'switch-off') {
                                honeySwitch.showOn(obj);
                            } else {
                                honeySwitch.showOff(obj);
                            }
                        }
                    }, function(index){
                        layer.close(index);//关闭本身
                        $.ajax({
                            url: "${ctx}/provider/md/servicePointNew/updateInsuranceFlag?id="+ id +"&insuranceFlag=" + insuranceFlag,
                            success:function (data) {
                                if (data.success) {
                                    // 停用
                                    if (insuranceFlag == 1) {
                                        $("#insuranceFlag").val(1);
                                    } else {
                                        $("#insuranceFlag").val(0);
                                    }

                                } else {
                                    layerError(title + "失败:" + data.message, "错误提示");
                                    // 取消操作
                                    if ($(obj).attr("class") == 'switch-off') {
                                        honeySwitch.showOn(obj);
                                    } else {
                                        honeySwitch.showOff(obj);
                                    }
                                }
                                return false;
                            },
                            error: function (data) {
                                ajaxLogout(data,null,"数据保存错误，请重试!");
                                if ($(obj).attr("class") == 'switch-off') {
                                    honeySwitch.showOn(obj);
                                } else {
                                    honeySwitch.showOff(obj);
                                }
                            },
                        });
                        return false;
                    }, function(){
                        // 取消操作
                        if ($(obj).attr("class") == 'switch-off') {
                            honeySwitch.showOn(obj);
                        } else {
                            honeySwitch.showOff(obj);
                        }
                    });
            }

        }
        //保存新的网店备注
        function saveServicePointRemark(servicePointId, btn) {
            if (btn.disabled == true) {
                return fasle;
            }
            btn.disabled = true;
            var remarks = $("#remarks").val();
            if (!remarks) {
                //layerError("请先输入备注信息");
                layerMsg('请先输入备注信息');
                btn.disabled = false;
                $("#remarks").focus()
                return false;
            }
            if (remarks.length > 250) {
                layerMsg('备注信息超长了，请限制在250个字符以内!');
                btn.disabled = false;
                $("#remarks").focus()
                return false;
            }
            remarks = encodeURI(remarks);
            if (servicePointId != null && remarks != null) {
                $.ajax({
                    cache: false,
                    type: "POST",
                    url: "${ctx}/md/servicepoint/ajax/updateRemark?servicePointId=" + servicePointId + "&remarks=" + (remarks || ''),
                    dataType: 'json',
                    success: function (data) {
                        btn.disabled = false;
                        if (ajaxLogout(data)) {
                            return false;
                        }
                        if (data.success) {
                            layerMsg('保存成功');

                        } else {
                            layerError(data.message);
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText, null, "保存备注错误，请重试!");
                        btn.disabled = false;
                    }
                });

            } else {
                layerError("获取保存类容错误", "错误提示");
                return false;
            }

        }

        //查看网店备注历史列表
        function viewRemarkList(servicePointId, servicePointNo, servicePointName) {
            var planIndex = top.layer.open({
                type: 2,
                id: 'layer_planRemarkList_view',
                zIndex: 19891016,
                title: '历史备注',
                content: "${ctx}/provider/md/servicePointNew/viewRemarkList?servicePointId=" + (servicePointId || '') + "&servicePointNo=" + (servicePointNo || '') + "&servicePointName=" + (servicePointName || ''),
                // area: ['980px', '640px'],
                area: ['936px', (screen.height / 2) + 'px'],
                shade: 0.3,
                shadeClose: true,
                maxmin: false,
                success: function (layero, index) {
                },
                end: function () {
                }
            });
        }
    </script>
    <style>
        .line_ {
            border-bottom: 3.5px solid #0096DA;
            width: 65px;
            border-radius: 10px;
        }

        /*父盒子*/
        .flex-container {
            display: -webkit-flex;
            display: flex;
            width: 100%;
            margin-bottom: 15px;
        }

        .x_line {
            width: 50%;
            margin-left: -32px;
        }

        .y_line {
            width: 50%;
            margin-left: -6px;
        }

        .search_button {
            width: 64px;
            height: 30px;
            background-color: #0096DA;
            color: #fff;
            border-radius: 3px;
            border: 1px solid rgba(255, 255, 255, 0);
        }

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
</head>
<body>
<br/>
<sys:message content="${message}"/>
<div style="overflow-y:auto;height: 723px;margin-top: -20px">
<form:form id="inputForm" modelAttribute="servicePoint" action="" method="post"
           class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="delFlag"/>
    <form:hidden path="address"/>
    <form:hidden path="useDefaultPrice"/>
    <form:hidden path="forTmall"/>
    <input type="hidden" value="${servicePoint.remotePriceEnabledFlag}" id="oldRemotePriceEnabledFlag" />
    <input type="hidden" value="${servicePoint.remotePriceFlag}" id="oldRemotePriceFlag" />

    <div id="main" style="padding: 25px;margin-top: -6px;">
        <legend>网点信息
            <div class="line_"></div>
        </legend>
        <div class="flex-container">
            <div class="x_line">
                <label class="control-label"><span
                        class=" red">*</span>编&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：</label>
                <div class="controls">
                    <form:input path="servicePointNo" htmlEscape="false" maxlength="20" class="required" readonly="true"
                                cssStyle="width: 250px"/>
                </div>
            </div>

            <div class="y_line">
                <label class="control-label"><span
                        class=" red">*</span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称：</label>
                <div class="controls">
                    <form:input path="name" htmlEscape="false" maxlength="50" class="required" readonly="true"
                                cssStyle="width: 250px"/>
                </div>
            </div>
        </div>
        <div class="flex-container">
            <div class="x_line">
                <label class="control-label"><span
                        class=" red">*</span>联系电话：</label>
                <div class="controls">
                    <form:input path="contactInfo1" htmlEscape="false" maxlength="11" class="required mobile"
                                cssStyle="width: 250px"/>
                </div>
            </div>
            <div>
                <div class="y_line">
                    <label class="control-label"><span class=" red">*</span>等&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;级：</label>
                    <div class="controls">
                        <form:select path="level.value" class="input-small required" cssStyle="width: 263px;">
                            <form:option value="" label="请选择"/>
                            <%--
                            <form:options items="${fns:getDictExceptListFromMS('ServicePointLevel', '6,7,8')}"
                                          itemLabel="label" itemValue="value" htmlEscape="false"/>
                                          --%>
                            <%--切换为微服务--%>
                            <form:options items="${fns:getDictListFromMS('ServicePointLevel')}"
                                          itemLabel="label" itemValue="value" htmlEscape="false"/>
                        </form:select>
                    </div>
                    <form:hidden path="level.label" htmlEscape="false"/>
                </div>
            </div>
        </div>
        <div class="flex-container">
            <div class="x_line">
                <label class="control-label">分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类：</label>
                <div class="controls" style="margin-top: 3px">
                    <c:forEach items="${fns:getDictListFromMS('degreeType')}" var="dict">
                        <input id="degree${dict.value}" name="degree" type="radio"
                            <c:out value="${servicePoint.degree == dict.value?'checked':''}"/> value="${dict.value}">
                        <label for="degree${dict.value}">${dict.label}</label>
                    </c:forEach>
                </div>
            </div>
        </div>

        <form:hidden path="primary.id" />
        <div class="flex-container">
            <div class="x_line">
                <label class="control-label"><span class=" red">*</span>主帐号姓名：</label>
                <div class="controls">
                    <form:input path="primary.name" htmlEscape="false" maxlength="20" class="required"
                                cssStyle="width: 248px"/>
                </div>
            </div>

            <div class="y_line">
                <label class="control-label"><span class=" red">*</span>主帐号电话：</label>
                <div class="controls" style="width: 75%;">
                    <form:input path="primary.contactInfo" htmlEscape="false" maxlength="11" class="required mobile"
                                cssStyle="width: 249px"/>
                    <button class="search_button" onclick="showContactInfo()" type="button">
                        <i class="icon-search"></i>&nbsp;查询
                    </button>
                </div>
            </div>
        </div>

        <div class="flex-container">
            <div class="x_line">
                <label class="control-label"><span class=" red">*</span>主帐号等级：</label>
                <div class="controls">
                    <form:select path="primary.level.value" class="input-small required"
                                 disabled="${!empty servicePoint.primary.id?'true':'false'}"
                                 cssStyle="width: 262px;">
                        <form:option value="0" label="请选择"/>
                        <form:options items="${fns:getDictInclueListFromMS('EngineerLevel','1,2,3,4,5')}"
                                      itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                    </form:select>
                </div>
            </div>

            <div class="y_line">
                <label class="control-label"><span class=" red">*</span>手机接单：</label>
                <div class="controls">
                        <%--                        <form:radiobuttons path="primary.appFlag" disabled="true"--%>
                        <%--                                           items="${fns:getDictListFromMS('yes_no')}" itemLabel="label"--%>
                        <%--                                           itemValue="value" htmlEscape="false" class="required"/>&lt;%&ndash;切换为微服务&ndash;%&gt;--%>
                    <c:set value="${servicePoint.primary.appFlag}" var="primaryAppFlag"/>
                    <span class="<c:out value="${primaryAppFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                          onclick="switchControl('primaryAppFlag')"></span>
                    <input type="hidden" value="${primaryAppFlag}" class="primaryAppFlag" name="primary.appFlag">
                    <span class="help-inline" style="margin-top: -13px;">主帐号手机接单权限</span>
                </div>
            </div>
        </div>
        <div class="flex-container">
            <div class="x_line">
                <label class="control-label">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
                <div class="controls">
                    <div style="width: 785px">
                        <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"
                                       cssStyle="width: 690px;float: left;"/>
                        <input id="oldRemarks" class="btn btn-small" type="button" value="历史备注" onclick="viewRemarkList('${servicePoint.id}','${servicePoint.servicePointNo}','${servicePoint.name}');" style="float: left;margin-left: 5px;margin-bottom: 5px;width: 66px"/>
                        <input id="oldSave" class="btn btn-small btn-primary" type="button" value="保 存" onclick="saveServicePointRemark('${servicePoint.id}',this);" style="float: left;width: 66px;margin-top: 4px;margin-left: 5px;"/>&nbsp;
                    </div>

                </div>

            </div>
        </div>
        <legend>控制开关
            <div class="line_"></div>
        </legend>
        <div class="flex-container">
            <div class="x_line">
                <label class="control-label">网点状态：</label>
                <div class="controls">
                    <select id="statusValue" name="status.value" class="required input-small" style="width:265px;">
                        <c:forEach items="${fns:getDictExceptListFromMS('service_point_status', '')}"
                                   var="dict"><%--切换为微服务--%>
                            <option value="${dict.value}"
                                    <c:out value="${(servicePoint.status.value.toString() == dict.value)?'selected=selected':''}"/>>${dict.label}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>

        <div class="flex-container">
            <div class="x_line">
                <label class="control-label">快可立时效：</label>
                <div class="controls">
                    <c:set value="${servicePoint.timeLinessFlag}" var="t"/>
                    <shiro:hasPermission name="md:servicepoint:timeliness">
                            <span class="<c:out value="${t == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                                  onclick="switchControl('timeLinessFlag')"></span>
                        <input type="hidden" value="${t}" class="timeLinessFlag" name="timeLinessFlag">
                    </shiro:hasPermission>
                        <%--没有权限不能点击--%>
                    <shiro:lacksPermission name="md:servicepoint:timeliness">
                        <c:choose>
                            <c:when test="${servicePoint.timeLinessFlag eq 1}">
                                <span class="switch-on switch-disabled" style="zoom: 0.7"></span>
                                <input type="hidden" value="1" class="timeLinessFlag" name="timeLinessFlag">
                            </c:when>
                            <c:otherwise>
                                <span class="switch-off switch-disabled" style="zoom: 0.7"></span>
                                <input type="hidden" value="0" class="timeLinessFlag" name="timeLinessFlag">
                            </c:otherwise>
                        </c:choose>
                    </shiro:lacksPermission>
                    <span class="help-inline" style="margin-top: -13px;">快可立时效奖励</span>
                </div>
            </div>
            <div class="y_line">
                <label class="control-label">客户时效：</label>
                <div class="controls">
                    <c:set value="${servicePoint.customerTimeLinessFlag}" var="c"/>
                    <span class="<c:out value="${c == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                          onclick="switchControl('customerTimeLinessFlag')"></span>
                    <input type="hidden" value="${c}" class="customerTimeLinessFlag" name="customerTimeLinessFlag">
                    <span class="help-inline" style="margin-top: -13px;">客户时效奖励</span>
                </div>
            </div>
        </div>
        <div class="flex-container">
            <div class="x_line">
                <label class="control-label">互助基金：</label>
                <div class="controls">
                    <c:set value="${servicePoint.insuranceFlag}" var="f"/>
                        <%--                    <span class="<c:out value="${f == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"--%>
                        <%--                          onclick="switchInsuranceFlag(this)"></span>--%>
                    <shiro:hasPermission name="md:servicepoint:insurance">
                            <span class="<c:out value="${f == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                                  onclick="switchInsuranceFlag(this)"></span>
                    </shiro:hasPermission>
                    <shiro:lacksPermission name="md:servicepoint:insurance">
                        <span class="<c:out value="${f == 1 ? 'switch-on' : 'switch-off'}"/> switch-disabled"  style="zoom: 0.7"></span>
                    </shiro:lacksPermission>
                    <input type="hidden" value="${f}" class="insuranceFlag" name="insuranceFlag" id="insuranceFlag">
                    <span class="help-inline" style="margin-top: -13px;width: 200px">完工后扣除,返现网点开启无效</span>
                </div>
            </div>
            <div class="y_line">
                <div class="control-group">
                    <label class="control-label">手机接单：</label>
                    <div class="controls">
                        <c:set value="${servicePoint.appFlag}" var="appFlag"/>
                        <span class="<c:out value="${appFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                              onclick="switchControl('appFlag')"></span>
                        <input type="hidden" value="${appFlag}" class="appFlag" name="appFlag">
                        <span class="help-inline" style="margin-top: -13px;">网点手机接单权限</span>
                    </div>
                </div>
            </div>
        </div>
        <legend>结算信息
            <div class="line_"></div>
        </legend>
        <div class="flex-container">
            <div class="x_line">
                <label class="control-label">结算标准：</label>
                <div class="controls">
                    <c:if test="${servicePoint.id == null}">
                        <select id="useDefaultPrice1" name="useDefaultPrice" class="required input-small"
                                style="width:265px;">
                            <c:forEach items="${fns:getDictInclueListFromMS('PriceType','10,20,30')}" var="dict"><%--切换为微服务--%>
                                <option value="${dict.value}"
                                        <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}"/>>${dict.label}</option>
                            </c:forEach>
                        </select>
                    </c:if>
                    <c:if test="${servicePoint.id != null}">
                        <shiro:lacksPermission name="md:servicepoint:defaultpriceedit">
                            <select id="useDefaultPrice1" name="useDefaultPrice" disabled="disabled"
                                    class="required input-small" style="width:265px;">
                                <c:forEach items="${fns:getDictInclueListFromMS('PriceType','10,20,30')}" var="dict"><%--切换为微服务--%>
                                    <option value="${dict.value}"
                                            <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}"/>>${dict.label}</option>
                                </c:forEach>
                            </select>
                        </shiro:lacksPermission>
                        <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
                            <select id="useDefaultPrice1" name="useDefaultPrice" class="required input-small"
                                    style="width:265px;">
                                <c:forEach items="${fns:getDictInclueListFromMS('PriceType','10,20,30')}" var="dict"><%--切换为微服务--%>
                                    <option value="${dict.value}"
                                            <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}"/>>${dict.label}</option>
                                </c:forEach>
                            </select>
                        </shiro:hasPermission>
                    </c:if>
                </div>
            </div>

            <div class="y_line">
                <label class="control-label">服务价格属性：</label>
                <div class="controls">
                    <c:set var ="canntEditCustomizePriceFlag" value="true"></c:set>
                    <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
                        <c:set var ="canntEditCustomizePriceFlag" value="false"></c:set>
                    </shiro:hasPermission>
                    <input id="standardPrice" name="customizePriceFlag" type="radio" <c:out value="${canntEditCustomizePriceFlag eq true?'disabled':''}" />
                        <c:out value="${servicePoint.customizePriceFlag == 0?'checked':''}"/> value="0"
                           class="required">
                    <label for="standardPrice">标准价</label>&nbsp;&nbsp;
                    <input id="customPrice" name="customizePriceFlag" type="radio" <c:out value="${canntEditCustomizePriceFlag eq true?'disabled':''}" />
                        <c:out value="${servicePoint.customizePriceFlag == 1?'checked':''}"/> value="1"
                           class="required">
                    <label for="customPrice">自定义</label>
                </div>
            </div>

        </div>

        <div class="flex-container">
            <div class="x_line">
                <label class="control-label">偏远价格：</label>
                <div class="controls">
                    <c:set value="${servicePoint.remotePriceEnabledFlag}" var="remotePriceEnabledFlag"/>
                    <c:if test="${servicePoint.id == null}">
                        <span class="<c:out value="${remotePriceEnabledFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                              onclick="switchRemotePriceEnabledFlag()"></span>
                        <input type="hidden" value="${remotePriceEnabledFlag}" class="remotePriceEnabledFlag" name="remotePriceEnabledFlag" id="remotePriceEnabledFlag">
                    </c:if>
                    <c:if test="${servicePoint.id != null}">
                        <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
                                    <span class="<c:out value="${remotePriceEnabledFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                                          onclick="switchRemotePriceEnabledFlag()"></span>
                            <input type="hidden" value="${remotePriceEnabledFlag}" class="remotePriceEnabledFlag" name="remotePriceEnabledFlag" id="remotePriceEnabledFlag">

                        </shiro:hasPermission>
                        <shiro:lacksPermission name="md:servicepoint:defaultpriceedit">
                            <span class="<c:out value="${remotePriceEnabledFlag == 1 ? 'switch-on' : 'switch-off'}"/> switch-disabled" style="zoom: 0.7"></span>
                            <input type="hidden" value="${remotePriceEnabledFlag}" class="remotePriceEnabledFlag" name="remotePriceEnabledFlag" id="remotePriceEnabledFlag">
                        </shiro:lacksPermission>
                    </c:if>
                </div>
            </div>
<%--            <div class="y_line">--%>
<%--                <label class="control-label">偏远价格属性：</label>--%>
<%--                <div class="controls">--%>
<%--                    <shiro:hasPermission name="md:servicepoint:defaultpriceedit">--%>
<%--                        <label><input name="remotePriceFlag" type="radio"--%>
<%--                            <c:out value="${servicePoint.remotePriceFlag == 0?'checked':''}"/> value="0"--%>
<%--                               class="required">--%>
<%--                        标准价</label>&nbsp;&nbsp;--%>
<%--                        <label><input name="remotePriceFlag" type="radio"--%>
<%--                            <c:out value="${servicePoint.remotePriceFlag == 1?'checked':''}"/> value="1"--%>
<%--                               class="required">--%>
<%--                        自定义</label>--%>
<%--                        <select id="remotePriceReference" name="remotePriceReference" class="required input-small">--%>
<%--                            <option value="0" selected="selected">选择参考价格</option>--%>
<%--                            <option value="1">服务价格</option>--%>
<%--                            <option value="2">偏远价格</option>--%>
<%--                        </select>--%>
<%--                    </shiro:hasPermission>--%>
<%--                    <shiro:lacksPermission name="md:servicepoint:defaultpriceedit">--%>
<%--                        <label><input name="remotePriceFlag" type="radio" disabled="true"--%>
<%--                            <c:out value="${servicePoint.remotePriceFlag == 0?'checked':''}"/> value="0"--%>
<%--                               class="required" >--%>
<%--                        标准价</label>&nbsp;&nbsp;--%>
<%--                        <label><input name="remotePriceFlag" type="radio" disabled="true"--%>
<%--                            <c:out value="${servicePoint.remotePriceFlag == 1?'checked':''}"/> value="1"--%>
<%--                               class="required" >--%>
<%--                        自定义</label>--%>
<%--                    </shiro:lacksPermission>--%>
<%--                </div>--%>
<%--                <div style="padding-top:6px;margin-left: 62px;color: #aaaaaa;">--%>
<%--                    价格属性为自定义时选择参考价格作为设置价格的参考数据--%>
<%--                </div>--%>
<%--            </div>--%>
            <div class="y_line">
                <label class="control-label"><span class=" red">*</span>扣&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;点：</label>
                <div class="controls">
                    <c:set value="${servicePoint.finance.discountFlag}" var="discountFlag"/>
                    <span class="<c:out value="${discountFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                          onclick="switchControl('discountFlag')"></span>
                    <input type="hidden" value="${discountFlag}" class="discountFlag" name="finance.discountFlag">
                    <div class="input-append" style="margin-left: 10px;margin-top: -10px">
                        <form:input path="finance.discount" htmlEscape="false" maxlength="7" min="0.0" max="100.0"
                                    class="required number" cssStyle="width: 175px;"/>
                        <span class="add-on">%</span>
                    </div>
                </div>
            </div>

        </div>
        <div class="flex-container">
            <div class="x_line">
                <label class="control-label">重置价格：</label>
                <div class="controls" style="margin-top: 3px">
                    <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
                         <span>
                             <label><input id="resetPrice_1" name="resetPrices" style="zoom: 1.4;" type="checkbox" value="1">服务价格</label>
                         </span>
<%--                        <span>--%>
<%--                            <label> <input id="resetPrice_2" name="resetPrices" style="zoom: 1.4;" type="checkbox" value="2">偏远价格</label>--%>
<%--                         </span>--%>
                    </shiro:hasPermission>
                    <shiro:lacksPermission name="md:servicepoint:defaultpriceedit">
                         <span>
                             <label><input id="resetPrice_1" name="resetPrices" disabled="disabled" style="zoom: 1.4;" type="checkbox" value="1">服务价格</label>
                         </span>
<%--                        <span>--%>
<%--                            <label> <input id="resetPrice_2" name="resetPrices" disabled="disabled" style="zoom: 1.4;" type="checkbox" value="2">偏远价格</label>--%>
<%--                         </span>--%>
                    </shiro:lacksPermission>
                    <input type="hidden" name="resetPrice" id="resetPrice" value="0">
                </div>
                <div style="padding-top:6px;margin-left: 89px;color: #aaaaaa;width: 350px;">
                    若网点价格属性为自定义，重置后原价格数据清空
                </div>
            </div>

        </div>

    </div>

    <div id="editBtn">
        <shiro:hasPermission name="md:servicepoint:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" onclick="$('#inputForm').submit()"
                   style="margin-top: 10px;width: 85px;height: 35px;"/>&nbsp;
        </shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()"
               style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
    </div>
</form:form>
</div>
<script type="text/javascript">
    function cancel() {
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        parent.layer.close(index);
    }

    function checkMobile() {
        $.ajax({
            type: "post",
            url: '${ctx}/sys/user/checkMobile',
            async: false,
            data: {
                mobile: $("[id='primary.contactInfo']").val(),
                expectType: "engineer",
                expectId: '${servicePoint.primary.id}'
            },
            dataType: "html",
            success: function (data, type) {
                return data == "true" ? true : false;
            }
        });
    }

    $(function () {
        <c:if test="${servicePoint.id == null}">
        $("#servicePointNo").focus();

        jQuery.validator.addMethod("checkEngineerMobile", function (value, element) {
            return checkMobile();
        }, '手机号码已被注册');
        </c:if>

        <c:if test="${servicePoint.id ne null}">
        <shiro:lacksPermission name="md:servicepoint:statuspaused">
        $("#statusValue option[value=20]").attr("disabled", "disabled");
        </shiro:lacksPermission>
        <shiro:lacksPermission name="md:servicepoint:statusblacklist">
        $("#statusValue option[value=100]").attr("disabled", "disabled");
        </shiro:lacksPermission>
        </c:if>


        $("[name='useDefaultPrice']:not(:hidden)").change(function () {
            $("[id='useDefaultPrice']").val($(this).val());
        });
    });
    $("#useDefaultPrice").val($("#useDefaultPrice1").val());
    <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
        editRemotePriceEnabledFlag()
    </shiro:hasPermission>
</script>
</body>
</html>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点权限设定管理</title>
    <meta name="decorator" content="default"/>
    <c:set var="user" value="${fns:getUser()}" />
    <script type="text/javascript">
        $(document).ready(function () {
            top.layer.closeAll();

            $("[id^='level.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if(optionSelected.val()=="0"){
                    $("[id^='level.label']").val("");
                }else {
                    $("[id^='level.label']").val(optionSelected.text());
                }
            });

            $("[id^='finance.paymentType.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if(optionSelected.val()=="0"){
                    $("[id^='finance.paymentType.label']").val("");
                }else {
                    $("[id^='finance.paymentType.label']").val(optionSelected.text());
                }
            });


            $("#btnSubmit").click(function () {
                var $btnSubmit = $("#btnSubmit");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                var appFlag = $("[name='appFlag']:checked").val();
                var primaryAppFlag = $("[name='primary.appFlag']:checked").val();

                if (appFlag ==0 && primaryAppFlag !=0) {
                    layerError("请注意，网点允许手机接单,主账号才能允许手机接单.", "错误提示");
                    return false;
                }

                $("#btnSubmit").prop("disabled",true);
                if (!$("#inputForm").valid()) {
                    $("#btnSubmit").prop("disabled",false);
                    return false;
                }
                //$btnSubmit.attr("disabled", "disabled");
                $("#inputForm").attr("action", "${ctx}/md/servicepoint/psSave");
                $("#inputForm").submit();
            });

            $("#inputForm").validate({
                rules: {
                    servicePointNo: {remote: "${ctx}/md/servicepoint/checkNo?id=${servicePoint.id}"},
                    contactInfo1: {remote: "${ctx}/md/servicepoint/checkContact?id=${servicePoint.id}"},
                    "primary.contactInfo": {remote: "${ctx}/md/servicepoint/checkEngineerMobile?id=${servicePoint.primary.id}"},
                    "finance.bankNo": {remote: "${ctx}/md/servicepoint/checkBankNo?id=${servicePoint.id}"}
//                    "primary.contactInfo":{
//                        checkEngineerMobile:true
//                    }
                },
                messages: {
                    servicePointNo: {remote: "服务网点编号已存在"},
                    contactInfo1: {remote: "服务网点编号已存在"},
                    "primary.contactInfo": {remote:"手机号已注册"},
                    "finance.bankNo": {remote: "服务网点银行卡号已存在"}
                },
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                submitHandler: function (form) {
                    layerLoading('正在提交，请稍等...',true);
                    form.submit();
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
                }
            });
        });

        function showContactInfo() {
            top.layer.open({
                type: 2,
                id:'layer_searchServicePointContactInfo',
                zIndex:19891015,
                title:'手机号查询',
                content: "${ctx}/md/servicepoint/findUserListByContactInfo",
                area: ['800px', '640px'],
                shade: 0.3,
                shadeClose:true,
                maxmin: false,
                success: function(layero,index){
                }
            });
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/md/servicepoint">服务网点列表</a></li>
    <li class="active">
        <a href="javascript:;">服务网点<shiro:hasPermission
            name="md:servicepoint:edit">${not empty servicePoint.id?'修改':'添加'}</shiro:hasPermission>
            <shiro:lacksPermission name="md:servicepoint:edit">查看</shiro:lacksPermission>
        </a>
    </li>
</ul>
<br/>
<sys:message content="${message}"/>
<form:form id="inputForm" modelAttribute="servicePoint" action="${ctx}/md/servicepoint/psSave" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="delFlag"/>
    <form:hidden path="address" />
    <form:hidden path="useDefaultPrice" />
    <form:hidden path="forTmall"/>
    <legend>基本信息</legend>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">网点编号:</label>
                <div class="controls">
                    <form:input path="servicePointNo" htmlEscape="false" maxlength="20" class="required" readonly="true"/>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">网点名称:</label>
                <div class="controls">
                    <form:input path="name" htmlEscape="false" maxlength="50" class="required" readonly="true"/>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">手机:</label>
                <div class="controls">
                    <form:input path="contactInfo1" htmlEscape="false" maxlength="11" class="required mobile"/>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
    </div>
    <legend>主帐号</legend>
    <form:hidden path="primary.id" />
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">姓名:</label>
                <div class="controls">
                    <form:input path="primary.name" htmlEscape="false" maxlength="20" class="required"  readonly="true"/>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">手机:</label>
                <div class="controls">
                    <%--<form:input path="primary.contactInfo" htmlEscape="false" readonly="${!empty servicePoint.primary.id?'true':'false'}" maxlength="11" class="required mobile"/>--%>
                    <form:input path="primary.contactInfo" htmlEscape="false" maxlength="11" class="required mobile"/>
                    <span class=" red">*</span>
                    <a class="btn btn-primary" href="javascript:showContactInfo()"/>手机号查询</a>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">等级:</label>
                <div class="controls">
                    <form:select path="primary.level.value" class="input-small required" cssStyle="width: 220px;">
                        <form:option value="" label="请选择"/>
                        <form:options items="${fns:getDictInclueListFromMS('ServicePointLevel','1,2,3,4,5')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                    </form:select>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">允许手机接单:</label>
                <div class="controls">
                    <form:radiobuttons path="primary.appFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/><%--切换为微服务--%>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
    </div>
    <legend>控制开关</legend>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">状态:</label>
                <div class="controls">
                    <select id="statusValue" name="status.value" class="required input-small" style="width:220px;">
                        <c:forEach items="${fns:getDictExceptListFromMS('service_point_status', '')}" var="dict"><%--切换为微服务--%>
                            <option value="${dict.value}"
                                    <c:out value="${(servicePoint.status.value.toString() == dict.value)?'selected=selected':''}" />>${dict.label}</option>
                        </c:forEach>
                    </select>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">购买保险</label>
                <div class="controls">
                    <shiro:hasPermission name="md:servicepoint:insurance">
                        <form:radiobutton path="insuranceFlag" value="1"></form:radiobutton>
                        购买
                        <form:radiobutton path="insuranceFlag" value="0"></form:radiobutton>
                        不购买
                    </shiro:hasPermission>
                    <shiro:lacksPermission name="md:servicepoint:insurance">
                        <c:choose>
                            <c:when test="${servicePoint.insuranceFlag eq 1}">
                                <form:radiobutton path="insuranceFlag" value="1"></form:radiobutton>
                                购买
                            </c:when>
                            <c:otherwise>
                                <form:radiobutton path="insuranceFlag" value="0"></form:radiobutton>
                                不购买
                            </c:otherwise>
                        </c:choose>
                    </shiro:lacksPermission>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                    <%--<label class="control-label">时效奖励(快可立)</label>--%>
                <label class="control-label">快可立补贴</label>
                <div class="controls">
                    <shiro:hasPermission name="md:servicepoint:timeliness">
                        <form:radiobutton path="timeLinessFlag" value="1"></form:radiobutton>
                        开启
                        <form:radiobutton path="timeLinessFlag" value="0"></form:radiobutton>
                        关闭
                    </shiro:hasPermission>
                    <shiro:lacksPermission name="md:servicepoint:timeliness">
                        <c:choose>
                            <c:when test="${servicePoint.timeLinessFlag eq 1}">
                                <form:radiobutton path="timeLinessFlag" value="1"></form:radiobutton>
                                开启
                            </c:when>
                            <c:otherwise>
                                <form:radiobutton path="timeLinessFlag" value="0"></form:radiobutton>
                                关闭
                            </c:otherwise>
                        </c:choose>
                    </shiro:lacksPermission>
                        <%--<form:radiobuttons path="timeLinessFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>--%>
                </div>
            </div>
        </div>
        <%--
        <div class="span6">
            <div class="control-group">
                <label class="control-label">自动派单</label>
                <div class="controls">
                    <form:radiobutton path="autoPlanFlag" value="1" disabled="true"></form:radiobutton>
                    开启
                    <form:radiobutton path="autoPlanFlag" value="0" disabled="true"></form:radiobutton>
                    关闭
                </div>
            </div>
        </div>
        --%>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">客户时效</label>
                <div class="controls">
                    <form:radiobutton path="customerTimeLinessFlag" value="1" ></form:radiobutton>
                    开启
                    <form:radiobutton path="customerTimeLinessFlag" value="0" ></form:radiobutton>
                    关闭
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">允许手机接单:</label>
                <div class="controls">
                    <form:radiobuttons path="appFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
    </div>
    <legend>结算信息</legend>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">结算标准:</label>
                <div class="controls">
                    <c:if test="${servicePoint.id == null}">
                        <select id="useDefaultPrice1" name="useDefaultPrice" class="required input-small" style="width:220px;">
                            <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict"><%--切换为微服务--%>
                                <option value="${dict.value}"
                                        <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}" />>${dict.label}</option>
                            </c:forEach>
                        </select>
                    </c:if>
                    <c:if test="${servicePoint.id != null}">
                        <shiro:lacksPermission name="md:servicepoint:defaultpriceedit">
                            <select id="useDefaultPrice1" name="useDefaultPrice" disabled="disabled" class="required input-small" style="width:220px;">
                                <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict"><%--切换为微服务--%>
                                    <option value="${dict.value}"
                                            <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}" />>${dict.label}</option>
                                </c:forEach>
                            </select>
                        </shiro:lacksPermission>
                        <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
                            <select id="useDefaultPrice1" name="useDefaultPrice" class="required input-small" style="width:220px;">
                                <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict"><%--切换为微服务--%>
                                    <option value="${dict.value}"
                                            <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}" />>${dict.label}</option>
                                </c:forEach>
                            </select>
                        </shiro:hasPermission>
                    </c:if>
                </div>
            </div>
        </div>
        <div class="span6">
            <c:if test="${servicePoint.id == null}">
                <div class="control-group">
                    <label class="control-label">是否重置价格:</label>
                    <div class="controls">
                        <form:radiobutton path="resetPrice" label="是" value="1"/>
                            <%--<form:radiobuttons path="resetPrice" readonly="readonly" items="${fns:getDictList('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false"/>--%>
                    </div>
                </div>
            </c:if>
            <c:if test="${servicePoint.id != null}">
                <div class="control-group">
                    <label class="control-label">是否重置价格:</label>
                    <div class="controls">
                        <shiro:lacksPermission name="md:servicepoint:defaultpriceedit">
                            <form:radiobutton path="resetPrice" label="否" value="0"/>
                        </shiro:lacksPermission>
                        <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
                            <form:radiobuttons path="resetPrice" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                        </shiro:hasPermission>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">是否扣点:</label>
                <div class="controls">
                    <form:radiobuttons path="finance.discountFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">扣点:</label>
                <div class="controls">
                    <form:input path="finance.discount" htmlEscape="false" maxlength="7" min="0.0" max="100.0" class="required number" />
                    <span id="span_bankOwner" class=" red"></span>
                    <span class="help-inline">格式为：小数，如0.01代表百分之一</span>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">价格类型:</label>
                <div class="controls">
                    <input id="standardPrice" name = "customizePriceFlag" type="radio" <c:out value="${servicePoint.customizePriceFlag == 0?'checked':''}"/> value="0" class="required">
                    <label for="standardPrice">参考价格</label>&nbsp;&nbsp;
                    <input id="customPrice" name = "customizePriceFlag" type="radio" <c:out value="${servicePoint.customizePriceFlag == 1?'checked':''}"/> value="1" class="required">
                    <label for="customPrice">自定义价格</label>
                    <span class=" red">*</span>
                </div>
            </div>
        </div>
    </div>
    <legend>等级及评价</legend>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">等级:</label>
                <div class="controls">
                    <form:select path="level.value" class="input-small required" cssStyle="width: 220px;">
                        <form:option value="" label="请选择"/>
                        <form:options items="${fns:getDictExceptListFromMS('ServicePointLevel', '6,7,8')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                    </form:select>
                    <span class=" red">*</span>
                </div>
                <form:hidden path="level.label" htmlEscape="false" />
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">分级:</label>
                <div class="controls">
                    <c:forEach items="${fns:getDictListFromMS('degreeType')}" var="dict">
                        <input id="degree${dict.value}" name = "degree" type="radio" <c:out value="${servicePoint.degree == dict.value?'checked':''}"/> value="${dict.value}">
                        <label for="degree${dict.value}">${dict.label}</label>&nbsp;&nbsp;
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>

    <div class="form-actions">
        <shiro:hasPermission name="md:servicepoint:edit">
            <input id="btnSubmit" class="btn btn-primary" type="button"
                   value="保 存"/>&nbsp;</shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="返 回"
               onclick="history.go(-1)"/>
        <%--
        <c:if test="${servicePoint.id != null && servicePoint.delFlag >=2 }"><input
                style="margin-left: 20px" id="btnApprove" class="btn btn-primary" type="button" value="保存并通过审核"/></c:if>
        --%>
    </div>
</form:form>
<script type="text/javascript">
    function checkMobile(){
        $.ajax({
            type:"post",
            url: '${ctx}/sys/user/checkMobile',
            async:false,
            data:{
                mobile:$("[id='primary.contactInfo']").val(),
                expectType:"engineer",
                expectId: '${servicePoint.primary.id}'
            },
            dataType: "html",
            success: function(data, type) {
                return data == "true"? true : false;
            }
        });
    }
    $(function(){
        <c:if test="${servicePoint.id == null}">
            $("#servicePointNo").focus();

            jQuery.validator.addMethod("checkEngineerMobile", function(value, element) {
                return checkMobile();
            },'手机号码已被注册');
    //        $("[id='primary.contactInfo']").rules("add",{checkEngineerMobile:true});
        </c:if>

        <c:if test="${servicePoint.id ne null}">
            <shiro:lacksPermission name="md:servicepoint:statuspaused">
                $("#statusValue option[value=20]").attr("disabled","disabled");
            </shiro:lacksPermission>
            <shiro:lacksPermission name="md:servicepoint:statusblacklist">
                $("#statusValue option[value=100]").attr("disabled","disabled");
            </shiro:lacksPermission>
        </c:if>


        $("[name='useDefaultPrice']:not(:hidden)").change(function(){
            $("[id='useDefaultPrice']").val($(this).val());
        });
    });
    $("#useDefaultPrice").val($("#useDefaultPrice1").val());
</script>
</body>
</html>
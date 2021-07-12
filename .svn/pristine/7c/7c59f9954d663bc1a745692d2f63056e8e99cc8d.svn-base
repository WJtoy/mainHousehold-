<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%--<script src="${ctxStatic}/layui/layui.js"></script>--%>
    <%--<link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">--%>
    <%--<script src="${ctxStatic}/jquery/jquery-1.9.1.min.js" type="text/javascript"></script>--%>
    <%--<script src="${ctxStatic}/jquery/jquery.form.min.js" type="text/javascript"></script>--%>
    <%--<script src="${ctxStatic}/jquery/jquery-migrate-1.1.1.min.js" type="text/javascript"></script>--%>
    <%--<link href="${ctxStatic}/jquery-validation/1.11.0/jquery.validate.min.css" type="text/css" rel="stylesheet"/>--%>
    <%--<script src="${ctxStatic}/jquery-validation/1.11.0/jquery.validate.min.js?_v=${OrderJsVersion}" type="text/javascript"></script>--%>
    <%--<script src="${ctxStatic}/jquery-validation/1.11.0/localization/messages_zh.js" type="text/javascript"></script>--%>
    <%--<link href="${ctxStatic}/bootstrap/2.3.1/css_${not empty cookie.theme.value ? cookie.theme.value : 'cerulean'}/bootstrap.min.css"--%>
          <%--type="text/css" rel="stylesheet"/>--%>
    <%--<script src="${ctxStatic}/bootstrap/2.3.1/js/bootstrap.min.js" type="text/javascript"></script>--%>
    <%--<link href="${ctxStatic}/bootstrap/2.3.1/awesome/font-awesome.min.css" type="text/css" rel="stylesheet"/>--%>
    <%--<link href="${ctxStatic}/bootstrap/2.3.1/awesome/font-awesome-ie7.min.css" type="text/css" rel="stylesheet" /><![endif]-->--%>
    <%--<script src="${ctxStatic}/bootstrap/bsie/js/bootstrap-ie.min.js" type="text/javascript"></script>--%>
    <%--<script src="/static/common/html5.js"></script>--%>
    <%--<script src="${ctxStatic}/common/mustache.min.js" type="text/javascript"></script>--%>
    <%--<link href="${ctxStatic}/common/jeesite.min.css?_v=${OrderJsVersion}" type="text/css" rel="stylesheet"/>--%>
    <%--<script src="${ctxStatic}/common/jeesite.min.js?_v=${OrderJsVersion}" type="text/javascript"></script>--%>
    <%--<script src="${ctxStatic}/common/Utils.js?_v=${OrderJsVersion}" type="text/javascript"></script>--%>
    <%--<script src="${ctxStatic}/common/dateformat.min.js" type="text/javascript"></script>--%>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>师傅地址管理</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/jquery-upload-file/js/ajaxfileupload.js"></script>

    <c:set var="currentuser" value="${fns:getUser() }" />
    <style type="text/css">
        .line-row {
            margin-left: -90px;
        }
        .line-address {
            color: rgba(102, 102, 102, 1);
            font-size: 14px;
            font-family: Roboto;
            border: 1px dashed #ccc;
            margin: 0 0 15px 90px;
            line-height: 28px;
            margin-top: -59px;
            padding: 15px;
            border-radius: 5px;
            background: #F6F6F6 100%;
        }
        .hide_input{
            width: 70%;
            border: 0px;
            background: #F6F6F6 100%;
            color: #555;
        }
        .prohibit{
            pointer-events: none;
        }
        #editBtn{
            position: fixed;
            left: 0px;
            bottom: 5px;
            width: 100%;
            height: 55px;
            background: #fff;
            z-index: 1;
            padding-left: 190px;
            border-top: 1px solid #ccc;
            border-top: 1px solid #e5e5e5;
        }
        .x{
            width: 45%;
            float: left;
        }
        .row-filed{
            width: 100%;
            margin-left: -21px;
        }
    </style>
    <script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';

        var areaName;
        var address;
        var areaId;
        function child(obj) {
                var data = eval(obj);
                areaId = data.areaId;// 个人地址id
                areaName = data.areaName;// 个人地址
                address = data.address;// 个人详细地址
                // var addressFlag = data.addressFlag;// 选择标识
                // var addressFlag = $("#addressFlag").val();
                if ($("#servicePointId").val() == '') {
                    $("#servicePointId").val(data.servicePointId);
                }
                $("#engineerId").val(data.engineerId);

                $("#parent_name").val(data.name);
                $("#parent_contactInfo").val(data.contactInfo);

                $("#subAddress").attr("value", data.subAddress);
                $("#serviceFullAddress").attr("value", data.serviceFullAddress);// 网点详细地址
                $("#servicePoint_address_fullName").attr("value", data.servicePointAddressFullName);// 网点区域全称如xx省xx市xx区
                $("#servicePoint_address_id").attr("value", data.servicePointAddressId);// 网点地址id


                $("#anwei_address").attr("value","" + (areaName+address) + "");// 拼装个人地址
                if (data.serviceFullAddress != null && data.serviceFullAddress != '') {
                    $("#servicePoint_address").attr("value", "" + data.serviceFullAddress + "");// 拼装网点地址
                }

            if (${engineerAddress.id == null}) {
                $("#e_userName").attr("value", $("#parent_name").val());
                $("#e_contactInfo").attr("value", $("#parent_contactInfo").val());
            }
        }
        // layui.use(['form','element'], function() {
        //     var form = layui.form,
        //         $ = layui.$,
        //         element = layui.element;

            // layui监管收件单选
            // form.on("radio(addressFlag)", function (data) {
        $(document).ready(function () {
            $('input[type=radio][name="addressFlag"]').change(function (){
                var addressFlag = $("input[name='addressFlag']:checked").val();// 3：自定义 1：网点地址 2：安维地址
                var fullAddress = $("#fullAddress").val();

                // 自定义
                if (addressFlag == 3) {
                    $("#engineerAddressButton").removeClass("prohibit");
                    $("#engineerAddress").attr("readOnly",false);
                }
                // 网点地址
                if (addressFlag == 1) {
                    var serviceFullAddress = $("#serviceFullAddress").val();
                    var subAddress = $("#subAddress").val();
                    if (serviceFullAddress != "") {// 详细地址不为空
                        // $("#engineerAddress").attr("value", serviceFullAddress);
                        $("#engineerAddress").attr("value", subAddress);
                    }
                    $("#engineerAddressName").attr("value", $("#servicePoint_address_fullName").val());// xx省xx市xx区
                    $("#engineerAddressId").attr("value", $("#servicePoint_address_id").val())

                    $("#engineerAddressButton").addClass("prohibit");
                    $("#engineerAddress").attr("readOnly","true");
                }
                // 个人地址
                if (addressFlag == 2) {
                    if (areaName != '' && address != '') {// 如果区域和详细地址都不为空
                        $("#engineerAddressName").attr("value", areaName);
                        $("#engineerAddressId").attr("value", areaId);
                        $("#engineerAddress").attr("value", address);
                        $("#engineerAddressButton").addClass("prohibit");
                        $("#engineerAddress").attr("readOnly","true");
                    } else {
                        setTimeout(function(){
                            $("input[type='radio'][name='addressFlag']:eq(2)").prop("checked",true);
                            $("#engineerAddressButton").removeClass("prohibit");
                            $("#engineerAddress").attr("readOnly",false);
                            // form.render();
                        },1000);
                        layerError("您未填写安维地址","错误提示");
                        return false;
                    }
                }
            });
        //     form.render();
        // });
        });

        var clickTag = 0;
        $(document).ready(function () {
            var $btnSubmit = $("#btnSubmit");
            $("#inputFormAddress").validate({
                highlight : function(element) {
                    $(element).closest('.control-group').addClass('has-error');
                },
                success : function(label) {
                    label.closest('.form-group').removeClass('has-error');
                    label.remove();
                },
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $btnSubmit.removeAttr('disabled');
                    if(element.context.name == 'area.fullName' ){
                        element.parent('div').parent('div').append(error);
                    }else if (element.context.name == 'engineerAddressArea.fullName'){
                        element.parent('div').parent('div').append(error);
                    }else{
                        element.parent('div').append(error);
                    }

                },
                submitHandler: function (form) {
                    var servicePoint = $("#servicePointId").val();
                    if(servicePoint == '' || servicePoint == null){
                        layerError("数据丢失，请稍候重试", "错误提示");
                        return false;
                    }

                    var areaId = $("#engineerAddressId").val();
                    if (areaId == '') {
                        layerError("请选择完整的省,市,区(县)", "错误提示");
                        return false;
                    }

                    var area = $("#engineerAddressName").val();
                    if (area != '') {
                        $("#areaName").attr("value", area);
                    }

                    submit(form);
                },
            });

            function submit(form){
                $btnSubmit.attr("disabled",true);
                // layui.use('layer', function(){
                //     var layer = layui.layer;
                var addressFlag = $("input[name='addressFlag']:checked").val();// 1：网点地址 2：个人地址 3：自定义
                var id = $("#id").val();
                if(id == null || id == '' || id == undefined){
                    if(parentIndex && parentIndex != undefined && parentIndex != ''){
                        var layero = $("#layui-layer" + parentIndex,top.document);
                        var iframeWin = top[layero.find('iframe')[0]['name']];
                        iframeWin.refreshAddress($("#engineerAddressId").val(),$("#e_userName").val(), $("#e_contactInfo").val(), ($("#engineerAddressName").val() + $("#engineerAddress").val()), $("#engineerAddressName").val(),$("#engineerAddress").val(),addressFlag);
                        var index = parent.layer.getFrameIndex(window.name);
                        parent.layer.close(index);//关闭当前页
                        return false;
                    }
                }
                var loadingIndex;
                var options = {
                    url: "${ctx}/md/engineerAddress/saveEngineerAddress",
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
                                // cancel();
                                var index = parent.layer.getFrameIndex(window.name);
                                parent.layer.close(index);//关闭当前页
                            }, 2000);
                            // 回调父窗口方法
                            if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                var layero = $("#layui-layer" + parentIndex,top.document);
                                var iframeWin = top[layero.find('iframe')[0]['name']];
                                iframeWin.refreshAddress(data.data ,$("#e_userName").val(), $("#e_contactInfo").val(), ($("#engineerAddressName").val() + $("#engineerAddress").val()), $("#engineerAddressName").val(),$("#engineerAddress").val(),addressFlag);
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
                $("#inputFormAddress").ajaxSubmit(options);
            // });
            }
        });

    </script>
</head>
<body>
<input type="hidden" id="fullAddress">
<input type="hidden" id="serviceFullAddress">
<input type="hidden" id="servicePoint_address_id" value="">
<input type="hidden" id="servicePoint_address_fullName" value="">
<input type="hidden" id="e_addressFlag">
<input type="hidden" id="parent_name">
<input type="hidden" id="parent_contactInfo">
<input type="hidden" id="subAddress">
<sys:message content="${message}"/>

<form:form id="inputFormAddress" modelAttribute="engineerAddress" action="" method="post" class="form-horizontal" style="margin-top: 25px;height: auto;overflow: hidden;">
    <form:hidden path="id"/>
    <form:hidden path="engineerId"/>
    <form:hidden path="servicePointId"/>
    <form:hidden path="areaName"/>
    <div>
        <%--<div class="layui-form" style="margin-top: 15px;">--%>
        <div class="" style="margin-top: 15px;">
            <input style="display: none" id="anwei_address_id" value="">

            <div class="sj-content" style="margin-top: 60px;">

                <div class="line-row">
                        <div style="padding: 28px;">
                        <div class="line-address">
                            <div id="second_servicePoint" class="receipt">
                                <input type="radio" name="addressFlag" value="1">网点地址
                                <input value="" id="servicePoint_address" class="hide_input" style="text-overflow: ellipsis;">
                            </div>
                            <div id="second" class="receipt">
                                <input type="radio" name="addressFlag" value="2">师傅地址
                                <input value="" id="anwei_address" class="hide_input" style="text-overflow: ellipsis;">
                            </div>

                            <div class="receipt">
                                <input type="radio" name="addressFlag" value="3">自定义
                            </div>
                        </div>
                        </div>

                    <div style="margin-left: 18px;margin-top: -20px;">
                            <div class="control-group x">
                                <label class="control-label"><span class=" red">*</span>姓&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;名:</label>
                                <div class="controls">
                                    <c:choose>
                                        <c:when test="${currentuser.isSystemUser() or currentuser.isSaleman()}">
                                            <form:input id="e_userName" path="userName" htmlEscape="false" maxlength="20" class="required" style="width:245px"/>
                                        </c:when>
                                        <c:otherwise>
                                            <form:input id="e_userName" path="userName" readonly="${empty engineer.id?'false':'true'}" maxlength="20" htmlEscape="false" class="required"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="control-group x" style="width: 0%;float: right;margin-right: 433px;">
                                <label class="control-label"><span class=" red">*</span>联系电话:</label>
                                <div class="controls" style="width: 240px">
                                    <c:choose>
                                        <c:when test="${currentuser.isSystemUser() or currentuser.isSaleman()}">
                                            <form:input id="e_contactInfo" path="contactInfo" htmlEscape="false" maxlength="11" class="required mobile" style="width:211px"/>
                                        </c:when>
                                        <c:otherwise>
                                            <form:input id="e_contactInfo" path="contactInfo" readonly="${empty engineer.id?'false':'true'}" maxlength="11" htmlEscape="false" class="input-y required mobile" style="width:211px"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="row-filed">
                                <div class="span4" style="width: auto;max-width:440px">
                                    <div class="control-group">
                                        <label class="control-label"><span class=" red">*</span>收货地址:</label>
                                        <div class="controls" style="width: auto;max-width:440px" >
                                            <sys:areaselect name="areaId" id="engineerAddress" value="${engineerAddress.areaId}"
                                                            labelValue="${engineerAddressArea.fullName}" labelName="engineerAddressArea.fullName" title=""
                                                            mustSelectCounty="true" cssClass="required">
                                            </sys:areaselect>
                                        </div>
                                    </div>
                                </div>
                                <div class="span7" style="margin-left:5px;width:40%">
                                    <div class="control-group">
                                        <div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
                                            <form:input path="address" id="engineerAddress" htmlEscape="false" maxlength="100" style="width:446px;" placeholder="详细地址，如XX大厦1层101室"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                    </div>
                </div>
            </div>

            <div id="editBtn">
                <shiro:hasPermission name="md:engineer:edit">
                    <input id="btnSubmit" class="btn btn-primary" type="button" value="保 存" onclick="$('#inputFormAddress').submit()" style="margin-left: 515px;margin-top: 10px;width: 85px;height: 37px;background: #0096DA;border-radius: 4px;"/>&nbsp;</shiro:hasPermission>
                <input id="btnCancel" class="btn" type="button" value="取 消" style="margin-top:10px;width: 85px;height: 37px;border-radius: 4px;"onclick="cancel()"/>
            </div>
        </div>
    </div>

</form:form>
<script class="removedscript" type="text/javascript">
    var this_index = top.layer.index;

    // 关闭页面
    function cancel() {
        top.layer.close(this_index);// 关闭本身
    }

    // 初始化
    $(document).ready(function() {
        if (${engineerAddress != null && engineerAddress.id != 0}) {
            // layui.use('form', function() {
            //     var form = layui.form,
            //         $ = layui.$;
                var addressFlag = ${engineerAddress.addressFlag == null ? 3 : engineerAddress.addressFlag};
                $("#engineerAddressFlag").attr("value", addressFlag);
                if (addressFlag == 1) {// 使用网点
                    $("#engineerAddressButton").addClass("prohibit");
                    $("#engineerAddress").attr("readOnly","true");
                }
                if (addressFlag == 2) {// 使用个人
                    $("#engineerAddressButton").addClass("prohibit");
                    $("#engineerAddress").attr("readOnly","true");
                }
                $("input[type='radio'][name='addressFlag'][value="+addressFlag+"]").attr("checked","checked");
            //     form.render();
            // });
        }
    });

</script>
</body>
</html>
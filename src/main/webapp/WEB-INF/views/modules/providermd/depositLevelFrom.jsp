<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>添加</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <style type="text/css">
        /*.table thead th, .table tbody td {*/
        /*    text-align: center;*/
        /*    vertical-align: middle;*/
        /*    BackColor: Transparent;*/
        /*    height: 30px;*/
        /*}*/
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0;
            width: 100%;
            height: 60px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #e5e5e5;
        }

        .form-horizontal {margin-top: 5px;}
        .form-horizontal .control-label {width: 80px;}
        .form-horizontal .controls {margin-left: 90px;}

        .row-fluid .span4 {
            width: 45.9149% !important;
        }

        .table tbody td {
            vertical-align: top;
        }

        .maxAmount{
            background-color: #fff;
            border: 1px solid #ccc;
            transition: border linear .2s,box-shadow linear .2s;
            display: inline-block;
            height: 20px;
            padding: 4px 6px;
            margin-bottom: 10px;
            font-size: 14px;
            line-height: 20px;
            color: #555;
            vertical-align: middle;
            border-radius: 4px;
        }


        .minAmount{
            background-color: #fff;
            border: 1px solid #ccc;
            transition: border linear .2s,box-shadow linear .2s;
            display: inline-block;
            height: 20px;
            padding: 4px 6px;
            margin-bottom: 10px;
            font-size: 14px;
            line-height: 20px;
            color: #555;
            vertical-align: middle;
            border-radius: 4px;
        }
        .control-group{border-bottom: 0px}

    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }

        var clickTag = 0;
        $(document).ready(function() {
            var ele = $('.input-xlarge')
            StrNumchange(ele)

            $.validator.addMethod("levelLimit",function(value, element){
                // 获取输入框的值
                var input1 = document.getElementById('inputA');
                var input2 = document.getElementById('inputB');
                // 输入框的值转为Number类型
                var num1 = parseInt(input1.value);
                var num2 = parseInt(input2.value);

                if(num1>num2){
                    $(element).data('error-msg','上限金额必须大于最低金额');
                    return false;
                };
                return true;
            },function(params, element){ return $(element).data('error-msg');});

            // 上线前，最小值调整为:500

            $("#inputForm").validate({
                rules: {
                    name: {
                        remote: {
                            type: "post",
                            url: "${ctx}/provider/md/depositLevel/checkName",
                            data: {
                                loginId: function () {
                                    return $("#id").val();
                                },
                                name: function () {
                                    return $("#name").val();
                                }
                            },
                            dataType: "json",
                            dataFilter: function (data) {
                                var data = eval('(' + data + ')');  //字符串转换成json
                                if (data == "false") {
                                    return false;
                                } else {
                                    return true;
                                }
                            }
                        }
                    },
                    code:{
                     remote: {
                         type: "post",
                         url: "${ctx}/provider/md/depositLevel/checkCode",
                         data: {
                             loginId: function () {
                                 return $("#id").val();
                             },
                             name: function () {
                                 return $("#value").val();
                             }
                         },
                         dataType: "json",
                         dataFilter: function (data) {
                             var data = eval('(' + data + ')');  //字符串转换成json
                             if (data == "false") {
                                 return false;
                             } else {
                                 return true;
                             }
                         }
                     }
                 },
                 maxAmount:{required:true,levelLimit:true},
                 minAmount:{required:true, min: 500},

                 deductPerOrder:{min:0}
                },
                messages: {
                    name: {remote: "该名称已存在,请重新输入"},
                    code:{remote: "该编码已存在,请重新输入"},
                    maxAmount:{required: "请填写上限金额"},
                    minAmount:{required: "请填写最低金额",min: "缴费金额最低500元"},
                    deductPerOrder:{min: "每单扣除必须大于或等于0"}
                },
                submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");

                    if(clickTag == 1){
                        return false;
                    }
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }

                    clickTag = 1;
                    $btnSubmit.prop("disabled", true);

                    $.ajax({
                        url:"${ctx}/provider/md/depositLevel/save",
                        type:"POST",
                        data:$(form).serialize(),
                        dataType:"json",
                        success: function(data){
                            //提交后的回调函数
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                top.layer.close(this_index);//关闭本身
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                top.layer.close(loadingIndex);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            top.layer.close(loadingIndex);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                            top.layer.close(loadingIndex);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                    });
                },
                errorContainer : "#messageBox",
                errorPlacement : function(error, element)
                {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                        error.appendTo(element.parent().parent());
                    } else if(element.parent().is(".depositLevel")){
                        error.insertAfter(element);
                    }else if(element.parent().is(".amount")){
                        var nspan = $(element.parent()).find("p");
                        if(nspan){
                            if(element.attr("name")=="minAmount"){
                                error.insertBefore(nspan);
                            }
                            if(element.attr("name")=="maxAmount"){
                                var inputError = $('#inputA-error').text();
                               if(inputError == null || inputError==""){
                                   error.insertBefore(nspan);
                               }
                            }
                        }

                    }else if(element.parent().is(".sort")){
                        error.insertAfter(element);
                    } else {
                        var nspan = $(element.parent()).find("span");
                        if(nspan){
                            error.insertAfter(nspan);
                        }else{
                            error.insertAfter(element);
                        }
                    }
                }});
        });
    </script>
</head>
<body>
<form:form id="inputForm" modelAttribute="mdDepositLevel" method="post" action="${ctx}/provider/md/depositLevel/save" class="form-horizontal">
    <sys:message content="${message}"/>
    <input id="isMinAmount"  type="hidden" value="0"/>
    <form:hidden path="id"/>
    <div class="row-fluid" style="margin-top: 20px;margin-left: 20px">
        <div class="span4">
            <div class="control-group">
                <label class="control-label"><span style="color: red">*</span>等级编码:</label>
                <div class="controls depositLevel">
                    <form:input path="code" htmlEscape="false" class="required changeA" placeholder="输入英文或数字组合"  oninput="value=value.replace(/[\W]/g,'')" style="width:199px;"/>
                </div>
            </div>
        </div>
        <div class="span4">
            <div class="control-group">
                <label class="control-label"><span style="color: red">*</span>等级名称:</label>
                <div class="controls depositLevel" >
                    <form:input path="name" htmlEscape="false"  class="required " style="width:186px;"/>
                </div>
            </div>
        </div>
    </div>


    <div class="row-fluid" style="margin-top: 10px;margin-left: 20px">
        <div class="span4">
            <div class="control-group">
                <label class="control-label"><span style="color: red">*</span>缴费金额:</label>
                <div class="controls amount">
<%--                    <form:input path="minAmount" htmlEscape="false"  class="required number minAmount" style="width:163px;" min="500"  maxlength="10" placeholder="最低500" oninput="clearNoNum(this)"/><span class="add-on">元</span>--%>
                    <input id="inputA" name="minAmount" htmlEscape="false"  class="required number minAmount" style="width:64px;" min="500"  maxlength="8" placeholder="最低500" onkeyup="value=value.replace(/^(0+)|[^\d]+/g,'')" value="<fmt:formatNumber value='${mdDepositLevel.minAmount}' pattern='#.##' />"/>
                   <%-- <p style="margin-left: -6px;margin-top: 10px" class="add-on">元</p>--%>
                    <span class="add-on" style="margin-left: -6px;border-radius:0px 4px 4px 0px;">元</span>
                    - <input id="inputB" name="maxAmount" htmlEscape="false"  class="required number maxAmount" style="width:65px;" maxlength="8" placeholder="" onkeyup="value=value.replace(/^(0+)|[^\d]+/g,'')" value="<fmt:formatNumber value='${mdDepositLevel.maxAmount}' pattern='#.##' />"/><span class="add-on" style="border-radius:0px 4px 4px 0px;">元</span>
                    <p></p>
                </div>
            </div>
        </div>

        <div class="span4">
            <div class="control-group">
                <label class="control-label" ><span class="red">*</span>每单扣除:</label>
                <div class="controls">
                    <form:input path="deductPerOrder" htmlEscape="false"  class="required number" style="width:163px;" min="0"  maxlength="10" placeholder="每单完工应扣质保金额" onkeyup="clearNoNum(this)" /><span class="add-on">元</span>
<%--                    <input  name="deductPerOrder" htmlEscape="false"  class="required  number maxAmount" style="width:163px;" min="0"  maxlength="10" placeholder="每单完工应扣除质保金额" oninput="clearNoNum(this)" value="<fmt:formatNumber value='${mdDepositLevel.deductPerOrder}' />"/><span class="add-on">元</span>--%>
                </div>
            </div>
        </div>
    </div>


    <div class="row-fluid" style="margin-left: 20px">
        <div class="span4">
            <div class="control-group">
                <label class="control-label"><span style="color: red">*</span>排&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;序:</label>
                <div class="controls sort">
                    <form:input path="sort" htmlEscape="false" maxlength="100" var="" class="required" cssStyle="width:199px;" onKeyUp="this.value=this.value.replace(/\D/g,'')" placeholder="数字越小,排序越靠前"/>
                </div>
            </div>
        </div>
    </div>

    <div class="control-group" style="margin-top:15px;float: left;margin-left: 20px;">
        <label style="float: left;margin-left: 20px;margin-top: 5px">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述：</label>
    <div class="controls" style="margin-left: 90px;position: relative;">
        <form:textarea path="description" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="resize:none;height: 76px ;overflow: hidden;width: 510px;
                height: 118px;margin: 0px;"/>
        <span class="wordsNum" style="text-align: right;width: 73px;display: inline-block;position: absolute;top: 105px;left: 443px;">0/200</span>
    </div>
    </div>
    <div id="editBtn" class="line-row">
        <shiro:hasPermission name="md:customervip:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 96px;height: 40px;margin-left: 67%;margin-top: 10px;margin-bottom: 10px"/>
            &nbsp;</shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 96px;height: 40px;margin-top: 10px;margin-left: 13px;margin-bottom: 10px"/>
    </div>
</form:form>
<script>
    var checkStrLengths = function (str, maxLength) {
        var maxLength = maxLength;
        var result = 0;
        if (str && str.length > maxLength) {
            result = maxLength;
        } else {
            result = str.length;
        }
        return result;
    }

    //监听输入
    $(".input-xlarge").on('input propertychange', function () {
        var Ele = this
        StrNumchange(Ele)
    });
    function StrNumchange(that) {
        //获取输入内容
        var userDesc = $(that).val();

        //判断字数
        var len;
        if (userDesc) {
            len = checkStrLengths(userDesc, 200);
        } else {
            len = 0
        }

        //显示字数
        $(".wordsNum").html(len + '/200');
    }


    function clearNoNum(obj){
        obj.value = obj.value.replace(/[^\d.]/g,"");
        obj.value = obj.value.replace(/^\./g,"");
        obj.value = obj.value.replace(".","$#$").replace(/\./g,"").replace("$#$",".");
        obj.value = obj.value.replace(/^(\-)*(\d+)\.(\d\d).*$/,'$1$2.$3');
        // obj.value = obj.value.replace(/^[\d(0,8)]\.[\d(0,2)]$/,'$1$2.$3');
        if(obj.value.indexOf(".")< 0 && obj.value !=""){//以上已经过滤，此处控制的是如果没有小数点，首位不能为类似于 01、02的金额
            if(obj.value.substr(0,1) == '0' && obj.value.length == 2){
                obj.value= obj.value.substr(1,obj.value.length);
            }
        }
    }
</script>.
</body>
</html>

<%@ taglib prefix="from" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点付款修改</title>
    <meta name="decorator" content="default"/>
    <script type="text/javascript">
        $(document).ready(function () {
            jQuery.validator.addMethod("outOfDate", function(value, element){
                var inputDate = new Date(Date.parse(value.replace("-", "/")));
                var currentDate = new Date();
                var inputDateInt = inputDate.getFullYear()*100+inputDate.getMonth();
                var currentDateInt = currentDate.getFullYear()*100+currentDate.getMonth();
                return inputDate.getFullYear()*100+inputDate.getMonth() == currentDate.getFullYear()*100+currentDate.getMonth();
            }, "付款日期不能跨月");

            $("#inputForm").validate({
                rules:
                    {
                        bankNo:{ required: true},
                        payDate: { outOfDate: true}
                    },
                messages:
                    {
                        bankNo:{ required: "请输入银行帐号！"},
                        payDate: { outOfDate: "付款日期不能跨月"}
                    },
                submitHandler: function(form){
                    $("#btnSubmit").attr("disabled", "disabled");
                    $("#btnCancel").attr("disabled", "disabled");
                    loading('正在提交，请稍等...');
                    var postUrl = "${ctx}/fi/servicepointinvoice/confirm/edit";
                    $.ajax({
                        type: "POST",
                        url: postUrl,
                        data: {withdrawId:$("#id").val(), bank:$("#bank").val(),
                            branch:$("#branch").val(), bankNo:$("#bankNo").val(),
                            bankOwner:$("#bankOwner").val(), payDate:$("#payDate").val()},
                        success: function (data) {
                            if (data.success){
                                top.$.jBox.tip('修改成功', 'success');
                                parent.repage();
                                parent.$.fancybox.close();
                            }
                            else{
                                top.$.jBox.error(data.message);
                            }
                            $("#btnSubmit").removeAttr("disabled");
                            $("#btnCancel").removeAttr("disabled");
                            top.$.jBox.closeTip();
                            return false;
                        },
                        error: function (e) {
                            $("#btnSubmit").removeAttr("disabled");
                            $("#btnCancel").removeAttr("disabled");
                            top.$.jBox.closeTip();
                            top.$.jBox.error("修改错误:网络请求失败,请重试.","错误提示");
                        }
                    });
                    return false;
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
        });

        $(document).off('click','#btnSubmit');//先解除事件绑定
        $(document).on("click", "#btnSubmit", function(){
            if ($("#btnSubmit").prop("disabled") == true) {
                return false;
            }

            $("#btnSubmit").prop("disabled", true);

            $("#inputForm").submit();

            $("#btnSubmit").removeAttr("disabled");
        });

        function closethisfancybox(){
            parent.$.fancybox.close();
        }
    </script>
</head>
<body>
<form:form id="inputForm" modelAttribute="servicePointWithdraw" class="form-horizontal">
    <form:hidden path="id"/>
    <legend>
        网点付款信息修改
    </legend>
    <div class="control-group">
        <label class="control-label">付款银行:</label>
        <div class="controls">
            <form:select path="bank" cssStyle="width: 220px;">
                <form:options items="${fns:getDictListFromMS('banktype')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
            </form:select>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">支行:</label>
        <div class="controls">
            <form:input path="branch" htmlEscape="false" maxlength="25"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">帐号:</label>
        <div class="controls">
            <form:input path="bankNo" htmlEscape="false" maxlength="25" class="required"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">开户人:</label>
        <div class="controls">
            <form:input path="bankOwner" htmlEscape="false" maxlength="20" class="required"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">付款金额:</label>
        <div class="controls">
            <form:input path="payAmount" htmlEscape="false" readonly="true" />
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">付款日期:</label>
        <div class="controls">
            <input id="payDate" name="payDate" type="text"
                  maxlength="20" class="input-small Wdate"
                  value="<fmt:formatDate value='${servicePointWithdraw.payDate}' pattern='yyyy-MM-dd'/>"
                  onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        </div>
    </div>
    <div class="form-actions">
        <input id="btnSubmit" class="btn btn-primary" type="button" value="保 存"/>&nbsp;
        <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();"/>
    </div>
</form:form>
</body>
</html>
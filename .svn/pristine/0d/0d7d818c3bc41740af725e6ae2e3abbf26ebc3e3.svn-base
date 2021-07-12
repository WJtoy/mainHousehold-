<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户账户</title>
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        // 关闭页面
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }

        var clickTag=0;

        $(document).ready(
            function()
            {
                $(document).ready(function() {
                    $("#inputForm").validate({
                        submitHandler: function(form){
                            var loadingIndex = layerLoading('正在提交，请稍候...');
                            var $btnSubmit = $("#btnSubmit");
                            if ($btnSubmit.prop("disabled") == true) {
                                event.preventDefault();
                                return false;
                            }
                            $btnSubmit.prop("disabled", true);

                            $.ajax({
                                url:"${ctx}/md/customerNew/saveCustomerFinance?financeType=" + ${financeType},
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
                                        if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                            var layero = $("#layui-layer" + parentIndex,top.document);
                                            var iframeWin = top[layero.find('iframe')[0]['name']];
                                            var bankName = $("#publicBank option:selected").text();
                                            var privateBankName = $("#privateBank option:selected").text();

                                            var bank = $("#publicBank option:selected").val();
                                            var privateBank = $("#privateBank option:selected").val();

                                            var financeType = $('#financeType').val();
                                            if(financeType == 1){
                                                iframeWin.refreshFinance(financeType,bank,bankName,$("#publicBranch").val(),$("#publicAccount").val(),$("#publicName").val());
                                            }else {
                                                iframeWin.refreshFinance(financeType,privateBank,privateBankName,$("#privateBranch").val(),$("#privateAccount").val(),$("#privateName").val());
                                            }

                                        }
                                    }else{
                                        setTimeout(function () {
                                            clickTag = 0;
                                            $btnSubmit.removeAttr('disabled');
                                        }, 2000);
                                        layerError(data.message, "错误提示");
                                        top.layer.close(loadingIndex);
                                    }
                                    return false;
                                },
                                error: function (data)
                                {
                                    if(loadingIndex) {
                                        layer.close(loadingIndex);
                                    }
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                    }, 2000);
                                    ajaxLogout(data,null,"数据保存错误，请重试!");
                                    top.layer.close(loadingIndex);
                                    //var msg = eval(data);
                                },
                                timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                            });
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
            });

    </script>

</head>

<body>

<form:form id="inputForm" modelAttribute="customerFinance" action="${ctx}/md/customerNew/customerFinanceForms" method="post" class="form-horizontal">
    <sys:message content="${message}" />
    <c:set value="${financeType}" var="financeType"></c:set>
    <input type="hidden" id="financeType" name="financeType" value="${financeType}">
    <form:hidden id="id" path="id" />
    <div class="control-group" style="margin-top: 38px;margin-left: 20px;">
        <label class="control-label" style="width: 188px">账户类型：</label>
        <div class="controls" style="margin-left: 0px">
            <c:choose>
                <c:when test="${financeType == 1}">
                    <input style="width:236px;" readonly="readonly" type="text" value="对公账户" maxlength="20" class="valid" aria-invalid="false">
                </c:when>
                <c:otherwise>
                    <input style="width:236px;" readonly="readonly" type="text" value="对私账户" maxlength="20" class="valid" aria-invalid="false">
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <c:set var="bankTypeList" value="${fns:getDictListFromMS('banktype')}"/>
    <div class="control-group" style="margin-top: 12px;margin-left: 20px;width: 98%">
        <label class="control-label" style="width: 188px">开户银行：</label>
        <div class="controls" style="margin-left: 0px">
            <c:choose>
                <c:when test="${financeType == 1}">
                    <form:select path="publicBank" cssClass="input-medium" cssStyle="width: 250px;">
                        <form:option value="" label="请选择" />
                        <form:options items="${bankTypeList}"
                                      itemLabel="label" itemValue="value" htmlEscape="false" /><%--切换为微服务--%>
                    </form:select>
                </c:when>
                <c:otherwise>
                    <form:select path="privateBank" cssClass="input-medium" cssStyle="width: 250px;">
                        <form:option value="" label="请选择" />
                        <form:options items="${bankTypeList}"
                                      itemLabel="label" itemValue="value" htmlEscape="false" /><%--切换为微服务--%>
                    </form:select>

                </c:otherwise>
            </c:choose>
        </div>

    </div>
    <div class="control-group" style="margin-top: 12px;margin-left: 20px">
        <label class="control-label" style="width: 188px">分&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp行：</label>
        <div class="controls" style="margin-left: 187px">
            <c:choose>
                <c:when test="${financeType == 1}">
                    <form:input id="publicBranch" path="publicBranch" htmlEscape="false" maxlength="20" style="width: 236px;"/>
                </c:when>
                <c:otherwise>
                    <form:input id="privateBranch" path="privateBranch" htmlEscape="false" maxlength="20" style="width: 236px;"/>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="control-group" style="margin-top: 12px;margin-left: 20px">
        <label class="control-label" style="width: 188px">账&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp号：</label>
        <div class="controls" style="margin-left: 187px">
            <c:choose>
                <c:when test="${financeType == 1}">
                    <form:input id="publicAccount" path="publicAccount" htmlEscape="false" maxlength="19"  class="required number" style="width: 236px;"/>
                </c:when>
                <c:otherwise>
                    <form:input id="privateAccount" path="privateAccount" htmlEscape="false" maxlength="19"  class="required number" style="width: 236px;"/>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <div class="row-fluid">
        <div class="control-group" style="margin-top: 12px;margin-left: 20px">
            <label class="control-label" style="width: 188px">开户人：</label>
            <div class="controls" style="margin-left: 187px">
                <c:choose>
                    <c:when test="${financeType == 1}">
                        <form:input id="publicName" path="publicName" htmlEscape="false" maxlength="20"  style="width: 236px;"/>
                    </c:when>
                    <c:otherwise>
                        <form:input id="privateName" path="privateName" htmlEscape="false" maxlength="20"  style="width: 236px;"/>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 38px"/>


    <input id="btnSubmit" class="btn btn-primary" type="submit" style="width: 92px;height: 40px;margin-left: 66%;margin-top: 10px"
           value="保存" />

    <input id="btnCancel" class="btn" type="button" value="取消" style="width: 92px;height: 40px;margin-top: 10px;margin-left: 10px"
           onclick="cancel()" />
</form:form>
</body>
</html>

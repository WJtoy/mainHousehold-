<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE HTML>
<head>
    <title>充值</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#inputForm").validate({
                rules: {
                    paymentType:{
                        required: true,
                        minlength: 1
                    },
                    amount:{
                        max : 10000000
                    }
                },
                messages: {
                    paymentTypeId:{ required: "请选择支付类型！",minlength: "请选择支付类型！"},
                    amount : { required: "请输入充值金额", max: "充值最大金额不能超过10000000."}
                },
                submitHandler: function(form){
                    $("#btnSubmit").attr("disabled", "disabled");
                    $("#btnCancel").attr("disabled", "disabled");
                    loading('正在提交，请稍等...');
                    form.submit();
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

        function check(){
            if($("#paymentTypeId").find("option:selected").text()=="请选择")
            {
                alert("请选择支付类型！");
                return false;
            }
        }
        function repage(){
            parent.repage();
        }

        function closethisfancybox(){
            parent.$.fancybox.close();
        }
        function openjBox(url,title,width,height){
            top.$.jBox.open("iframe:" + url , title, width, height,{top:'5%',buttons:{}	});
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <shiro:hasPermission name="customer:fi:blockamountlist:view"><li><a href="${ctx}/customer/fi/customerCurrency/blockamountlist">冻结查询</a></li></shiro:hasPermission>
    <li><a href="${ctx}/customer/fi/customerCurrency/list">余额查询</a></li>
    <li class="active"><a href="javascript:void(0);">充值</a></li>
</ul><br/>
<form:form id="inputForm" modelAttribute="customerCurrency" action="${ctx}/customer/fi/customerCurrency/save" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <sys:message content="${message}"/>

    <c:choose>
        <c:when test="${customerCurrency.id==null}">
            <div class="control-group">
                <label class="control-label">客户:</label>
                <div class="controls">
                        <%--<sys:treeselect id="customer" name="customer.id" value="${customerCurrency.customer.id}" labelName="customer.name" labelValue="${customerCurrency.customer.name}"
                            title="客户" url="/md/customer/treeData" cssClass="required"  disabled="false"/>--%>
                    <select name="customer.id" class="input-large required" style="width:220px;">
                        <option value="">请选择</option>
                        <c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
                            <option value="${customer.id}"  <c:out value="${customer.id==customerCurrency.customer.id ?'selected':''}"/>>${customer.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">支付类型:</label>
                <div class="controls">
                    <form:select path="paymentType"  disabled="false" style="width:220px;">
                        <form:option value="" label="请选择"/>
                        <form:options items="${fns:getDictListFromMS('ChargeType')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                    </form:select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">充值金额:</label>
                <div class="controls">
                    <form:input path="amount" htmlEscape="false" maxlength="10"  class="required number" disabled="false"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">备注:</label>
                <div class="controls">
                    <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"  disabled="false"/>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="control-group">
                <label class="control-label">客户:</label>
                <div class="controls">
                    <input type="text" readonly="true" id="customer.name" name="customer.name" value="${customerCurrency.customer.name}" />
                    <input type="hidden" readonly="true" id="customer.id" name="customer.id" value="${customerCurrency.customer.id}" />

                </div>
            </div>
            <div class="control-group">
                <label class="control-label">支付类型:</label>
                <div class="controls">
                    <form:select path="paymentType" cssClass="required"  disabled="true">
                        <form:option value="" label="请选择"/>
                        <form:options items="${fns:getDictListFromMS('ChargeType')}" itemLabel="label" itemValue="value" htmlEscape="true"/><%--切换为微服务--%>
                    </form:select>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">充值金额:</label>
                <div class="controls">
                    <form:input path="amount" htmlEscape="false" maxlength="10"  class="required number" disabled="true"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">备注:</label>
                <div class="controls">
                    <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"  disabled="true"/>
                </div>
            </div>
        </c:otherwise>
    </c:choose>

    <div class="form-actions">
        <shiro:hasPermission name="customer:fi:customercurrency:charge"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"  />&nbsp;</shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
    </div>
</form:form>
</body>
</html>
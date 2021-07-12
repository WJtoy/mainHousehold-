<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品型号</title>
    <meta about="客户产品型号(微服务md)" />
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="${ctxStatic}/jquery-ztree/3.5.12/css/zTreeStyle/zTreeStyle.min.css" type="text/css">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <script type="text/javascript" src="${ctxStatic}/jquery-ztree/3.5.12/js/jquery.ztree.all-3.5.min.js"></script>
    <c:set var="currentuser" value="${fns:getUser()}"/>
    <script type="text/javascript">
        var this_index = top.layer.index;
        // 关闭页面
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }

        var clickTag=0;

        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler : function(form) {
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }

                    $btnSubmit.prop("disabled", true);
                    var customerId = $("#customerId").val();
                    $.ajax({
                        url:"${ctx}/customer/md/customerBrand/ajaxSave",
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
                                if(pframe!=undefined){
                                    pframe.document.location="${ctx}/customer/md/customerBrand/getList?customerId="+customerId;
                                }
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
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
                            //var msg = eval(data);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                    });
                },
                errorContainer : "#messageBox",
                errorPlacement : function(error, element)
                {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")
                        || element.is(":radio")
                        || element.parent().is(
                            ".input-append"))
                    {
                        error.appendTo(element.parent()
                            .parent());
                    } else
                    {
                        error.insertAfter(element);
                    }
                }
            });
        });
    </script>
    <style type="text/css">
        .form-horizontal .control-label{
            width: 123px;
        }
        .form-horizontal .controls{
            margin-left: 130px;
        }
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
    </style>
</head>

<body>
<form:form id="inputForm" modelAttribute="customerBrand" action="${ctx}/customer/md/customerBrand/save" method="post" class="form-horizontal">
<sys:message content="${message}" />
<c:if test="${canAction == true}">
<form:hidden path="id"></form:hidden>
<c:choose>
    <c:when test="${currentuser.isCustomer()==true}">
        <form:hidden path="customerId"></form:hidden>
    </c:when>
    <c:otherwise>
        <div class="control-group" style="margin-top: 45px">
            <label class="control-label"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户:</label>
            <div class="controls">
                <c:choose>
                    <c:when test="${customerBrand.customerId > 0}">
                        <form:hidden path="customerId"></form:hidden>
                        <form:input path="customerName" readonly="true" style="width:236px"></form:input>
                    </c:when>
                    <c:otherwise>
                        <select id="customerId" name="customerId" class="input-small required selectCustomer" style="width:250px;">
                            <option value=""
                                    <c:out value="${(empty customerBrand.customerId)?'selected=selected':''}" />>请选择</option>
                            <c:forEach items="${fns:getMyCustomerList()}" var="customer">
                                <option value="${customer.id}"
                                        <c:out value="${(customerBrand.customerId eq customer.id)?'selected=selected':''}" />>${customer.name}</option>
                            </c:forEach>
                        </select>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:otherwise>
</c:choose>

    <div class="control-group" style="margin-top: 9px">
        <label class="control-label"><span class="red">*</span>品&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp牌:</label>
        <div class="controls">
            <form:input path="brandName" htmlEscape="false" maxlength="30" class="required" style="width:236px;"/>
        </div>
    </div>
    <div class="control-group" style="margin-top: 9px">
        <label class="control-label"><span class="red">*</span>排&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp序:</label>
        <div class="controls">
            <form:input path="sort" htmlEscape="false" maxlength="3" style="width: 236px"/>
        </div>
    </div>
    <div class="control-group" style="margin-top: 9px">
        <label class="control-label">描&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp述:</label>
        <div class="controls">
            <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="100" class="input-xlarge" cssStyle="width: 466px;"/>
        </div>
    </div>
    </c:if>
    <div id="editBtn" class="line-row">
        <c:if test="${canAction == true}">
            <shiro:hasPermission name="customer:md:customerbrand:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 96px;height: 40px;margin-left: 70%;margin-top: 10px;margin-bottom: 10px"/>
            </shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 96px;height: 40px;margin-top: 10px;margin-left: 13px;margin-bottom: 10px"/>
    </div>
    </form:form>
</body>
</html>

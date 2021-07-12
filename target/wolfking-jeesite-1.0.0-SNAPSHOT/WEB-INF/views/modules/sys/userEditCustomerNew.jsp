<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>用户管理</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/layui/layui.js"></script>
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <style>
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
    <script type="text/javascript">
        var num = 0;
        $(document).ready(function () {
            var clickTag = 0;
            var $btnSubmit = $("#btnSubmit");

                $("#tableForm").validate({
                    submitHandler: function (form) {
                        clickTag = 1;

                        var salesCustomerIds = $("#salesCustomerIds").val();
                        if (salesCustomerIds == '') {
                            clickTag = 0;
                            layerError("当前无客户变更","错误提示");
                            return false;
                        }

                        var options = {
                            url: "${ctx}/sys/user/batchEditCustomer",
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
                        $("#tableForm").ajaxSubmit(options);
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

            $("#merchandiserId").change(function(){
                searchCustomers();// 同步查询
            });

            $("#salesId").change(function(){
                searchCustomers();// 同步查询
            });
        });

        function searchCustomers(){
            var salesCustomerIds = [];// 清空数组
            $("#tbody").html("");// 清空表格
            $("#count").text(0);
            $("#salesCustomerIds").attr("value","");

            var subFlag = $("#subFlag").val();
            var ids;
            var sales = $("#salesId option:selected");
            var merchandiser = $("#merchandiserId option:selected");
            var salesId = sales.val();// 业务员
            var merchandiserId = merchandiser.val();// 跟单员
            if (salesId == '' && merchandiserId == '') {
                layerError("请选择业务员或跟单员", "错误提示");
                return false;
            }
            if (subFlag == 1) {
                if (salesId == '') {
                    layerError("请选择业务员", "错误提示");
                    return false;
                }
            }

            var loadingIndex;
            var options = {
                url: "${ctx}/sys/user/responsibleCustomer",
                type: 'post',
                dataType: 'json',
                beforeSubmit: function (formData, jqForm, options) {
                    loadingIndex = layer.msg('正在查询，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                    return true;
                },// 提交前的回调函数
                success: function (result) {
                    // 提交后的回调函数
                    if (loadingIndex) {
                        layer.close(loadingIndex);
                    }
                    if (!result.success) {
                        layerError("数据查询失败:" + result.message, "错误提示");
                    } else {
                        var customers = [];
                        if (result.data && result.data.length > 0) {
                            customers = result.data;
                            var tr;
                            for (var i in result.data) {
                                var merchandiserName = customers[i].merchandiserName == undefined ? '' : customers[i].merchandiserName;
                                var salesName = customers[i].salesName  == undefined ? '' : customers[i].salesName;
                                tr = $("<tr></tr>");
                                tr.append("<td style='text-align: left'>"+customers[i].customerName+"</td>" +
                                    "<td style='text-align: left'>"+salesName+"</td>" +
                                    "<td style='text-align: left'>"+merchandiserName+"</td>");
                                $("#tbody").append(tr);

                                salesCustomerIds.push(customers[i].customerId);
                            }
                            ids = salesCustomerIds.join(",");
                            num = result.data.length;
                            $("#count").text(num);
                            $("#salesCustomerIds").val(ids);
                        }
                    }
                },
                error: function (data) {
                },
            };
            $("#searchForm").ajaxSubmit(options);
        }

    </script>
</head>
<body>

<form:form id="searchForm" modelAttribute="customer" action="" method="post"
           class="form-inline" cssStyle="padding: 20px;margin-bottom: -10px;">
    <input type="hidden" id="subFlag" value="${user.subFlag}">
    <c:choose>
        <c:when test="${user.subFlag == 1}">
            <label></span>业&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;务：</label>
            <select id="salesId" name="sales.id" class="input-small" style="width:150px;">
                <option value=""/>请选择</option>
                <c:forEach items="${fns:getSaleList()}" var="dict">
                    <option value="${dict.id}">${dict.name}</option>
                </c:forEach>
            </select>
        </c:when>
        <c:otherwise>
            <label></span>业&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;务：</label>
            <select id="salesId" name="sales.id" class="input-small" style="width:150px;">
                <option value=""/>请选择</option>
                <c:forEach items="${fns:getSaleList()}" var="dict">
                    <option value="${dict.id}">${dict.name}</option>
                </c:forEach>
            </select>
            <label style="margin-left: 30px;">跟&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;单：</label>
            <select id="merchandiserId" name="merchandiser.id" class="input-small" style="width:150px;">
                <option value=""/>请选择</option>
                <c:forEach items="${fns:getMerchandiserList()}" var="dict">
                    <option value="${dict.id}">${dict.name}</option>
                </c:forEach>
            </select>
        </c:otherwise>
    </c:choose>
    &nbsp;
    <input id="btnSeach" type="button" class="layui-btn layui-btn-sm" onclick="searchCustomers();" value="查询"
           style="width: 64px;height: 32px;background: #0096DA;"/>
</form:form>

<div style="height: 450px;overflow: auto;">
    <form:form id="tableForm" modelAttribute="user" method="post" class="form-horizontal" cssStyle="width: 100%;margin: 10px auto;">
        <form:hidden path="id"/>
        <form:hidden path="subFlag"/>
        <input type="hidden" name="salesCustomerIds" id="salesCustomerIds">

        <div class="layui-form" style="text-align: center;">
            <table id="contentTable" class="layui-table" style="width: 95%;margin: 10px auto;">
                <thead>
                <tr>
                    <th>
                        <label>客户列表</label>
                        <label>共<i id="count" style="color: red">0</i>个客户</label>
                    </th>
                    <th>
                        <label>业务员</label>
                    </th>
                    <th>
                        <label>跟单员</label>
                    </th>
                </tr>
                </thead>
                <tbody id="tbody">
                </tbody>
            </table>

        </div>
    </form:form>

</div>
<div id="editBtn">
    <shiro:hasPermission name="sys:user:edit">
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;" onclick="$('#tableForm').submit()"/>&nbsp;
    </shiro:hasPermission>
    <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
</div>
</body>
<script class="removedscript" type="text/javascript">
    function cancel() {
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        parent.layer.close(index);
    }
    $(document).ready(function() {
        $("th").css({"text-align":"left"});
        $("td").css({"text-align":"left"});
    });
</script>
</html>
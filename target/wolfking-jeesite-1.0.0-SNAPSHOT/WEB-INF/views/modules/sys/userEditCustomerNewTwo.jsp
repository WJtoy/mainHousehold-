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
        /*.table-head table,.table-body table{*/
        /*width:100%;border-collapse:collapse;*/
        /*}*/
        .table-head{
            padding-right:17px;
        }
        .table-body{
            width:100%; height:547px;overflow-y:auto;margin-top: -11px;
        }

        /*.table-body table tr td{*/
        /*border:1px solid red;*/
        /*text-align: center;*/
        /*}*/
    </style>
    <script type="text/javascript">
        var num = 0;
        $(document).ready(function () {
            layui.use('form', function () {
                var form = layui.form,
                    $ = layui.$;

                form.render();

                form.on('checkbox(all)', function (data) {
                    if (data.elem.checked) {
                        $(":checkbox[name='customers']").attr("checked", "checked");
                        form.render();
                    } else {
                        $(":checkbox[name='customers']").removeAttr("checked");
                        form.render();
                    }
                    num = $("input[type='checkbox'][name='customers']:checked").length;
                    $("#count").text(num);
                    form.render();
                });


                form.on('checkbox(customers)', function (data) {
                    var customerCount = $("#customerCount").val();
                    if (data.elem.checked) {
                        num ++;
                    } else {
                        num --;
                    }
                    if (num == customerCount) {
                        $("input[type='checkbox'][name='all']").attr("checked", true);
                    } else {
                        $("input[type='checkbox'][name='all']").attr("checked", false);
                    }
                    $("#count").text(num);
                    form.render();
                });
            });

            var clickTag = 0;
            var $btnSubmit = $("#btnSubmit");

            var subFlag = $("#subFlag").val();

            $("#tableForm").validate({
                submitHandler: function (form) {
                    clickTag = 1;
                    $("#salesCustomerIds").val("");// 清空
                    var salesCustomerIdArr = [];// 清空数组
                    var checkIds;

                    $("input[type='checkbox'][name='customers']:checked").each(function (index, element) {
                        salesCustomerIdArr.push($(this).val());
                    });
                    checkIds = salesCustomerIdArr.join(",");
                    $("#salesCustomerIds").val(checkIds);

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
            $("input[type='checkbox'][name='all']").attr("checked", false);
            viewRendering();// 渲染

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
                            layui.use('form', function () {
                                var form = layui.form,
                                    $ = layui.$;

                                for (var i in result.data) {
                                    var merchandiserName = customers[i].merchandiserName == undefined ? '' : customers[i].merchandiserName;
                                    var salesName = customers[i].salesName  == undefined ? '' : customers[i].salesName;
                                    tr = $("<tr></tr>");
                                    tr.append("<td style='width: 14px'><div>" +
                                        "<input type='checkbox' name='customers' lay-filter='customers' value='"+customers[i].customerId+"' lay-skin='primary'>" +
                                        "</div></td>");
                                    tr.append("<td style='text-align: left;width: 396px;'>"+customers[i].customerName+"</td>" +
                                        "<td style='text-align: left;width: 104px;'>"+salesName+"</td>" +
                                        "<td style='text-align: left;width: 104px;'>"+merchandiserName+"</td>");
                                    $("#tbody").append(tr);
                                }
                                form.render();
                            });
                            $("#customerCount").val(result.data.length);
                        }
                    }
                },
                error: function (data) {
                },
            };
            $("#searchForm").ajaxSubmit(options);
        }

        function viewRendering(){
            layui.use('form', function () {
                var form = layui.form,
                    $ = layui.$;

                form.render();
            });
        }

    </script>
</head>
<body>

<input id="customerCount" type="hidden">
<form:form id="searchForm" modelAttribute="customer" action="" method="post"
           class="form-inline" cssStyle="padding: 20px;margin-bottom: -10px;">
    <input type="hidden" id="subFlag" value="${user.subFlag}">
    <c:choose>
        <c:when test="${user.subFlag == 1}">
            <label></span>业&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;务：</label>
            <select id="salesId" name="sales.id" class="input-small" style="width:200px;">
                <option value=""/>请选择</option>
                <c:forEach items="${fns:getSaleList()}" var="dict">
                    <option value="${dict.id}">${dict.name}</option>
                </c:forEach>
            </select>
        </c:when>
        <c:otherwise>
            <label></span>业&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;务：</label>
            <select id="salesId" name="sales.id" class="input-small" style="width:200px;">
                <option value=""/>请选择</option>
                <c:forEach items="${fns:getSaleList()}" var="dict">
                    <option value="${dict.id}">${dict.name}</option>
                </c:forEach>
            </select>
            <label style="margin-left: 30px;">跟&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;单：</label>
            <select id="merchandiserId" name="merchandiser.id" class="input-small" style="width:200px;">
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

<form:form id="tableForm" modelAttribute="user" method="post" class="form-horizontal" cssStyle="width: 100%;margin: 10px auto;">
    <form:hidden path="id"/>
    <form:hidden path="subFlag"/>
    <input type="hidden" name="salesCustomerIds" id="salesCustomerIds">

    <div class="layui-form" style="text-align: center;">

        <div class="table-head">
            <table class="layui-table" style="width: 95%;margin: 10px auto;table-layout: fixed;">
                <thead>
                <tr>
                    <th width="6"><input type="checkbox" name="all" lay-filter="all" lay-skin='primary' title=""></th>
                    <th width="320">
                        <label>客户列表</label>
                        <label>已选<i id="count" style="color: red">0</i>个客户</label>
                    </th>
                    <th width="80">
                        <label>业务员</label>
                    </th>
                    <th width="80">
                        <label>跟单员</label>
                    </th>
                </tr>
                </thead>
            </table>
        </div>

        <div class="table-body">
            <table class="layui-table" style="width:92%;margin: 0 0 0 20px;table-layout: fixed;">
                <tbody id="tbody">
                </tbody>
            </table>
        </div>

    </div>
</form:form>

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
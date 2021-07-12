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
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';

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
                    if (data.elem.checked) {
                        num ++;
                    } else {
                        num --;
                    }
                    $("#count").text(num);
                    form.render();
                });
            });

            var clickTag = 0;
            $("#btnSubmit").click(function () {

                var arr = [];// 选中客户
                var idArr = [];// 选中客户id
                $("input[type='checkbox'][name='customers']:checked").each(function (index, element) {
                    var entity = {};
                    entity.id = $(this).val();
                    entity.contractFlag = $(this).attr("data-contractFlag");
                    entity.customerName = $(this).attr("data-customerName");
                    arr.push(entity);
                    idArr.push(parseInt($(this).val()));
                });
                var index = parent.layer.getFrameIndex(window.name);
                parent.layer.close(index);//关闭当前页
                if (idArr.toString() != oldIdArr.toString()) {
                    // 交集
                    let intersection = $(idArr).filter(oldIdArr).toArray();
                    // 回调父窗口方法
                    if(parentIndex && parentIndex != undefined && parentIndex != ''){
                        var layero = $("#layui-layer" + parentIndex,top.document);
                        var iframeWin = top[layero.find('iframe')[0]['name']];
                        iframeWin.refresh(arr, num, oldArr, intersection);
                    }
                }
            });
        });

        function searchCustomers(){
            var sales = $("#salesId option:selected");
            var merchandiser = $("#merchandiserId option:selected");
            var salesId = sales.val();// 业务员
            var merchandiserId = merchandiser.val();// 跟单员
            if (salesId == '' && merchandiserId == '') {
                layerError("请选择业务员或跟单员", "错误提示");
                return false;
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
                        layui.use('form', function () {
                            var form = layui.form,
                                $ = layui.$;

                            var customerCheck = [];
                            $('input[type=checkbox][name=customers]').attr('checked', false);
                            if (result.data && result.data.length > 0) {
                                customerCheck = result.data;
                                for (var i in result.data) {
                                    var customerId = customerCheck[i];
                                    var query = ":checkbox[name='customers'][value=" + customerId + "]";
                                    $(query).attr("checked", "checked");
                                    form.render();
                                }
                                num = result.data.length;
                                $("#count").text(num);
                                form.render();
                            } else {
                                $('input[type=checkbox][name=customers]').attr('checked', false);
                                num = 0;
                                $("#count").text(0);
                                form.render();
                            }
                        });
                    }
                },
                error: function (data) {
                },
            };
            $("#searchForm").ajaxSubmit(options);
        }

        function equar(a, b) {
            // 判断数组的长度
            if (a.length !== b.length) {
                return false
            } else {
                // 循环遍历数组的值进行比较
                for (let i = 0; i < a.length; i++) {
                    if (a[i] !== b[i]) {
                        return false
                    }
                }
                return true;
            }
        }
    </script>
</head>
<body>

<form:form id="searchForm" modelAttribute="customer" action="" method="post"
           class="form-inline" cssStyle="padding: 20px;margin-bottom: -10px;">
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
    &nbsp;
    <input id="btnSeach" type="button" class="layui-btn layui-btn-sm" onclick="searchCustomers();" value="查询"
           style="width: 64px;height: 32px;background: #0096DA;"/>
</form:form>

<div style="height: 450px;overflow: auto;">
    <div class="layui-form" style="text-align: center;">
            <table id="contentTable" class="layui-table" style="width: 95%;margin: 10px auto;">
                <thead>
                <tr>
                    <th colspan="2">
                        <input type="checkbox" name="all" lay-filter="all" lay-skin='primary' title="客户列表">
                        <label>已选择<i id="count" style="color: red">0</i>个</label>
                    </th>
                </tr>
                </thead>
                <tbody>
                    <c:if test="${customerList.size() > 0}">
                        <c:forEach items="${customerList}" var="customer" varStatus="i">
                                <c:choose>
                                    <c:when test="${i.index % 2 == 0}">
                                        <tr>
                                        <td>
                                            <div style="text-align: left;">
                                                <input type="checkbox" name="customers" lay-filter="customers"
                                                   value="${customer.id}" lay-skin='primary' data-contractFlag="${customer.contractFlag}" data-customerName="${customer.name}" title="${customer.name}">
                                            </div>
                                        </td>
                                        <%--<c:if test="${i.last}">--%>
                                            <%--<td><td>--%>
                                            <%--</tr>--%>
                                        <%--</c:if>--%>
                                    </c:when>
                                    <c:otherwise>
                                        <td>
                                            <div style="text-align: left;">
                                                <input type="checkbox" name="customers" lay-filter="customers"
                                                        value="${customer.id}" lay-skin='primary' data-contractFlag="${customer.contractFlag}" data-customerName="${customer.name}" title="${customer.name}">
                                            </div>
                                        </td>
                                        <tr/>
                                    </c:otherwise>
                                </c:choose>

                        </c:forEach>
                    </c:if>

                </tbody>
            </table>

    </div>
</div>
<div id="editBtn">
    <shiro:hasPermission name="sys:user:edit">
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;"/>&nbsp;
    </shiro:hasPermission>
    <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
</div>
</body>
<script class="removedscript" type="text/javascript">
    function cancel() {
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        parent.layer.close(index);
    }
    var oldArr = [];
    var oldIdArr = [];
    $(document).ready(function() {
        // 服务客户列表
        <c:if test="${customers != null && customers.size() > 0}">
            var customerTotal = ${customerList.size()};
            var customerChecked = ${customers.size()};
            if (customerChecked == customerTotal) {
                $(":checkbox[name='all']").attr("checked", "checked");
            }
            var customerCheck = ${fns:toJson(customers)};
            for (var i in customerCheck) {
                var query = ":checkbox[name='customers'][value=" + customerCheck[i].id + "]";
                $(query).attr("checked", "checked");

                var entity = {};
                entity.id = customerCheck[i].id;
                entity.contractFlag = customerCheck[i].contractFlag;
                entity.customerName = customerCheck[i].name;
                oldArr.push(entity);
                oldIdArr.push(customerCheck[i].id);
            }
            num = customerCheck.length;
            $("#count").text(customerCheck.length);
        </c:if>
    });
</script>
</html>
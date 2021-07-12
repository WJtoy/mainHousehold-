<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>产品型号导入表单</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script type="text/javascript">
        top.layer.closeAll();
        var clickTag = 0;
        $(document).ready(function () {
            $("#btnImportSubmit").click(function () {
                if (clickTag == 1) {
                    return false;
                }
                clickTag = 1;
                $("#btnImportSubmit").attr('disabled', 'disabled');
                top.layer.msg('正在读取excel，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3
                });
                $("#searchForm").attr("action", "${ctx}/provider/md/customerProductModel/read");
                $("#searchForm").attr("enctype", "multipart/form-data");
                $("#searchForm").submit();

            });

            $("#btnExport").click(function () {
                var confirmClickTag = 0;
                layer.confirm('确认要导出异常明细吗?', {icon: 3, title: '系统确认'}, function (index, layero) {
                    if (confirmClickTag == 1) {
                        return false;
                    }
                    var btn0 = $(".layui-layer-btn0", layero);
                    if (btn0.hasClass("layui-btn-disabled")) {
                        return false;
                    }
                    confirmClickTag = 1;

                    var trs = $("#contentTable").find(".error");
                    if (trs.length == 0) {
                        layer.close(index);//关闭本身
                        layerInfo("无异常明细", "提示", true);
                        return false;
                    }

                    btn0.addClass("layui-btn-disabled").attr("disabled", "disabled");
                    layer.close(index);//关闭本身
                    $("#btnExport").attr('disabled', 'disabled');
                    var loadingIndex = layer.msg('正在导出，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                    if ($("#exportForm").length > 0) {
                        $("#exportForm").remove();
                    }
                    jQuery('<form id="exportForm" action="${ctx}/provider/md/customerProductModel/errorlist" method="post"></form>')
                        .appendTo('body').submit();
                    setTimeout(function () {
                        $("#btnExport").removeAttr('disabled');
                        layer.close(loadingIndex);
                    }, 2000);
                });
                return false;
            });

            $("#btnTransfer").click(function () {
                if (clickTag == 1) {
                    return false;
                }
                clickTag = 1;
                var ids = [];
                $("#contentTable input[name='checkedRecords']:checked").each(function () {
                    ids.push($(this).val());
                });

                if (ids.length == 0) {
                    clickTag = 0;
                    layerAlert('请选择需要保存的订单', '系统提示');
                    return;
                }
                var $btnSubmit = $("#btnTransfer");
                $btnSubmit.attr('disabled', 'disabled');
                var loadingIndex;
                var ajaxSuccess = 0;
                var data = {ids: ids};
                $.ajax({
                    cache: false,
                    type: "POST",
                    url: "${ctx}/provider/md/customerProductModel/importSave",
                    data: data,
                    beforeSend: function () {
                        loadingIndex = layer.msg('订单保存中，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if (loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        if (ajaxSuccess == 0) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                        }
                    },
                    success: function (data) {

                        if (ajaxLogout(data)) {
                            return false;
                        }
                        if (data.success) {
                            ajaxSuccess = 1;
                            layerMsg('订单保存成功');
                            location.href = "${ctx}/provider/md/customerProductModel/importForm";
                        }
                        else {
                            layer.alert(data.message, {zIndex: 29891014, area: ['500px', '400px'], title: "错误提示"});
                            setTimeout(function () {
                                location.href = "${ctx}/provider/md/customerProductModel/importForm";
                            }, 3000);

                            //layerError(data.message,'错误提示');
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText, null, "订单保存错误，请重试!");
                    }
                });

            });

            $("#btnClear").click(function () {
                if (clickTag == 1) {
                    return false;
                }
                clickTag = 1;

                var ids = [];
                $("#contentTable input[name='checkedRecords']:checked").each(function () {
                    ids.push($(this).val());
                });
                if (ids.length == 0) {
                    clickTag = 0;
                    layerAlert('请选择需要清除的产品', '系统提示');
                    return;
                }
                var $btnSubmit = $("#btnClear");
                $btnSubmit.attr('disabled', 'disabled');
                var loadingIndex;
                var ajaxSuccess = 0;
                var data = {ids: ids};
                $.ajax({
                    cache: false,
                    type: "POST",
                    url: "${ctx}/provider/md/customerProductModel/importClear",
                    data: data,
                    beforeSend: function () {
                        loadingIndex = layer.msg('产品清空中，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if (loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        if (ajaxSuccess == 0) {
                            clickTag = 0;
                            $btnSubmit.removeAttr("disabled");
                        }
                    },
                    success: function (data) {
                        if (ajaxLogout(data)) {
                            return false;
                        }
                        if (data.success) {
                            ajaxSuccess = 1;
                            layerMsg('清空产品成功');
                            location.href = "${ctx}/provider/md/customerProductModel/importForm";
                        }
                        else {
                            layerError(data.message, '错误提示');
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText, null, "清空产品错误，请重试!");
                    }
                });

            });


            $("#selectAll").change(function () {
                var $check = $(this);
                $("input[type=checkbox]:enabled").each(function () {
                    if ($(this).val() != "on") {
                        if ($check.attr("checked") == "checked") {
                            $(this).attr("checked", true);
                        }
                        else {
                            $(this).attr("checked", false);
                        }
                    }
                });
            });

        });

    </script>
    <style type="text/css">
        #contentTable td {word-break: break-word;}
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li><a href="/provider/md/customerProductModel/getList">列表</a></li>
    <shiro:hasPermission name="md:customerproductmodel:edit">
        <li><a href="${ctx}/provider/md/customerProductModel/form">添加</a></li>
    </shiro:hasPermission>
    <li class="active"><a href="javascript:void(0);">批量添加</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="customerProductModel" action="${ctx}/provider/md/customerProductModel/importForm" method="post" class="breadcrumb form-search">

    <c:choose>
        <c:when test="${currentuser.isCustomer()==true}">
        </c:when>
        <c:when test="${currentuser.isSaleman()==true}">
            &nbsp;
            <label>客户：</label>
            <select id="customerId" name="customerId" class="input-small" style="width:250px;">
                <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                    <option value="${dict.id}"
                            <c:out value="${(customerProductModel.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>
        </c:when>
        <c:otherwise>
            &nbsp;
            <label>客户：</label>
            <select id="customerId" name="customerId" class="input-small" style="width:250px;">
                <option value=""
                        <c:out value="${(empty customerProductModel.customerId)?'selected=selected':''}" />>所有</option>
                <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                    <option value="${dict.id}"
                            <c:out value="${(customerProductModel.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>
        </c:otherwise>
    </c:choose>
    &nbsp;
    <label>品牌：</label>
    <form:select path="brandId" cssClass="input-small" cssStyle="width:200px;">
        <form:option value="" label="所有"/>
        <form:options items="${customerBrandList}" itemLabel="brandName" itemValue="id" htmlEscape="false"/>
    </form:select>
            &nbsp;
            <label>文件：</label>
            <input id="uploadFile" name="file" type="file"/>
            <input id="btnImportSubmit" class="btn btn-primary" type="button" value="读取 "/>
            <input id="btnExport" class="btn btn-primary" type="button" value="导出异常"/>
            <a id="btnTransfer" href="javascript:" class="btn btn-primary">保存</a>
            <a id="btnClear" href="javascript:" class="btn btn-primary">清空</a>
            &nbsp&nbsp&nbsp&nbsp
            <c:if test="${currentuser.isCustomer() }"><a href="${ctxStatic}/doc/快可立批量添加产品型号模板.xls">产品型号模版</a></c:if>
            <c:if test="${currentuser.isCustomer()==false}"><a
                    href="${ctxStatic}/doc/快可立批量添加产品型号模板.xls">产品模版</a></c:if>


</form:form>
<legend>导入订单信息</legend>
<sys:message content="${message}"/>

<table id="contentTable" class="table table-striped table-bordered table-condensed" style="table-layout:fixed;">
    <thead>
    <tr>
        <th width="20px"><input type="checkbox" id="selectAll" name="selectAll"/></th>
        <th width="40px">序号</th>
        <th width="100px">客戶</th>
        <th width="100px">品牌</th>
        <th width="90px">产品</th>
        <th width="120px">型号</th>
        <th width="150px">名称</th>
        <th width="200px">描述</th>
        <th width="200px">错误信息</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="rowNumber" value="0"/>

    <c:forEach items="${page.list}" var="model">
        <c:set var="rowNumber" value="${rowNumber+1}"/>
        <tr class="${model.canSave==0?'error':((!empty model.errorMsg)?'warning':'')}">
            <td>
                <input type="checkbox" id="cbox${rowNumber}" value="${model.id}" name="checkedRecords"/>
                <!-- ${model.canSave==0?'disabled':''} -->
            </td>
            <td>${rowNumber}</td>
            <td>${model.customerName}</td>
            <td>${model.brandName}</td>
            <td>${model.productName}</td>
            <td>${model.customerModel}</td>
            <td>${model.customerProductName}</td>
            <td>${model.remarks}</td>
            <td>${model.errorMsg}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>

</body>
</html>

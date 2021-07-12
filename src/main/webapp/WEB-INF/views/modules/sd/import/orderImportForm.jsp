<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>订单导入表单</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <c:set var="currentuser" value="${fns:getUser()}"/>
    <script type="text/javascript">
        top.layer.closeAll();
        var clickTag = 0;
        $(document).ready(function () {
            $("#btnImportSubmit").click(function () {
                if (clickTag == 1) {
                    return false;
                }
                clickTag = 1;
                var shopName = $("#shopId").find("option:selected").text();
                if(shopName!=null && shopName!=''){
                    $("#shopName").val(shopName)
                }
                $("#btnImportSubmit").attr('disabled', 'disabled');
                top.layer.msg('正在读取excel，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3
                });
                $("#searchForm").attr("action", "${ctx}/sd/order/import/new/read");
                $("#searchForm").attr("enctype", "multipart/form-data");
                $("#searchForm").submit();

            });
            //筛选异常
            $("#btnSearch").click(function () {
                if (clickTag == 1) {
                    return false;
                }
                clickTag = 1;
                $("#btnSearch").attr('disabled', 'disabled');
                top.layer.msg('正在提交，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3
                });
                $("#searchForm").attr("action", "${ctx}/sd/order/import/new/list");
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
                    jQuery('<form id="exportForm" action="${ctx}/sd/order/import/new/errorlist" method="post"></form>')
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
                    url: "${ctx}/sd/order/import/new/transferOrders",
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
                            location.href = "${ctx}/sd/order/import/new/list";
                        }
                        else {
                            layer.alert(data.message, {zIndex: 29891014, area: ['500px', '400px'], title: "错误提示"});
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
                    layerAlert('请选择需要清除的订单', '系统提示');
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
                    url: "${ctx}/sd/order/import/new/clear",
                    data: data,
                    beforeSend: function () {
                        loadingIndex = layer.msg('订单清空中，请稍等...', {
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
                            layerMsg('清空订单成功');
                            location.href = "${ctx}/sd/order/import/new/list";
                        }
                        else {
                            layerError(data.message, '错误提示');
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText, null, "清空订单错误，请重试!");
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

            <c:if test="${currentuser.isCustomer()==true}">
                var customerId = ${currentuser.customerAccountProfile.customer.id}
                loadShopId(customerId);
            </c:if>

        });

        function loadShopId(customerId) {
            $("#shopId").empty();
            var shopId_sel=[];
            shopId_sel.push('<option value="" selected="selected">请选择</option>');
            if(customerId!=null && customerId>0){
                $.ajax({
                    url : "${ctx}/sd/order/import/new/getB2BShop",
                    type : "GET",
                    data : {customerId:customerId},
                    contentType : "application/json",
                    success : function(data){
                        if (ajaxLogout(data)) {
                            return false;
                        }
                        if (data.success == false) {
                            layerError(data.message, "错误提示");
                            return;
                        }
                        if(data && data.data.length>0){
                            $.each(data.data, function(i, item) {
                                shopId_sel.push('<option value="'+item.value+'" data-channel="' + item.sort + '">'+item.label+'</option>');
                            });
                        }
                        $("#shopId").append(shopId_sel.join(' '));
                        $("#shopId").select2();
                    }
                });
            }else{
                $("#shopId").append(shopId_sel.join(' '));
                $("#shopId").select2();
            }
        }
        
    </script>
    <style type="text/css">
        #contentTable td {word-break: break-word;}
        .table tbody tr.error_99 > td{background-color: #c71c22bd;}/*ff000070*/
        .table tbody tr.error_98 > td{background-color: #ff00004d;}
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">订单导入</a></li>
</ul>
<form:form id="searchForm" action="${ctx}/sd/order/import/new" method="post" class="breadcrumb form-search">
    <input type="hidden" id="shopName" name="shopName" value="">
    <c:if test="${canAction == true}">
        <div class="control-group">
            <c:if test="${currentuser.isCustomer()==false}">
                <label>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
                <select id="customerId" name="customerId" class="input-small required" style="width:328px;" onchange="loadShopId(this.value)">
                    <option value="">请选择</option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}"
                                <c:out value="${(customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
                    </c:forEach>
                </select>
            </c:if>
            <c:choose>
                <c:when test="${currentuser.isCustomer()==false}">
                    <label style="margin-left: 70px">购买店铺：</label>
                </c:when>
                <c:otherwise>
                    <label>购买店铺：</label>
                </c:otherwise>
            </c:choose>
            <select id="shopId" name="shopId" class="input-small required" style="width:328px;">
                <option value="">请选择</option>
            </select>
        </div>
        <div class="control-group">
            <label>检查错误：</label>
            <select id="errorMsg" name="errorMsg" class="input-small" style="width:328px;">
                <option value="">请选择</option>
                <c:forEach items="${errorMsgList}" var="msgItem">
                    <option value="${msgItem}" <c:out value="${(errorMsg eq msgItem)?'selected=selected':''}"/>>${msgItem}</option>
                </c:forEach>
            </select>
            <input id="btnSearch" class="btn btn-primary" type="button" value="筛选"/>
            <label>文&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</label>
            <input id="uploadFile" name="file" type="file"/>
            <input id="btnImportSubmit" class="btn btn-primary" type="button" value="读取 "/>
            <input id="btnExport" class="btn btn-warning" type="button" value="导出异常"/>
            <a id="btnTransfer" href="javascript:" class="btn btn-success">保存</a>
            <span style="padding-left:20px;" >&nbsp;</span>
            <a id="btnClear" href="javascript:" class="btn btn-danger">清空</a>
            <span style="padding-left:20px;" >&nbsp;</span>
            <a href="${ctxStatic}/doc/快可立全国联保批量下单数据模板.xls">订单模版</a>
        </div>
    </c:if>
</form:form>
<legend>导入订单信息</legend>
<sys:message content="${message}"/>

<table id="contentTable" class="table table-striped table-bordered table-condensed" style="table-layout:fixed;">
    <thead>
    <tr>
        <th width="20px"><input type="checkbox" id="selectAll" name="selectAll"/></th>
        <th width="40px">序号</th>
        <th width="100px">客戶</th>
        <th width="100px">购买店铺</th>
        <th width="80px">联系人</th>
        <th width="90px">手机</th>
        <th width="80px">电话</th>
        <th width="150px">地址</th>
        <th width="80px">产品名称</th>
        <th width="60px">品牌</th>
        <th width="80px">产品型号</th>
        <th width="80px">服务项目</th>
        <th width="200px">订单描述</th>
        <th width="40px">数量</th>
        <th width="80px">快递公司</th>
        <th width="100px">快递单号</th>
        <th width="80px">备注</th>
        <th width="120px">第三方单号</th>
        <th width="300px">错误信息</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="rowNumber" value="0"/>
    <c:forEach items="${page.list}" var="order">
        <c:set var="rowNumber" value="${rowNumber+1}"/>
        <c:set var="error_class" value=""/>
        <c:choose>
            <c:when test="${order.sort == 99}"><c:set var="error_class" value="error_99"/></c:when>
            <c:when test="${order.sort == 98}"><c:set var="error_class" value="error_98"/></c:when>
            <c:otherwise></c:otherwise>
        </c:choose>
        <tr class="${order.canSave==0?'error':((!empty order.errorMsg)?'warning':'')} ${error_class}">
            <td>
                <input type="checkbox" id="cbox${rowNumber}" value="${order.id}" name="checkedRecords"/>
                <!-- ${order.canSave==0?'disabled':''} -->
            </td>
            <td>${rowNumber}</td>
            <td>${order.customer.name}</td>
            <td>${order.b2bShop.shopName}</td>
            <td>${order.userName}</td>
            <td>${order.phone}</td>
            <td>${order.tel}</td>
            <td>${order.address}</td>
            <td>${order.product.name}</td>
            <td>${order.brand}</td>
            <td>${order.productSpec}</td>
            <td>${order.serviceType.name}</td>
            <td>${order.description}</td>
            <td>${order.qty}</td>
            <td>${order.expressCompany.label}</td>
            <td>${order.expressNo}</td>
            <td>${order.remarks}</td>
            <td>${order.thdNo}</td>
            <td>${order.errorMsg}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>

</body>
</html>

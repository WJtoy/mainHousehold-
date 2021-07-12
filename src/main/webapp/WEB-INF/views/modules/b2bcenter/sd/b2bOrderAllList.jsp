<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>待转换B2B工单列表</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
        }
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";
        $(document).on("click", "#btnClearSearch", function () {
            // $("#searchForm")[0].reset();
            // var dateStr = DateFormat.format(new Date(), 'yyyy-MM-dd');
            // $("#endCreateDate").val(dateStr);
            // $("#beginCreateDate").val(DateFormat.format(DateFormat.addMonthStr(dateStr, -1), 'yyyy-MM-01'));
            // $("#processFlag").val("");
            // $("#s2id_processFlag").find("span.select2-chosen").html('所有');
            // $("#shopId").val("");
            // $("#s2id_shopId").find("span.select2-chosen").html('所有');
            // $("#shopIdsString").val("");
            $("#parentBizOrderId").val("");
            $("#userMobile").val("");
            $("#userName").val("");
            $("#productCode").val("");
            $("#productSpec").val("");
            search();
        });
        $(document).on("click", "#btnAdvanceSearch", function () {
            if ($(this).hasClass("collapsed")) {
                $(this).removeClass("collapsed");
                $("#div_search_customer").show();
            } else {
                $(this).addClass("collapsed");
                $("#div_search_customer").hide();
            }
        });
    </script>

</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <c:forEach items="${dataSourceList}" var="dict">
        <c:choose>
            <c:when test="${dict.value eq currenctDataSource.value}">
                <li class="active"><a href="javascript:void(0);">${dict.label}</a>
            </c:when>
            <c:otherwise>
                <li><a href="${ctx}/b2b/b2bcenter/order/b2bOrderAllList?dataSource=${dict.value}">${dict.label}</a>
            </c:otherwise>
        </c:choose>
        </li>
    </c:forEach>
</ul>
<form:form id="searchForm" modelAttribute="order" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="repageFlag" name="repageFlag" type="hidden" value="false"/>
    <form:hidden path="dataSource"/>
    <div>
        <c:if test="${fns:canSearchB2BOrderByCustomer(order.dataSource)}">
            <label>客户：</label>
            &nbsp;
            <select id="shopIdsString" name="shopIdsString" class="input-small" style="width:160px;">
                <option value="">所有</option>
                <c:forEach items="${customers}" var="dict">
                    <option value="${dict.value}" <c:out value="${(dict.value eq order.shopIdsString)?'selected=selected':''}" />>${dict.label}</option>
                </c:forEach>
            </select>
        </c:if>
        <label>店铺：</label>
        &nbsp;
        <select id="shopId" name="shopId" class="input-small" style="width:160px;">
            <option value="">所有</option>
            <c:forEach items="${fns:getAllShopList(order.dataSource)}" var="dict">
                <option value="${dict.shopId}" <c:out value="${(dict.shopId eq order.shopId)?'selected=selected':''}" />>${dict.shopName}</option>
            </c:forEach>
        </select>
        <label>处理状态：</label>
        <select id="processFlag" name="processFlag" class="input-small" style="width:160px;">
            <option value="">所有</option>
            <c:forEach items="${processFlags}" var="dict">
                <option value="${dict.value}" <c:out value="${(dict.intValue eq order.processFlag)?'selected=selected':''}" /> >${dict.label}</option>
            </c:forEach>
        </select>
        <c:if test="${currenctDataSource.intValue eq 2|| currenctDataSource.intValue eq 16}">
            <label>工单类型：</label>
            <select id="abnormalOrderFlag" name="abnormalOrderFlag" class="input-small" style="width:160px;">
                <c:forEach items="${abnormalOrderFlags}" var="dict">
                    <option value="${dict.value}" <c:out value="${(dict.intValue eq order.abnormalOrderFlag)?'selected=selected':''}" />>${dict.label}</option>
                </c:forEach>
            </select>
        </c:if>
        <label class="label-search">客户单号：</label>
        <input type=text class="input-small" id="parentBizOrderId" name="parentBizOrderId" maxlength="30" value="${order.parentBizOrderId}"/>
        <label>用户：</label>
        <input type=text class="input-mini" id="userName" name="userName" value="${order.userName}" maxlength="20"/>
        <label>用户电话：</label>
        <input type=text class="input-small digits" id="userMobile" name="userMobile" value="${order.userMobile}"
               placeholder="用户电话" maxlength="20"/>
        <c:if test="${currenctDataSource.intValue eq 16}">
            <label>产品类目：</label>
            <input type=text class="input-small" id="productCode" name="productCode" maxlength="30" value="${order.productCode}"/>
            <label>产品型号：</label>
            <input type=text class="input-small" id="productSpec" name="productSpec" maxlength="30" value="${order.productSpec}"/>
        </c:if>
    </div>
    <div style="margin-top:8px">
        <label>创建日期：</label>
        <input id="beginCreateDate" name="beginCreateDate" type="text" readonly="readonly" style="width:95px;"
               maxlength="20" class="input-small Wdate" value="${fns:formatDate(order.beginCreateDate,'yyyy-MM-dd')}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endCreateDate" name="endCreateDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
               class="input-small Wdate" value="${fns:formatDate(order.endCreateDate,'yyyy-MM-dd')}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>&nbsp;&nbsp;
        <input id="btnTransfer" class="btn btn-primary" type="button" value="转换"/>&nbsp;&nbsp;
        <c:if test="${currenctDataSource.intValue eq 2 || currenctDataSource.intValue eq 7 || currenctDataSource.intValue eq 16}">
            <input id="btnCancelBatch" class="btn btn-primary" type="button" value="${APPOINT_AND_CANCEL}"/>&nbsp;&nbsp;
        </c:if>
        <c:if test="${currenctDataSource.intValue eq 2 || currenctDataSource.intValue eq 16}">
            <input id="btnUpdateOrderAbnormalFlagBatch" class="btn btn-primary" type="button" value="刷新"/>&nbsp;&nbsp;
        </c:if>
    </div>
</form:form>
<sys:message content="${message}"/>
<script type="text/javascript">
    $(document).ready(function () {
        var h = $(window).height();
        if ($("#contentTable tbody>tr").length > 0) {
            //无数据报错
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w - 20,
                height: h - 138 - 60,
                fixedCols: 4,
                headerRows: 1,
                colWidths:
                    [40, 60, 160, 80, 145, 140, 120, 120,
                        80, 120, 120, 65, 45, 65, 65,
                        100, 100, 200, 100,
                        70, 125, 160],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h - 138 - 60);//pagination:50
        }
    });
    Order.rootUrl = "${ctx}";
    
    function addCustomerMapping(dataSource,shopId,shopName) {
        top.layer.open({
            type: 2,
            id:"customerShop",
            zIndex:19891015,
            title:"添加客户店铺",
            content: "${ctx}/b2bcenter/md/customer/addCustomerMappingForm?dataSource=" + dataSource +"&shopId="+shopId+"&shopName="+shopName,
            area: ['600px', '550px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    }
    //一键配置类目
    function addProductMapping(dataSource,customerId,shopId,shopName,allProductCode,allProductSpec) {
        top.layer.open({
            type: 2,
            id:"customerShop",
            zIndex:19891015,
            title:"添加产品类目",
            content: "${ctx}/b2bcenter/md/product/addProductMapping?dataSource=" + dataSource+"&customerId=" +customerId +"&shopId="+shopId+"&shopName="+shopName
                       +"&customerCategoryId="+allProductCode +"&productCode="+allProductSpec,
            area: ['600px', '500px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    }
</script>
<c:set var="rowNumber" value="0"/>
<div id="divGrid" style="overflow: auto;margin-right: 5px;">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th width="40"><input type="checkbox" id="selectAll" name="selectAll"/></th>
            <th width="60">序号</th>
            <th width="160">客户单号</th>
            <th width="80">来源</th>
            <th width="145">厂商</th>
            <th width="140">店铺</th>
            <th width="120">商品名称</th>
            <th width="120">产品分类</th>
            <th width="80">品牌</th>
            <th width="120">类目</th>
            <th width="120">型号</th>
            <th width="65">服务类型</th>
            <th width="65">质保</th>
            <th width="45">数量</th>
            <th width="65">买家姓名</th>
            <th width="100">买家电话1</th>
            <th width="100">买家电话2</th>
            <th width="200">买家地址</th>
            <th width="100">处理进度</th>
            <th width="70">处理次数</th>
            <th width="125">上次处理备注</th>
            <th width="160">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="order">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <c:set var="isValid" value="true"/>
            <tr>
                <td><c:if test="${order.isReadOnly == false}">
                    <input type="checkbox" id="cbox${rowNumber}" data-orderno="${order.orderNo}" data-b2borderid="${order.b2bOrderId}"
                           data-datasource="${order.dataSource}" value="${order.orderNo}" name="checkedRecords"/>
                </c:if></td>
                <td>${rowNumber}</td>
                <td>${order.parentBizOrderId}</td>
                <td>${order.dataSourceName}</td>
                <td>${order.customer.name}
                    <c:if test="${empty order.customer.name}">
                        <span class="label status_Canceled">无关联厂商</span>
                        <c:set var="isValid" value="false"/>
                        <br/><br/>
                        <a href="javascript:void(0);" class="btn btn-mini btn-primary"
                           onclick="addCustomerMapping('${order.dataSource}','${fns:urlEncode(order.shopId)}','${fns:urlEncode(order.shopName)}')">配置厂商</a>
                    </c:if>
                </td>
                <td>${order.shopId} <br/>${order.shopName}</td>
                <td>${order.allProductName}</td>
                <td>${order.allClassName}</td>
                <td>${order.allProductBrand}</td>
                <td>${order.allProductCode}
                    <br/><br/>
                    <a href="javascript:void(0);" class="btn btn-mini btn-primary"
                       onclick="addProductMapping('${order.dataSource}','${order.customer.id}','${fns:urlEncode(order.shopId)}','${fns:urlEncode(order.shopName)}',
                                                  '${fns:urlEncode(order.firstProductCode)}','${fns:urlEncode(order.firstProductSpec)}')">一键配置</a>
                </td>
                <td>
                        ${order.allProductSpec}
                    <c:if test="${not empty order.allB2bWarrantyCodes}">
                        <br/>(${order.allB2bWarrantyCodes})
                    </c:if>
                </td>
                <td>${order.firstServiceType}</td>
                <td>${order.firstWarrantyType}</td>
                <td>${order.productQty}</td>
                <td>${order.userName}</td>
                <td>${order.userMobile}</td>
                <td>${order.userPhone}</td>
                <td>${order.userAddress}</td>
                <td>${order.processFlagName}</td>
                <td><p class='${order.processTime gt 0?"text-error":""}'>${order.processTime}</p></td>
                <td><p class="text-error">${order.processComment}</p></td>
                <td>
                    <c:if test="${order.isReadOnly == false}">
                        <a href="javascript:void(0);" class="btn btn-mini btn-warning"
                           onclick="transferManual(${order.dataSource},'${order.b2bOrderId}','${order.orderNo}','${order.quarter}');">${MANUAL_TRANSFER}</a>
                        <c:if test="${canIgnore == true}">
                            <a href="javascript:void(0);" class="btn btn-mini btn-warning"
                               onclick="ignoreOrderTransition(${order.dataSource},'${order.b2bOrderId}','${order.orderNo}');">${IGNORE_AND_HIDE}</a>
                        </c:if>
                        <a href="javascript:void(0);" class="btn btn-mini btn-warning"
                           onclick="cancelOrderTransition(${order.dataSource},'${order.b2bOrderId}','${order.orderNo}','${order.quarter}','${order.processComment}');">${APPOINT_AND_CANCEL}</a>
                        <c:if test="${canDirectlyCancel == true}">
                            <a href="javascript:void(0);" class="btn btn-mini btn-warning"
                               onclick="directlyCancelOrderTransition(${order.dataSource},'${order.b2bOrderId}','${order.orderNo}');">${DIRECTLY_CANCEL}</a>
                        </c:if>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<c:if test="${rowcnt > 0}">
    <div id="pagination" class="pagination">${page}</div>
</c:if>
<script type="text/javascript" language="javascript">
    $(document).ready(function () {
        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity: 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});
    });
</script>
<style type="text/css">
    .dropdown-menu {
        min-width: 80px;
    }

    .dropdown-menu > li > a {
        text-align: left;
        padding: 3px 10px;
    }

    .pagination {
        margin: 4px 0 0 4px;
    }

    .label-search {
        width: 70px;
        text-align: right;
    }

    form {
        margin: 0 0 5px;
    }

    .processFlagError {
        color: red;
    }
</style>
<script type="text/javascript">
    $(document).ready(function () {
        //全选绑定
        $("#selectAll").change(function () {
            var $check = $(this);
            $("div.sFDataInner").find("[name='checkedRecords']").each(function () {
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
<script type="text/javascript">
    function transferManual(dataSource, b2bOrderId, b2bOrderNo, quarter) {
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id: 'layer_b2b_workcard',
            zIndex: 19891016,
            title: 'B2B订单人工处理',
            content: "${ctx}/b2b/b2bcenter/order/manual?dataSource=" + dataSource + "&b2bOrderId=" + (b2bOrderId || '0') + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || ''),
            area: ['1255px', screen.height + 'px'],
            shade: 0.3,
            maxmin: false,
            success: function (layero, index) {
            },
            end: function () {
            }
        });
    }

    function cancelOrderTransition(dataSource, b2bOrderId, b2bOrderNo, quarter, comment) {
        var candelIndex = top.layer.open({
            type: 2,
            id: 'layer_jdCancel',
            zIndex: 19891015,
            title: '取消订单',
            content: "${ctx}/b2b/b2bcenter/order/cancelOrderTransitionForm?dataSource=" + dataSource + "&b2bOrderId=" + (b2bOrderId || '0') + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || '') + "&comment=" + (comment || ''),
            area: ['650px', '300px'],
            shade: 0.3,
            maxmin: false,
            success: function (layero, index) {
            }
        });
    }

    function directlyCancelOrderTransition(dataSource, b2bOrderId, b2bOrderNo) {
        top.layer.confirm('确定要取消工单吗?', {icon: 3, title: '系统确认'}, function (index) {
            top.layer.close(index);
            var loadingIndex = top.layer.msg('正在取消订单，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/b2b/b2bcenter/order/directlyCancelOrderTransition",
                data: {dataSource: dataSource, b2bOrderId: b2bOrderId, b2bOrderNo:b2bOrderNo},
                success: function (data) {
                    top.layer.close(loadingIndex);
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success) {
                        top.layer.close(index);
                        repage();
                        return false;
                    }
                    else {
                        layerError(data.message, '错误提示');
                        repage();
                    }
                },
                error: function (e) {
                    top.layer.close(loadingIndex);
                    ajaxLogout(e.responseText, null, "订单取消错误，请重新查询并取消!");
                }
            });
        });
        return false;
    }

    function ignoreOrderTransition(dataSource, b2bOrderId, b2bOrderNo) {
        top.layer.confirm('确定要忽略工单吗?', {icon: 3, title: '系统确认'}, function (index) {
            top.layer.close(index);
            var loadingIndex = top.layer.msg('正在忽略订单，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/b2b/b2bcenter/order/ignoreOrderTransition",
                data: {dataSource: dataSource, b2bOrderId: b2bOrderId, b2bOrderNo:b2bOrderNo},
                success: function (data) {
                    top.layer.close(loadingIndex);
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success) {
                        top.layer.close(index);
                        repage();
                        return false;
                    }
                    else {
                        layerError(data.message, '错误提示');
                        repage();
                    }
                },
                error: function (e) {
                    top.layer.close(loadingIndex);
                    ajaxLogout(e.responseText, null, "操作失败，请重新查询后再操作!");
                }
            });
        });
        return false;
    }

    $('#btnTransfer').click(function () {
        $btnTransfer = $(this);
        orders = [];
        var datasource;
        var orderNo;
        var b2bOrderId;
        $(".sFDataInner input[type='checkbox'][name='checkedRecords']:checkbox:checked").each(function (index, element) {
            datasource = $(this).data('datasource');
            orderNo = $(this).data("orderno");
            b2bOrderId = $(this).data("b2borderid");
            orders.push({dataSource: datasource, b2bOrderId: b2bOrderId, b2bOrderNo: orderNo});
        });
        if ($btnTransfer.prop("disabled") == true) {
            return false;
        }
        if (orders.length == 0) {
            top.layerError("请选择要进行工单转换的项！", "错误");
            return false;
        }
        top.layer.confirm('确定转换所选的订单吗?', {icon: 3, title: '系统确认'}, function (index) {
            top.layer.close(index);//关闭本身
            // do something
            $btnTransfer.prop("disabled", true);
            var loadingIndex = top.layer.msg('正在转换订单，请稍等...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/b2b/b2bcenter/order/transfer",
                data: JSON.stringify(orders),
                dataType: 'json',
                contentType: "application/json;charset=utf-8",
                success: function (data) {
                    top.layer.close(loadingIndex);
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success) {
                        // layerMsg('处理完成，请查看【本次处理结果】列具体的描述!');
                        var $ul = $("<ul></ul>");
                        $.each(data.data, function (idx, obj) {
                            var $li = $("<li>" + obj.b2bOrderNo + "：" + obj.processComment + "</li>");
                            if (obj.processFlag == 4) {
                                $li.addClass("text-success");
                            }
                            else {
                                $li.addClass("text-error");
                            }
                            $ul.append($li);
                        });

                        layer.open({
                            title: '转换结果',
                            content: $ul.html(),
                            cancel: function (index, layero) {
                                layer.close(index);
                                repage();
                                return false;
                            },
                            yes: function (index, layero) {
                                layer.close(index);
                                repage();
                                return false;
                            }
                        });
                    }
                    else {
                        layerError(data.message, '错误提示');
                    }
                    $btnTransfer.removeAttr('disabled');
                },
                error: function (e) {
                    top.layer.close(loadingIndex);
                    $btnTransfer.removeAttr('disabled');
                    ajaxLogout(e.responseText, null, "订单转换错误，请重新查询并转换!");
                }
            });//end ajax
        });//end confirm
        return false;
    });

    $('#btnCancelBatch').click(function () {
        $btnCancelBatch = $(this);
        orders = [];
        var datasource;
        var orderNo;
        $(".sFDataInner input[type='checkbox'][name='checkedRecords']:checkbox:checked").each(function (index, element) {
            datasource = $(this).data('datasource');
            orderNo = $(this).data("orderno");
            b2bOrderId = $(this).data("b2borderid");
            orders.push({dataSource: datasource, b2bOrderId: b2bOrderId, b2bOrderNo: orderNo});
            // datasource = $(this).data('datasource');
            // orderNo = $(this).data("orderno");
            // b2bOrderId = $(this).data("b2borderid");
            // orders.push({dataSource: datasource, b2bOrderId: b2bOrderId, b2bOrderNo: orderNo});
        });
        if ($btnCancelBatch.prop("disabled") == true) {
            return false;
        }
        if (orders.length == 0) {
            top.layerError("请选择要进行工单取消的项！", "错误");
            return false;
        }
        top.layer.confirm('确定取消所选的订单吗?', {icon: 3, title: '系统确认'}, function (index) {
            top.layer.close(index);//关闭本身
            $btnCancelBatch.prop("disabled", true);
            var loadingIndex = top.layer.msg('正在取消订单，请稍等...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/b2b/b2bcenter/order/cancelOrderTransitionBatch",
                data: JSON.stringify(orders),
                dataType: 'json',
                contentType: "application/json;charset=utf-8",
                success: function (data) {
                    top.layer.close(loadingIndex);
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success) {
                        // layerMsg('处理完成，请查看【本次处理结果】列具体的描述!');
                        var $ul = $("<ul></ul>");
                        $.each(data.data, function (idx, obj) {
                            var $li = $("<li>" + obj.b2bOrderNo + "：" + obj.processComment + "</li>");
                            if (obj.processFlag == 4) {
                                $li.addClass("text-success");
                            }
                            else {
                                $li.addClass("text-error");
                            }
                            $ul.append($li);
                        });

                        layer.open({
                            title: '取消结果',
                            content: $ul.html(),
                            cancel: function (index, layero) {
                                layer.close(index);
                                repage();
                                return false;
                            },
                            yes: function (index, layero) {
                                layer.close(index);
                                repage();
                                return false;
                            }
                        });
                    }
                    else {
                        layerError(data.message, '错误提示');
                    }
                    $btnCancelBatch.removeAttr('disabled');
                },
                error: function (e) {
                    top.layer.close(loadingIndex);
                    $btnCancelBatch.removeAttr('disabled');
                    ajaxLogout(e.responseText, null, "订单取消错误，请重新查询并取消!");
                }
            });//end ajax
        });//end confirm
        return false;
    });

    $('#btnUpdateOrderAbnormalFlagBatch').click(function () {
        $btnUpdateOrderAbnormalFlagBatch = $(this);
        var datasource  = ${currenctDataSource.intValue};
        if ($btnUpdateOrderAbnormalFlagBatch.prop("disabled") == true) {
            return false;
        }
        top.layer.confirm('确定更新所有订单的状态吗?', {icon: 3, title: '系统确认'}, function (index) {
            top.layer.close(index);
            $btnUpdateOrderAbnormalFlagBatch.prop("disabled", true);
            var loadingIndex = top.layer.msg('正在更新订单，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/b2b/b2bcenter/order/updateOrderAbnormalFlagBatch?dataSource="+datasource,
                dataType: 'json',
                contentType: "application/json;charset=utf-8",
                success: function (data) {
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    setTimeout(function() {
                        if (data.success){
                            layerMsg("工单刷新成功");
                        }
                        else{
                            layerError(data.message, "刷新失败")
                            // top.$.jBox.error(data.message, '操作失败');
                        }
                        $btnUpdateOrderAbnormalFlagBatch.removeAttr('disabled');
                        repage();
                        top.layer.close(loadingIndex);
                    }, 500);
                },
                error: function (e) {
                    top.layer.close(loadingIndex);
                    $btnUpdateOrderAbnormalFlagBatch.removeAttr('disabled');
                    ajaxLogout(e.responseText, null, "订单更新错误，请重试!");
                }
            });//end ajax
        });//end confirm
        return false;
    });
</script>
</body>
</html>

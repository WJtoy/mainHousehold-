<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>订单退单明细</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
<%--    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>--%>
<%--    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>--%>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/common/moment.min.js" type="text/javascript"></script>
    <script type="text/javascript">
        function datePicker(startId, endId) {
            $('#' + startId).unbind("click");
            $('#' + startId).bind("click", function () {
                WdatePicker({
                    readOnly: true,
                    dateFmt: 'yyyy-MM-dd',
                    isShowClear: false | false,
                    maxDate: '#F{$dp.$D(\'' + endId + '\')||\'%y-%M-%d\'}',
                    minDate: '#F{$dp.$D(\'' + endId + '\',{M:-3,d:+1})}'
                });
            });

            $('#' + endId).unbind("click");
            $('#' + endId).bind("click", function () {
                WdatePicker({
                    dateFmt: 'yyyy-MM-dd',
                    isShowClear: false | false,
                    minDate: '#F{$dp.$D(\'' + startId + '\')||\'%y-%M-%d\'}',
                    maxDate: '#F{$dp.$D(\'' + startId + '\',{M:3,d:-1})}'
                });
            });
        }
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            $(".triggle").on('hover', function(){
                $(".border").css({
                    display:"block"
                })
            })
            $(".triggle").on('mouseleave', function(){
                $(".border").css({
                    display:"none"
                })
            })
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"vertical-align": "middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});

            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/cancelledOrderNew/cancelledOrderNewReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/cancelledOrderNew/checkExportTask?" + (new Date()).getTime(),
                    data: $(searchForm).serialize(),
                    success: function (data) {
                        if (ajaxLogout(data)) {
                            return false;
                        }
                        if (data && data.success == true) {
                            top.$.jBox.closeTip();
                            top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
                                if (v == "ok") {
                                    top.$.jBox.tip('请稍候...', 'loading');
                                    $.ajax({
                                        type: "POST",
                                        url: "${ctx}/rpt/provider/cancelledOrderNew/export?" + (new Date()).getTime(),
                                        data: $(searchForm).serialize(),
                                        success: function (data) {
                                            if (ajaxLogout(data)) {
                                                return false;
                                            }
                                            if (data && data.success == true) {
                                                top.$.jBox.closeTip();
                                                top.$.jBox.tip(data.message, "success");
                                                $('#btnExport').removeAttr('disabled');
                                                return false;
                                            }
                                            else if (data && data.message) {
                                                top.$.jBox.error(data.message, "导出错误");
                                            }
                                            else {
                                                top.$.jBox.error("导出错误", "错误提示");
                                            }
                                            $('#btnExport').removeAttr('disabled');
                                            top.$.jBox.closeTip();
                                            return false;
                                        },
                                        error: function (e) {
                                            $('#btnExport').removeAttr('disabled');
                                            ajaxLogout(e.responseText, null, "导出错误，请重试!");
                                            top.$.jBox.closeTip();
                                        }
                                    });
                                }
                            }, {buttonsFocus: 1});
                            $('#btnExport').removeAttr('disabled');
                            top.$.jBox.closeTip();
                            return false;
                        }
                        else if (data && data.message) {
                            top.$.jBox.error(data.message, "导出错误");
                        }
                        else {
                            top.$.jBox.error("导出错误", "错误提示");
                        }
                        $('#btnExport').removeAttr('disabled');
                        top.$.jBox.closeTip();
                        return false;
                    },
                    error: function (e) {
                        $('#btnExport').removeAttr('disabled');
                        ajaxLogout(e.responseText, null, "导出错误，请重试!");
                        top.$.jBox.closeTip();
                    }
                });
                top.$('.jbox-body .jbox-icon').css('top', '55px');
            });
        });
    </script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .parent:after{
            content:"";
            height:0;
            line-height:0;
            display:block;
            visibility:hidden;
            clear:both;
        }

        .target{
            display:none;
            z-index: 4;
        }

        .triggle:hover + .target {
            display: block;
        }

        .border{
            display: none;
            opacity: 0.8;
            width: 0 !important;
            border-bottom:solid 12px #1B1E24;
            border-left:12px solid transparent;
            border-right: 6px solid transparent;
            boder-top: 0px solid transparent;
        }
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">每日退单明细</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/cancelledOrderNew/cancelledOrderNewReport" method="post"
           class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>客　　户：</label>
        <select id="customerId" name="customerId" class="input-small" style="width:225px;">
            <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
<%--        &nbsp;&nbsp;--%>
<%--        <label>下单日期：</label>--%>
<%--        <input id="beginPlanDate" name="beginPlanDate" type="text" readonly="readonly"--%>
<%--               style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"--%>
<%--               value="<fmt:formatDate value='${rptSearchCondition.beginPlanDate}' pattern='yyyy-MM-dd' type='date'/>"--%>
<%--               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>--%>
<%--        <label>~</label>--%>
<%--        &nbsp;&nbsp;&nbsp;--%>
<%--        <input id="endPlanDate" name="endPlanDate" type="text" readonly="readonly" style="width:98px" maxlength="20"--%>
<%--               class="input-small Wdate"--%>
<%--               value="<fmt:formatDate value='${rptSearchCondition.endPlanDate}' pattern='yyyy-MM-dd' type='date'/>"--%>
<%--               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>--%>
        &nbsp;&nbsp;
        <label>退单类型：</label>
        <c:set var="cancelResponsibleList" value="${fns:getDictListFromMS('cancel_responsible')}" /><!-- 切换为微服务 -->
        <select id="cancelResponsible" name="cancelResponsible" style="width:200px;">
            <option value="" selected="selected">所有</option>
            <c:forEach items="${cancelResponsibleList}" var="Dict">
                <option value="${Dict.value}" <c:out
                        value="${(rptSearchCondition.cancelResponsible eq Dict.value)?'selected=selected':''}"/>>${Dict.label}</option>
            </c:forEach>
        </select>
    </div>
    <div style="margin-top:8px">
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>退单日期：</label>
        <input id="beginCancelApplyDate" name="beginCancelApplyDate" type="text" readonly="readonly"
               style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="${fns:formatDate(rptSearchCondition.beginCancelApplyDate,'yyyy-MM-dd')}"/>
        <label>~</label>
        &nbsp;&nbsp;&nbsp;
        <input id="endCancelApplyDate" name="endCancelApplyDate" type="text" readonly="readonly" style="width:98px"
               maxlength="20" class="input-small Wdate"
               value="${fns:formatDate(rptSearchCondition.endCancelApplyDate,'yyyy-MM-dd')}"/>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:cancelledOrderNew:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:cancelledOrderNew:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 150px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据(5分钟延迟) <br/>
                    统计方式：退单时间
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>

</form:form>
<sys:message content="${message}"/>
<%--<script type="text/javascript">--%>
<%--    $(document).ready(function () {--%>
<%--        var h = $(window).height();--%>
<%--        if ($("#contentTable tbody>tr").length > 0) {--%>
<%--            //无数据报错--%>
<%--            var w = $(window).width();--%>
<%--            $("#contentTable").toSuperTable({--%>
<%--                width: w - 10,--%>
<%--                height: h - 230,--%>
<%--                fixedCols: 1,--%>
<%--                headerRows: 1,--%>
<%--                colWidths:--%>
<%--                    [60, 140, 140,140,140, 60, 300],--%>
<%--                onStart: function () {--%>
<%--                },--%>
<%--                onFinish: function () {--%>
<%--                }--%>
<%--            });--%>
<%--        }--%>
<%--        else {--%>
<%--            $("#divGrid").css("height", h - 230)--%>
<%--        }--%>
<%--    });--%>
<%--</script>--%>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="table-layout: fixed;">
        <thead>
        <tr>
            <th width="60">序号</th>
            <th width="140">客户名称</th>
            <th width="140">店铺名称</th>
            <th width="140">单号</th>
            <th width="140">第三方单号</th>
            <th width="60">状态</th>
            <th width="600">详情</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="totalCount" value="0"/>
        <c:set var="totalCharge" value="0"/>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <tr>
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <td>${rowIndex}</td>
            <td>${item.customer.name}</td>
            <td>${item.shop.label}</td>
            <td><a href="javascript:void(0);" onclick="Order.viewOrderDetail('${item.orderId}');">
                    <abbr title="点击查看订单详情">${item.orderNo}</abbr> </a>
                </a>
            </td>
            <td >${item.parentBizOrderId}</td>
            <td ><span
                    class="label status_${item.status.label}">${item.status.label}</span></td>
            <td class="autocut"><a href="javascript:" data-toggle="tooltip"
                                                              data-tooltip="${item.kefu.name},${item.kefu.phone},${item.userPhone},${item.cancelApplyComment}">${item.kefu.name},${item.kefu.phone},${item.userPhone},${fns:abbr(item.cancelApplyComment,40)}</a>
            </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <style type="text/css">
        .autocut {
            min-width: 40px;
            overflow: hidden;
            white-space: nowrap;
        }
    </style>
</div>
<div class="pagination">${page}</div>
<script type="text/javascript">
    datePicker('beginCancelApplyDate', 'endCancelApplyDate');
</script>
</body>
</html>

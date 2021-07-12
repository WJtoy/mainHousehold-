<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE HTML>
<html>
<head>
    <title>工单时效统计</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
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
    <script type="text/javascript" language="javascript">
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
                $("#searchForm").attr("action", "${ctx}/customer/rpt/customerOrderTime/customerOrderTimeReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/customer/rpt/customerOrderTime/checkExportTask?" + (new Date()).getTime(),
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
                                        url: "${ctx}/customer/rpt/customerOrderTime/export?" + (new Date()).getTime(),
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
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">工单时效统计</a></li>
    <li>
        <a href="${ctx}/customer/rpt/customerOrderTime/customerOrderTimeChart">工单时效图表</a>
    </li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/customer/rpt/customerOrderTime/customerOrderTimeReport" method="post" class="breadcrumb form-search">
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>下单时间：</label>
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false,maxDate:'${rptSearchCondition.endPlanDate}'});"/>
        &nbsp;&nbsp;
        <c:set var="serviceTypeList" value="${fns:getDictListFromMS('order_service_type')}" />
        <label>工单类型：</label>
        <select id="orderServiceType" name="orderServiceType" style="width:100px;">
            <option value="" selected="selected">所有</option>
            <c:forEach items="${serviceTypeList}" var="serviceTypeDict">
                <option value="${serviceTypeDict.value}" <c:out value="${(rptSearchCondition.orderServiceType eq serviceTypeDict.value)?'selected=selected':''}" />>${serviceTypeDict.label}</option>
            </c:forEach>
        </select>
        <c:if test="${currentuser.isCustomer() || currentuser.isSaleman()}">
            &nbsp;&nbsp;
            <shiro:hasPermission name="rpt:customer:customerOrderTimeReport:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询" /></shiro:hasPermission>
            &nbsp;&nbsp;
            <shiro:hasPermission name="rpt:customer:customerOrderTimeReport:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
        </c:if>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据<br/>
                    统计方式：客评时间<br/>
                    栏位说明：<br/>
                    【完成率】完成工单数量/每日派单数量*100%
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
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
                width: w - 10,
                height: h - 168,
                fixedCols: 1,
                headerRows: 3,
                colWidths:
                    [100, 110, 110, 130, 130,
                        130, 130, 130, 130, 130, 130,
                        130, 130, 110
                    ],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h - 168);
        }

    });
</script>

<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-bordered table-condensed table-hover"
           style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">

        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="100"></th>

            <th width="110"></th>
            <th width="110"></th>

            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="110"></th>

        </tr>
        <tr>
            <th rowspan="2">天/月</th>
            <th colspan="2">派单时效</th>
            <th colspan="10">结单时效</th>
            <th rowspan="2">完成合计(单)</th>


        </tr>
        <tr>
            <th>小于3小时</th>
            <th>大于3小时</th>

            <th>小于12小时</th>
            <th>小于12小时完成率</th>
            <th>小于24小时</th>
            <th>小于24小时完成率</th>
            <th>小于48小时</th>
            <th>小于48小时完成率</th>
            <th>小于72小时</th>
            <th>小于72小时完成率</th>
            <th>大于72小时</th>
            <th>大于72小时完成率</th>

        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>

        <c:forEach items="${rptSearchCondition.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <c:if test="${rowIndex<=31}">
                <tr>
                    <td>
                            ${item.orderCreateDate}
                    </td>
                    <td>
                            ${item.planTimeLessSix}
                    </td>
                    <td>
                            ${item.planTimeMoreSix}
                    </td>
                    <td>
                            ${item.closeLessTwelve}
                    </td>
                    <td>
                            ${item.lessTwelveProportion}%
                    </td>
                    <td>
                            ${item.closeTwelveBetweenOneDay + item.closeLessTwelve}
                    </td>
                    <td>
                            ${item.less24Proportion}%
                    </td>

                    <td>
                            ${item.closeOneDayBetweenTwoDay + item.closeTwelveBetweenOneDay + item.closeLessTwelve}
                    </td>
                    <td>
                            ${item.less48Proportion}%
                    </td>
                    <td>
                            ${item.closeTwoDayBetweenThreeDay + item.closeOneDayBetweenTwoDay + item.closeTwelveBetweenOneDay + item.closeLessTwelve}
                    </td>
                    <td>
                            ${item.less72Proportion}%
                    </td>

                    <td>
                            ${item.closeMoreThreeDay}
                    </td>
                    <td>
                            ${item.moreThreeDayProportion}%
                    </td>
                    <td>
                            ${item.closeSum}
                    </td>
                </tr>
            </c:if>
        </c:forEach>
        <c:if test="${rptSearchCondition.list.size()>0}">
            <tr>
                <td>合计</td>
                <c:set var="size" value="${rptSearchCondition.list.size()}"/>
                <c:set var="item" value="${rptSearchCondition.list[size-1]}"/>

                <td>${item.planTimeLessSix}</td>
                <td>${item.planTimeMoreSix}</td>
                <td>${item.closeLessTwelve}</td>
                <td>${item.lessTwelveProportion}%</td>
                <td>${item.closeTwelveBetweenOneDay}</td>
                <td>${item.twelveBetweenOneDayProportion}%</td>
                <td>${item.closeOneDayBetweenTwoDay}</td>
                <td>${item.oneDayBetweenTwoDayProportion}%</td>
                <td>${item.closeTwoDayBetweenThreeDay}</td>
                <td>${item.twoDayBetweenThreeDayProportion}%</td>
                <td>${item.closeMoreThreeDay}</td>
                <td>${item.moreThreeDayProportion}%</td>
                <td>${item.closeSum}</td>

            </tr>
        </c:if>
        </tbody>
    </table>

</div>
</body>
</html>

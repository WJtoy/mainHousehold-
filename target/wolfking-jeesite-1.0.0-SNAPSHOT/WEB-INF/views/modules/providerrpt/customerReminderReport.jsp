<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>CustomerChageOrder</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
<%--    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>--%>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        a:LINK { /**连接文字本身的颜色**/
            color: #333333
        }

        a:VISITED { /**连接文字被点击后的颜色**/
            color: #333333;
        }

        a:HOVER { /**鼠标移到连接文字上，文字的颜色**/
            color: #0000ff;
            text-decoration: underline;
        }

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
    <script type="text/javascript" language="JavaScript">
        function validate(f) {
            if (f.customerId.value == "") {
                top.$.jBox.error("请选择客户！", "错误提示");
                top.$.jBox.closeTip();
                return false;
            } else {
                return true;
            }

        }
    </script>
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
                $("#searchForm").attr("action", "${ctx}/rpt/provider/customerReminder/customerReminderReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/customerReminder/checkExportTask?" + (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/customerReminder/export?" + (new Date()).getTime(),
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

    <li class="active"><a href="javascript:void(0);">客户每日催单</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<c:set var="mdcustomerID" value="${currentuser.getCustomerAccountProfile().getCustomer().getId()}"></c:set>
<form:form id="searchForm" modelAttribute="rptSearchCondition" method="post" class="breadcrumb form-search"
           onsubmit="return validate(this)">
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="hidden" id="customerId" name="customerId" value="${mdcustomerID}" maxlength="50"
                       style="width:345px;"/>
                <input type="hidden" id="customerName" name="customerName" value="${mdcustomerName}" maxlength="50"
                       style="width:105px;"/>
            </c:when>
            <c:otherwise>
                <label>客 户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:225px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>
                        所有
                    </option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out
                                value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
                    </c:forEach>
                </select>
                <span class="add-on red">必选*</span>
            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;
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
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out
                        value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}"/>>${year}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;

        <label>月份：</label>
        <select id="month" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${i+1}" <c:out
                        value="${(rptSearchCondition.selectedMonth eq i+1)?'selected=selected':''}"/>>${i+1}</option>
            </c:forEach>
        </select>

        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 400px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据 <br/>
                    统计方式：催单时间 <br/>
                    栏位说明：<br/>
                    【占比】日催单数/每日接单量 * 100% <br/>
                    【一次催单数】当天1次催单次数 <br/>
                    【多次催单】催单次数超过1为多次催单 <br/>
                    【超48时催单】当天催单时间-下单时间 > 48 <br/>
                    【24小时完成率】当天完工数((完工时间-首次催单时间)<24)/催单量*100% <br/>
                    【超时48小时完成率】：当天完成数((完工时间-首次催单时间<24) 且(最后催单时间-下单时间>48))/催单量*100% <br/>
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>


        </div>

    </div>

</form:form>
<sys:message content="${message}"/>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-striped table-bordered table-condensed"
           style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">
        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="100"></th>

            <th width="200"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="120"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="80"></th>
            <th width="100"></th>
            <th width="100"></th>

        </tr>
        <tr>
            <th>日期</th>
            <th>客户</th>
            <th>每日接单量</th>
            <th>日催单数</th>
            <th>占比</th>
            <th>一次催单数</th>
            <th>占比</th>
            <th>多次催单数</th>
            <th>占比</th>
            <th>超48时催单</th>
            <th>占比</th>
            <th>24小时完成率</th>
            <th>超时48时完成率</th>
        </tr>
        </thead>
        <tbody>

        <c:set var="orderNewQty" value="0"/>
        <c:set var="reminderQty" value="0"/>
        <c:set var="reminderFirstQty" value="0"/>
        <c:set var="reminderMultipleQty" value="0"/>
        <c:set var="reminderOrderQty" value="0"/>
        <c:set var="exceed48hourReminderQty" value="0"/>
        <c:set var="complete24hourQty" value="0"/>
        <c:set var="over48ReminderCompletedQty" value="0"/>

        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${list}" var="item">
            <tr>
            <td><fmt:formatDate value="${item.statisticsDate}" pattern="yyyy-MM-dd"/></td>
            <td>${item.customerName}</td>
            <td>${item.orderNewQty}</td>
            <td>${item.reminderQty}</td>
            <td>${item.reminderRate}%</td>
            <td>${item.reminderFirstQty}</td>
            <td>${item.reminderFirstRate}%</td>
            <td>${item.reminderMultipleQty}</td>
            <td>${item.reminderMultipleRate}%</td>
            <td>${item.exceed48hourReminderQty}</td>
            <td>${item.exceed48hourReminderRate}%</td>
            <td>${item.complete24hourRate}%</td>
            <td>${item.over48ReminderCompletedRate}%</td>
                <c:set var="orderNewQty" value="${orderNewQty+item.orderNewQty}"/>
                <c:set var="reminderQty" value="${reminderQty+item.reminderQty}"/>
                <c:set var="reminderFirstQty" value="${reminderFirstQty+item.reminderFirstQty}"/>
                <c:set var="reminderMultipleQty" value="${reminderMultipleQty+item.reminderMultipleQty}"/>
                <c:set var="reminderOrderQty" value="${reminderOrderQty+item.reminderOrderQty}"/>
                <c:set var="exceed48hourReminderQty" value="${exceed48hourReminderQty+item.exceed48hourReminderQty}"/>
                <c:set var="complete24hourQty" value="${complete24hourQty+item.complete24hourQty}"/>
                <c:set var="over48ReminderCompletedQty" value="${over48ReminderCompletedQty+item.over48ReminderCompletedQty}"/>
            </tr>
        </c:forEach>

        <c:if test="${list.size()>0}">
            <tr>
                <td colspan="2"><B>合计</B></td>
                <td>${orderNewQty}</td>
                <td>${reminderQty}</td>
                <c:choose>
                    <c:when test="${orderNewQty != 0}">
                        <td><fmt:formatNumber pattern="0.00" value="${reminderQty / orderNewQty*100}"/>%</td>
                    </c:when>
                    <c:otherwise>
                        <td>0.00%</td>
                    </c:otherwise>
                </c:choose>
                <td>${reminderFirstQty}</td>
                <c:choose>
                    <c:when test="${orderNewQty != 0}">
                        <td><fmt:formatNumber pattern="0.00" value="${reminderFirstQty/orderNewQty*100}"/>%</td>
                    </c:when>
                    <c:otherwise>
                        <td>0.00%</td>
                    </c:otherwise>
                </c:choose>

                <td>${reminderMultipleQty}</td>

                <c:choose>
                    <c:when test="${orderNewQty != 0}">
                        <td><fmt:formatNumber pattern="0.00" value="${reminderMultipleQty/orderNewQty*100}"/>%</td>
                    </c:when>
                    <c:otherwise>
                        <td>0.00%</td>
                    </c:otherwise>
                </c:choose>

                <td>${exceed48hourReminderQty}</td>
                <c:choose>
                    <c:when test="${orderNewQty != 0}">
                        <td><fmt:formatNumber pattern="0.00" value="${exceed48hourReminderQty/orderNewQty*100}"/>%</td>
                    </c:when>
                    <c:otherwise>
                        <td>0.00%</td>
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${reminderOrderQty != 0}">
                        <td><fmt:formatNumber pattern="0.00" value="${complete24hourQty/reminderOrderQty*100}"/>%</td>
                        <td><fmt:formatNumber pattern="0.00" value="${over48ReminderCompletedQty/reminderOrderQty*100}"/>%</td>
                    </c:when>
                    <c:otherwise>
                        <td>0.00%</td>
                        <td>0.00%</td>
                    </c:otherwise>
                </c:choose>

            </tr>
        </c:if>
        </tbody>
    </table>
    <style type="text/css">
        .autocut {min-width:40px;overflow:hidden;white-space:nowrap;}
    </style>
</div>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>网点对账单</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td, .table tbody th {
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
            if (f.servicePointId.value == null || f.servicePointId.value == ""|| f.servicePointId.value == "0") {
                top.$.jBox.info('请选择服务网点','网点明细对帐');
                return false;
            }
            else {
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
                $("#pageNo").val(1);
                $("#searchForm").attr("action", "${ctx}/finance/rpt/detailedReconciliation/getNetworkReconciliation");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                if ( $("#servicePointId").val() == null ||  $("#servicePointId").val() == ""||  $("#servicePointId").val() == "0") {
                    top.$.jBox.info('请选择服务网点','网点明细对帐');
                    return false;
                }
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/finance/rpt/detailedReconciliation/checkExportTask?" + (new Date()).getTime(),
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
                                        url: "${ctx}/finance/rpt/detailedReconciliation/export?" + (new Date()).getTime(),
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
                                            } else if (data && data.message) {
                                                top.$.jBox.error(data.message, "导出错误");
                                            } else {
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
                        } else if (data && data.message) {
                            top.$.jBox.error(data.message, "导出错误");
                        } else {
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
<c:set var="currentuser" value="${fns:getUser() }"/>
<c:set var="userId" value="${currentuser.getId()}"></c:set>
<c:set var="userName" value="${currentuser.getName()}"></c:set>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">网点对账单</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/finance/rpt/detailedReconciliation/getNetworkReconciliation" method="post" class="breadcrumb form-search" onsubmit="return validate(this)">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <c:choose>
            <c:when test="${currentuser.isEngineer()}">
                <input id="servicePointId" type="hidden" name="servicePointId" value="${userId}" maxlength="50"
                       style="width:245px;"/>
                <input id="servicePointName" type="hidden" name="servicePointNo.name" value="${userName}" maxlength="50"
                       style="width:245px;"/>
            </c:when>
            <c:otherwise>
                <label class="control-label">服务网点：</label>
                <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                          labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                          width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>
                <span class="add-on red">必选*</span>
            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0">所有</option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
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
        <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${i+1}" <c:out
                        value="${(rptSearchCondition.selectedMonth eq i+1)?'selected=selected':''}"/>>${i+1}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:getNetworkReconciliation:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:getNetworkReconciliation:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
    </div>

    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据<br/>
                    统计方式：审单时间<br/>
                    栏位说明：<br/>
                    【本月完工单未付】上月完工单未付+本月完工单金额-本月已付金额<br/>
                    【合计金额】服务费+配件费+远程费+快递费+其他费+互助基金+时效奖励+厂商时效+加急费+扣点+平台费+质保金额+好评费
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}"/>

<table id="treetable" class="fancyTable datatable table table-bordered table-condensed table-hover" style="table-layout:fixed"
       cellspacing="0" width="100%">
    <thead>
    <tr>
        <th>上月完工单未付（元）</th>
        <th>本月完工单金额（元）</th>
        <th>本月已付金额（元）</th>
        <th>本月完工单未付（元）</th>
    </tr>
    </thead>
    <tbody>
    <c:if test="${page.list[0] != null}">
        <tr>
            <td><fmt:formatNumber pattern="0.00" value="${page.list[0].preBalance}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${page.list[0].payableAmount}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${page.list[0].paidAmount}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${page.list[0].theBalance}"/></td>
        </tr>
    </c:if>
    </tbody>
</table>
<script type="text/javascript">
    $(document).ready(function () {
        var h = $(window).height();
        if ($("#contentTable tbody>tr").length > 0) {
            //无数据报错
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w - 10,
                height: h - 298,
                fixedCols: 2,
                headerRows: 1,
                colWidths:
                    [40,
                        140, 80, 100, 300,
                        100, 50, 40, 150, 100, 100, 100, 80, 80,
                        80, 80, 80, 80, 80, 80, 80, 80,80,80,80,80,
                        80, 80, 80, 80, 100,
                        200],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        } else {
            $("#divGrid").css("height", h - 298);
        }

    });
</script>
<div id="divGrid" style="overflow-x:auto;">

    <table id="contentTable" class="table  table-bordered table-condensed table-hover" style="table-layout:fixed;">
        <thead>
        <th width="40">序号</th>

        <th width="140">接单编码</th>
        <th width="80">用户名</th>
        <th width="100">用户电话</th>
        <th width="300">用户地址</th>

        <th width="100">产品</th>
        <th width="50">项目</th>
        <th width="40">台数</th>
<%--        <th width="200">反馈问题</th>--%>
        <th width="150">服务网点</th>
        <th width="100">实际上门人员</th>
        <th width="100">预约上门时间</th>
        <th width="100">完成日期</th>
        <th width="80">是否完成</th>
        <th width="80">上门次数</th>

        <th width="80">服务费</th>
        <th width="80">配件费</th>
        <th width="80">远程费</th>
        <th width="80">快递费</th>
        <th width="80">其他费</th>
        <th width="80">互助基金</th>
        <th width="80">时效奖励</th>
        <th width="80">厂商时效</th>
        <th width="80">加急费</th>
        <th width="80">扣点</th>
        <th width="80">平台费</th>
        <th width="80">质保金额</th>
        <th width="80">好评费</th>
        <th width="80">合计金额</th>
        <th width="80">状态</th>
        <th width="80">结算方式</th>
        <th width="100">付款日期</th>
        <th width="200">备注</th>
        </thead>
        <tbody>

        <c:set var="totalQty" value="0"/>
        <c:set var="totalServiceTimes" value="0"/>
        <c:set var="totalServiceCharge" value="0"/>
        <c:set var="totalMaterialCharge" value="0"/>
        <c:set var="totalTravelCharge" value="0"/>
        <c:set var="totalExpressCharge" value="0"/>
        <c:set var="totalOtherCharge" value="0"/>
        <c:set var="totalWriteOffCharge" value="0"/>
        <c:set var="totalCharge" value="0"/>
        <c:set var="totalInsurance" value="0"/>
        <c:set var="totalTimeLinessCharge" value="0"/>
        <c:set var="totalCustomerTimeLinessCharge" value="0"/>
        <c:set var="totalEngineerUrgentCharge" value="0"/>
        <c:set var="engineerPraiseFee" value="0"/>
        <c:set var="engineerTaxFee" value="0"/>
        <c:set var="engineerInfoFee" value="0"/>
        <c:set var="engineerDeposit" value="0"/>
        <c:set var="rowIndex" value="0"/>



        <c:forEach items="${page.list[0].list}" var="item">

            <tr>
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <td rowspan="${item.maxRow}">${rowIndex}</td>

            <td rowspan="${item.maxRow}">${item.orderNo}</td>
            <td rowspan="${item.maxRow}">${item.userName}</td>
            <td rowspan="${item.maxRow}">${item.userPhone}</td>
            <td rowspan="${item.maxRow}"><a href="javascript:" data-toggle="tooltip"
                                            data-tooltip="${item.userAddress}">${fns:abbr(item.userAddress,40)}</a></td>

            <c:forEach begin="0" end="${item.maxRow-1}" var="i">
                <c:if test="${i ne 0}">
                    <tr>
                </c:if>
                <c:choose>
                    <c:when test="${i lt item.details.size()}">
                        <td>${item.details.get(i).product.name}</td>
                        <td>${item.details.get(i).serviceType.name}</td>
                        <td>${item.details.get(i).qty}</td>
                        <c:set var="totalQty" value="${totalQty+item.details.get(i).qty}"/>
                    </c:when>
                    <%--<c:otherwise>--%>
                    <%--<td></td>--%>
                    <%--<td></td>--%>
                    <%--<td></td>--%>
                    <%--</c:otherwise>--%>
                </c:choose>

                <c:if test="${i eq 0}">
                    <td rowspan="${item.maxRow}">${item.servicePoint.name}</td>
                </c:if>


                <td>
                    <c:choose>
                        <c:when test="${i lt item.details.size()}">
                            ${item.details.get(i).engineer.name}
                        </c:when>
                    </c:choose>
                </td>


                <c:if test="${i eq 0}">
                    <td rowspan="${item.maxRow}"><fmt:formatDate value="${item.appointmentDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                    <td rowspan="${item.maxRow}"><fmt:formatDate value="${item.closeDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                    <td rowspan="${item.maxRow}">
                        <c:choose>
                            <c:when test="${item.closeDate != null}">是</c:when>
                            <c:otherwise>否</c:otherwise>
                        </c:choose>
                    </td>
                </c:if>

                <c:choose>
                    <c:when test="${i lt item.details.size()}">
                        <td>${item.details.get(i).serviceTimes}</td>
                        <td>${item.details.get(i).engineerServiceCharge}</td>
                        <td>${item.details.get(i).engineerMaterialCharge}</td>
                        <td>${item.details.get(i).engineerTravelCharge}</td>
                        <td>${item.details.get(i).engineerExpressCharge}</td>
                        <td>${item.details.get(i).engineerOtherCharge}</td>
                        <c:set var="totalServiceTimes" value="${totalServiceTimes + item.details.get(i).serviceTimes}"/>
                        <c:set var="totalServiceCharge" value="${totalServiceCharge + item.details.get(i).engineerServiceCharge}"/>
                        <c:set var="totalMaterialCharge" value="${totalMaterialCharge + item.details.get(i).engineerMaterialCharge}"/>
                        <c:set var="totalTravelCharge" value="${totalTravelCharge + item.details.get(i).engineerTravelCharge}"/>
                        <c:set var="totalExpressCharge" value="${totalExpressCharge + item.details.get(i).engineerExpressCharge}"/>
                        <c:set var="totalOtherCharge" value="${totalOtherCharge +item.details.get(i).engineerOtherCharge}"/>
                    </c:when>
                </c:choose>


                <c:if test="${i eq 0}">
                    <td rowspan="${item.maxRow}">${item.engineerInsuranceCharge}</td>
                    <td rowspan="${item.maxRow}">${item.engineerCustomerTimelinessCharge}</td>
                    <td rowspan="${item.maxRow}">${item.engineerTimelinessCharge}</td>
                    <td rowspan="${item.maxRow}">${item.engineerUrgentCharge}</td>
                    <td rowspan="${item.maxRow}">${item.engineerTaxFee}</td>
                    <td rowspan="${item.maxRow}">${item.engineerInfoFee}</td>
                    <td rowspan="${item.maxRow}">${item.engineerDeposit}</td>
                    <td rowspan="${item.maxRow}">${item.engineerPraiseFee}</td>
                    <td rowspan="${item.maxRow}">${item.engineerTotalCharge+item.engineerInsuranceCharge+item.engineerUrgentCharge+item.engineerTimelinessCharge+item.engineerCustomerTimelinessCharge+item.engineerTaxFee+item.engineerInfoFee+item.engineerDeposit+item.engineerPraiseFee}</td>
                    <td rowspan="${item.maxRow}"><span class="label status_Completed">${item.status.label}</span></td>
                    <td rowspan="${item.maxRow}">${item.paymentType.label}</td>
                    <td rowspan="${item.maxRow}"></td>
                    <c:set var="totalInsurance" value="${totalInsurance + item.engineerInsuranceCharge}"/>
                    <c:set var="totalTimeLinessCharge"
                           value="${totalTimeLinessCharge + item.engineerCustomerTimelinessCharge}"/>
                    <c:set var="totalCustomerTimeLinessCharge"
                           value="${totalCustomerTimeLinessCharge+item.engineerTimelinessCharge}"/>
                    <c:set var="totalEngineerUrgentCharge"
                           value="${totalEngineerUrgentCharge+item.engineerUrgentCharge}"/>
                    <c:set var="engineerTaxFee"
                           value="${engineerTaxFee+item.engineerTaxFee}"/>
                    <c:set var="engineerInfoFee"
                           value="${engineerInfoFee+item.engineerInfoFee}"/>
                    <c:set var="engineerDeposit"
                           value="${engineerDeposit+item.engineerDeposit}"/>
                    <c:set var="engineerPraiseFee"
                           value="${engineerPraiseFee+item.engineerPraiseFee}"/>
                    <c:set var="totalCharge"
                           value="${totalCharge+item.engineerTotalCharge +item.engineerInsuranceCharge+item.engineerUrgentCharge+item.engineerTimelinessCharge+item.engineerCustomerTimelinessCharge+item.engineerTaxFee+item.engineerInfoFee+item.engineerDeposit+item.engineerPraiseFee}"/>
                </c:if>

                <c:choose>
                    <c:when test="${i lt item.details.size()}">
                        <td><a href="javascript:" data-toggle="tooltip"
                               data-tooltip="${item.details.get(i).remarks}">${fns:abbr(item.details.get(i).remarks,40)}</a>
                        </td>
                    </c:when>
                    <c:otherwise>
                        <%--<td></td>--%>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            </tr>
        </c:forEach>
        <c:if test="${page.list[0].list.size()>0}">
            <tr>
                <td colspan="7" style="color: red;">合计</td>
                <td style="color: red;">${totalQty}</td>
                <td colspan="5"></td>
                <td style="color: red;">${totalServiceTimes}</td>
                <td style="color: red;"><fmt:formatNumber value="${totalServiceCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalMaterialCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalTravelCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalExpressCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalOtherCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalInsurance}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalTimeLinessCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalCustomerTimeLinessCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalEngineerUrgentCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${engineerTaxFee}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${engineerInfoFee}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${engineerDeposit}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${engineerPraiseFee}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalCharge}" pattern="0.00"/></td>
                <td></td>
                <td></td>
                <td colspan="2"></td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>


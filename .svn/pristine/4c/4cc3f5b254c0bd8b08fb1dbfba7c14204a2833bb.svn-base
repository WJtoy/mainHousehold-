<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE HTML>
<html>
<head>
    <title>财务完工明细</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>

    <link href="${ctxStatic}/jquery-daterangepicker/daterangepicker.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/common/moment.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/jquery-daterangepicker/jquery.daterangepicker.min.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .date-picker-wrapper {
            z-index: 1000;
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
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
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
                $("#pageNo").val(1);
                $("#searchForm").attr("action", "${ctx}/rpt/provider/completedOrderNew/completedOrderNewReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/completedOrderNew/checkExportTask?" + (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/completedOrderNew/export?" + (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">财务完工明细</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition"
           action="${ctx}/rpt/provider/completedOrderNew/completedOrderNewReport" method="post"
           class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>客　　户：</label>
        <select id="customerId" name="customerId" class="input-small" style="width:225px;">
            <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${fns:getCustomerList()}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label style="padding-left:7px;">结算方式：</label>
        <select id="paymentType" name="paymentType" style="width:80px;">
            <option value="" selected="selected">所有</option>
            <c:forEach items="${fns:getDictListFromMS('PaymentType')}" var="paymentDict">
                <option value="${paymentDict.value}" <c:out
                        value="${(rptSearchCondition.paymentType eq paymentDict.value)?'selected=selected':''}"/>>${paymentDict.label}</option>
            </c:forEach>
        </select>
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
        <label>审单日期：</label>
        <input id="remarks" name="remarks" type="text" style="width:185px;margin-left:4px;"
               maxlength="25" class="input-xlarge Wdate"
               value="${fns:formatDate(rptSearchCondition.beginDate,'yyyy-MM-dd')} ~ ${fns:formatDate(rptSearchCondition.endDate,'yyyy-MM-dd')}"/>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:completedOrderNewReport:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:completedOrderNewReport:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据 <br/>
                    统计方式：审单时间 <br/>
                    栏位说明：<br/>
                    【网点应付合计】服务费+ 配件费 +远程费+ 快递费 + 其他费用+ 时效奖励+ 厂商时效+ 加急费+ 互助基金+ 扣点+ 质保金额+ 平台费 +好评费<br/>
                    【客户应付合计】服务费+ 配件费 + 远程费+ 快递费+ 其他费用 +厂商时效 +加急费 + 好评费
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

        $("#remarks").dateRangePicker({
            language: 'cn',
            autoClose: true,
            startOfWeek: 'monday',
            separator: ' ~ ',
            format: 'YYYY-MM-DD',
            time: {

                enabled: false
            },
            minDays: 1,
            maxDays: 31,
            showWeekNumbers: true,
            selectForward: true,
            shortcuts: null,
            showShortcuts: true
        });
        var h = $(window).height();
        if ($("#contentTable tbody>tr").length > 0) {
            //无数据报错

            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w - 10,
                height: h - 220,
                fixedCols: 1,
                headerRows: 3,
                colWidths:
                    [   140, 140,
                        140, 80, 110, 200,
                        100, 100, 100, 70,
                        70, 70, 100, 100, 100, 90, 200,
                        70, 70, 70, 70, 70, 70, 70, 70, 70, 70,70,70,70,70,90,
                        70, 70, 70, 70, 70, 70, 70, 70,70, 90,
                        250],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h - 220);
        }

    });
</script>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-bordered table-condensed table-hover"
           style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">

        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">


            <th width="140"></th>
            <th width="140"></th>

            <th width="140"></th>
            <th width="80"></th>
            <th width="110"></th>
            <th width="200"></th>

            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="70"></th>

            <th width="70"></th>
            <th width="70"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="90"></th>
            <th width="200"></th>

            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="90"></th>

            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="90"></th>

            <th width="250"></th>
        </tr>
        <tr>
            <th colspan="2">客户信息</th>
            <th colspan="4">下单信息</th>
            <th colspan="4">安维人员信息</th>
            <th colspan="7">实际服务项目</th>
            <th colspan="15">应付安维费用</th>
            <th colspan="10">应收客户货款</th>
            <th rowspan="2">退补描述</th>
        </tr>
        <tr>
            <th>客户名称</th>
            <th>店铺名称</th>


            <th>接单编码</th>
            <th>用户名</th>
            <th>用户电话</th>
            <th>用户地址</th>

            <th>网点编号</th>
            <th>姓名</th>
            <th>电话</th>
            <th>结算方式</th>

            <th>上门次数</th>
            <th>服务类型</th>
            <th>产品</th>
            <th>型号规格</th>
            <th>品牌</th>
            <th>台数</th>
            <th>备注</th>

            <th>服务费</th>
            <th>配件费</th>
            <th>远程费</th>
            <th>快递费</th>
            <th>其他费用</th>
            <th>时效奖励</th>
            <th>厂商时效</th>
            <th>加急费</th>
            <th>汇总</th>
            <th>互助基金</th>
            <th>扣点</th>
            <th>平台费</th>
            <th>质保金额</th>
            <th>好评费</th>
            <th>应付合计</th>

            <th>服务费</th>
            <th>配件费</th>
            <th>远程费</th>
            <th>快递费</th>
            <th>其他费用</th>
            <th>厂商时效</th>
            <th>加急费</th>
            <th>汇总</th>
            <th>好评费</th>
            <th>应收合计</th>
        </tr>
        </thead>
        <tbody>

        <c:set var="totalBlockedCharge"/>
        <c:set var="totalCount"/>
        <c:set var="totalActualCount"/>
        <c:set var="totalInCharge"/>
        <c:set var="totalOutCharge"/>
        <c:set var="engineerServiceChargeTotal"/>
        <c:set var="engineerMaterialChargeTotal"/>
        <c:set var="engineerTravelChargeTotal"/>
        <c:set var="engineerDismantleChargeTotal"/>
        <c:set var="engineerOtherChargeTotal"/>
        <c:set var="engineerInsuranceChargeTotal"/>
        <c:set var="engineerTimelinessChargeTotal"/>
        <c:set var="engineerCustomerTimelinessChargeTotal"/>
        <c:set var="engineerUrgentChargeTotal"/>
        <c:set var="engineerTaxFeeTotal"/>
        <c:set var="engineerInfoFeeTotal"/>
        <c:set var="engineerDepositTotal"/>
        <c:set var="engineerPraiseFeeTotal"/>
        <c:set var="customerServiceChargeTotal"/>
        <c:set var="customerMaterialChargeTotal"/>
        <c:set var="customerTravelChargeTotal"/>
        <c:set var="customerDismantleChargeTotal"/>
        <c:set var="customerOtherChargeTotal"/>
        <c:set var="customerTimelinessChargeTotal"/>
        <c:set var="customerUrgentChargeTotal"/>
        <c:set var="customerChargeSumTotal"/>
        <c:set var="customerPraiseFeeTotal"/>
        <c:set var="engineerChargeTotal"/>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">

            <tr>
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <td rowspan="${item.maxRow}">${item.customer.name}</td>
            <td rowspan="${item.maxRow}">${item.shop.label}</td>

            <td rowspan="${item.maxRow}">${item.orderNo}</td>

            <c:forEach begin="0" end="${item.maxRow-1}" var="i">
                <c:if test="${i ne 0}">
                    <tr>
                </c:if>
                <c:if test="${i eq 0}">
                    <td rowspan="${item.maxRow}">${item.userName}</td>
                    <td rowspan="${item.maxRow}">${item.userPhone}</td>
                    <td rowspan="${item.maxRow}"><a href="javascript:" data-toggle="tooltip"
                                                    data-tooltip="${item.userAddress}">${fns:abbr(item.userAddress,40)}</a>
                    </td>
                </c:if>
                <c:choose>
                    <c:when test="${i lt item.details.size()}">
                        <td>${item.details.get(i).servicePoint.servicePointNo}</td>
                        <td>${item.details.get(i).engineer.name}</td>
                        <td>${item.details.get(i).servicePoint.contactInfo1}</td>
                        <td>${item.details.get(i).servicePoint.paymentType.label}</td>
                    </c:when>
                    <c:otherwise>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${i lt item.details.size()}">
                        <td>${item.details.get(i).serviceTimes}</td>
                        <td>${item.details.get(i).serviceType.name}</td>
                        <td>${item.details.get(i).product.name}</td>
                        <td>${item.details.get(i).productSpec}</td>
                        <td>${item.details.get(i).brand}</td>
                        <td>${item.details.get(i).qty}</td>
                        <c:set var="totalActualCount" value="${totalActualCount+item.details.get(i).qty}"/>
                        <td>${item.details.get(i).remarks}</td>
                        <td>${item.details.get(i).engineerServiceCharge}</td>
                        <c:set var="engineerServiceChargeTotal" value="${engineerServiceChargeTotal+item.details.get(i).engineerServiceCharge}"/>
                        <td>${item.details.get(i).engineerMaterialCharge}</td>
                        <c:set var="engineerMaterialChargeTotal" value="${engineerMaterialChargeTotal+item.details.get(i).engineerMaterialCharge}"/>
                        <td>${item.details.get(i).engineerTravelCharge}</td>
                        <c:set var="engineerTravelChargeTotal" value="${engineerTravelChargeTotal+item.details.get(i).engineerTravelCharge}"/>
                        <td>${item.details.get(i).engineerExpressCharge}</td>
                        <c:set var="engineerDismantleChargeTotal" value="${engineerDismantleChargeTotal+item.details.get(i).engineerExpressCharge}"/>
                        <td>${item.details.get(i).engineerOtherCharge}</td>
                        <c:set var="engineerOtherChargeTotal" value="${engineerOtherChargeTotal+item.details.get(i).engineerOtherCharge}"/>
                        <td>${item.details.get(i).engineerTimelinessCharge}</td>
                        <c:set var="engineerTimelinessChargeTotal" value="${engineerTimelinessChargeTotal+item.details.get(i).engineerTimelinessCharge}"/>
                        <td>${item.details.get(i).engineerCustomerTimelinessCharge}</td>
                        <c:set var="engineerCustomerTimelinessChargeTotal" value="${engineerCustomerTimelinessChargeTotal+item.details.get(i).engineerCustomerTimelinessCharge}"/>
                        <td>${item.details.get(i).engineerUrgentCharge}</td>
                        <c:set var="engineerUrgentChargeTotal" value="${engineerUrgentChargeTotal+item.details.get(i).engineerUrgentCharge}"/>
                        <c:set var="engineerTotalItem" value="${item.details.get(i).engineerServiceCharge+item.details.get(i).engineerMaterialCharge+item.details.get(i).engineerTravelCharge+item.details.get(i).engineerExpressCharge+item.details.get(i).engineerOtherCharge+item.details.get(i).engineerTimelinessCharge+item.details.get(i).engineerCustomerTimelinessCharge+item.details.get(i).engineerUrgentCharge}"/>
                        <c:set var="engineerChargeTotal" value="${engineerChargeTotal+engineerTotalItem}"/>
                        <td>${engineerTotalItem}</td>
                        <td>${item.details.get(i).engineerInsuranceCharge}</td>
                        <c:set var="engineerInsuranceChargeTotal" value="${engineerInsuranceChargeTotal+item.details.get(i).engineerInsuranceCharge}"/>
                        <td>${item.details.get(i).engineerTaxFee}</td>
                        <c:set var="engineerTaxFeeTotal" value="${engineerTaxFeeTotal+item.details.get(i).engineerTaxFee}"/>
                        <td>${item.details.get(i).engineerInfoFee}</td>
                        <c:set var="engineerInfoFeeTotal" value="${engineerInfoFeeTotal+item.details.get(i).engineerInfoFee}"/>
                        <td>${item.details.get(i).engineerDeposit}</td>
                        <c:set var="engineerDepositTotal" value="${engineerDepositTotal+item.details.get(i).engineerDeposit}"/>
                        <td>${item.details.get(i).engineerPraiseFee}</td>
                        <c:set var="engineerPraiseFeeTotal" value="${engineerPraiseFeeTotal+item.details.get(i).engineerPraiseFee}"/>
                        <c:set var="engineerOtherCharge" value="${engineerTotalItem + item.details.get(i).engineerInsuranceCharge+item.details.get(i).engineerTaxFee +item.details.get(i).engineerInfoFee + item.details.get(i).engineerDeposit + item.details.get(i).engineerPraiseFee}"/>
                        <td>${engineerOtherCharge}</td>
                        <c:set var="totalOutCharge"
                               value="${totalOutCharge+engineerOtherCharge}"/>
                    </c:when>
                    <c:otherwise>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${i lt item.details.size()}">
                        <c:set var="customerTotalItem" value="${item.details.get(i).serviceCharge+item.details.get(i).materialCharge+item.details.get(i).travelCharge+item.details.get(i).expressCharge+item.details.get(i).otherCharge}"/>
                        <td>${item.details.get(i).serviceCharge}</td>
                        <c:set var="customerServiceChargeTotal" value="${customerServiceChargeTotal+item.details.get(i).serviceCharge}"/>
                        <td>${item.details.get(i).materialCharge}</td>
                        <c:set var="customerMaterialChargeTotal" value="${customerMaterialChargeTotal+item.details.get(i).materialCharge}"/>
                        <td>${item.details.get(i).travelCharge}</td>
                        <c:set var="customerTravelChargeTotal" value="${customerTravelChargeTotal+item.details.get(i).travelCharge}"/>
                        <td>${item.details.get(i).expressCharge}</td>
                        <c:set var="customerDismantleChargeTotal" value="${customerDismantleChargeTotal+item.details.get(i).expressCharge}"/>
                        <td>${item.details.get(i).otherCharge}</td>
                        <c:set var="customerOtherChargeTotal" value="${customerOtherChargeTotal+item.details.get(i).otherCharge}"/>
                    </c:when>
                    <c:otherwise>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </c:otherwise>
                </c:choose>
                <c:if test="${i eq 0}">
                    <td rowspan="${item.maxRow}">${item.customerTimelinessCharge}</td>
                    <c:set var="customerTimelinessChargeTotal" value="${customerTimelinessChargeTotal+item.customerTimelinessCharge}"/>
                    <td rowspan="${item.maxRow}">${item.customerUrgentCharge}</td>
                    <c:set var="customerUrgentChargeTotal" value="${customerUrgentChargeTotal+item.customerUrgentCharge}"/>
                    <td rowspan="${item.maxRow}">${item.customerTotalCharge - item.customerPraiseFee}</td>
                    <c:set var="customerChargeSumTotal" value="${customerChargeSumTotal+item.customerTotalCharge - item.customerPraiseFee}"/>
                    <td rowspan="${item.maxRow}">${item.customerPraiseFee}</td>
                    <c:set var="customerPraiseFeeTotal" value="${customerPraiseFeeTotal+item.customerPraiseFee}"/>
                    <td style="color:red;"
                        rowspan="${item.maxRow}">${item.customerTotalCharge}</td>
                    <c:set var="totalInCharge"
                           value="${totalInCharge+item.customerTotalCharge}"/>
                </c:if>

                <c:if test="${i eq 0}">
                    <td rowspan="${item.maxRow}" class="autocut"><a href="javascript:" data-toggle="tooltip"
                                                                    data-tooltip="${item.writeOffRemarks}">${fns:abbr(item.writeOffRemarks,40)}</a>
                    </td>
                </c:if>

<%--                <c:choose>--%>
<%--                    <c:when test="${i lt item.details.size()}">--%>
<%--                        <td></td>--%>
<%--                        <td></td>--%>
<%--                    </c:when>--%>
<%--                    <c:otherwise>--%>
<%--                        <td></td>--%>
<%--                        <td></td>--%>
<%--                    </c:otherwise>--%>
<%--                </c:choose>--%>

                <c:if test="${i eq 0}">

                    </tr>
                </c:if>

            </c:forEach>

            </tr>
        </c:forEach>
        <c:if test="${page.list.size()>0}">
            <tr>
                <th colspan="14"></th>
                <th>合计:</th>
                <th><B>${totalActualCount}</B></th>
                <th></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerServiceChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerMaterialChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerTravelChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerDismantleChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerOtherChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerTimelinessChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerCustomerTimelinessChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerUrgentChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerInsuranceChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerTaxFeeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerInfoFeeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerDepositTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${engineerPraiseFeeTotal}"/></th>
                <th style="color:red;"><B><fmt:formatNumber pattern="0.00" value="${totalOutCharge}"/></B></th>
                <th><fmt:formatNumber pattern="0.00" value="${customerServiceChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${customerMaterialChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${customerTravelChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${customerDismantleChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${customerOtherChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${customerTimelinessChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${customerUrgentChargeTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${customerChargeSumTotal}"/></th>
                <th><fmt:formatNumber pattern="0.00" value="${customerPraiseFeeTotal}"/></th>
                <th style="color:green;"><B><fmt:formatNumber pattern="0.00" value="${totalInCharge}"/></B></th>
                <th></th>
            </tr>
        </c:if>
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
</body>
</html>

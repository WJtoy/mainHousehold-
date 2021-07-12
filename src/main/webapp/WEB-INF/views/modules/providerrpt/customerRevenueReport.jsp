<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>客户月营收统计</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
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
        .dark-tooltip{
            margin-top: 6px !important;
        }


        .delft{
            margin-left: -3px;
            cursor: pointer;
            vertical-align: middle;
        }

        /*.table tr:nth-child(n+1){background:red}*/

    </style>
    <script type="text/javascript">
        $(document).ready(function () {
            if($("#status").val() == 1){
                console.log($("#status").val())
                $("#divGrid").hide();
                $("#divGridA").show();
                $("#status").val(1);
                $("[name='vehicle']").attr("checked",'true');
            }else {
                $("#divGridA").hide();
                $("#divGrid").show();
                $("#status").val(0);
            }

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

            $('#vehicle').click(function () {
                if ($('#vehicle').is(':checked')) {
                    $("#divGrid").hide();
                    $("#divGridA").show();
                    $("#status").val(1);
                } else {
                    $("#divGridA").hide();
                    $("#divGrid").show();
                    $("#status").val(0);
                }
            })
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"vertical-align": "middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip({gravity: 'north'});

            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/customerRevenue/customerRevenueReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/customerRevenue/checkExportTask?" + (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/customerRevenue/export?" + (new Date()).getTime(),
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

        function details(year,month,productCategoryId,customerId,finishOrder,receivableCharge,payableCharge,orderGrossProfit,everySingleGrossProfit,customerName){
            if(customerName != ''){
                customerName = '【'+customerName+'】';
            }
            var h = $(top.window).height();
            var w = $(top.window).width();
            top.layer.open({
                type: 2,
                id:'layer_unitCode',
                zIndex:19891015,
                title: customerName+year +'年'+month +'月费用明细',
                content: "${ctx}/rpt/order/customerRevenueExpenses/customerRevenueDetailsOfRptChargeDate?year="+ (year || '') + "&month=" + (month || '')  + "&productCategoryId=" + (productCategoryId || "") + "&customerId=" + (customerId || "")+ "&finishOrder=" + (finishOrder || "")
                         + "&receivableCharge=" + (receivableCharge || "") + "&payableCharge=" + (payableCharge || "")+ "&orderGrossProfit=" + (orderGrossProfit || "")+ "&everySingleGrossProfit=" + (everySingleGrossProfit || ""),
                area:[1000+'px',660+'px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">客户营收明细</a></li>
    <li>
        <a href="${ctx}/rpt/provider/customerRevenue/customerRevenueChart">客户营收排名</a>
    </li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition"
           action="${ctx}/rpt/provider/customerRevenue/customerRevenueReport" method="post"
           class="breadcrumb form-search">
    <input type="hidden" id="status" name="status" value="${status}">
    <div style="width: 90%">
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
        <label>产品品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}" />>所有</option>
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
                <option value="${1+i}" <c:out
                        value="${(rptSearchCondition.selectedMonth eq 1+i)?'selected=selected':''}"/>>${1+i}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <input style="margin-left: 20px;zoom:1.4" type="checkbox" id="vehicle" name="vehicle" value="Bike" />
        <label style="margin-left: -3px" class="delft">扣除其他项</label>
        <a href="javascript:void(0);" data-toggle="tooltipnorth" data-tooltip="扣除互助基金和质保金额"><img src="${ctxStatic}/images/rpt/interrogation.png"></a>
        &nbsp;&nbsp;
        <input style="margin-left: 20px" id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 200px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据<br/>
                    统计方式：审单时间<br/>
                    栏位说明：<br/>
                    【每单毛利】工单毛利/完成订单
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}"/>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-striped table-bordered table-condensed table-hover"
           style="table-layout:fixed; margin-top: 0px;">
        <thead>
        <tr>
            <th width="60">序号</th>
            <th width="150">客户</th>
            <th width="80">业务员</th>
            <th width="80">客户费用(元)</th>
            <th width="80">网点费用(元)</th>
            <th width="80">工单毛利(元)</th>
            <th width="80">完成单量(单)</th>
            <th width="80">每单毛利(元/单)</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${rptSearchCondition.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <tr>

                <c:if test="${rptSearchCondition.list.size()==rowIndex}">
                    <td colspan="3"> 合计</td>
                    <td style="color: red;"> <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                            '${rptSearchCondition.productCategory}','0','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','')">
                        <fmt:formatNumber value="${item.receivableCharge}" pattern="0.00"/></a></td>

                    <td style="color: red;"> <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                            '${rptSearchCondition.productCategory}','0','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','')">
                        <fmt:formatNumber value="${0-item.payableCharge}" pattern="0.00"/></a></td></td>

                    <td style="color: red;"><fmt:formatNumber value="${item.orderGrossProfit}" pattern="0.00"/></td>
                    <td style="color: red;">${item.finishOrder}</td>
                    <td style="color: red;"><fmt:formatNumber value="${item.everySingleGrossProfit}" pattern="0.00"/></td>

                </c:if>
                <c:if test="${rptSearchCondition.list.size()!=rowIndex}">
                    <td> ${rowIndex}</td>
                    <td>${item.customerName}</td>
                    <td>${item.salesName}</td>
                    <td>
                        <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                                '${rptSearchCondition.productCategory}','${item.customerId}','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','${item.customerName}')">
                            <fmt:formatNumber value="${item.receivableCharge}" pattern="0.00"/></a>

                    </td>
                    <td>
                        <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                                '${rptSearchCondition.productCategory}','${item.customerId}','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','${item.customerName}')">
                          <fmt:formatNumber value="${0-item.payableCharge}" pattern="0.00"/></a></td>
                    <td><fmt:formatNumber value="${item.orderGrossProfit}" pattern="0.00"/></td>
                    <td>${item.finishOrder}</td>
                    <td><fmt:formatNumber value="${item.everySingleGrossProfit}" pattern="0.00"/></td>
                </c:if>



            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>


<div id="divGridA" style="overflow: auto;display: none">
    <table id="contentTableA" class="table table-striped table-bordered table-condensed table-hover"
           style="table-layout:fixed; margin-top: 0px;">
        <thead>

        <tr>
            <th width="60" rowspan="2" >序号</th>
            <th width="150" rowspan="2" >客户名称</th>
            <th width="60" rowspan="2">业务员</th>
            <th width="80" rowspan="2">客户费用(元)</th>
            <th width="120" colspan="2">网点费用(元)</th>
            <th width="120" colspan="2">工单毛利(元)</th>
            <th width="80" rowspan="2" >完成单量(单)</th>
            <th width="120" colspan="2">每单毛利(元/单)</th>
        </tr>
        <tr>
            <th>未扣除其他项</th>
            <th>扣除其他项</th>
            <th>未扣除其他项</th>
            <th>扣除其他项</th>
            <th>未扣除其他项</th>
            <th>扣除其他项</th>
        </tr>

        </thead>
        <tbody>
        <c:set var="payableDepositCharge" value="0" />
        <c:set var="payableDepositCharge" value="0" />
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${rptSearchCondition.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <tr>

                <c:if test="${rptSearchCondition.list.size()==rowIndex}">
                    <td colspan="3"> 合计</td>
                    <td style="color: red;"> <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                            '${rptSearchCondition.productCategory}','0','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','')">
                        <fmt:formatNumber value="${item.receivableCharge}" pattern="0.00"/></a></td>

                    <td style="color: red;"> <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                            '${rptSearchCondition.productCategory}','0','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','')">
                        <fmt:formatNumber value="${0-item.payableCharge}" pattern="0.00"/></a></td></td>

                    <td style="color: red;"> <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                            '${rptSearchCondition.productCategory}','0','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','')">
                        <fmt:formatNumber value="${0-item.noPayableCharge}" pattern="0.00"/></a></td></td>

                    <td style="color: red;"><fmt:formatNumber value="${item.orderGrossProfit}" pattern="0.00"/></td>
                    <td style="color: red;"><fmt:formatNumber value="${item.noOrderGrossProfit}" pattern="0.00"/></td>
                    <td style="color: red;">${item.finishOrder}</td>
                    <td style="color: red;"><fmt:formatNumber value="${item.everySingleGrossProfit}" pattern="0.00"/></td>
                    <td style="color: red;"><fmt:formatNumber value="${item.noEverySingleGrossProfit}" pattern="0.00"/></td>

                </c:if>
                <c:if test="${rptSearchCondition.list.size()!=rowIndex}">
                    <td> ${rowIndex}</td>
                    <td>${item.customerName}</td>
                    <td>${item.salesName}</td>
                    <td>
                        <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                                '${rptSearchCondition.productCategory}','${item.customerId}','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','${item.customerName}')">
                            <fmt:formatNumber value="${item.receivableCharge}" pattern="0.00"/></a>

                    </td>
                    <td>
                        <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                                '${rptSearchCondition.productCategory}','${item.customerId}','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','${item.customerName}')">
                            <fmt:formatNumber value="${0-item.payableCharge}" pattern="0.00"/></a></td>
                    <td>
                        <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}',
                                '${rptSearchCondition.productCategory}','${item.customerId}','${item.finishOrder}','${item.receivableCharge}','${item.payableCharge}','${item.orderGrossProfit}','${item.everySingleGrossProfit}','${item.customerName}')">
                            <fmt:formatNumber value="${0-item.noPayableCharge}" pattern="0.00"/></a>
                    </td>

                    <td><fmt:formatNumber value="${item.orderGrossProfit}" pattern="0.00"/></td>
                    <td><fmt:formatNumber value="${item.noOrderGrossProfit}" pattern="0.00"/></td>
                    <td>${item.finishOrder}</td>
                    <td><fmt:formatNumber value="${item.everySingleGrossProfit}" pattern="0.00"/></td>
                    <td><fmt:formatNumber value="${item.noEverySingleGrossProfit}" pattern="0.00"/></td>
                </c:if>


            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

</body>
</html>

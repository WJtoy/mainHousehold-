<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>工单网点费用</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>

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
            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/gradedOrder/orderServicePointFeeCheckExportTask?"+ (new Date()).getTime(),
                    data:$(searchForm).serialize(),
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data && data.success == true){
                            top.$.jBox.closeTip();
                            top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
                                if (v == "ok") {
                                    top.$.jBox.tip('请稍候...', 'loading');
                                    $.ajax({
                                        type: "POST",
                                        url: "${ctx}/rpt/provider/gradedOrder/orderServicePointFeeExport?"+ (new Date()).getTime(),
                                        data:$(searchForm).serialize(),
                                        success: function (data) {
                                            if(ajaxLogout(data)){
                                                return false;
                                            }
                                            if(data && data.success == true){
                                                top.$.jBox.closeTip();
                                                top.$.jBox.tip(data.message, "success");
                                                $('#btnExport').removeAttr('disabled');
                                                return false;
                                            }
                                            else if( data && data.message){
                                                top.$.jBox.error(data.message,"导出错误");
                                            }
                                            else{
                                                top.$.jBox.error("导出错误","错误提示");
                                            }
                                            $('#btnExport').removeAttr('disabled');
                                            top.$.jBox.closeTip();
                                            return false;
                                        },
                                        error: function (e) {
                                            $('#btnExport').removeAttr('disabled');
                                            ajaxLogout(e.responseText,null,"导出错误，请重试!");
                                            top.$.jBox.closeTip();
                                        }
                                    });
                                }
                            }, {buttonsFocus: 1});
                            return false;
                        }
                        else if( data && data.message){
                            top.$.jBox.error(data.message,"导出错误");
                        }
                        else{
                            top.$.jBox.error("导出错误","错误提示");
                        }
                        $('#btnExport').removeAttr('disabled');
                        top.$.jBox.closeTip();
                        return false;
                    },
                    error: function (e) {
                        $('#btnExport').removeAttr('disabled');
                        ajaxLogout(e.responseText,null,"导出错误，请重试!");
                        top.$.jBox.closeTip();
                    }
                });
                top.$('.jbox-body .jbox-icon').css('top', '55px');
            });

            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/gradedOrder/orderServicePointFeeRpt");
                $("#searchForm").submit();
            });
        });
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">工单网点费用</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition"
           action="${ctx}/rpt/provider/gradedOrder/orderServicePointFeeRpt" method="post"
           class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>客评时间：</label>
        <input id="remarks" name="remarks" type="text" style="width:185px;margin-left:4px;"
               maxlength="25" class="input-xlarge Wdate"
               value="${fns:formatDate(rptSearchCondition.beginDate,'yyyy-MM-dd')} ~ ${fns:formatDate(rptSearchCondition.endDate,'yyyy-MM-dd')}"/>
        &nbsp;&nbsp;
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out
                    value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}"/>>
                所有
            </option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp; &nbsp;
        <label class="control-label">服务网点：</label>
        <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                  labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                  width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>
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
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据(5分钟延迟) <br/>
                    统计方式：客评时间 <br/>
                    栏位说明：<br/>
                    【总额】服务费+材料费+远程费+快递费+其他费用+互助基金+快可立时效费+客户时效费+加急费+扣点+平台费+质保金额+好评费
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

        }
        else {
            $("#divGrid").css("height", h - 168);
        }

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

        $("#contentTable").toSuperTable({
            width: w - 10,
            height: h - 208,
            fixedCols: 1,
            headerRows: 1,
            colWidths:
                [60, 130, 120, 80, 80, 80, 130,100, 100, 80, 80, 120, 120,
                    100, 100, 100,100,100,130, 120, 100, 100, 100, 100, 100, 100,100,
                    100, 100, 100,100,100,100, 100
                ],
            onStart: function () {
            },
            onFinish: function () {
            }
        });
    });
</script>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-bordered table-condensed" style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="60">序号</th>
            <th width="130">工单号</th>
            <th width="120">客户名称</th>
            <th width="80">省</th>
            <th width="80">市</th>
            <th width="80">区</th>
            <th width="130">用户地址</th>
            <th width="100">用户名称</th>
            <th width="100">用户电话</th>
            <th width="80">服务品类</th>
            <th width="80">产品</th>
            <th width="120">工单描述</th>
            <th width="120">下单时间</th>
            <th width="100">首次派单时间</th>
            <th width="100">派单时间</th>
            <th width="100">到货时间</th>
            <th width="100">APP完成时间</th>
            <th width="100">客评时间</th>
            <th width="130">网点编号</th>
            <th width="120">网点名称</th>
            <th width="100">服务费</th>
            <th width="100">材料费</th>
            <th width="100">远程费</th>
            <th width="100">快递费</th>
            <th width="100">其他费用</th>
            <th width="100">互助基金</th>
            <th width="100">快可立时效费</th>
            <th width="100">客户时效费</th>
            <th width="100">加急费</th>
            <th width="100">扣点</th>
            <th width="100">平台费</th>
            <th width="100">质保金额</th>
            <th width="100">好评费</th>
            <th width="100">总额</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:set var="serviceCharge" value="0"/>
        <c:set var="materialCharge" value="0"/>
        <c:set var="travelCharge" value="0"/>
        <c:set var="expressCharge" value="0"/>
        <c:set var="otherCharge" value="0"/>
        <c:set var="insuranceCharge" value="0"/>
        <c:set var="timeLinessCharge" value="0"/>
        <c:set var="customerTimeLinessCharge" value="0"/>
        <c:set var="urgentCharge" value="0"/>
        <c:set var="engineerPraiseFee" value="0"/>
        <c:set var="engineerTaxFee" value="0"/>
        <c:set var="engineerInfoFee" value="0"/>
        <c:set var="engineerDeposit" value="0"/>
        <c:set var="total" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <tr>
                <td>${rowIndex}</td>
                <td>${item.orderNo}</td>
                <td >${item.customerName}</td>
                <td>${item.provinceName}</td>
                <td>${item.cityName}</td>
                <td>${item.countyName}</td>
                <td class="autocut"><a href="javascript:" data-toggle="tooltip"
                                       data-tooltip="${item.serviceAddress}">${fns:abbr(item.serviceAddress,30)}</a>
                </td>
                <td>${item.userName}</td>
                <td>${item.servicePhone}</td>
                <td>${item.productCategoryName}</td>
                <td>${item.productNames}</td>
                <td><a href="javascript:" data-toggle="tooltip"
                       data-tooltip="${item.description}">${fns:abbr(item.description,40)}</a></td>
                <td><fmt:formatDate value="${item.createDateD}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatDate value="${item.firstPlanDateD}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatDate value="${item.planDateD}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatDate value="${item.arrivalDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatDate value="${item.appCompleteDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatDate value="${item.closeDateD}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td>${item.servicePointNo}</td>
                <td>${item.servicePointName}</td>
                <td>${item.serviceCharge}</td>
                <c:set var="serviceCharge" value="${serviceCharge + item.serviceCharge}"/>
                <td>${item.materialCharge}</td>
                <c:set var="materialCharge" value="${materialCharge + item.materialCharge}"/>
                <td>${item.travelCharge}</td>
                <c:set var="travelCharge" value="${travelCharge + item.travelCharge}"/>
                <td>${item.expressCharge}</td>
                <c:set var="expressCharge" value="${expressCharge + item.expressCharge}"/>
                <td>${item.otherCharge}</td>
                <c:set var="otherCharge" value="${otherCharge + item.otherCharge}"/>
                <td>${item.insuranceCharge}</td>
                <c:set var="insuranceCharge" value="${insuranceCharge + item.insuranceCharge}"/>
                <td>${item.timeLinessCharge}</td>
                <c:set var="timeLinessCharge" value="${timeLinessCharge + item.timeLinessCharge}"/>
                <td>${item.customerTimeLinessCharge}</td>
                <c:set var="customerTimeLinessCharge"
                       value="${customerTimeLinessCharge + item.customerTimeLinessCharge}"/>
                <td>${item.urgentCharge}</td>
                <c:set var="urgentCharge" value="${urgentCharge + item.urgentCharge}"/>
                <td>${item.taxFee}</td>
                <c:set var="engineerTaxFee" value="${engineerTaxFee + item.taxFee}"/>
                <td>${item.infoFee}</td>
                <c:set var="engineerInfoFee" value="${engineerInfoFee + item.infoFee}"/>
                <td>${item.engineerDeposit}</td>
                <c:set var="engineerDeposit" value="${engineerDeposit + item.engineerDeposit}"/>
                <td>${item.praiseFee}</td>
                <c:set var="engineerPraiseFee" value="${engineerPraiseFee + item.praiseFee}"/>
                <td>${item.totalCharge}</td>
                <c:set var="total" value="${total + item.totalCharge}"/>

            </tr>
        </c:forEach>
        <c:if test="${page.list.size()>0}">
            <tr style="font-weight: bold; color: red;">
                <td colspan="20">合计</td>
                <td><fmt:formatNumber value="${serviceCharge}" pattern="0.0"/></td>
                <td><fmt:formatNumber value="${materialCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${travelCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${expressCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${otherCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${insuranceCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${timeLinessCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${customerTimeLinessCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${urgentCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${engineerTaxFee}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${engineerInfoFee}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${engineerDeposit}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${engineerPraiseFee}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${total}" pattern="0.00"/></td>
                <td></td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>

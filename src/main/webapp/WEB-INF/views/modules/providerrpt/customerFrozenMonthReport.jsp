<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>客户每日下单明细</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
<%--    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>--%>
<%--    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>--%>
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

            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/customerFrozenMonthRpt/customerFrozenMonthRptData");
                $("#searchForm").submit();
            });
            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/customerFrozenMonthRpt/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/customerFrozenMonthRpt/export?"+ (new Date()).getTime(),
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
                            $('#btnExport').removeAttr('disabled');
                            top.$.jBox.closeTip();
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
        });
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li>
        <a href="${ctx}/rpt/provider/customerNewOrderDaily/getList">客户每日下单</a>
    </li>

    <li>
        <a href="${ctx}/rpt/provider/customerFrozenDailyRpt/customerFrozenDailyRptData">每日冻结明细</a>
    </li>


    <li>
        <a href="${ctx}/rpt/provider/customerNewOrderMonth/getMonthList">客户每月下单</a>
    </li>


    <li class="active"><a href="javascript:void(0);">每月冻结明细</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="rptSearchCondition"
           action="${ctx}/rpt/provider/customerFrozenMonthRpt/customerFrozenMonthRptData" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}" />>${year}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>月份：</label>
        <select id="month" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${i+1}" <c:out value="${(rptSearchCondition.selectedMonth eq i+1)?'selected=selected':''}" />>${i+1}</option>
            </c:forEach>
        </select>

        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <%--<input type="text" readonly="true" id="customer.name" name="customer.name" value="${currentuser.customerAccountProfile.customer.name}" />--%>
                <input type="hidden" readonly="true" id="customer.id" name="customer.id"
                       value="${currentuser.customerAccountProfile.customer.id}"/>
            </c:when>
            <c:otherwise>
                &nbsp;&nbsp;
                <label>客户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:225px;">
                        <%--<option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>所有</option>--%>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
                    </c:forEach>
                </select>
            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;

        <shiro:hasPermission name="rpt:customerNewOrderDaily:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:customerNewOrderDaily:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
    </div>

    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据 <br/>
                    统计方式：下单时间 <br/>
                    栏位说明：<br/>
                    【冻结金额】一个工单对应一个冻结金额，若修改工单金额则一个工单对应多个冻结金额 <br/>
                    【解冻金额】工单完成后的解冻该工单的冻结金额
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
       /* var h = $(window).height();
        if ($("#contentTable tbody>tr").length > 0) {
            //无数据报错
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w - 10,
                height: h - 205,
                fixedCols: 3,
                headerRows: 3,
                colWidths:
                    [30, 140, 120,
                        140, 50, 50, 70, 120, 60,170,60
                        ],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        } else {
            $("#divGrid").css("height", h - 205);
        }*/

    });
</script>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover "
           style="table-layout:fixed;margin-top: 0px;border-top-width: 0px;" cellspacing="0" width="100%">
        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="30"></th>

            <th width="140"></th>
            <th width="100"></th>
            <th width="120"></th>

            <th width="50"></th>
            <th width="50"></th>
            <th width="70"></th>
            <th width="120"></th>
            <th width="60"></th>
            <th width="200"></th>
            <th width="60"></th>
        </tr>
        <tr>
            <th  width="30">序号</th>

            <th width="140">接单编码</th>
            <th width="100">店铺</th>
            <th width="120">第三方单号</th>

            <th width="50">下单人</th>
            <th width="50">服务类型</th>
            <th width="70">产品</th>
            <th width="120">型号规格</th>
            <th width="50">冻结金额</th>
            <th width="200">冻结描述</th>
            <th width="50">解冻金额</th>
        </tr>
        </thead>
        <tbody>

        <c:set var="totalBlockAmount"/>
        <c:set var="unBlockAmount"/>

        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">

            <tr>
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <c:set value="${item.orderItems.size() eq 0?1:item.orderItems.size()}" var="rowSpan"/>
            <td rowspan="${rowSpan}">${rowIndex+(page.pageNo-1)*page.pageSize}</td>
            <td rowspan="${rowSpan}">${item.orderNo}</td>
            <td rowspan="${rowSpan}">${item.shopName}</td>
            <td rowspan="${rowSpan}">${item.parentBizOrderId}</td>
            <td rowspan="${rowSpan}">${item.orderCreateByName}</td>
            <td>${item.orderItems[0].serviceType.name}</td>
            <td>${item.orderItems[0].product.name}</td>
            <td>${item.orderItems[0].productSpec}</td>
                <c:choose>
                    <c:when test="${item.currencyType == '10' }">
                        <td rowspan="${rowSpan}">${item.blockAmount}</td>
                        <c:set var="totalBlockAmount" value = "${totalBlockAmount+item.blockAmount}"/>
                    </c:when>
                    <c:otherwise>
                        <td rowspan="${rowSpan}" >0</td>
                        <c:set var="totalBlockAmount" value = "${totalBlockAmount+0}"/>
                    </c:otherwise>
                </c:choose>
                <td rowspan="${rowSpan}">${item.remarks}</td>
                <c:choose>
                    <c:when test="${item.currencyType == '20' }">
                        <td rowspan="${rowSpan}">${item.blockAmount}</td>
                        <c:set var="unBlockAmount" value = "${unBlockAmount+item.blockAmount}"/>
                    </c:when>
                    <c:otherwise>
                        <td rowspan="${rowSpan}" >0</td>
                        <c:set var="unBlockAmount" value = "${unBlockAmount+0}"/>
                    </c:otherwise>
                </c:choose>
            </tr>
             <c:forEach items="${item.orderItems}" begin="1" var="orderItem">
                <tr>
                    <td>${orderItem.serviceType.name}</td>
                    <td>${orderItem.product.name}</td>
                    <td>${orderItem.productSpec}</td>
                </tr>
             </c:forEach>
        </c:forEach>
        <c:if test="${page.list.size()>0}">
            <tr>
                <th colspan="7"></th>
                <th><B>合计:</B></th>
                <th style="color:red;"><B>${totalBlockAmount}</B></th>
                <th colspan="1"></th>
                <th style="color:red;"><B>${unBlockAmount}</B></th>
            </tr>
        </c:if>
        </tbody>
    </table>

</div>
<div class="pagination">${page}</div>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>CustomerChageOrder</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
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

        .table thead th,.table tbody td {
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
            if(f.customerId.value =="") {
                top.$.jBox.error("请选择客户！","客户对帐单");
                top.$.jBox.closeTip();
                return false;
            }else {
                return true;
            }

        }
    </script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript" language="javascript">
        $(document).ready(function() {
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
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});


            $("#btnSubmit").click(function(){
                top.$.jBox.tip('请稍候...', 'loading');
                $("#pageNo").val(1);
                $("#searchForm").attr("action","${ctx}/rpt/provider/customerCharge/returnedOrderCancelledRpt");
                $("#searchForm").submit();
            });
            $("#btnExport").click(function () {+
                top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
                    if (v == "ok") {
                        $("#pageNo").val(1);
                        top.$.jBox.tip('请稍候...', 'loading');
                        $.ajax({
                            type: "POST",
                            url: "${ctx}/rpt/provider/customerCharge/export?" + (new Date()).getTime(),
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
                top.$('.jbox-body .jbox-icon').css('top', '55px');
            });

        });
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/rpt/provider/customerCharge/customerChargeSummaryRpt">订单|消费|充值</a>
    </li>
    <li><a href="${ctx}/rpt/provider/customerCharge/customerChargeCompleteReport">完工单</a>
    </li>
    <li class="active"><a href="javascript:void(0);">退单/取消单</a>
    </li>
    <li><a href="${ctx}/rpt/provider/customerCharge/customerChargeWriteOffRpt">退补单</a>
    </li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<c:set var="mdcustomerID" value="${currentuser.getCustomerAccountProfile().getCustomer().getId()}"></c:set>
<form:form id="searchForm" modelAttribute="rptSearchCondition" method="post" class="breadcrumb form-search" onsubmit="return validate(this)">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="hidden" id="customerId" name="customerId" value="${mdcustomerID}" maxlength="50" style="width:345px;" />
                <input type="hidden" id="customerName" name="customerName" value="${mdcustomerName}" maxlength="50" style="width:105px;" />
            </c:when>
            <c:otherwise>
                <label>客 户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:225px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}" />>所有</option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                    </c:forEach>
                </select>
                <span class="add-on red">必选*</span>
            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;
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
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询" />
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出" />
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 200px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据 <br/>
                    统计方式：退单取消单时间
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}" />
<script type="text/javascript">
    $(document).ready(function() {
        var h = $(window).height();
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错

            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 200,
                fixedCols: 1,
                headerRows: 3,
                colWidths:
                    [40,
                        140,140, 120,
                        140, 140,70, 80, 80, 70, 60, 90,100, 200, 80, 110, 200,
                        60,100, 200],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h-200);
        }

    });
</script>

<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-bordered table-condensed table-hover" style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">

        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="40"></th>


            <th width="140"></th>
            <th width="140"></th>
            <th width="120"></th>

            <th width="140"></th>
            <th width="140"></th>
            <th width="70"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="70"></th>
            <th width="60"></th>
            <th width="90"></th>
            <th width="100"></th>
            <th width="200"></th>
            <th width="80"></th>
            <th width="110"></th>
            <th width="200"></th>

            <th width="60"></th>
            <th width="100"></th>
            <th width="200"></th>
        </tr>
        <tr>
            <th rowspan="2">序号</th>
            <th colspan="3">客户信息</th>

            <th colspan="13">下单信息</th>
            <th rowspan="2">状态</th>
            <th rowspan="2">退单/取消<BR/>时间</th>
            <th rowspan="2">退单描述</th>

        </tr>
        <tr>

            <th>客户名称</th>
            <th>店铺名称</th>
            <th>下单人</th>

            <th>接单编码</th>
            <th>第三方单号</th>
            <th>服务类型</th>
            <th>产品</th>
            <th>型号规格</th>
            <th>品牌</th>
            <th>台数</th>
            <th>下单金额</th>
            <th>下单时间</th>
            <th>服务描述</th>
            <th>用户名</th>
            <th>用户电话</th>
            <th>用户地址</th>
        </tr>
        </thead>
        <tbody>

        <c:set var="totalBlockedCharge" />
        <c:set var="totalCount"/>
        <c:set var="totalActualCount"/>
        <c:set var="totalInCharge"/>

        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">

            <tr>
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <td rowspan="${item.rowCount}">${rowIndex}</td>
            <td rowspan="${item.rowCount}">${item.customer.name}</td>
            <td rowspan="${item.rowCount}">${item.shop.label}</td>
            <td rowspan="${item.rowCount}">${item.createBy.name}</td>
            <td rowspan="${item.rowCount}">${item.orderNo}</td>
            <td rowspan="${item.rowCount}">${item.parentBizOrderId}</td>
            <c:forEach begin="0" end="${item.rowCount-1}" var="i">
                <c:if test="${i ne 0}">
                    <tr>
                </c:if>
                <c:choose>
                    <c:when test = "${i lt item.items.size()}">
                        <td>${item.items.get(i).serviceType.name}</td>
                        <td>${item.items.get(i).product.name}</td>
                        <td>${item.items.get(i).productSpec}</td>
                        <td>${item.items.get(i).brand}</td>
                        <td>${item.items.get(i).qty}</td>
                        <c:set var="totalCount" value="${totalCount+item.items.get(i).qty}" />
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
                    <td rowspan="${item.rowCount}" style="color:blue;">${item.expectCharge}</td>
                    <c:set var="totalBlockedCharge" value="${totalBlockedCharge+item.expectCharge}" />
                    <td rowspan="${item.rowCount}"><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                    <td rowspan="${item.rowCount}"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.description}">${fns:abbr(item.description,40)}</a></td>
                    <td rowspan="${item.rowCount}">${item.userName}</td>
                    <td rowspan="${item.rowCount}">${item.userPhone}</td>
                    <td rowspan="${item.rowCount}"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.userAddress}">${fns:abbr(item.userAddress,40)}</a></td>

                    <td rowspan="${item.rowCount}"><span class="label status_${item.status.label}">${item.status.label}</span></td>
                    <td rowspan="${item.rowCount}"><fmt:formatDate value="${item.closeDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                    <td rowspan="${item.rowCount}" class="autocut"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.cancelApplyComment}">${fns:abbr(item.cancelApplyComment,40)}</a></td>
                </c:if>

                <c:if test="${i eq 0}">
                    </tr>
                </c:if>

            </c:forEach>

            </tr>
        </c:forEach>
        <c:if test="${page.list.size()>0}">
            <tr>
                <th colspan="9"></th>
                <th><B>合计:</B></th>
                <th><B>${totalCount}</B></th>
                <th style="color:blue;"><B>${totalBlockedCharge}</B></th>
                <th colspan="8"></th>
            </tr>
        </c:if>
        </tbody>
    </table>

</div>
<div class="pagination">${page}</div>
</body>
</html>

<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>完成时效统计</title>
    <script src="${ctxStatic}/echarts/echarts.min.js"></script>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
<%--    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>--%>
<%--    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>--%>
    <style type="text/css">
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
                $("#searchForm").attr("action", "${ctx}/rpt/provider/keFuCompleteTimeNew/keFuCompleteTimeNewReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/keFuCompleteTimeNew/checkExportTask?" + (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/keFuCompleteTimeNew/export?" + (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">完成时效统计</a></li>
    <li>
        <a href="${ctx}/rpt/provider/keFuCompleteTimeNew/keFuCompleteTimeNewChart">完成时效图表</a>
    </li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/keFuCompleteTimeNew/keFuCompleteTimeNewReport" method="post" class="breadcrumb form-search">
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>下单时间：</label>
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:242px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false,maxDate:'${rptSearchCondition.endPlanDate}'});"/>
        &nbsp;&nbsp;
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="hidden" id="customer.name" name="customer.name" value="${currentuser.customerAccountProfile.customer.name}" />
                <input type="hidden" id="customer.id" name="customer.id" value="${currentuser.customerAccountProfile.customer.id}" />
            </c:when>
            <c:otherwise>
                <label style="margin-left: 12px">客户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:255px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}" />>所有</option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                    </c:forEach>
                </select>
            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;
        <c:if test="${!currentuser.isCustomer() && !currentuser.isSaleman()}">
            <label class="control-label" style="margin-left: 12px;">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;服：</label>
            <form:select path="kefuId" style="width:180px;">
                <form:option value="0" label="所有" />
                <form:options items="${fns:getKefuList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>
        </c:if>
        &nbsp;&nbsp;
        <label>客服类型：</label>
        <select id="subFlag" name="subFlag" class="input-small" style="width:125px;">
            <option value="-1" <c:out value="${(empty rptSearchCondition.subFlag)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${keFuTypeEnumList}" var="dict">
                <option value="${dict.value}" <c:out
                        value="${(rptSearchCondition.subFlag eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
            </c:forEach>
        </select>
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
            <shiro:hasPermission name="rpt:keFuCompleteTimeNewReport:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询" /></shiro:hasPermission>
            &nbsp;&nbsp;
            <shiro:hasPermission name="rpt:keFuCompleteTimeNewReport:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
        </c:if>

    </div>
    &nbsp;&nbsp;
    <c:if test="${!currentuser.isCustomer() && !currentuser.isSaleman()}">
        <div>
            <label class="control-label">服务网点：</label>
            <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                      labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                      width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>
            &nbsp;&nbsp;
            <label>区域：</label>
            <sys:treeselectareanew id="area" name="areaId" value="${rptSearchCondition.areaId}" levelValue="${rptSearchCondition.areaLevel}" nodeLevel="true"
                            labelName="areaName" labelValue="${rptSearchCondition.areaName }" title="区域"
                            url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1"
                            nameLevel="3" />
            &nbsp;&nbsp;
            <label>服务品类：</label>
            <select id="productCategory" name="productCategory" class="input-small" style="width:182px;">
                <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}"/>>所有
                </option>
                <c:forEach items="${productCategoryList}" var="dict">
                    <option value="${dict.id}" <c:out
                            value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
                </c:forEach>
            </select>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <shiro:hasPermission name="rpt:keFuCompleteTimeNewReport:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询" /></shiro:hasPermission>
            &nbsp;&nbsp;
            <shiro:hasPermission name="rpt:keFuCompleteTimeNewReport:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
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
                        【比率】完成或未完成工单数量/(下单数量 - 取消单数量)*100%<br/>
                        【未完成】未完成工单数量
                    </div>
                </div>
                <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
                </div>
            </div>
        </div>
    </c:if>
</form:form>
<sys:message content="${message}"/>
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table  table-bordered table-condensed table-hover" style="table-layout:fixed;">
        <thead>
        <tr>
            <th rowspan="2" width="100">下单日期</th>
            <th rowspan="2" width="80">下单数量</th>
            <th colspan="2" width="200">24小时</th>
            <th colspan="2" width="200">48小时</th>
            <th colspan="2" width="200">72小时</th>
            <th colspan="2" width="200">72小时外</th>
            <th rowspan="2" width="100">未完成</th>
            <th rowspan="2" width="100">比率</th>
        </tr>
        <tr>
            <th width="100">完成</th>
            <th width="100">比率</th>
            <th width="100">完成</th>
            <th width="100">比率</th>
            <th width="100">完成</th>
            <th width="100">比率</th>
            <th width="100">完成</th>
            <th width="100">比率</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${rptSearchCondition.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <tr>
                <td>
                    <c:if test="${rptSearchCondition.list.size()!=rowIndex}">
                        ${item.orderCreateDate}
                    </c:if>
                    <c:if test="${rptSearchCondition.list.size()==rowIndex}">
                        合计
                    </c:if>
                </td>
                <td>${item.theTotalOrder}</td>
                <td>${item.complete24hour}</td>
                <td>${item.complete24hourRate}%</td>
                <td>${item.complete48hour + item.complete24hour}</td>
                <td>${item.complete48hourRate}%</td>
                <td>${item.complete72hour + item.complete48hour + item.complete24hour}</td>
                <td>${item.complete72hourRate}%</td>
                <td>${item.overComplete72hour}</td>
                <td>${item.overComplete72hourRate}%</td>
                <td>${item.unfulfilledOrder}</td>
                <td>${item.unfulfilledOrderRate}%</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%--<script type="text/javascript">--%>
<%--    $(document).ready(function() {--%>
<%--        if($("#contentTable tbody>tr").length>0) {--%>
<%--            //无数据报错--%>
<%--            var h = $(window).height();--%>
<%--            var w = $(window).width();--%>
<%--            $("#contentTable").toSuperTable({--%>
<%--                width: w - 10,--%>
<%--                height: h - 168,--%>
<%--                fixedCols: 2,--%>
<%--                headerRows: 2,--%>
<%--                colWidths: [100, 100,--%>
<%--                    <c:forEach var="i" begin="1" end="8" step="1">120,</c:forEach>--%>
<%--                    120],--%>
<%--                onStart: function () {--%>
<%--                },--%>
<%--                onFinish: function () {--%>
<%--                }--%>
<%--            });--%>
<%--        }--%>
<%--    });--%>
<%--</script>--%>
</body>
</html>

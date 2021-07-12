<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>KA均单费用</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/common/moment.min.js" type="text/javascript"></script>
    <script type="text/javascript">
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
            $("#btnSubmit").click(function() {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/rpt/provider/keFuAverageOrderFee/vipKeFuAverageOrderFeeReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/keFuAverageOrderFee/vipCheckExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/keFuAverageOrderFee/vipExport?"+ (new Date()).getTime(),
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
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">KA均单费用</a></li>
    <li>
        <a href="${ctx}/rpt/provider/keFuAverageOrderFee/keFuAverageOrderFeeReport">客服均单费用</a>
    </li>
    <c:if test="${siteCode eq 'CW'}">
    <li>
        <a href="${ctx}/rpt/provider/keFuAverageOrderFee/CWKeFuAverageOrderFeeReport">厨电客服均单费用</a>
    </li>
    </c:if>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/keFuAverageOrderFee/vipKeFuAverageOrderFeeReport" method="post" class="breadcrumb form-search">
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="hidden" id="customer.name" name="customer.name" value="${currentuser.customerAccountProfile.customer.name}" />
                <input type="hidden" id="customer.id" name="customer.id" value="${currentuser.customerAccountProfile.customer.id}" />
            </c:when>
            <c:otherwise>
                <label>客户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:225px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}" />>所有</option>
                    <c:forEach items="${fns:getMyVipCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                    </c:forEach>
                </select>
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
        <label>区域：</label>
        <sys:treeselectareanew id="area" name="areaId" value="${rptSearchCondition.areaId}" levelValue="${rptSearchCondition.areaLevel}" nodeLevel="true"
                               labelName="areaName" labelValue="${rptSearchCondition.areaName }" title="区域"
                               url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1"
                               nameLevel="3" />
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:keFuAverageOrderFee:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:keFuAverageOrderFee:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
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
                    【完成单数】省市区的完成单数(VIP客户)<br/>
                    【合计费用】其他费用+远程费用(VIP客户其他远程费用)<br/>
                    【均单费用】合计费用/完成单数
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}"/>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="table-layout: fixed;">
        <thead>
        <tr>
            <th width="100">省市区</th>
            <th width="120">完成单数</th>
            <th width="100">远程费用</th>
            <th width="100">其他费用</th>
            <th width="120">合计费用(元)</th>
            <th width="160">均单费用(元)</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="totalOrderSum" value="0" />
        <c:set var="totalOrderFee" value="0" />
        <c:set var="totalTravelFee" value="0" />
        <c:set var="totalFee" value="0" />
        <c:forEach items="${list}" var="item">
            <tr>
                <td>${item.areaName}</td>
                <td>${item.orderSum}</td>
                <c:set var="totalOrderSum" value="${totalOrderSum +item.orderSum}"/>

                <td><fmt:formatNumber value="${item.travelFee}" pattern="0.00"/></td>
                <c:set var="totalTravelFee" value="${totalTravelFee + item.travelFee}"/>

                <td><fmt:formatNumber value="${item.orderFee}" pattern="0.00"/></td>
                <c:set var="totalOrderFee" value="${totalOrderFee + item.orderFee}"/>

                <td><fmt:formatNumber value="${item.travelFee + item.orderFee}" pattern="0.00"/></td>
                <c:set var="totalFee" value="${totalFee+item.travelFee + item.orderFee}"/>

                <td><fmt:formatNumber value="${item.averageOrderFee}" pattern="0.00"/></td>
            </tr>
        </c:forEach>
        <c:if test="${list.size()>0}">
            <tr style="color: red;">
                <td>合计</td>
                <td>${totalOrderSum}</td>
                <td><fmt:formatNumber value="${totalTravelFee}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${totalOrderFee}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${totalFee}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${totalOrderSum == 0 ?totalFee : totalFee/totalOrderSum}" pattern="0.00"/></td>
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

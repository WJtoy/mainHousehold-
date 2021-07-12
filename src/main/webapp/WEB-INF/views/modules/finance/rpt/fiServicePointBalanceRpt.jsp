<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <title>网点余额</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <%--<link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>--%>
    <%--<script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>--%>
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
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
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
            $("#btnSubmit").click(function(){
                top.$.jBox.tip('请稍候...', 'loading');
                $("#pageNo").val(1);
                $("#searchForm").attr("action","${ctx}/finance/rpt/servicePointRpt/servicePointBalanceRpt");
                $("#searchForm").submit();
            });


            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/finance/rpt/servicePointRpt/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/finance/rpt/servicePointRpt/export?"+ (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">网点余额</a></li>
</ul>
<form:form id="searchForm"  modelAttribute="rptSearchCondition"  action="${ctx}/finance/rpt/servicePointRpt/servicePointBalanceRpt" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label class="control-label">服务网点：</label>
        <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                  labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                  width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>        &nbsp;&nbsp;
        &nbsp;&nbsp;
        <label>订单结算方式：</label>
        <select id="paymentType" name="paymentType" class="input-small" style="width:125px;">
            <option value="" <c:out value="${(empty rptSearchCondition.paymentType)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${fns:getDictListFromMS('PaymentType')}" var="dict"><%--切换为微服务--%>
                <option value="${dict.value}" <c:out value="${(rptSearchCondition.paymentType eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
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
        <shiro:hasPermission name="rpt:finance:servicePointBalanceRpt:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:servicePointBalanceRpt:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出" /></shiro:hasPermission>
    </div>

    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 200px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据<br/>
                    栏位说明：<br/>
                    【本月余额】上月余额+本月应付-本月已付
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}" />

<div id="divGrid" style="overflow-x:auto;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:auto;">
        <thead>
        <tr>
            <th width="60">序号</th>

            <th width="140">网点编号</th>
            <th width="170">网点名称</th>
            <th width="120">负责人</th>
            <th width="120">手机</th>
            <th width="120">联系电话</th>
            <th width="120">开户银行</th>
            <th width="120">开户人</th>
            <th width="170">银行账号</th>

            <th width="80">结算方式</th>
            <th width="100">上月余额</th>
            <th width="100">本月应付</th>
            <th width="100">本月已付</th>
            <th width="100">本月余额</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="totalPreBalance" value="0"/>
        <c:set var="totalPayableAmount" value="0"/>
        <c:set var="totalPaidAmount" value="0"/>
        <c:set var="totalTheBalance" value="0"/>

        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">

            <tr>
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <td rowspan="${item.maxRow}">${rowIndex}</td>

            <td rowspan="${item.maxRow}">${item.servicePointNo}</td>
            <td rowspan="${item.maxRow}">${item.servicePointName}</td>
            <td rowspan="${item.maxRow}">${item.engineerName}</td>
            <td rowspan="${item.maxRow}">${item.contactInfo1}</td>
            <td rowspan="${item.maxRow}">${item.contactInfo2}</td>
            <td rowspan="${item.maxRow}">${item.bank.label}</td>
            <td rowspan="${item.maxRow}">${item.bankOwner}</td>
            <td rowspan="${item.maxRow}">${item.bankNo}</td>
                <td>
                <c:if test="${item.paymentType != null}">
                        ${item.paymentType.label}
                </c:if>
                </td>
            <td><fmt:formatNumber value="${item.preBalance}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${item.payableAmount}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${item.paidAmount}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${item.theBalance}" pattern="0.00"/></td>
            <c:set var="totalPreBalance" value="${totalPreBalance + item.preBalance}"/>
            <c:set var="totalPayableAmount" value="${totalPayableAmount + item.payableAmount}"/>
            <c:set var="totalPaidAmount" value="${totalPaidAmount + item.paidAmount}"/>
            <c:set var="totalTheBalance" value="${totalTheBalance + item.theBalance}"/>

        </c:forEach>

        <c:if test="${page.list.size()>0}">
            <tr style="color: red;">
                <td colspan="10">合计</td>
                <td><fmt:formatNumber value="${totalPreBalance}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${totalPayableAmount}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${totalPaidAmount}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${totalTheBalance}" pattern="0.00"/></td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>

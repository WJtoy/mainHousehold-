<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>每月完工单情况</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
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
                $("#searchForm").attr("action","${ctx}/rpt/provider/keFuCompletedMonth/keFuCompletedMonthRpt");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/keFuCompletedMonth/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/keFuCompletedMonth/export?"+ (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">每月完工单情况</a></li>

    <li>
        <a href="${ctx}/rpt/provider/keFuCompletedMonth/keFuCompletedMonthChart">客服完工图表</a>
    </li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/keFuCompletedMonth/keFuCompletedMonthRpt" method="post" class="breadcrumb form-search">
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>客服：</label>
        <form:select path="kefuId" style="width:180px;">
            <form:option value="0" label="所有" />
            <form:options items="${fns:getKefuList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
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
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}" />>${year}</option>
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
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" />
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
                    数据时效：实时数据(5分钟延迟)<br/>
                    统计方式：客评时间 <br/>
                    栏位说明：<br/>
                    【客服每月完工单对比】(当月完工单量-上月完工单量)/上月完工单量
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}" />
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed" cellspacing="0" width="100%">
        <thead>
        <tr>
            <th rowspan="2" width="60">序号</th>
            <th rowspan="2" width="120">客服</th>
            <c:choose>
                <c:when test="${rptSearchCondition.rowsCount == 0}">
                    <th rowspan="2">每月完工单(单)</th>
                </c:when>
                <c:otherwise>
                    <th colspan="12" width="${12*120}">每月完工单(单)</th>
                </c:otherwise>
            </c:choose>
            <th rowspan="2" width="180">合计(单)</th>
        </tr>
        <c:if test="${rptSearchCondition.rowsCount != 0}">
            <tr>
                <c:forEach var="i" begin="1" end="12" step="1">
                    <th>${i}月</th>
                </c:forEach>
            </tr>
        </c:if>
        </thead>

        <tbody>
        <c:set var="rowIndex" value="0"></c:set>
        <c:forEach items="${rptSearchCondition.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"></c:set>
            <c:choose>
                <c:when test="${item.rowNumber < rptSearchCondition.sumRowNumber}">
                    <tr>
                        <td>${rowIndex}</td>
                        <td>${item.kefuName}</td>
                        <c:forEach var="i" begin="1" end="12" step="1">
                            <c:set var="colname" value="m${i}" />
                            <td>
                                <c:if test="${item[colname]!=0}">
                                    <fmt:formatNumber maxFractionDigits="0">${item[colname]}</fmt:formatNumber>
                                </c:if>
                            </td>
                        </c:forEach>
                        <td>
                            <c:if test="${item.total!=0}">
                                <fmt:formatNumber maxFractionDigits="0">${item.total}</fmt:formatNumber>
                            </c:if>
                        </td>
                    </tr>
                </c:when>
                <c:when test="${item.rowNumber == rptSearchCondition.sumRowNumber}">
                    <tr>
                        <td></td>
                        <td>${item.kefuName}</td>
                        <c:forEach var="i" begin="1" end="12" step="1">
                            <c:set var="colname" value="m${i}" />
                            <td>
                                <c:if test="${item[colname]!=0}">
                                    <fmt:formatNumber maxFractionDigits="0">${item[colname]}</fmt:formatNumber>
                                </c:if>
                            </td>
                        </c:forEach>
                        <td>
                            <c:if test="${item.total!=0}">
                                <fmt:formatNumber maxFractionDigits="0">${item.total}</fmt:formatNumber>
                            </c:if>
                        </td>
                    </tr>
                </c:when>
                <c:when test="${item.rowNumber == rptSearchCondition.perRowNumber}">
                    <tr>
                        <td></td>
                        <td>${item.kefuName}</td>
                        <c:forEach var="i" begin="1" end="12" step="1">
                            <c:set var="colname" value="m${i}" />
                            <td>
                                <c:if test="${item[colname]!=0}">
                                    <fmt:formatNumber maxFractionDigits="2">${item[colname]}</fmt:formatNumber>%
                                </c:if>
                            </td>
                        </c:forEach>
                        <td></td>
                    </tr>
                </c:when>
            </c:choose>
        </c:forEach>
        </tbody>
    </table>

<script type="text/javascript">
    $(document).ready(function() {
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错
            var h = $(window).height();
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 138,
                fixedCols: 2,
                headerRows: 2,
                colWidths: [60, 120,
                    <c:forEach var="i" begin="1" end="12" step="1">120,</c:forEach>
                    180],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
    });
</script>

</body>
</html>

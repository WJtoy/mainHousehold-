﻿<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE HTML>
<html>
<head>
    <title>客评数量统计</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
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
                $("#searchForm").attr("action","${ctx}/rpt/provider/gradeQtyRpt/gradeQtyDailyRpt");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/gradeQtyRpt/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/gradeQtyRpt/export?"+ (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">客评数量统计</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/gradeQtyRpt/gradeQtyDailyRpt" method="post" class="breadcrumb form-search">
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}" />>${year}</option>
            </c:forEach>
        </select>
        &nbsp; &nbsp;

        <label>月份：</label>
        <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${1+i}" <c:out value="${(rptSearchCondition.selectedMonth eq 1+i)?'selected=selected':''}" />>${1+i}</option>
            </c:forEach>
        </select>

        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询" />
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
                    数据时效：隔天数据<br/>
                    统计方式：审单时间<br/>
                    栏位说明：<br/>
                    【占比】当天各类型客评单量/当天总客评单量*100%
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}"/>

<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-bordered table-condensed" style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">
        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="100"></th>

            <th width="130"></th>
            <th width="130"></th>

            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="130"></th>
            <th width="100"></th>
        </tr>
        <tr>
            <th>统计项目</th>
            <th colspan="2">手动客评</th>
            <th colspan="2">APP完成客评</th>
            <th colspan="2">短信客评</th>
            <th colspan="2">语音客评</th>
            <th rowspan="2">总客评单量</th>
        </tr>
        <tr>
            <th>天</th>
            <th>单量</th>
            <th>客评占比</th>
            <th>单量</th>
            <th>客评占比</th>
            <th>单量</th>
            <th>客评占比</th>
            <th>单量</th>
            <th>客评占比</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${rptSearchCondition.list}" var="item">
           <c:set var="rowIndex" value="${rowIndex+1}"/>
           <c:if test="${rptSearchCondition.list.size()>0}">
        <tr>
            <td>
                <c:if test="${rptSearchCondition.list.size()!=rowIndex}">
                      ${item.dayIndex}
                </c:if>
                <c:if test="${rptSearchCondition.list.size()==rowIndex}">
                    合计
                </c:if>
            </td>
            <td>${item.manualGradeQty}</td>
            <td>${item.manualGradeQtyPercentage}%</td>
            <td>${item.appGradeQty}</td>
            <td>${item.appGradeQtyPercentage}%</td>
            <td>${item.smsGradeQty}</td>
            <td>${item.smsGradeQtyPercentage}%</td>
            <td>${item.voiceGradeQty}</td>
            <td>${item.voiceGradeQtyPercentage}%</td>
            <td>${item.totalQty}</td>
        </tr>
            </c:if>
            </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
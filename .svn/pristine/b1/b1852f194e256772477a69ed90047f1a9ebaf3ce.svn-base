<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <title>突击单分布表</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>

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
            $("#contentTable").treeTable({expandLevel : 3});

            $("#btnSubmit").click(function(){
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/rpt/provider/crushArea/crushAreaList");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/crushArea/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/crushArea/Export?"+ (new Date()).getTime(),
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
        });


    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">突击单区域表</a></li>
</ul>
<form:form id="searchForm"  modelAttribute="rptSearchCondition"  action="${ctx}/rpt/provider/crushArea/crushAreaList" method="post" class="breadcrumb form-search">
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>服务品类：</label>
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
                <option value="${year}" <c:out value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}" />>${year}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>月份 ：</label>
        <select id="month" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${i+1}" <c:out value="${(rptSearchCondition.selectedMonth eq i+1)?'selected=selected':''}" />>${i+1}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:crushAreaQty:view"><input id="btnSubmit" class="btn btn-primary"type="button" value="查询" /></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:crushAreaQty:export"><input id="btnExport"class="btn btn-primary" type="button" value="导出" /></shiro:hasPermission>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 200px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据<br/>
                    统计方式：配件单创建时间<br/>
                    栏位说明：<br/>
                    【完成时间】配件单完成时间
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
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错
            var h = $(window).height();
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 138,
                fixedCols: 1,
                headerRows: 2,
                colWidths: [300,
                    <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">80,</c:forEach>
                    100],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h-138);
        }
    });
</script>
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;">
        <thead>
        <tr>
            <th rowspan="3" width="100">省市区/街道</th>
            <th colspan="${rptSearchCondition.days}" width="${rptSearchCondition.days*80}">突击单量(单)</th>
            <th rowspan="2" width="100">合计(单)</th>
        </tr>
        <tr>
            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                <th>${i}</th>
            </c:forEach>
        </tr>
        </thead>
        <tbody>

        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${list}" var="provinceItem">
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <c:choose>
                <c:when test="${rowIndex < list.size()}">
                    <tr style="color: dodgerblue; background-color: lightgrey;">
                        <td>${provinceItem.provinceName}</td>
                        <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                            <c:set var="dColname" value="t${i}"/>
                            <td>${provinceItem[dColname] eq 0 ? '' : provinceItem[dColname]}</td>
                        </c:forEach>
                        <td>${provinceItem.crushTotal}</td>
                    </tr>

                     <c:forEach items="${provinceItem.itemList}" var="cityItem">
                         <tr style="color:cornflowerblue;background-color: GhostWhite">
                         <td>${cityItem.provinceName}${cityItem.cityName}</td>
                         <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                             <c:set var="dColname" value="t${i}" />
                             <td>${cityItem[dColname] eq 0 ? '' : cityItem[dColname]}</td>
                         </c:forEach>
                         <td>${cityItem.crushTotal}</td>
                         </tr>
                        <c:forEach items="${cityItem.itemList}" var="countyItem">
                            <tr style="color:cornflowerblue;background-color: whitesmoke ">
                            <td>${countyItem.provinceName}${countyItem.cityName}${countyItem.countyName}</td>
                            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                                <c:set var="dColname" value="t${i}" />
                                <td>${countyItem[dColname] eq 0 ? '' : countyItem[dColname]}</td>
                            </c:forEach>
                            <td>${countyItem.crushTotal}</td>
                            </tr>
                             <c:forEach items="${countyItem.itemList}" var="townItem">
                                <tr>
                                <td>${townItem.provinceName}${townItem.cityName}${townItem.countyName}${townItem.streetName}</td>
                                <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                                    <c:set var="dColname" value="t${i}" />
                                    <td>${townItem[dColname] eq 0 ? '' : townItem[dColname]}</td>
                                </c:forEach>
                                <td>${townItem.crushTotal}</td>
                            </c:forEach>
                        </c:forEach>
                     </c:forEach>
                        </tr>
                    </c:when>
                <c:otherwise>
                    <tr style="color: red;">
                        <td colspan="1">合计</td>
                        <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                            <c:set var="dColname" value="t${i}" />
                            <td>
                               ${provinceItem[dColname] eq 0 ? '' : provinceItem[dColname]}
                            </td>
                        </c:forEach>
                        <td>
                           ${provinceItem.crushTotal}
                        </td>
                    </tr>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>

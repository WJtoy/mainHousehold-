<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>网点基础资料</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/area/AreaFourLevel.js" type="text/javascript"></script>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }

        .provinceCityAll {
            z-index:1000 !important;
        }
    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#btnSubmit").click(function(){
                top.$.jBox.tip('请稍候...', 'loading');
                $("#pageNo").val(1);
                $("#searchForm").attr("action","${ctx}/rpt/provider/servicePointBaseInfo/servicePointBaseInfoRpt");
                $("#searchForm").submit();
            });


            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/servicePointBaseInfo/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/servicePointBaseInfo/export?"+ (new Date()).getTime(),
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


        function page(n,s){
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            $("#searchForm").submit();
            return false;
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">网点基础资料</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition"  action="${ctx}/rpt/provider/servicePointBaseInfo/servicePointBaseInfoRpt" method="post" class="breadcrumb form-search" >
    <div>
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label class="control-label">服务网点：</label>
        <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                  labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                  width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>
        &nbsp;&nbsp;
        <label>区域：</label>
        <sys:areaSelectFourLevel id="areaId" name="areaId" value="${rptSearchCondition.areaId}" levelValue=""
                                 labelValue="${rptSearchCondition.areaName}" labelName="areaName"
                                 title="区域" mustSelectCounty="true" cssClass="required" showMaxLevel="3"> </sys:areaSelectFourLevel>

        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
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
                height: h - 198,
                fixedCols: 2,
                headerRows: 1,
                colWidths:
                    [150, 150, 100, 150, 300,
                        100, 150, 150, 150, 150, 150, 200,
                        100, 200, 100,
                        80,80, 80, 100, 200, 100, 100],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h-198);
        }

    });
</script>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-bordered table-condensed " style="table-layout:fixed;">
        <thead>
        <tr>
            <th style="width:150px">网点编号</th>
            <th style="width:150px">网点名称</th>
            <th style="width:100px">省份</th>
            <th style="width:150px">地级市</th>
            <th style="width:300px">覆盖区域</th>

            <th style="width:100px">姓名</th>
            <th style="width:150px">手机</th>
            <th style="width:150px">电话</th>
            <th style="width:150px">联系方式1</th>
            <th style="width:150px">联系方式2</th>
            <th style="width:150px">签约日期</th>
            <th style="width:200px">详细地址</th>

            <th style="width:100px">开户行</th>
            <th style="width:200px">卡号</th>
            <th style="width:100px">开户人</th>

            <th style="width:80px">等级</th>
            <th style="width:80px">状态</th>
            <th style="width:80px">是否签约</th>
            <th style="width:100px">结算方式</th>
            <th style="width:200px">备注</th>
            <th style="width:100px">接单总量</th>
            <th style="width:100px">当前客评得分</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${page.list}" var="item">
            <tr>
                <td>${item.servicePointNo}</td>
                <td>${item.servicePointName}</td>
                <td>${item.provinceName}</td>
                <td>${item.cityName}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.areasName}">${fns:abbr(item.areasName,40)}</a></td>

                <td>${item.engineerName}</td>
                <td>${item.engineerMobile}</td>
                <td>${item.engineerMobile}</td>
                <td>${item.contactInfo1}</td>
                <td>${item.contactInfo2}</td>
                <td><fmt:formatDate value="${item.contractDate}" pattern="yyyy-MM-dd"/></td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.address}">${fns:abbr(item.address,40)}</a></td>

                <td>${item.bank.label}</td>
                <td>${item.bankNo}</td>
                <td>${item.bankOwner}</td>
                <td>${item.level.label}</td>
                <td>${item.status.label}</td>
                <td>${item.signFlag.label}</td>
                <td>${item.paymentType.label}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.remarks}">${fns:abbr(item.remarks,40)}</a></td>
                <td>${item.orderCount}</td>
                <td>${item.grade}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>
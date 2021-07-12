<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <title>业务业绩明细表</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script type="text/javascript">
        $(document).ready(function () {
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"text-align": "center", "vertical-align": "middle"});
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
            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/customerPerformance/salesManAchievementDetail");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/customerPerformance/customerCheckExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/customerPerformance/customerExport?"+ (new Date()).getTime(),
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
</head>
<body>
<ul class="nav nav-tabs">
    <li>
        <a href="${ctx}/rpt/provider/customerPerformance/salesPerformanceReport">业务业绩排名表</a>
    </li>
    <li class="active"><a href="javascript:void(0);">业务员业绩明细表</a></li>

</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/customerPerformance/salesManAchievementDetail" method="post"
           class="breadcrumb form-search">
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
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
        <c:if test="${!currentuser.isCustomer() && !currentuser.isSaleman()}">
            <label>业务员：</label>
            <form:select path="salesId" style="width:120px;">
                <form:options items="${fns:getSaleList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>
        </c:if>
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
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 150px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据 <br/>
                    统计方式：下单时间
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
        $("#divGrid").css("height", h - 138);
    });
</script>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th>业务员</th>
            <th>客户编号</th>
            <th>客户名称</th>
            <th>派单量数量</th>
            <th>完工单量数量</th>
            <th>未完工单数量</th>
            <th>退单数量</th>
            <th>取消单数量</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:if test="${not empty list}">

            <c:set var="totalNewQty" value="0"/>
            <c:set var="totalFinishQty" value="0"/>
            <c:set var="totalProcessQty" value="0"/>
            <c:set var="totalReturnQty" value="0"/>
            <c:set var="totalCancelQty" value="0"/>
            <c:forEach items="${list}" var="item">

                <c:set var="totalNewQty" value="${totalNewQty + item.createQty}"/>
                <c:set var="totalFinishQty" value="${totalFinishQty + item.finishQty}"/>
                <c:set var="totalProcessQty" value="${totalProcessQty + item.noFinishQty}"/>
                <c:set var="totalReturnQty" value="${totalReturnQty + item.returnQty}"/>
                <c:set var="totalCancelQty" value="${totalCancelQty + item.cancelQty}"/>

                <c:set var="rowIndex" value="${rowIndex+1}"/>

                <tr id="${item.salesId}">
                    <c:choose>
                        <c:when test="${currentuser.isSaleman() && currentuser.subFlag ne 3}">
                            <!-- sale and not manger -->
                            <c:if test="${rowIndex == 1}">
                                <td rowspan="${list.size()}">${item.salesName}</td>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <td >${item.salesName}</td>
                        </c:otherwise>
                    </c:choose>
                    <td>${item.customerCode}</td>
                    <td>${item.customerName}</td>
                    <td>${item.createQty}</td>
                    <td>${item.finishQty}</td>
                    <td>${item.noFinishQty}</td>
                    <td>${item.returnQty}</td>
                    <td>${item.cancelQty}</td>
                </tr>


            </c:forEach>
            <tr style="color: red; font-weight: bold;">
                <td colspan="3">合计</td>
                <td>${totalNewQty}</td>
                <td>${totalFinishQty}</td>
                <td>${totalProcessQty}</td>
                <td>${totalReturnQty}</td>
                <td>${totalCancelQty}</td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
</body>
</html>

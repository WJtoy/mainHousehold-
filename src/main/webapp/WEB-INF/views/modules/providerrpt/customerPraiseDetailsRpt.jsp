<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <title>客户好评明细</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
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
            $("#btnSubmit").click(function() {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/rpt/provider/customerPraiseDetails/customerPraiseDetailsRptData");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/customerPraiseDetails/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/customerPraiseDetails/export?"+ (new Date()).getTime(),
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
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">客户好评</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/customerPraiseDetails/customerPraiseDetailsRptData" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <c:if test="${!currentuser.isCustomer()}">
        <label>客 户：</label>
        <select id="customerId" name="customerId" class="input-small" style="width:225px;">
            <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>
                所有
            </option>
            <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        </c:if>
        &nbsp;&nbsp;
        <c:if test="${!currentuser.isCustomer() && !currentuser.isSaleman()}">
        <label>业 务 员：</label>
        <form:select path="salesId" style="width:120px;">
            <form:option value="" label="所有"/>
            <form:options items="${fns:getSaleList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
        </c:if>
        &nbsp;&nbsp;
        <label>状态：</label>
        <select id="status" name="status" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.status)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${praiseStatusEnumList}" var="dict">
                <option value="${dict.value}" <c:out
                        value="${(rptSearchCondition.status eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>创建时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px"
               maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.beginDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>
        &nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20"
               class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
    </div>
    &nbsp;&nbsp;
    <div>
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
                    数据时效：实时数据<br/>
                    统计方式：好评单创建时间<br/>
                    栏位说明：<br/>
                    【好评费】当好评单为新建、待审核、驳回状态时，好评费显示为向客户申请好评费。当好评单为审核通过、已取消、无效状态时，好评费显示为应收客户好评费
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>

</form:form>
<sys:message content="${message}"/>
<%--<script type="text/javascript">--%>
<%--    $(document).ready(function() {--%>
<%--        if($("#contentTable tbody>tr").length>0) {--%>
<%--            //无数据报错--%>
<%--            var h = $(window).height();--%>
<%--            var w = $(window).width();--%>
<%--            $("#contentTable").toSuperTable({--%>
<%--                width: w-5,--%>
<%--                height: h - 248,--%>
<%--                fixedCols: 2,--%>
<%--                headerRows: 1,--%>
<%--                colWidths:--%>
<%--                    [60,--%>
<%--                        140, 140, 140, 140, 100, 140, 100,--%>
<%--                        100, 200, 140,100],--%>
<%--                onStart: function () {--%>

<%--                },--%>
<%--                onFinish: function () {--%>

<%--                }--%>
<%--            });--%>
<%--        }--%>
<%--        else {--%>
<%--            var h = $(window).height();--%>
<%--            $("#divGrid").height(h-248);--%>
<%--        }--%>
<%--    });--%>
<%--</script>--%>
<div id="divGrid" style="overflow-x:auto;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="30">序号</th>

            <th width="140">客户</th>
            <th width="140">工单单号</th>
            <th width="140">销售单号</th>
            <th width="140">服务单号</th>

            <th width="100">创建时间</th>
            <th width="80">状态</th>
            <th width="140">区域</th>
            <th width="60">用户姓名</th>
            <th width="100">用户电话</th>
            <th width="200">用户地址</th>

            <th width="60">业务员</th>
            <th width="50">好评费</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="totalPriaseFee" value="0"/>
        <c:set var="i" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <c:set var="i" value="${i+1}"/>
            <tr>
                <td>${i}</td>
                <td>${item.customerName}</td>
                <td>${item.orderNo}</td>
                <td>${item.parentBizOrderId}</td>
                <td>${item.workCardId}</td>

                <td ><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <c:choose>
                    <c:when test="${item.status.value == '10' || item.status.value == '20'}">
                        <td><span class="badge badge-info" >${item.status.label}</span></td>
                    </c:when>
                    <c:when test="${item.status.value == '30'}">
                        <td ><span class="badge badge-important">${item.status.label}</span></td>
                    </c:when>
                    <c:when test="${item.status.value == '40'}">
                        <td><span  class="badge badge-success">${item.status.label}</span></td>
                    </c:when>
                    <c:when test="${item.status.value == '50' || item.status.value == '60'}">
                        <td><span class="badge">${item.status.label}</span></td>
                    </c:when>
                    <c:otherwise>
                        <td><span>${item.status.label}</span></td>
                    </c:otherwise>
                </c:choose>
                <td>${item.areaName}</td>
                <td>${item.userName}</td>
                <td>${item.userPhone}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.userAddress}">${fns:abbr(item.userAddress,40)}</a></td>
                <td>${item.keFuName}</td>
                <c:choose>
                    <c:when test="${item.status.value >= '40'}">
                        <td>${item.praiseFee}</td>
                        <c:set var="totalPriaseFee" value="${totalPriaseFee+item.praiseFee}"/>
                    </c:when>
                    <c:otherwise>
                        <td>${item.applyPraiseFee}</td>
                        <c:set var="totalPriaseFee" value="${totalPriaseFee+item.applyPraiseFee}"/>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:forEach>
        <c:if test="${page.list.size()>0}">
            <tr style="font-weight: bold; color: red;">
                <td colspan="11"></td>
                <td>合计:</td>
                <td><fmt:formatNumber value="${totalPriaseFee}" pattern="0.00"/></td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>




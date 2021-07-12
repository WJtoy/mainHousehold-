<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <title>客服好评明细</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/area/AreaFourLevel.js" type="text/javascript"></script>
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
                $("#searchForm").attr("action","${ctx}/rpt/provider/keFuPraiseDetails/keFuPraiseDetailsRptData");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/keFuPraiseDetails/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/keFuPraiseDetails/export?"+ (new Date()).getTime(),
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
        .sBase{z-index: 2}
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">客服好评</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/keFuPraiseDetails/keFuPraiseDetailsRptData" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>客 服：</label>
        <form:select path="kefuId" style="width:180px;">
            <form:option value="0" label="所有" />
            <form:options items="${fns:getKefuList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
        &nbsp;&nbsp;
        <c:choose>
            <c:when test="${currentuser.isEngineer()}">
                <input id="servicePointId" type="hidden" name="servicePointId" value="${userId}" maxlength="50"
                       style="width:245px;"/>
                <input id="servicePointName" type="hidden" name="servicePointNo.name" value="${userName}" maxlength="50"
                       style="width:245px;"/>
            </c:when>
            <c:otherwise>
                <label class="control-label">服务网点：</label>
                <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                          labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                          width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>
            </c:otherwise>
        </c:choose>
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
        <label>区 域：</label>
        <sys:areaSelectFourLevel id="area" name="areaId" value="${rptSearchCondition.areaId}" levelValue=""
                        labelValue="${rptSearchCondition.areaName}" labelName="areaName"
                        title="区域" mustSelectCounty="true" cssClass="required" showMaxLevel="3"> </sys:areaSelectFourLevel>
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
                    【好评费】当好评单为新建、待审核、驳回状态时，好评费显示为网点申请好评费。好评单为审核通过、已取消、无效状态时，好评费显示为应付网点好评费
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
<%--                width: w-10,--%>
<%--                height: h - 248,--%>
<%--                fixedCols: 2,--%>
<%--                headerRows: 1,--%>
<%--                colWidths:--%>
<%--                    [30,--%>
<%--                        140, 140, 80, 100, 140, 100,--%>
<%--                        100, 200, 140, 140,--%>
<%--                        80,80],--%>
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
            <th width="80">状态</th>
            <th width="100">创建时间</th>
            <th width="50">省</th>
            <th width="50">市</th>
            <th width="50">区</th>
            <th width="60">用户姓名</th>

            <th width="100">用户电话</th>
            <th width="200">用户地址</th>
            <th width="140">网点编号</th>
            <th width="140">网点名称</th>
            <th width="60">师傅</th>

            <th width="60">客服</th>
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
<%--                <td>${item.parentBizOrderId}</td>--%>
<%--                <td>${item.workCardId}</td>--%>
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
                <td ><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td>${item.provinceName}</td>
                <td>${item.cityName}</td>
                <td>${item.areaName}</td>
                <td>${item.userName}</td>
                <td>${item.userPhone}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.userAddress}">${fns:abbr(item.userAddress,40)}</a></td>
                <td>${item.servicePointNo}</td>
                <td>${item.servicePointName}</td>
                <td>${item.engineerName}</td>
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
                <td colspan="14"></td>
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




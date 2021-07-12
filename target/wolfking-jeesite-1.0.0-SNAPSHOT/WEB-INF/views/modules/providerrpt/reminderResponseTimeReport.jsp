<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>催单回复时效</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
<%--    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>--%>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <script src="${ctxStatic}/area/AreaFourLevel.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
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
    <script src="${ctxStatic}/common/moment.min.js" type="text/javascript"></script>
    <script type="text/javascript">
        function datePicker(startId, endId) {
            $('#' + startId).unbind("click");
            $('#' + startId).bind("click", function () {
                WdatePicker({
                    readOnly: true,
                    dateFmt: 'yyyy-MM-dd',
                    isShowClear: false | false,
                    maxDate: '#F{$dp.$D(\'' + endId + '\')||\'%y-%M-%d\'}',
                    minDate: '#F{$dp.$D(\'' + endId + '\',{M:-3,d:+1})}'
                });
            });

            $('#' + endId).unbind("click");
            $('#' + endId).bind("click", function () {
                WdatePicker({
                    dateFmt: 'yyyy-MM-dd',
                    isShowClear: false | false,
                    minDate: '#F{$dp.$D(\'' + startId + '\')||\'%y-%M-%d\'}',
                    maxDate: '#F{$dp.$D(\'' + startId + '\',{M:3,d:-1})}'
                });
            });
        }
    </script>
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
                $("#pageNo").val(1);
                $("#searchForm").attr("action", "${ctx}/rpt/provider/reminderResponseTime/reminderResponseTimeReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/reminderResponseTime/checkExportTask?" + (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/reminderResponseTime/export?" + (new Date()).getTime(),
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
    <li>
        <a href="${ctx}/rpt/provider/kaReminderResponseTime/kaReminderResponseTimeReport">KA回复时效</a>
    </li>
    <li class="active"><a href="javascript:void(0);">催单回复时效</a></li>
</ul>

<form:form id="searchForm" modelAttribute="rptSearchCondition"
           action="${ctx}/rpt/provider/reminderResponseTime/reminderResponseTimeReport" method="post"
           class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="searchType" name="searchType" type="hidden" value="processing"/>
    <input id="repageFlag" name="repageFlag" type="hidden" value="false"/>
    <div style="margin-top:0px; width: 90%" class="control-group">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label class="label-search">工单号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${rptSearchCondition.orderNo}"
               maxlength="20"/>
        &nbsp;&nbsp;
        <label>区 域：</label>
        <sys:areaSelectFourLevel id="areaId" name="areaId" value="${rptSearchCondition.areaId}" levelValue=""
                                labelValue="${rptSearchCondition.areaName}" labelName="areaName"
                                title="区域" mustSelectCounty="true" cssClass="required" showMaxLevel="3"> </sys:areaSelectFourLevel>
        &nbsp;&nbsp;
        <label class="label-search">催单号：</label>&nbsp;
        <input type=text class="input-small" id="reminderNo" name="reminderNo" value="${rptSearchCondition.reminderNo}"
               maxlength="20"/>
        &nbsp;&nbsp;
        <label>次数：</label>
        <form:input path="reminderTimes" htmlEscape="false" maxlength="6" class="input-small" onkeyup="value=value.replace(/[^\d]/g,'')"/>
        &nbsp;&nbsp;
        <label>发起时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;"
               maxlength="20" class="input-small Wdate"
               value="${fns:formatDate(rptSearchCondition.beginDate,'yyyy-MM-dd')}"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
               class="input-small Wdate" value="${fns:formatDate(rptSearchCondition.endDate,'yyyy-MM-dd')}"/>
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
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据<br/>
                    统计方式：催单时间<br/>
                    栏位说明：<br/>
                    【回复用时】催单回复时间-创建催单时间(单位：小时)<br/>
                    【客评用时】完工时间-创建催单时间(单位：小时)
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}"/>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover"
           style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">
        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="60"></th>

            <th width="120"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="100"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="120"></th>
            <th width="80"></th>
            <th width="100"></th>
            <th width="150"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="80"></th>
            <th width="150"></th>
            <th width="80"></th>
            <th width="100"></th>
            <th width="80"></th>

        </tr>
        <tr>
            <th>序号</th>
            <th>工单号</th>
            <th>客服</th>
            <th>网点名称</th>
            <th>用户姓名</th>
            <th>用户电话</th>
            <th>省</th>
            <th>市</th>
            <th>区</th>
            <th>催单号</th>
            <th>催单次数</th>
            <th>发起时间</th>
            <th>催单意见</th>
            <th>催单回复人</th>
            <th>回复时间</th>
            <th>回复用时</th>
            <th>回复结果</th>
            <th>师傅</th>
            <th>客评时间</th>
            <th>客评用时</th>
        </tr>
        </thead>
        <tbody>

        <c:set var="reminderTime" value="0"/>
        <c:set var="processTimelines" value="0.0"/>
        <c:set var="orderTimelines" value="0.0"/>


        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <tr>
                <c:set var="rowIndex" value="${rowIndex+1}"/>
                <td>${rowIndex}</td>
                <td>${item.orderNo}</td>
                <td>${item.keFuName}</td>
                <td>${item.servicePointName}</td>
                <td>${item.userName}</td>
                <td>${item.userPhone}</td>
                <td>${item.provinceName}</td>
                <td>${item.cityName}</td>
                <td>${item.areaName}</td>
                <td>${item.reminderNo}</td>
                <td>${item.reminderTimes}</td>
                <td><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td class="autocut"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.reminderRemark}">${fns:abbr(item.reminderRemark,40)}</a></td>
                <td>${item.processBy}</td>
                <td><fmt:formatDate value="${item.processDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatNumber value="${item.processTimeliness}" pattern="0.00"/></td>
                <td class="autocut"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.processRemark}">${fns:abbr(item.processRemark,40)}</a></td>
                <td>${item.engineerName}</td>
                <td><fmt:formatDate value="${item.completeDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatNumber value="${item.orderTimeliness}" pattern="0.00"/></td>
                <c:set var="reminderTime" value="${reminderTime+item.reminderTimes}"/>
                <c:set var="processTimelines" value="${processTimelines+item.processTimeliness}"/>
                <c:set var="orderTimelines" value="${orderTimelines+item.orderTimeliness}"/>

            </tr>
        </c:forEach>

        <c:if test="${page.list.size()>0}">
            <tr>
                <td colspan="10"><B>合计</B></td>
                <td>${reminderTime}</td>
                <td colspan="4"></td>
                <td><fmt:formatNumber value="${processTimelines}" pattern="0.00"/></td>
                <td colspan="3"></td>
                <td><fmt:formatNumber value="${orderTimelines}" pattern="0.00"/></td>

            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>

<script type="text/javascript">
    datePicker('beginDate', 'endDate');
</script>
</body>
</html>

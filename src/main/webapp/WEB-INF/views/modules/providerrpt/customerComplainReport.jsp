<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>投诉数据报表</title>
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
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
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
                $("#searchForm").attr("action", "${ctx}/rpt/provider/customerComplain/customerComplainReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/customerComplain/checkExportTask?" + (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/customerComplain/export?" + (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">投诉数据报表</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/customerComplain/customerComplainReport" method="post" class="breadcrumb form-search">
    <div style="width: 90%">
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>

        <label>投诉日期：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.beginDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>
        &nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        &nbsp;&nbsp;
        <label>客 户：</label>
        <select id="customerId" name="customerId" class="input-small" style="width:225px;">
            <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>

        &nbsp;&nbsp;
        <label class="control-label">客服：</label>
        <form:select path="kefuId" style="width:180px;">
            <form:option value="" label="所有" />
            <form:options items="${fns:getKefuList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
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
    </div>
    &nbsp;&nbsp;
    <div>
        <c:set var="complainStatusList" value="${fns:getDictListFromMS('complain_status')}" />
        <label>状态：</label>
        <select id="status" name="status" style="width:100px;">
            <option value="" selected="selected">所有</option>
            <c:forEach items="${complainStatusList}" var="complainStatusDict">
                <option value="${complainStatusDict.value}" <c:out value="${(rptSearchCondition.status eq complainStatusDict.value)?'selected=selected':''}" />>${complainStatusDict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>区域：</label>
        <sys:treeselectareanew id="area" name="areaId" value="${rptSearchCondition.areaId}" levelValue="${rptSearchCondition.areaLevel}" nodeLevel="true"
                        labelName="areaName" labelValue="${rptSearchCondition.areaName }" title="区域"
                        url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1"
                        nameLevel="3" />
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
                    数据时效：实时数据<br/>
                    统计方式：投诉时间
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>

</form:form>
<sys:message content="${message}"/>
<script type="text/javascript">
    $(document).ready(function() {
        var h = $(window).height();
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 240,
                fixedCols: 1,
                headerRows: 3,
                colWidths:
                    [60,
                        120, 140, 140,
                        70, 70, 70, 70,70, 140, 100,100,
                        90,90,120,100, 100,100,100,
                        100, 100, 70, 100, 90 ,100, 100, 100,100,100],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h-240)
        }
    });
</script>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-bordered table-condensed" style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">
        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="60"></th>

            <th width="120"></th>
            <th width="140"></th>
            <th width="140"></th>

            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="70"></th>
            <th width="140"></th>
            <th width="100"></th>
            <th width="100"></th>

            <th width="90"></th>
            <th width="90"></th>
            <th width="120"></th>
            <th width="100"></th>
            <th width="100"></th>

            <th width="100"></th>
            <th width="100"></th>

            <th width="100"></th>
            <th width="100"></th>
            <th width="70"></th>
            <th width="100"></th>
            <th width="90"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
        </tr>

        <tr>
            <th rowspan="2">序号</th>
            <th colspan="16">投诉信息</th>
            <th colspan="2">判责</th>
            <th colspan="10">处理结果</th>
        </tr>
        <tr>
            <th>投诉单号</th>
            <th>工单号</th>
            <th>客户名称</th>
<%--            <th>部门</th>--%>
            <th>师傅</th>
            <th>客服</th>
            <th>省</th>
            <th>市</th>
            <th>区</th>
            <th>详细地址</th>
            <th>网点编号</th>
            <th>网点名称</th>

            <th>投诉方</th>
            <th>投诉对象</th>
            <th>投诉项目</th>
            <th>投诉描述</th>
            <th>投诉日期</th>

            <th>投诉判责</th>
            <th>判责意见</th>

            <th>处理方案</th>
            <th>处理意见</th>
            <th>状态</th>
            <th>完成时间</th>
            <th>完成人</th>
            <th>责任对象</th>
            <th>赔偿厂商金额</th>
            <th>赔偿用户金额</th>
            <th>网点罚款金额</th>
            <th>客服罚款金额</th>

        </tr>
        </thead>
        <tbody>
        <c:set var="totalCustomerAmount" value="0" />
        <c:set var="totalUserAmount" value="0" />
        <c:set var="totalServicePointAmount" value="0" />
        <c:set var="totalKeFuAmount" value="0" />
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <tr>
                <c:set var="rowIndex" value="${rowIndex+1}"/>
                <td>${rowIndex}</td>
                <td>${item.complainNo}</td>
                <td>${item.orderNo}</td>
                <td>${item.customerName}</td>
<%--                <td>${item.department}</td>--%>
                <td>${item.engineerName}</td>
                <td>${item.keFuName}</td>
                <td>${item.provinceName}</td>
                <td>${item.cityName}</td>
                <td>${item.areaName}</td>
                <td class="autocut"><a href="javascript:" data-toggle="tooltip"
                                       data-tooltip="${item.userAddress}">${fns:abbr(item.userAddress,30)}</a>
                </td>
                <td>${item.servicePointNo}</td>
                <td>${item.servicePointName}</td>
                <td>${item.complainType.label}</td>
                <td>${item.complainObjectString}</td>
                <td>${item.complainItemString}</td>
                <td><a href="javascript:" data-toggle="tooltip"
                       data-tooltip="${item.complainRemark}">${fns:abbr(item.complainRemark,30)}</a>
                </td>

                <td><fmt:formatDate value="${item.complainDate}" pattern="yyyy-MM-dd"/></td>
                <td>${item.judgeItemString}</td>
                <td class="autocut"><a href="javascript:" data-toggle="tooltip"
                                       data-tooltip="${item.judgeRemark}">${fns:abbr(item.judgeRemark,30)}</a>
                <td>${item.completeResultString}</td>
                <td class="autocut"><a href="javascript:" data-toggle="tooltip"
                                       data-tooltip="${item.completeRemark}">${fns:abbr(item.completeRemark,30)}</a>
                </td>
                <td>${item.status.label}</td>
                <td><fmt:formatDate value="${item.completeDate}" pattern="yyyy-MM-dd"/></td>
                <td>${item.completeByName}</td>
                <td>${item.judgeObjectString}</td>
                <td>${item.customerAmount}</td>
                <td>${item.userAmount}</td>
                <td>${item.servicePointAmount}</td>
                <td>${item.keFuAmount}</td>
                <c:set var="totalCustomerAmount" value="${totalCustomerAmount+item.customerAmount}"/>
                <c:set var="totalUserAmount" value="${totalUserAmount+item.userAmount}"/>
                <c:set var="totalServicePointAmount" value="${totalServicePointAmount+item.servicePointAmount}"/>
                <c:set var="totalKeFuAmount" value="${totalKefuAmount+item.keFuAmount}"/>
            </tr>
        </c:forEach>
        <c:if test="${list.size()>0}">
            <tr>
                <td></td>
                <td colspan="25">
                </td>
                <td><B>合计</B></td>
                <td style="color:blue;"><B><fmt:formatNumber pattern="0.00" value="${totalCustomerAmount}"/></B></td>
                <td style="color:blue;"><B><fmt:formatNumber pattern="0.00" value="${totalUserAmount}"/></B></td>
                <td style="color:blue;"><B><fmt:formatNumber pattern="0.00" value="${totalServicePointAmount}"/></B></td>
                <td style="color:blue;"><B><fmt:formatNumber pattern="0.00" value="${totalKeFuAmount}"/></B></td>
            </tr>
        </c:if>
        </tbody>
    </table>

</div>
<div class="pagination">${page}</div>
</body>
</html>

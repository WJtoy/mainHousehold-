<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>工单投诉列表</title>
    <meta name="description" content="工单下投诉单列表">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            /*background: transparent;*/
        }
    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        Order.rootUrl = "${ctx}";
    </script>
</head>
<body style="margin-left:3px;margin-right:3px;">
    <form:form id="searchForm" action="${ctx}/sd/complain/orderlist" method="post" class="form-inline">
        <input type="hidden" id="orderId" name="orderId" value="${orderId}" maxlength="20" />
        <input type="hidden" id="orderNo" name="orderNo" value="${orderNo}" maxlength="30" />
    </form:form>
    <legend>${orderNo}</legend>
    <sys:message content="${message}"/>
    <div id="divGrid">
        <table id="contentTable" class="table table-bordered table-condensed " style="table-layout:fixed;">
            <thead>
            <tr>
                <th width="40">序号</th>
                <th width="100">单号</th>
                <th width="120">工单号</th>

                <th width="40">客服</th>
                <th width="40">投诉方</th>
                <th width="40">状态</th>
                <th width="120">厂商</th>

                <th width="80">投诉日期</th>
                <th width="100">投诉对象</th>
                <th width="150">投诉项目</th>
                <th width="*">投诉描述</th>
                <th width="50">判定人</th>
                <th width="80">判定日期</th>

                <th width="50">用户</th>
                <th width="45">电话</th>
                <th width="50">投诉人</th>

                <th width="120">责任对象</th>
                <th width="350">判定项目</th>
                <th width="400">判定意见</th>

                <th width="50">结案人</th>
                <th width="80">结案日期</th>
                <th width="150">处理方案</th>

                <th width="50">操作</th>
            </tr>
            </thead>
            <tbody>
            <c:set var="rowNumber" value="0"/>
            <c:forEach items="${page.list}" var="model">
                <c:set var="rowNumber" value="${rowNumber+1}"/>
                <tr>
                    <td>${rowNumber}</td>
                    <td>
                        <c:choose>
                            <c:when test="${model.status.value eq '0'}">
                                <a href="javascript:void(0);" onclick="Order.complain_form('${model.id}','${model.orderId}','${model.quarter}',this_index);">${model.complainNo}</a>
                            </c:when>
                            <c:otherwise>
                                <a href="javascript:void(0);"  onclick="Order.complain_view('${model.id}','${model.quarter}');">${model.complainNo}</a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>${model.orderNo}<br/>${model.orderStatus.label}</td>
                    <td>${model.kefu.name}</td>
                    <td>${model.complainType.label}</td>
                    <td>
                        <c:set var="statusclass" value="success" />
                        <c:choose>
                            <c:when test="${model.status.value eq '0'}">
                                <c:set var="statusclass" value="warning" />
                            </c:when>
                            <c:when test="${model.status.value eq '1'}">
                                <c:set var="statusclass" value="info" />
                            </c:when>
                            <c:when test="${model.status.value eq '2'}">
                                <c:set var="statusclass" value="success" />
                            </c:when>
                        </c:choose>
                        <span class="label label-${statusclass}">
                            ${model.status.label}
                        </span>
                    </td>
                    <td>${model.customer.name}</td>
                    <td><fmt:formatDate value="${model.complainDate}" pattern="yyyy-MM-dd"/></td>
                    <td>${model.complainObjectLabels}</td>
                    <td>${model.complainItemLabels}</td>
                    <td>
                        <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.complainRemark}">
                            ${fns:abbr(model.complainRemark,60)}
                        </a>
                    </td>
                    <td>${model.judgeBy.name}</td>
                    <td><fmt:formatDate value="${model.judgeDate}" pattern="yyyy-MM-dd"/></td>

                    <td>${model.userName}</td>
                    <td>${model.userPhone}</td>
                    <td>${model.complainBy}</td>
                    <td>${model.judgeObjectLabels}</td>
                    <td>${model.judgeItemLabels}</td>
                    <td>
                        <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.judgeRemark}">
                                ${fns:abbr(model.judgeRemark,60)}
                        </a>
                    </td>
                    <td>${model.completeBy.name}</td>
                    <td><fmt:formatDate value="${model.completeDate}" pattern="yyyy-MM-dd"/></td>
                    <td>${model.completeResultLabels}</td>
                    <td>
                        <c:choose>
                            <c:when test="${model.status.value eq '0'}">
                                <a href="javascript:void(0);" class="btn btn-mini btn-warning" onclick="Order.complain_form('${model.id}','${model.orderId}','${model.quarter}',this_index);">编辑</a>
                            </c:when>
                            <c:otherwise>
                                <a href="javascript:void(0);" class="btn btn-mini btn-warning" onclick="Order.complain_view('${model.id}','${model.quarter}');">查看</a>
                            </c:otherwise>
                        </c:choose>

                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
        });
    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            if($("#contentTable tbody>tr").length>0) {
                //无数据报错
                var screen = getOpenDialogWidthAndHeight();
                $("#divGrid").height(screen.height-100);
                $("#contentTable").toSuperTable({
                    width: screen.width-30,
                    height: screen.height - 120,
                    fixedCols: 3,
                    headerRows: 1,
                    colWidths:
                        [   40,100,130,
                            90, 70, 60, 120,
                            90, 100, 100, 150,90, 90,
                            80, 100, 80,
                            120, 150,180,
                            100, 90,150,
                            60
                        ],
                    onStart: function () {

                    },
                    onFinish: function () {

                    }
                });
            }
            else {
                var h = document.body.clientHeight;
                $("#divGrid").height(h-100);
            }
        });
    </script>
    <style type="text/css">
        .dropdown-menu {min-width: 80px;}
        .dropdown-menu > li > a {text-align: left;padding: 3px 10px;}
        .pagination {margin: 4px 0 0 4px;}
        .label-search {width: 70px;  text-align: right;}
        form {margin: 0 0 5px;}
    </style>
</body>
</html>

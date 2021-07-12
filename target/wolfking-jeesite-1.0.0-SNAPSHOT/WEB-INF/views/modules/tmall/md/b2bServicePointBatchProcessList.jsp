<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点资料管理</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%--<%@ include file="/WEB-INF/views/include/treetable.jsp" %>--%>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }

        .success-count {
            font-weight: bold;
            color: green;
        }

        .failure-count {
            font-weight: bold;
            color: red;
        }

    </style>

    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"text-align": "center", "vertical-align": "middle"});
            // $("#contentTable").treeTable({expandLevel: 1});
            var h = $(window).height();
            $("#divGrid").css("height", h - 138);

            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});

            h = $(window).height();
            if ($("#contentTable tbody>tr").length > 0) {
                //无数据报错

                var w = $(window).width();
                $("#contentTable").toSuperTable({
                    width: w - 30,
                    height: h - 158,
                    fixedCols: 3,
                    headerRows: 1,
                    colWidths:
                        [40, 100, 150, 150, 150,
                            100, 100, 100, 100, 100,
                            200, 200],
                    onStart: function () {
                    },
                    onFinish: function () {
                    }
                });
            }
            else {
                $("#divGrid").css("height", h - 138);
            }
        });

        function uploadBatch(cityId) {
            top.$.jBox.tip("正在执行批量上传操作...", "loading");
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/tmall/md/servicepoint/servicePointBatchUpload",
                data: {"cityId": cityId},
                success: function (data) {
                    setTimeout(function () {
                        if (data.success) {
                            top.$.jBox.tip(data.message, "批量上传操作成功");
                        }
                        else {
                            top.$.jBox.error(data.message, '批量上传操作失败');
                        }
                        top.$.jBox.closeTip();
                        $("#searchForm").submit();
                    }, 1500);
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    top.$.jBox.closeTip();
                    top.$.jBox.error(thrownError.toString(), '批量上传操作失败');
                }
            });
        }

        function uploadProvinceBatch(provinceId) {
            top.$.jBox.tip("正在执行批量上传操作...", "loading");
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/tmall/md/servicepoint/servicePointProvinceBatchUpload",
                data: {"provinceId": provinceId},
                success: function (data) {
                    setTimeout(function () {
                        if (data.success) {
                            top.$.jBox.tip(data.message, "批量上传操作成功");
                        }
                        else {
                            top.$.jBox.error(data.message, '批量上传操作失败');
                        }
                        top.$.jBox.closeTip();
                        $("#searchForm").submit();
                    }, 1500);
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    top.$.jBox.closeTip();
                    top.$.jBox.error(thrownError.toString(), '批量上传操作失败');
                }
            });
        }

        function deleteBatch(cityId) {
            top.$.jBox.tip("正在执行批量删除操作...", "loading");
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/tmall/md/servicepoint/servicePointBatchDelete",
                data: {"cityId": cityId},
                success: function (data) {
                    setTimeout(function () {
                        if (data.success) {
                            top.$.jBox.tip(data.message, "批量上传操作成功");
                        }
                        else {
                            top.$.jBox.error(data.message, '批量上传操作失败');
                        }
                        top.$.jBox.closeTip();
                        $("#searchForm").submit();
                    }, 1500);
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    top.$.jBox.closeTip();
                    top.$.jBox.error(thrownError.toString(), '批量上传操作失败');
                }
            });
        }

    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <%--<li><a href="${ctx}/tmall/md/servicepoint/serviceStoreList">网点资料管理</a></li>--%>
    <%--<li><a href="${ctx}/tmall/md/servicepoint/serviceStoreCoverServiceList">覆盖服务管理</a></li>--%>
    <%--<li><a href="${ctx}/tmall/md/servicepoint/serviceStoreCapacityList">网点容量管理</a></li>--%>
    <%--<li><a href="${ctx}/tmall/md/servicepoint/workerList">网点师傅管理</a></li>--%>
    <li class="active"><a href="javascript:void(0);">网点批处理</a></li>
</ul>
<form:form id="searchForm" modelAttribute="servicePointProvinceBatch"
           action="${ctx}/tmall/md/servicepoint/servicePointBatchProcessList" method="POST"
           class="breadcrumb form-search">
    <ul class="ul-form">
        <li>
            <label>省份：</label>
            <select id="provinceId" name="province.id" style="width:100px;">
                <option value="" selected="selected">所有</option>
                <c:forEach items="${fns:getProvinceList()}" var="province">
                    <option value="${province.id}" <c:out
                            value="${(servicePointProvinceBatch.province.id == province.id)?'selected=selected':''}"/>>${province.name}</option>
                </c:forEach>
            </select>
            <%--<form:input path="province.name" htmlEscape="false" maxlength="50" class="input-small"/>--%>

        </li>
        <li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/></li>
        <li class="clearfix"></li>
    </ul>
</form:form>
<sys:message content="${message}"/>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-bordered table-condensed"
           style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">
        <thead>
        <tr>
            <th width="40">序号</th>
            <th width="100">省份</th>
            <th width="150">城市</th>
            <th width="150">服务网点数量</th>
            <th width="150">安维师傅数量</th>
            <th width="100">上传时间</th>
            <th width="100">上传网点</th>
            <th width="100">上传覆盖服务</th>
            <th width="100">上传容量</th>
            <th width="100">上传工人</th>
            <th width="200">错误信息</th>
            <th width="200">网点资料管理</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:if test="${not empty list}">
            <c:forEach items="${list}" var="item">
                <c:set var="rowIndex" value="${rowIndex+1}"/>
                <tr id="${item.province.id}" pId="0" style="background-color:#EEE; color:#08C; font-weight:bold;">
                    <td>${rowIndex}</td>
                    <td>${item.province.name}</td>
                    <td></td>
                    <td>${item.totalServicePointCount}</td>
                    <td>${item.totalEngineerCount}</td>
                    <td></td>
                    <td>
                        <span class="success-count">${item.totalServicePointSuccessCount}</span>
                        &nbsp;&nbsp;&nbsp;
                        <span class="failure-count">${item.totalServicePointFailureCount}</span>
                    </td>
                    <td>
                        <span class="success-count">${item.totalCoverServiceSuccessCount}</span>
                        &nbsp;&nbsp;&nbsp;
                        <span class="failure-count">${item.totalCoverServiceFailureCount}</span>
                    </td>
                    <td>
                        <span class="success-count">${item.totalCapacitySuccessCount}</span>
                        &nbsp;&nbsp;&nbsp;
                        <span class="failure-count">${item.totalCapacityFailureCount}</span>
                    </td>
                    <td>
                        <span class="success-count">${item.totalWorkerSuccessCount}</span>
                        &nbsp;&nbsp;&nbsp;
                        <span class="failure-count">${item.totalWorkerFailureCount}</span>
                    </td>
                    <td></td>
                    <td>
                        <input id="btnUploadProvince" class="btn btn-primary" type="button"
                               onclick="uploadProvinceBatch(${item.province.id})" value="批量上传"/>
                    </td>
                </tr>
                <c:if test="${item.maxRow > 0}">
                    <tr id="${item.subItemlist.get(0).city.id}" pId="${item.province.id}">
                        <td rowspan="${item.maxRow}"></td>
                        <td rowspan="${item.maxRow}">${item.province.name}</td>
                        <td>${item.subItemlist.get(0).city.name}</td>
                        <td>${item.subItemlist.get(0).servicePointCount}</td>
                        <td>${item.subItemlist.get(0).engineerCount}</td>
                        <td><fmt:formatDate value="${item.subItemlist.get(0).batchLog.updateDate}"
                                            pattern="yyyy-MM-dd HH:mm:ss"/></td>
                        <td>
                            <span class="success-count">${item.subItemlist.get(0).batchLog.servicePointSuccessCount}</span>
                            &nbsp;&nbsp;&nbsp;
                            <span class="failure-count">${item.subItemlist.get(0).batchLog.servicePointFailureCount}</span>
                        </td>
                        <td>
                            <span class="success-count">${item.subItemlist.get(0).batchLog.coverServiceSuccessCount}</span>
                            &nbsp;&nbsp;&nbsp;
                            <span class="failure-count">${item.subItemlist.get(0).batchLog.coverServiceFailureCount}</span>
                        </td>
                        <td>
                            <span class="success-count">${item.subItemlist.get(0).batchLog.capacitySuccessCount}</span>
                            &nbsp;&nbsp;&nbsp;
                            <span class="failure-count">${item.subItemlist.get(0).batchLog.capacityFailureCount}</span>
                        </td>
                        <td>
                            <span class="success-count">${item.subItemlist.get(0).batchLog.workerSuccessCount}</span>
                            &nbsp;&nbsp;&nbsp;
                            <span class="failure-count">${item.subItemlist.get(0).batchLog.workerFailureCount}</span>
                        </td>
                        <td><a href="javascript:" data-toggle="tooltip"
                               data-tooltip='${item.subItemlist.get(0).lastUpdateErrorMsg}'>${fns:abbr(item.subItemlist.get(0).lastUpdateErrorMsg,40)}</a>
                        </td>
                        <td>
                            <input id="btnUpload" class="btn btn-primary" type="button"
                                   onclick="uploadBatch(${item.subItemlist.get(0).city.id})" value="批量上传"/>
                                <%--&nbsp;&nbsp;--%>
                                <%--<input id="btnDelete" class="btn btn-danger" type="button"--%>
                                <%--onclick="deleteBatch(${item.subItemlist.get(0).city.id})" value="批量删除"/>--%>
                        </td>
                    </tr>

                    <c:forEach begin="1" end="${item.maxRow-1}" var="i">
                        <tr id="${item.subItemlist.get(i).city.id}" pId="${item.province.id}">
                            <td>${item.subItemlist.get(i).city.name}</td>
                            <td>${item.subItemlist.get(i).servicePointCount}</td>
                            <td>${item.subItemlist.get(i).engineerCount}</td>
                            <td><fmt:formatDate value="${item.subItemlist.get(i).batchLog.updateDate}"
                                                pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td>
                                <span class="success-count">${item.subItemlist.get(i).batchLog.servicePointSuccessCount}</span>
                                &nbsp;&nbsp;&nbsp;
                                <span class="failure-count">${item.subItemlist.get(i).batchLog.servicePointFailureCount}</span>
                            </td>
                            <td>
                                <span class="success-count">${item.subItemlist.get(i).batchLog.coverServiceSuccessCount}</span>
                                &nbsp;&nbsp;&nbsp;
                                <span class="failure-count">${item.subItemlist.get(i).batchLog.coverServiceFailureCount}</span>
                            </td>
                            <td>
                                <span class="success-count">${item.subItemlist.get(i).batchLog.capacitySuccessCount}</span>
                                &nbsp;&nbsp;&nbsp;
                                <span class="failure-count">${item.subItemlist.get(i).batchLog.capacityFailureCount}</span>
                            </td>
                            <td>
                                <span class="success-count">${item.subItemlist.get(i).batchLog.workerSuccessCount}</span>
                                &nbsp;&nbsp;&nbsp;
                                <span class="failure-count">${item.subItemlist.get(i).batchLog.workerFailureCount}</span>
                            </td>
                            <td><a href="javascript:" data-toggle="tooltip"
                                   data-tooltip='${item.subItemlist.get(i).lastUpdateErrorMsg}'>${fns:abbr(item.subItemlist.get(i).lastUpdateErrorMsg,40)}</a>
                            </td>
                            <td>
                                <input id="btnUpload2" class="btn btn-primary" type="button"
                                       onclick="uploadBatch(${item.subItemlist.get(i).city.id})" value="批量上传"/>
                                    <%--&nbsp;&nbsp;--%>
                                    <%--<input id="btnDelete2" class="btn btn-danger" type="button"--%>
                                    <%--onclick="deleteBatch(${item.subItemlist.get(i).city.id})" value="批量删除"/>--%>
                            </td>
                        </tr>
                    </c:forEach>
                </c:if>
            </c:forEach>
        </c:if>
        </tbody>
    </table>
</div>
</body>
</html>

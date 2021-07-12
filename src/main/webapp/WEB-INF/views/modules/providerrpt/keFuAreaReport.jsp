<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<head>
    <title>客服区域报表</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
<%--    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>--%>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>

    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"vertical-align": "middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});

            $("#btnExport").click(function () {+
                top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
                    if (v == "ok") {
                        top.$.jBox.tip('请稍候...', 'loading');
                        $.ajax({
                            type: "POST",
                            url: "${ctx}/rpt/provider/keFuArea/export?" + (new Date()).getTime(),
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
                top.$('.jbox-body .jbox-icon').css('top', '55px');
            });

        });
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">客服区域报表</a></li>
</ul>

<form:form id="searchForm"  action="${ctx}/rpt/provider/keFuArea/keFuAreaReport" method="post" class="breadcrumb form-search">
    <div>
        <shiro:hasPermission name="rpt:keFuArea:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
    </div>
</form:form>


<sys:message content="${message}" />
<table id="contentTable" class="fancyTable datatable table table-bordered table-condensed table-hover" style="table-layout:fixed" cellspacing="0" width="100%">
    <thead>
    <th width="100">客服</th>
    <th width="180">qq</th>
    <th>区</th>
    </thead>
    <tbody>
    <c:forEach items="${list}" var="item">
        <tr>
        <td>${item.kefuName}</td>
        <td>${item.qq}</td>
        <td>${item.areaName}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>

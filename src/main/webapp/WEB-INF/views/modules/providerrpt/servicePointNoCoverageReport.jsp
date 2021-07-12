<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<head>
    <title>全国区域网点覆盖表</title>
    <meta name="decorator" content="default" />
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
                            url: "${ctx}/rpt/provider/servicePointCoverage/servicePointNoCoverageExport?" + (new Date()).getTime(),
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
    <li><a href="${ctx}/rpt/provider/servicePointCoverage/servicePointCoverageReport">网点覆盖区域</a></li>
    <li class="active"><a href="javascript:void(0);">网点未覆盖区域</a></li>
</ul>
<form:form id="searchForm"  action="${ctx}/rpt/provider/servicePointCoverage/servicePointNoCoverageExport" method="post" class="breadcrumb form-search">
    <div>
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>
</form:form>


<sys:message content="${message}" />
<table id="contentTable" class="fancyTable datatable table table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
    <thead>
    <th width="120">省</th>
    <th width="180">市</th>
    <th width="180">区（县）</th>
    <th>街道</th>
    </thead>
    <tbody>
    <c:forEach items="${list}" var="item">
        <tr>
        <td rowspan="${item.maxRow}">${item.provinceName}</td>
        <c:forEach  var="i" begin="0" end="${item.areaList.size()-1}">
            <c:if test="${i ne 0}">
                <tr>
            </c:if>
            <c:choose>
                <c:when test = "${i lt item.areaList.size()}">
                    <td rowspan="${item.areaList.get(i).countyMaxRow}">${item.areaList.get(i).cityName}</td>
                    <c:forEach var="j" begin="0" end="${item.areaList.get(i).countyMaxRow-1}">
                        <c:if test="${j ne 0}">
                            <tr>
                        </c:if>
                        <c:choose>
                            <c:when test="${j lt item.areaList.get(i).areaList.size()}">
                                <td>${item.areaList.get(i).areaList.get(j).countyName}</td>
                                <td>${item.areaList.get(i).areaList.get(j).countyListString}</td>
                            </c:when>
                            <c:otherwise>
                                <td></td>
                                <td></td>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${j eq 0}">
                            </tr>
                        </c:if>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <td></td>
                    <td></td>
                    <td></td>
                </c:otherwise>
            </c:choose>
            <c:if test="${i eq 0}">
                </tr>
            </c:if>
        </c:forEach>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>

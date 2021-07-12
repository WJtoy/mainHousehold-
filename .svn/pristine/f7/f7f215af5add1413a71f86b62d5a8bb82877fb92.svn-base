<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>重建中间表</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <style type="text/css">
        .form-horizontal {
            margin-top: 5px;
        }
    </style>
</head>
<body>
<form:form id="inputForm" modelAttribute="searchCondition" action="${ctx}/rpt/provider/rebuildMiddleTableTask/rebuild"
           method="post"
           class="form-horizontal">
    <form:hidden path="middleTableId"/>
    <form:hidden path="middleTableType"/>
    <form:hidden path="rebuildOperationType"/>
    <sys:message content="${message}"/>
    <c:choose>
        <c:when test="${searchCondition.middleTableType == 1}">
            <div class="control-group" style="margin-top:30px;">
                <label class="control-label">重建日期：</label>
                <input id="beginDate" name="beginDate" type="text" readonly="readonly"
                       style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
                       value="<fmt:formatDate value='${searchCondition.beginDate}' pattern='yyyy-MM-dd' />"
                       onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

                <label>~</label>
                &nbsp;&nbsp;&nbsp;

                <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20"
                       class="input-small Wdate"
                       value="<fmt:formatDate value='${searchCondition.endDate}' pattern='yyyy-MM-dd' />"
                       onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
            </div>
        </c:when>
        <c:when test="${searchCondition.middleTableType == 2}">
            <div class="control-group" style="margin-top:30px;">
                <label class="control-label">重建月份：</label>
                <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
                    <c:forEach items="${fns:getReportQueryYears()}" var="year">
                        <option value="${year}" <c:out
                                value="${(searchCondition.selectedYear eq year)?'selected=selected':''}"/>>${year}</option>
                    </c:forEach>
                </select>
                <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
                    <c:forEach var="i" begin="0" end="11" step="1">
                        <option value="${1+i}" <c:out
                                value="${(searchCondition.selectedMonth eq 1+i)?'selected=selected':''}"/>>${1+i}</option>
                    </c:forEach>
                </select>
            </div>
        </c:when>
    </c:choose>
    &nbsp;&nbsp;
    <div class="form-actions">
        <input id="btnSubmit" class="btn btn-primary" type="button" value="重  建"/>&nbsp;
        <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();"/>
    </div>
</form:form>
<script type="text/javascript">
    var pIndex = parent.layer.getFrameIndex(window.name);
    $('#btnSubmit').click(function () {
        layer.confirm('确认创建重建中间表数据任务吗?', function (index) {
            $('#btnSubmit').attr("disabled", "disabled");
            $.ajax({
                async: false,
                cache: false,
                type: "POST",
                url: "${ctx}/rpt/provider/rebuildMiddleTableTask/rebuild?" + (new Date()).getTime(),
                data: $(inputForm).serialize(),
                success: function (data) {
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data && data.success === true) {
                        $('#btnExport').removeAttr('disabled');
                        parent.layer.close(pIndex);
                        return false;
                    } else if (data && data.message) {
                        layerError(data.message, "创建任务失败")
                    } else {
                        layerError("创建任务失败", "创建任务失败")
                    }
                    $('#btnSubmit').removeAttr('disabled');
                    return false;
                },
                error: function (e) {
                    $('#btnSubmit').removeAttr('disabled');
                    ajaxLogout(e.responseText, null, "创建任务失败，创建任务失败!");
                }
            });
            layer.close(index);
        });
    });

    function closethisfancybox() {
        parent.layer.close(pIndex);
    }

</script>
</body>
</html>
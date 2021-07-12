<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>网点公告</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});

            $("#btnSubmit").click(function() {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/provider/systemNotice/servicePointNoticeList");
                $("#searchForm").submit();
            });
        });


        function details(id){
            top.layer.open({
                type: 2,
                id:'layer_unitCode',
                zIndex:19891015,
                title:'通知详情',
                content: "${ctx}/provider/systemNotice/findDetails?noticeId=" + id,
                area: ['830px', '380px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        function page(n,s){
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            $("#searchForm").submit();
            return false;
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">网点公告</a></li>
</ul>
<form:form id="searchForm" modelAttribute="systemNotice" action="${ctx}/provider/systemNotice/" method="post" class="breadcrumb form-search">
    <div>
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <label>标题：</label>
            <%-- <input id="title" class="input-small" name="title" value="${servicePointNotice.title}" maxlength="20">--%>
        <input type=text class="input-small" id="title" name="title" maxlength="50" value="${systemNotice.title}"/>
        &nbsp; &nbsp;
        <label>发布时间：</label>
        <input id="startDate" name="startDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${systemNotice.startDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        <label>~</label>
        &nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${systemNotice.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询" />
    </div>
</form:form>
<sys:message content="${message}"/>

<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-bordered table-condensed" style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="10">序号</th>
            <th width="150">标题</th>
            <th width="110">副标题</th>
            <th width="240">内容</th>
            <th width="70">发布时间</th>
            <th width="20">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <tr>
                <c:set var="rowIndex" value="${rowIndex+1}"/>
                <td>${rowIndex}</td>
                <td>${item.title}</td>
                <td>${item.subtitle}</td>
                <td>
                    <a href="javascript:" data-toggle="tooltip"
                       data-tooltip="${item.content}">${fns:abbr(item.content,70)}</a>
                </td>
                <td>
                   <%-- <fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm" />--%>
                           ${fns:formatDateString(item.createDt)}
                </td>
                <td>
                    <a href="javascript:void(0);" class="btn btn-mini btn-warning"
                       onclick="javascript:details(${item.id})">查看</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <div class="pagination">${page}</div>
</div>
</body>
</html>

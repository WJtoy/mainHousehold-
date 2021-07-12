<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>个人通知</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th,.table tbody td {text-align: center;vertical-align: middle;BackColor: Transparent;}
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        //覆盖分页前方法
        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            layerLoading("查询中...", true);
        }
        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                search();
            }
        });

        $(document).ready(function() {
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});
        });

        function viewNotice(id,title){
            top.layer.open({
                type: 2,
                id:'layer_viewNotice',
                zIndex:19891015,
                title:title || '通知详情',
                content: "${ctx}/im/notice/manage/view?id=" + id,
                area: ['750px','90%'],
                shade: 0.3,
                resize: true,
                maxmin: false,
                success: function(layero,index){
                    layer.iframeAuto(index);
                },
                end:function(){
                }
            });
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">我的通知</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="notice" action="${ctx}/im/notice/user/list" method="post" class="breadcrumb form-search">
    <div>
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <label>标题：</label>
        <input type=text class="input-small" id="title" name="title" maxlength="50" value="${notice.title}"/>
        &nbsp; &nbsp;
        <label>发布时间：</label>
        <input id="startAt" name="startAt" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="${notice.startAt}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>
        &nbsp;
        <input id="endAt" name="endAt" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="${notice.endAt}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询" />
    </div>
</form:form>
<sys:message content="${message}"/>
    <table id="contentTable" class="table table-bordered table-condensed" style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="30">序号</th>
            <th width="150">标题</th>
            <th width="110">副标题</th>
            <th width="240">内容</th>
            <th width="110">发布时间</th>
            <th width="50">查看</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <tr>
                <c:set var="rowIndex" value="${rowIndex+1}"/>
                <td>${rowIndex}</td>
                <td>${item.title}</td>
                <td>${item.subTitle}</td>
                <td>
                    <a href="javascript:" >${fns:abbr(item.content,150)}</a>
                </td>
                <td>
                    ${fns:formatDateString(item.createAt)}
                </td>
                <td>
                    <a href="javascript:void(0);" class="btn btn-mini btn-primary"
                       onclick="javascript:viewNotice('${item.id}','${item.title}');">查看</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <div class="pagination">${page}</div>
</body>
</html>

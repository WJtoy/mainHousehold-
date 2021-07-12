<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>通知管理</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th,.table tbody td {text-align: center;vertical-align: middle;BackColor: Transparent;}
        form {margin: 0 0 5px;}
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
                    // var height = layero.height() + "px";
                    // console.log(height);
                    // var body = layer.getChildFrame('body', index); //得到子页面层的BODY
                    // body.find('#viewNoticeForm').css("min-height",height); //将本层的窗口索引传给子页面层的hidValue中
                },
                end:function(){
                }
            });
        }
        // 重送
        <shiro:hasPermission name="im:notice:resend">
        function resendNotice(id){
            if(!id){
                layerError("参数不合法！");
                return;
            }
            var confirmClickTag = 0;
            top.layer.confirm('通知已发出，确定再次发出该通知吗?',
                {
                    icon: 3,
                    title:'重送通知',
                    cancel: function(index, layero){}
                },
                function(index,layero){
                    if(confirmClickTag == 1){
                        return false;
                    }
                    var btn0 = $(".layui-layer-btn0",layero);
                    if(btn0.hasClass("layui-btn-disabled")){
                        return false;
                    }
                    confirmClickTag = 1;
                    btn0.addClass("layui-btn-disabled").attr("disabled","disabled");
                    top.layer.close(index);//关闭本身
                    // do something
                    var loadingIndex;
                    var ajaxSuccess = 0;
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/im/notice/manage/resend",
                        data:{"id":id},
                        beforeSend: function () {
                            loadingIndex = layer.msg('正在发送，请稍等...', {
                                icon: 16,
                                time: 0,//不定时关闭
                                shade: 0.3
                            });
                        },
                        complete: function () {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                        },
                        success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            if(data && data.success == true){
                                layerInfo("通知已重送！");
                                ajaxSuccess = 1;
                            }
                            else if( data && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else{
                                layerError("重送通知错误","错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"重送通知错误，请重试!");
                        }
                    });
                },function(index){//cancel
                });
            return false;
        }
        </shiro:hasPermission>

        // 撤销
        <shiro:hasPermission name="im:notice:cancel">
        function cancelNotice(id){
            if(!id){
                layerError("参数不合法！");
                return;
            }
            var confirmClickTag = 0;
            top.layer.confirm('通知已发出，确定撤销此通知吗?',
                {
                    icon: 3,
                    title:'系统确认',
                    cancel: function(index, layero){}
                },
                function(index,layero){
                    if(confirmClickTag == 1){
                        return false;
                    }
                    var btn0 = $(".layui-layer-btn0",layero);
                    if(btn0.hasClass("layui-btn-disabled")){
                        return false;
                    }
                    confirmClickTag = 1;
                    btn0.addClass("layui-btn-disabled").attr("disabled","disabled");
                    top.layer.close(index);//关闭本身
                    // do something
                    var loadingIndex;
                    var ajaxSuccess = 0;
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/im/notice/manage/cancel",
                        data:{"id":id},
                        beforeSend: function () {
                            loadingIndex = layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,//不定时关闭
                                shade: 0.3
                            });
                        },
                        complete: function () {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                        },
                        success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            if(data && data.success == true){
                                //回调父窗口方法
                                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(iframe != undefined){
                                    iframe.repage();
                                }
                                ajaxSuccess = 1;
                            }
                            else if( data && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else{
                                layerError("撤销通知错误","错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"撤销通知错误，请重试!");
                        }
                    });
                },function(index){//cancel
                });
            return false;
        }
        </shiro:hasPermission>
    </script>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">通知管理</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="notice" action="${ctx}/im/notice/manage/list" method="post" class="breadcrumb form-search">
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
        <label>通知对象:</label>
            <c:forEach items="${userTypeList}" var="userType" varStatus="i">
                <spring:eval var="containsItem" expression="notice.userTypeValues.contains(userType.value)" />
                <span><input id="userTypeValues${i.index}" name="userTypeValues" type="checkbox" value="${userType.value}"
                             <c:if test="${containsItem}">checked="checked"</c:if>
                ><label for="userTypeValues${i.index}">${userType.name}</label></span>
            </c:forEach>
        &nbsp;&nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-info" type="button" value="查询" />
        &nbsp;&nbsp;&nbsp;
        <shiro:hasPermission name="im:notice:new">
            <a id="btnNew" name="btnNew" class="btn btn-primary" href="${ctx}/im/notice/manage/new" title="发布新的通知">新通知</a>
        </shiro:hasPermission>
    </div>
</form:form>
<sys:message content="${message}"/>
    <table id="contentTable" class="table table-bordered table-condensed" style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="30">序号</th>
            <th width="100">通知对象</th>
            <th width="150">标题</th>
            <th width="110">副标题</th>
            <th width="240">内容</th>
            <th width="80">发布人</th>
            <th width="110">发布时间</th>
            <th width="50">撤销</th>
            <th width="110">撤销时间</th>
            <th width="100">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <tr>
                <c:set var="rowIndex" value="${rowIndex+1}"/>
                <td>${rowIndex}</td>
                <td>${item.userTypeLabels}</td>
                <td><a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="点击查看通知具体内容"
                       onclick="javascript:viewNotice('${item.id}','${item.title}');">${item.title}</a></td>
                <td>${item.subTitle}</td>
                <td>
                    <a href="javascript:" >${fns:abbr(item.content,150)}</a>
                </td>
                <td>${item.createBy}</td>
                <td>
                    ${fns:formatDateString(item.createAt)}
                </td>
                <td>
                    <c:choose>
                        <c:when test="${item.isCanceled == 1}"><span class="label status_100">已撤销</span></c:when>
                        <c:otherwise><span class="label status_10">未撤销</span></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <c:if test="${item.isCanceled == 1}">
                        ${fns:formatDateString(item.createAt)}
                    </c:if>
                </td>
                <td>
                    <c:if test="${item.isCanceled == 0}">
                        <shiro:hasPermission name="im:notice:resend">
                            &nbsp;
                            <a href="javascript:void(0);" class="btn btn-mini btn-primary"
                               onclick="javascript:resendNotice('${item.id}');">重送</a>
                        </shiro:hasPermission>
                        <shiro:hasPermission name="im:notice:cancel">
                            &nbsp;
                        <a href="javascript:void(0);" class="btn btn-mini btn-warning"
                           onclick="javascript:cancelNotice('${item.id}');">撤销</a>
                        </shiro:hasPermission>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <div class="pagination">${page}</div>
</body>
</html>

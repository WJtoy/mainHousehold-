<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>用户网点管理</title>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <style>
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
    </style>

    <script type="text/javascript">
        function editUserServicePoint(id) {
            var h = $(top.window).height();
            var text = "添加网点帐号";
            var url = "${ctx}/sys/userServicePoint/form";
            var height = (h-200);
            var area = ['1000px', (h-200)+'px'];
            if (id != null){
                text = "修改";
                url = "${ctx}/sys/userServicePoint/form?userId=" + id;
            }
            top.layer.open({
                type: 2,
                id:"userServicePoint",
                zIndex:150,
                title:text,
                content: url,
                area: area,
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        function removeUser(userId,userName){

            layer.confirm(
                '确认要删除网点帐号' +'<label style="color:#63B9E6">'+ userName +'</label>吗？',
                {
                    btn: ['确定','取消'], //按钮
                    title:'提示'
                }, function(index){
                    layer.close(index);//关闭本身
                    var loadingIndex = top.layer.msg('正在删除，请稍等...', {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });
                    $.ajax({
                        url: "${ctx}/sys/userKeFu/delete?id=" + userId,
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                setTimeout(function () {
                                    layer.close(loadingIndex);
                                }, 2000);
                            }
                            if (data.success) {
                                layerMsg("用户已删除");

                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            } else {
                                layerError("删除网点帐号失败:" + data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            ajaxLogout(data,null,"数据操作错误，请重试!");
                        },
                    });
                    return false;
                }, function(){
                    // 取消操作
                });
        }
        function enableUser(userId,loginName,obj){
            var title;
            var statusFlag = $("#statusFlag_"+userId+"").val();
            if(statusFlag == 0){
                title = "停用";
                statusFlag = 1;
            }else {
                statusFlag = 0;
                title = "启用";
            }

            layer.confirm(
                '确认要'+ title+'<label style="color:#63B9E6">'+loginName +'</label>登录帐号吗？',
                {
                    btn: ['确定','取消'], //按钮
                    title:'提示',
                    cancel: function(index, layero){
                        // 右上角叉
                        if ($(obj).attr("class") == 'switch-off') {
                            honeySwitch.showOn(obj);
                        } else {
                            honeySwitch.showOff(obj);
                        }
                    }
                }, function(index){
                    layer.close(index);//关闭本身
                    var loadingIndex = top.layer.msg('正在'+ title +'，请稍等...', {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });
                    $.ajax({
                        url: "${ctx}/sys/user/userEnableDisable?userId="+ userId +"&statusFlag=" + statusFlag,
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                setTimeout(function () {
                                    layer.close(loadingIndex);
                                }, 2000);
                            }
                            if (data.success) {
                                layerMsg(data.message);

                                // 停用
                                if (statusFlag == 1) {
                                    $("#statusFlag_"+userId+"").val(1);
                                    $("#tr_"+userId+" .noStop").css("color","red");
                                    $("#tr_"+userId+" .noStop").attr("class", "stop");

                                } else {
                                    $("#statusFlag_"+userId+"").val(0);
                                    $("#tr_"+userId+" .stop").css("color","");
                                    $("#tr_"+userId+" .stop").attr("class", "noStop");
                                }

                            } else {
                                layerError(title + "失败:" + data.message, "错误提示");
                                // 取消操作
                                if ($(obj).attr("class") == 'switch-off') {
                                    honeySwitch.showOn(obj);
                                } else {
                                    honeySwitch.showOff(obj);
                                }
                            }
                            return false;
                        },
                        error: function (data) {
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            if ($(obj).attr("class") == 'switch-off') {
                                honeySwitch.showOn(obj);
                            } else {
                                honeySwitch.showOff(obj);
                            }
                        },
                    });
                    return false;
                }, function(){
                    // 取消操作
                    if ($(obj).attr("class") == 'switch-off') {
                        honeySwitch.showOn(obj);
                    } else {
                        honeySwitch.showOff(obj);
                    }
                });
        }
    </script>
</head>
<body>
    <ul class="nav nav-tabs">
        <li><a href="${ctx}/sys/user/list">用户</a></li>
        <li><a href="${ctx}/sys/user/salesList">业务</a></li>
        <li><a href="${ctx}/sys/userKeFu/list">客服</a></li>
        <li class="active"><a href="javascript:void(0);">网点</a></li>
    </ul>
    <form:form id="searchForm" modelAttribute="user" action="${ctx}/sys/userServicePoint/list" method="post"
               class="form-inline">
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <sys:tableSort id="orderBy" name="orderBy" value="${page.orderBy}" callback="page();"/>

        <div class="control-group" style="height: 45px;margin-top: 20px;border-bottom: 1px solid #EEEEEE;">

            <label class="margin_line" style="margin-left: 5px">用户名称：</label>
            <form:input path="name" htmlEscape="false" maxlength="50" class="input-medium"/>
            &nbsp;&nbsp;
            <label>登录帐号：</label>
            <form:input path="loginName" htmlEscape="false" maxlength="50" class="input-medium"/>
            &nbsp;&nbsp;
            <label class="margin_line">状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：</label>
            <select id="statusFlag" name="statusFlag" class="input-small" style="width:213px;">
                <option value="-1" <c:out value="${(empty user.statusFlag)?'selected=selected':''}" />>所有</option>
                <option value="0" <c:out value="${(user.statusFlag eq 0)?'selected=selected':''}" />>正常</option>
                <option value="1" <c:out value="${(user.statusFlag eq 1)?'selected=selected':''}" />>停用</option>
            </select>
            &nbsp;&nbsp;
            <input id="btnSubmit" class="btn btn-primary" type="button" onclick="search();" value="查询"/>
        </div>
    </form:form>
    <shiro:hasPermission name="sys:user:edit">
        <button style="margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 140px;height: 32px" onclick="editUserServicePoint()">
            <i class="icon-plus-sign"></i>&nbsp;添加网点帐号
        </button></shiro:hasPermission>
    <sys:message content="${message}"/>
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th>序号</th>
            <th class="sort-column login_name">用户名称</th>
            <th class="sort-column name">登录帐号</th>
            <th>联系电话</th>
            <th>用户角色</th>
            <th>授权品类</th>
            <shiro:hasPermission name="sys:user:edit">
                <th>启用</th>
                <th>操作</th>
            </shiro:hasPermission>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${page.list}" var="user">
            <c:set var="index" value="${index+1}" />
            <tr id="tr_${user.id}">
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${user.name}</td>
                <td>
                    <c:choose>
                        <c:when test="${user.statusFlag == 1}">
                            <label class="stop" style="color: red;">${user.loginName}</label>
                        </c:when>
                        <c:otherwise>
                            <label class="noStop">${user.loginName}</label></c:otherwise>
                    </c:choose>
                </td>
                <td>${user.mobile}</td>
                <td>${user.roleNames}</td>
                <td>${user.productCategoryNames}</td>
                <shiro:hasPermission name="sys:user:edit">
                    <td>
                        <c:choose>
                            <c:when test="${user.statusFlag == 0}">
                                <span class="switch-on"  style="zoom: 0.7"  onclick="enableUser('${user.id}','${user.name}',this)"></span>
                            </c:when>
                            <c:when test="${user.statusFlag == 1}">
                                <span class="switch-off"  style="zoom: 0.7"  onclick="enableUser('${user.id}','${user.name}',this)"></span>
                            </c:when>
                        </c:choose>
                        <input type="hidden" value="${user.statusFlag}" name="statusFlag" id="statusFlag_${user.id}">
                    </td>
                    <td>
                        <a href="javascript:editUserServicePoint('${user.id}')">修改</a>
                        &nbsp
                        <a href="javascript:removeUser('${user.id}','${user.name}')">删除</a>
                        &nbsp
                    </td>
                </shiro:hasPermission>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <div class="pagination">${page}</div>
</body>
</html>

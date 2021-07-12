<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>质保金设定</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <style>
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
    </style>
    <script type="text/javascript">

        //覆盖分页前方法
        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...", true);
        }

        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                this.form.submit();
            }
        });

        function editCustomerVipLevel(type, id) {
            var text = "添加质保等级";
            var url = "${ctx}/provider/md/depositLevel/form";
            if (type == 2) {
                text = "修改";
                url = "${ctx}/provider/md/depositLevel/form?id=" + id;
            }
            top.layer.open({
                type: 2,
                id: "customerVipLevel",
                zIndex: 19891015,
                title: text,
                content: url,
                area: ['735px', '450px'],
                shade: 0.3,
                maxmin: false,
                success: function (layero, index) {
                },
                end: function () {
                }
            });
        }

        function removeCustomerVipLevel(id){
            $.ajax({
                url: "${ctx}/provider/md/depositLevel/check?id="+id,
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data && data.success == true){
                        layer.confirm(
                            '确认要删除吗？',
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
                                    url: "${ctx}/provider/md/depositLevel/delete?id=" + id,
                                    success:function (data) {
                                        // 提交后的回调函数
                                        if(loadingIndex) {
                                            setTimeout(function () {
                                                layer.close(loadingIndex);
                                            }, 2000);
                                        }
                                        if (data.success) {
                                            layerMsg("删除成功");

                                            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                            if(pframe){
                                                pframe.repage();
                                            }
                                        } else {

                                            var mylayer = top.layer;
                                            mylayer.alert(data.message, {zIndex:29891014, title: "提示"});

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
                        }else {
                           var mylayer = top.layer;
                           mylayer.alert(data.message, {zIndex:29891014, title: "提示"});
                           return false;
                        }
                    },
                });
            return false;
        }
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">质保等级</a></li>
</ul>
<form:form id="searchForm" action="${ctx}/provider/md/depositLevel/list" method="post" class="" cssStyle="margin: 0 0 0px">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
</form:form>
<shiro:hasPermission name="md:depositLevel:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 114px;height: 30px" onclick="editCustomerVipLevel(1,null)">
        <i class="icon-plus-sign"></i>&nbsp;添加质保等级
    </button>
</shiro:hasPermission>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th>等级编码</th>
        <th>等级名称</th>
        <th>缴费金额(元)</th>
        <th>每单扣除(元)</th>
        <th width="50">排序</th>
        <th width="300">描述</th>
        <shiro:hasPermission name="md:depositLevel:edit">
            <th width="200px">操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="entity">
    <tr>
        <c:set var="index" value="${index+1}" />
        <td>${index+(page.pageNo-1)*page.pageSize}</td>
        <td>${entity.code}</td>
        <td>${entity.name}</td>
        <td><fmt:formatNumber value='${entity.minAmount}' pattern='#.##' />-<fmt:formatNumber value='${entity.maxAmount}' pattern='#.##' /></td>
        <td>${entity.deductPerOrder}</td>
        <td>${entity.sort}</td>
        <td>${entity.description}</td>
        <shiro:hasPermission name="md:depositLevel:edit">
            <td>
                <a href="javascript:editCustomerVipLevel(2,'${entity.id}')">修改</a>
                &nbsp
                <a href="javascript:removeCustomerVipLevel('${entity.id}')">删除</a>
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

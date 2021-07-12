<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>禁用词语</title>
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

        function editDisableWord() {
            var text = "添加禁用词语";
            var url = "${ctx}/provider/md/disableWord/form";
            top.layer.open({
                type: 2,
                id: "disableWord",
                zIndex: 19891015,
                title: text,
                content: url,
                area: ['800px', '550px'],
                shade: 0.3,
                maxmin: false,
                success: function (layero, index) {
                },
                end: function () {
                }
            });
        }



        function removeDisableWord(id){

            layer.confirm(
                '确定要删除此禁用词语吗？',
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
                        url: "${ctx}/provider/md/disableWord/delete?id=" + id,
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
                                layerError("删除"+typeName+"失败:" + data.message, "错误提示");
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
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">禁用词语</a></li>
</ul>
<form:form id="searchForm" modelAttribute="mdDisableWord" action="${ctx}/provider/md/disableWord/list" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>


        <label style="margin-left: 0px">禁用词语：</label>
        <form:input path="word" htmlEscape="false" maxlength="30" class="input-small" cssStyle="width: 186px"/>


        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />

</form:form>
<shiro:hasPermission name="md:disableWord:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 124px;height: 30px" onclick="editDisableWord()">
        <i class="icon-plus-sign"></i>&nbsp;添加禁用词语
    </button>
</shiro:hasPermission>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th>禁用词语</th>
        <shiro:hasPermission name="md:disableWord:edit">
            <th width="200px">操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="entity">
    <tr>
        <c:set var="index" value="${index+1}" />
        <td>${index}</td>
        <td>${entity.word}</td>
        <shiro:hasPermission name="md:disableWord:edit">
            <td>
                <a href="javascript:removeDisableWord('${entity.id}')">删除</a>
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

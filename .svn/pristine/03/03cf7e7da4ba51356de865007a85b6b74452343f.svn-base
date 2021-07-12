<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
    <title>缓存管理</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#treeTable").treeTable({expandLevel : 5});
        });
        function page(n,s){
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            $("#searchForm").submit();
            return false;
        }
        var clickTag = 0;

        function deleteCache(action,code,type){
            if(clickTag == 1){
                return false;
            }
            clickTag = 1;
            var title = "重载";
            if('Delete' === action){
                title = "清除";
            }
            var confirmClickTag = 0;
            top.layer.confirm('确定' + title + '此缓存项吗？'
                ,{
                    icon: 3
                    ,title:'系统确认'
                    ,cancel: function(index, layero){
                        clickTag = 0;
                    }
                    ,success: function(layro, index) {
                        $(document).on('keydown', layro, function(e) {
                            if (e.keyCode == 13) {
                                layro.find('a.layui-layer-btn0').trigger('click')
                            }else if(e.keyCode == 27){
                                clickTag = 0;
                                top.layer.close(index);//关闭本身
                            }
                        })
                    }
                }, function(index,layero) {
                    if(confirmClickTag == 1){
                        return false;
                    }
                    var btn0 = $(".layui-layer-btn0",layero);
                    if(btn0.hasClass("layui-btn-disabled")){
                        return false;
                    }

                    btn0.addClass("layui-btn-disabled").attr("disabled","disabled");
                    top.layer.close(index);//关闭本身
                    // do something
                    var loadingIndex;
                    var ajaxSuccess = 0;
                    var val;
                    if(type && type === "input"){
                        val = $("#txt_"+code).val();
                    }
                    var ajaxRequest = $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/sys/cache/ajax" + action + "?code=" + code +"&id=" + val + "&t=" + (new Date()).getTime(),
                        data:null,
                        // 设置超时的时间10s
                        timeout: 10000,
                        beforeSend: function () {
                            loadingIndex = layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,//不定时关闭
                                shade: 0.3
                            });
                        },
                        success: function (data) {
                            if(ajaxLogout(data)){
                                return false;
                            }
                            if(data && data.success == true) {
                                layerMsg('操作成功');
                            }
                            else if( data && data.message){
                                layerError(data.message,"错误提示");
                            }
                            else {
                                layerError("操作错误", "错误提示");
                            }
                            return false;
                        },
                        complete: function (XMLHttpRequest, status) {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            //console.log("complete:" + new Date().getTime());
                            if(ajaxSuccess == 0) {
                                setTimeout(function () {
                                    clickTag = 0;
                                }, 2000);
                            }
                            if (status == 'timeout') { //超时,status还有success,error等值的情况
                                ajaxRequest.abort();
                                layerError("请求超时,请重试", "错误提示");
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText,null,"操作错误，请重试!");
                        }
                    });
                    return false;
                },function(index) {
                    clickTag = 0;
                });

        }

    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">缓存管理</a></li>
</ul>
<sys:message content="${message}"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead><tr><th>名称</th>
        <th>操作</th>
    <tbody>
    <c:forEach items="${list}" var="master">
        <tr id="${master.id}" pId="0">
            <td>${master.name}</td>
            <td></td>
        </tr>
        <c:forEach items="${master.itemList}" var="item">
            <tr id="${item.id}" pId="${master.id}">
                <td>${item.name}</td>
                <td>
                    <c:if test="${item.reload == 1}">
                        <%--<a href="${ctx}/sys/cache/load?code=${item.code}" onclick="return layerConfirmx('确认要重新加载 [${item.name}] 缓存吗？', this.href)">重新加载</a>--%>
                        <a href="javascript:;" onclick="deleteCache('Reload','${item.code}','${item.type}');">重新加载</a>&nbsp;&nbsp;
                        <c:if test="${item.type == 'input'}">
                            <input type="text" id="txt_${item.code}" name="txt_${item.code}" />
                        </c:if>
                    </c:if>
                    <c:if test="${item.delete == 1}">
                        <a href="javascript:;" onclick="deleteCache('Delete','${item.code}','${item.type}');">清除</a>&nbsp;&nbsp;
                        <c:if test="${item.type == 'input'}">
                            <input type="text" id="txt_${item.code}" name="txt_${item.code}" />
                        </c:if>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
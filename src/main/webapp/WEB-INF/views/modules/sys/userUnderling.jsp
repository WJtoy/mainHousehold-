<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/treeview.jsp" %>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <title>用户下属</title>
    <script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
        var clickTag = 0;
        $(document).ready(function() {

            $("#inputForm").validate({
                submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    var userId = $("#userId").val();
                    $btnSubmit.prop("disabled", true);

                    var entity = {};
                    var subUserIds = [];
                    entity["userId"] = userId;
                    $("#selectedTree").children("div").each(function (i, element) {
                        var subUserId = $(this).attr("id");
                        subUserIds.push(subUserId);
                        entity["subUserIds[" + i + "]"] = subUserId;
                    });

                    $.ajax({
                        url:"${ctx}/sys/userSales/saveUserUnderling",
                        type:"POST",
                        data:entity,
                        dataType:"json",
                        success: function(data){
                            //提交后的回调函数
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                if(userId != ''){
                                    layerMsg("保存成功");
                                }
                                if (parentIndex && parentIndex != undefined && parentIndex != '') {
                                    var layero = $("#layui-layer" + parentIndex, top.document);
                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                    iframeWin.refreshUserUnderling(subUserIds.join(","),data.data);
                                }
                                top.layer.close(this_index);//关闭本身
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data)
                        {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                    });
                },
                errorContainer: "#messageBox",
                errorPlacement: function(error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

            $("[name=userSales]").change(function () {
                var id = $(this).val();
                var name = $(this).data("name");
                var subName = $(this).data("subname");
                var userSales_sel = [];
                var span = '';
                if(subName != ''){
                    span = '<span style="margin-left: 10px;color: #1D89FF;background-color: #E9F3FF;font-size: 12px;padding:4px 4px;border-radius:2px">主管：' + subName +'</span>';
                }
                if($(this).is(':checked')){

                    userSales_sel.push('<div class="selected" id="' + id +'"><label style="margin-top: 12px;width: 215px">' + name + span +'</label><a href="javascript:void(0);" id="delete" style="" onclick="remove(' + id +')"><img  src="${ctxStatic}/images/sys_remove.png" style="width: 20px;height: 20px"></a></div>');
                    $("#selectedTree").append(userSales_sel);
                }else {
                    $("div[id=" + id +"]").remove();
                }

                var  sum = $("#selectedTree").children("div").length;
                $('#selectedCount').text(sum);
            });


            $("#key").on('input', function () {
                var text = $(this).val();
                $("#userTree").children("div").each(function (i, element) {
                    var id = $(this).attr("id");
                    if(text != ''){
                        if(id.search(text) != -1){
                            $(this).show();
                        }else {
                            $(this).hide();
                        }
                    }else {
                        $(this).show();
                    }

                });
            });
        });


        function remove(id) {
            $("div[id=" + id +"]").remove();
            $("input[type=checkbox][value=" + id +"]").attr("checked", false); //取消勾选
            var  sum = $("#selectedTree").children("div").length;
            $('#selectedCount').text(sum);
        }

    </script>

    <style type="text/css">
        .selected{
            margin-left: 20px;
            height: 33px;
        }
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0;
            width: 100%;
            height: 60px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #e5e5e5;
        }
    </style>
</head>
<body>
    <sys:message content="${message}"/>
    <form:form id="inputForm" action="${ctx}/sys/userSales/userUnderling" method="post" class="form-horizontal" cssStyle="margin-left: 0px;width: 98%">
        <input type="hidden" id="userId" name="userId" value="${userId}">
        <div class="row-fluid" style="margin-top: 15px">

            <div class="control-group">
                <label class="control-label" style="width: 109px;">业务主管：</label>
                    <input id="name" name="name" type="text" disabled="disabled" class="required" value="${userName}" style="width:200px;margin-left:4px;height: 30px"/>
            </div>
        </div>

        <div class="row-fluid" style="margin-top: 10px;margin-left: 39px;width: 91.5%">
            <div class="span6" style="border: solid 1px #DCDEE2;height: 475px;overflow-x:auto;">
                <input id="key" placeholder="用户名称" type="text" style="margin-left: 15px;margin-top: 10px;width: 235px;height: 30px">
                <div id="userTree"  style="margin-left: 15px">
                    <c:forEach items="${userSalesList}"  var="userSales">
                        <div id="${userSales.name}">
                            <label for="${userSales.id}" style="margin-top: 12px;width: 210px">${userSales.name}<span <c:if test="${userSales.office.id != null}">style="margin-left: 15px;color: #1D89FF;background-color: #E9F3FF;font-size: 12px;padding:4px 4px;border-radius:2px"</c:if>><c:if test="${userSales.office.id != null}">主管：</c:if>${userSales.office.name}</span></label><input id="${userSales.id}" type="checkbox" name="userSales" style="zoom: 1.4;margin-left: 7px" value="${userSales.id}" data-name="${userSales.name}" data-subname="${userSales.office.name}"/>
                        </div>
                    </c:forEach>
                </div>
            </div>
           <input type="hidden" id="userSubList" value="${userSubList}">
            <div class="span6" style="border: solid 1px #DCDEE2;border-left: none;height: 475px;margin-left: 0px;overflow-x:auto;">
                <div style="margin-left: 15px;margin-top: 15px;width: 245px"><span>下属用户</span><span style="float:right;font-size: 12px;padding-right: 8px">已选择<span style="color: red" id="selectedCount">${userSubList.size()}</span>个</span></div>
                <div id="selectedTree" style="margin-top: 5px">
                    <c:forEach items="${userSubList}" var="userSub">
                        <div class="selected" id="${userSub.subUserId}">
                            <label style="margin-top: 12px;width: 215px">${userSub.subUserName}</label><a href="javascript:void(0);" id="delete" style="" onclick="remove(${userSub.subUserId})"><img src="${ctxStatic}/images/sys_remove.png" style="width: 20px;height: 20px"></a>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
        <div id="editBtn" class="line-row" style="width: 100%;">

            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保存"
                   style="width: 96px;height: 40px;margin-top: 10px;margin-left: 410px"/>
            <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()"
                   style="width: 96px;height: 40px;margin-top: 10px;margin-left: 13px;"/>
        </div>

    </form:form>

</body>

<script type="text/javascript">
    $(document).ready(function () {
        <c:forEach items="${userSubList}" var="userSub">
        $("[value=" + ${userSub.subUserId}+"]").attr("checked", true);
        </c:forEach>
    });

</script>
</html>

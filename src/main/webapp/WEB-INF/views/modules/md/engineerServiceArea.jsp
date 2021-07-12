<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>师傅服务区域</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/layui/layui.js"></script>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/jquery-upload-file/js/ajaxfileupload.js"></script>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .type2{
            width: 60px;
            text-align: center;
        }
        .type3{
            width: 60px;
            text-align: center;
        }
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0px;
            width: 100%;
            height: 55px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #ccc;
            border-top: 1px solid #e5e5e5;
            text-align: right;
        }

    </style>

    <script type="text/javascript">
        var this_index = top.layer.index;

        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';

        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
        $(document).ready(function() {
            var clickTag = 0;
            $("#inputForm").validate({

                submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");

                    if(clickTag == 1){
                        return false;
                    }
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    areas = $('input[name="county"]:checked').map(function() {
                        return $(this).val();
                    });

                    $("#areas").val(areas.get().join(","));
                    clickTag = 1;
                    $btnSubmit.prop("disabled", true);
                    var engineerId = $("#engineerId").val();
                    $.ajax({
                        url:"${ctx}/md/engineerServiceArea/saveEngineerServiceArea?engineerId=" + engineerId,
                        type:"POST",
                        data:$(form).serialize(),
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
                                layerMsg("保存成功");
                                top.layer.close(this_index);//关闭本身

                                if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                    var layero = $("#layui-layer" + parentIndex,top.document);
                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                    iframeWin.refreshArea(data.data,$("#areas").val());
                                }
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                top.layer.close(loadingIndex);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            top.layer.close(loadingIndex);
                            ajaxLogout(data,null,"数据保存错误，请重试!");

                            top.layer.close(loadingIndex);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                    });
                },
                errorContainer : "#messageBox",
                errorPlacement : function(error, element)
                {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")
                        || element.is(":radio")
                        || element.parent().is(
                            ".input-append"))
                    {
                        error.appendTo(element.parent()
                            .parent());
                    } else
                    {
                        error.insertAfter(element);
                    }
                }});


        });

        // 装载区域
        function loadArea(){
            var sid = $("#servicePointId").val();
            var eid = $("#engineerId").val();

            var loadingIndex = top.layer.msg('正在装载区域，请稍等...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });
            var ids;
            //ajax获取数据源后存入content数据中。
            $.ajax({
                cache : false,
                type : "GET",
                url : "${ctx}/md/engineerServiceArea/loadEngineerAreas",
                data : {sid:sid,eid:eid},
                success : function(data)
                {
                    top.layer.close(loadingIndex);
                    if (data.success==false) {
                        layerError(data.message,"错误提示");
                        return false;
                    } else {
                        ids = data.data || [];
                        if (ids.length > 0) {
                            for(var i in ids){
                                var id = ids[i];
                                $(":checkbox[name='county'][value="+id+"]").attr("checked","checked");
                            }
                        }

                    }
                },
                error : function(xhr, ajaxOptions, thrownError)
                {
                    top.layer.close(loadingIndex);
                    layerError(thrownError.toString(),"错误提示");
                }
            });//end ajax
        }

    </script>
</head>
<body>
<form:form id="inputForm" action="${ctx}/md/engineerServiceArea/saveEngineerServiceArea" method="post" class="form-horizontal"
           cssStyle="margin-left: 0px;width: 100%">
    <sys:message content="${message}"/>
    <input type="hidden" id="servicePointId" value="${servicePointId}">
    <input type="hidden" id="engineerId" value="${engineerId}">
    <div style="text-align: center; width: 960px;margin-left: 15px">
        <table id="contentTable" class="layui-table table_line">
            <thead>
            <tr>
                <th  style="text-align: center">省</th>
                <th  style="text-align: center">市</th>
                <th  style="text-align: center">区/县</th>
            </tr>
            </thead>
            <tbody>
                <c:forEach items="${list}" var="province" varStatus="i">
                    <c:forEach items="${province.models}" var="city" varStatus="i">
                            <tr id="${city.entity.id}">
                                <td class="type2">${province.entity.name}</td>
                                <td class="type3">${city.entity.name}</td>
                                <td style="width: 75%;line-height: 25px;">
                                    <c:forEach items="${city.models}" var="area" varStatus="i">
                                    <div style='width: 98px;float: left;text-align: left;white-space: nowrap;overflow: hidden;'>
                                        <input type="checkbox" value="${area.entity.id}" name="county">
                                            ${area.entity.name}
                                    </div>
                                    </c:forEach>
                                </td>
                            </tr>
                    </c:forEach>
                </c:forEach>
            </tbody>
        </table>
    </div>

    <input type="hidden" id="areas" name="areas" />
    <div style="height: 45px"></div>
    <div id="editBtn">
        <input id="btnSubmit" class="btn btn-primary layui-btn" type="submit"  value="保 存"
               style="margin-left: 410px;margin-top: 10px;width: 85px;background: #0096DA;border-radius: 4px;"/>
        <input id="btnCancel" class="btn layui-btn layui-btn-primary" type="button" value="取 消"
               style="margin-right: 40px;margin-top:10px;width: 85px;border-radius: 4px;" onclick="cancel()"/>
    </div>
</form:form>

<script class="removedscript" type="text/javascript">


    function mergeCell(columnIndex) {
        tr = $("#contentTable tr").length;// 获取当前表格中tr的个数
        var mark = 0; //要合并的单元格数
        var index = 0; //起始行数
        /*
         *  要合并单元格，需要存储两个参数，
         * 1，开始合并的单元格的第一行的行数，
         * 2.要合并的单元格的个数
        **/
        //console.log(tr);
        //判断 若只有一行数据，则不做调整
        if(tr <= 2){
        } else {
            //var i=1  比较当前的tr和上一个tr的值
            for(var i=0;i < tr ;i++){
                var ford = $("#contentTable tr:gt(0):eq("+i+") td:eq("+columnIndex+")").text();
                //根据下标获取单元格的值
                // tr:gt(0)  从下标0 开始获取
                // tr:gt(0):eq( i ) :i 标识 当前行的下标 ，0 开始
                // td:eq(0) 当前行的第一个单元格，下标从0开始
                var behind = $("#contentTable tr:gt(0):eq("+(parseInt(i)+1)+") td:eq("+columnIndex+")").text();
                if(ford == behind){
                    $("#contentTable tr:gt(0):eq("+(parseInt(i)+1)+") td:eq("+columnIndex+")").hide();
                    mark = mark +1;
                } else if(ford != behind){
                    //如果值不匹配则遍历到不同种的分类,将旧分类隐藏
                    index = i-mark;
                    if (mark >0) {
                        $("#contentTable tr:gt(0):eq(" + index + ") td:eq(" + columnIndex + ")").attr("rowspan", mark + 1);//+1 操作标识，将当前的行加入到隐藏
                        //rowspan 列上横跨， colspan 行上横跨
                        //后面的参数，表示横跨的单元格个数，
                        //合并单元格就是将其他的单元格隐藏（hide）,或删除（remove）。
                        //将一个单元格的rowspan 或colsspan 加大
                        mark = 0;
                        $("#contentTable tr:gt(0):eq(" + (parseInt(i)) + ") td:eq(" + columnIndex + ")").hide();
                    }
                }
            }
        }
    }
    $(document).ready(function() {

        loadArea();
    });

    mergeCell(0);
    mergeCell(1);
</script>
</body>
</html>

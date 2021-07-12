<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/treeview.jsp" %>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <title>关联客户账号店铺</title>
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
                    entity["userId"] = userId;
                    $("#selectedTree").children("div").each(function (i, element) {
                        var shopId = $(this).attr("id");
                        var shopName = $(this).attr("name");
                        var dataSource = $(this).data("source");
                        entity["customerShops[" + i + "].id"] = shopId;
                        entity["customerShops[" + i + "].name"] = shopName;
                        entity["customerShops[" + i + "].dataSource"] = dataSource;
                    });

                    $.ajax({
                        url:"${ctx}/md/customerAccount/saveCustomerShop",
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

                                    iframeWin.refreshCustomerShop(data.data);
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

            $("[name=shop]").change(function () {
                var id = $(this).val();
                var name = $(this).data("name");
                var dataSource = $(this).data("source");
                var customerShop_sel = [];
                if($(this).is(':checked')){
                    customerShop_sel.push('<div class="selected" id="' + id +'" name="' + name +'" data-source="'+ dataSource +'"><label style="margin-top: 12px;width: 215px">' + name +'</label><a href="javascript:void(0);" id="delete" style="" onclick="remove(\''+ id +'\')"><img  src="${ctxStatic}/images/sys_remove.png" style="width: 20px;height: 20px"></a></div>');
                    $("#selectedTree").append(customerShop_sel);
                }else {
                    $("#" + id +"").remove();
                }

                var  sum = $("#selectedTree").children("div").length;
                $('#selectedCount').text(sum);
            });


            $("#key").on('input', function () {
                var text = $(this).val();
                $("#shopTree").children("div").each(function (i, element) {
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
            $("#" + id +"").remove();
            $("input[type=checkbox][value=" + id +"]").attr("checked", false); //取消勾选
            var  sum = $("#selectedTree").children("div").length;
            $('#selectedCount').text(sum);
        }

    </script>

    <style type="text/css">
        .selected{
            margin-left: 20px;
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
<form:form id="inputForm" action="${ctx}/md/customerAccount/customerShop" method="post" class="form-horizontal" cssStyle="margin-left: 0px;width: 98%">
    <input type="hidden" id="userId" name="userId" value="${userId}">
    <div class="row-fluid" style="margin-top: 15px">

        <div class="control-group">
            <label class="control-label" style="width: 109px;">姓&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp名：</label>
            <input id="name" name="name" type="text" disabled="disabled" class="required" value="${name}" style="width:200px;margin-left:4px;height: 30px"/>
        </div>
    </div>

    <div class="row-fluid" style="margin-top: 10px;margin-left: 39px;width: 91.5%">
        <div class="span6" style="border: solid 1px #DCDEE2;height: 475px;overflow-x:auto;">
            <input id="key" placeholder="店铺名称" type="text" style="margin-left: 15px;margin-top: 10px;width: 235px;height: 30px">
            <div id="shopTree"  style="margin-left: 15px">
                <c:forEach items="${shops}"  var="shop">
                    <div id="${shop.name}">
                        <label style="margin-top: 13.38px;width: 210px">${shop.name}</label><input type="checkbox" name="shop" style="zoom: 1.4;margin-left: 7px" value="${shop.id}" data-name="${shop.name}" data-source="${shop.dataSource}"/>
                    </div>
                </c:forEach>
            </div>
        </div>

        <div class="span6" style="border: solid 1px #DCDEE2;border-left: none;height: 475px;margin-left: 0px;overflow-x:auto;">
            <div style="margin-left: 15px;margin-top: 15px;width: 245px"><span>关联店铺</span><span style="float:right;font-size: 12px;padding-right: 8px">已选择<span style="color: red" id="selectedCount">${customerShopList.size()}</span>个</span></div>

            <div id="selectedTree" style="margin-top: 5px">
                <c:forEach items="${customerShopList}" var="customerShop">
                    <div class="selected" id="${customerShop.id}" name="${customerShop.name}" data-source="${customerShop.dataSource}">
                        <label style="margin-top: 12px;width: 215px">${customerShop.name}</label><a href="javascript:void(0);" id="delete" style="" onclick="remove('${customerShop.id}')"><img src="${ctxStatic}/images/sys_remove.png" style="width: 20px;height: 20px"></a>
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
        <c:forEach items="${customerShopList}" var="entity">
        $("[value=${entity.id}]").attr("checked", true);
        </c:forEach>
    });

</script>
</html>

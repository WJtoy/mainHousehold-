<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/treeview.jsp" %>
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <script src="${ctxStatic}/layui/layui.js"></script>
    <title>用户管理</title>
    <meta name="decorator" content="default"/>
    <style>
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 3px;
            width: 100%;
            height: 55px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #ccc;
            border-top: 1px solid #e5e5e5;
            text-align: right;
        }
        .line_{
            border-bottom: 3.5px solid #0096DA;
            width: 65px;
            border-radius: 10px;
        }
        .line_inline{
            float: left;
            height: 38px;
            width: 53%;
            display: inline-block;
            margin-left: -50px;
        }
        .line_div{
            margin-left: -58px;
        }
        .add_button{
            margin-top: -4px;
            margin-bottom: 15px;
            border-radius: 4px;
            border: 1px solid;
            border-color: #0096DA;
            background-color: #0096DA;
            width: 96px;
            height: 32px;
            color: #FFFFFF;
        }
        .layui-tab-brief > .layui-tab-title .layui-this {
            color: #0096DA;
        }
        .layui-tab-brief > .layui-tab-more li.layui-this::after, .layui-tab-brief > .layui-tab-title .layui-this::after {
            border-top-width: initial;
            border-right-width: initial;
            border-left-width: initial;
            border-top-color: initial;
            border-right-color: initial;
            border-left-color: initial;
            border-style: none none solid;
            border-image: initial;
            border-radius: 0px;
            border-bottom: 3px solid #0096DA;
        }
    </style>
    <script type="text/javascript">
        var orderdetail_index = parent.layer.getFrameIndex(window.name);
        var ctree;
        var regions = [];
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function () {
            var $btnSubmit = $("#btnSubmit");
            $("#loginName").focus();
            $("#inputForm").validate({
                rules: {
                    loginName: {remote: "${ctx}/sys/user/checkLoginName?oldLoginName=" + encodeURIComponent('${user.loginName}')}
                },
                messages: {
                    loginName: {remote: "用户登录名已存在"},
                    confirmNewPassword: {equalTo: "两次密码输入不一致"}
                },
                submitHandler: function (form) {
                    var areas = [];
                    var ids = [], nodes = tree.getCheckedNodes(true);
                    var node;
                    for(var i=0; i<nodes.length; i++) {
                        node = nodes[i];
                        if(node.level ==3) {
                            ids.push(node.id);
                            var area = {};
                            area.id = node.id;
                            area.type = node.level + 1;
                            areas.push(area);
                        }
                    }
                    $("#areaIds").val(ids);
                    $("#areas").val(JSON.stringify(areas));
                    parseRegions(tree);
                    if(regions && regions.length>0){
                        $("#regions").val(JSON.stringify(regions));
                    }
                    var productCategoryIds = [];
                    var productCategoryNodes = productCategoryTree.getCheckedNodes(true);
                    for (var j=0; j<productCategoryNodes.length; j++) {
                        productCategoryIds.push(productCategoryNodes[j].id);
                    }
                    $("#productCategoryIds").val(productCategoryIds);

                    var uType = $("#userType").val();
                    if (uType != '7' && uType != '2' && uType !='10' && uType != '11') {
                        $("input[name='subFlag']").prop("checked","");
                    }
                    var subFlag = $("input[name='subFlag']:checked").val();
                    // customerIds
                    if (uType =='2' && subFlag=='1') {
                        var vipCustomerIds = [];
                        var vipCustomerNodes = vipCustomerTree.getCheckedNodes(true);
                        for (var k = 0; k < vipCustomerNodes.length; k++) {
                            vipCustomerIds.push(vipCustomerNodes[k].id);
                        }
                        $("#customerIds").val(vipCustomerIds);
                    }else{
                        $("#customerIds").val("");
                    }

                    var roleIdList = $("input[name='roleIdList']:checked");
                    if(roleIdList.length <= 0){
                        layerError("请选择用户角色", "错误提示");
                        return false;
                    }

                    var visible = $('#divProductCategory').is(':visible');//true 为显示状态
                    if (visible) {
                        var productCategoryIds = $("input[name='productCategoryIds']:checked");
                        if(productCategoryIds.length <= 0){
                            layerError("请选择授权品类", "错误提示");
                            return false;
                        }
                    }

                    // loading('正在提交，请稍等...');
                    // form.submit();
                    var options = {
                        url: "${ctx}/sys/user/save",
                        type: 'post',
                        dataType: 'json',
                        data:$(form).serialize(),
                        beforeSubmit: function(formData, jqForm, options){
                            loadingIndex = layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,
                                shade: 0.3
                            });
                            return true;
                        },// 提交前的回调函数
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg(data.message);
                                setTimeout(function () {
                                    cancel();
                                    // loading('同步中...');
                                }, 1000);
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError("数据保存错误:" + data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                        },
                    };
                    $("#inputForm").ajaxSubmit(options);
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

            var setting = {check:{enable:true,nocheckInherit:true},view:{selectedMulti:false},
                data:{simpleData:{enable:true}},callback:{beforeClick:function(id, node){
                    tree.checkNode(node, !node.checked, true, true);
                    return false;
                }}};


            // 用户-区域
            var zNodes=[
                <c:forEach items="${areaList}" var="area">
                    {id:'${area.id}', pId:'${not empty area.parent.id?area.parent.id:0}',
                        name:"${area.id==1?'区域列表':area.name}"},
                </c:forEach>];

            // 初始化树结构
            var tree = $.fn.zTree.init($("#areaTree"), setting, zNodes);
            // 默认选择节点
            var ids = ${user.areaIds};
            for(var i=0; i<ids.length; i++) {
                var node = tree.getNodeByParam("id", ids[i]);
                try{
                    tree.checkNode(node, true, true,false);
                    //tree.expandNode(node,true,false,false);
                }catch(e){}
            }

            // 默认展开全部节点
            //tree.expandAll(true);
            /* 默认展开一级节点 */
            var nodes = tree.getNodesByParam("level", 0);
            for(var i=0; i<nodes.length; i++) {
                tree.expandNode(nodes[i], true, false, false);
            }

            zNodes = [
                <c:forEach items="${fns:getProductCategories()}" var="category">
                {id: '${category.id}', pId: '0', name: "${category.name}"},
                </c:forEach>
            ];
            // 初始化树结构
            var productCategoryTree = $.fn.zTree.init($("#productCategoryTree"), setting, zNodes);
            // 默认选择节点
            var pcIds = [];
            <c:forEach items="${user.productCategoryIds}" var="pcId">
                pcIds.push(${pcId});
            </c:forEach>
            pcIds.forEach(function (value) {
                var pNode = productCategoryTree.getNodeByParam("id", value);
                try {
                    productCategoryTree.checkNode(pNode, true, false);
                } catch (e) {
                }
            });

            // 初始化客户树
            var vipCustomerNodes = [
              <c:forEach items="${fns:getVipCustomerListFromMS()}" var="customer">
                {id: '${customer.id}', pId: '0', name:"${customer.name}"},
              </c:forEach>
            ];

            var vipCustomerTree = $.fn.zTree.init($("#vipCustomerTree"), setting, vipCustomerNodes);
            // 默认选择节点
            var customerIds = [];
            <c:forEach items="${user.customerIds}" var="customerId">
            customerIds.push(${customerId});
            </c:forEach>
            customerIds.forEach(function (value) {
                var pNode = vipCustomerTree.getNodeByParam("id", value);
                try {
                    vipCustomerTree.checkNode(pNode, true, false);
                } catch (e) {
                }
            });

            // 刷新（显示/隐藏）机构
            $("input[name='subFlag'][value='${user.subFlag}']").prop("checked",true);
            refreshAreaTree();
            top.$.jBox.closeTip();
            $("#userType").change(function(){
                refreshAreaTree();
            });

            $("input[name='subFlag']").on("change",function(){
                var uType = $("#userType").val();
                var subFlag = $(this).val();
                if (subFlag == "1" && uType == "2") {
                    $("#divCustomer").show();
                } else {
                    $("#divCustomer").hide();
                }
            })

            $("#add_button").click(function(){
                var userId = $("#id").val();
                var url;
                if (userId == '') {
                    url = "${ctx}/sys/user/addCustomer?parentIndex=" + (orderdetail_index || '');
                } else {
                    url = "${ctx}/sys/user/addCustomer?id=" + userId + "&parentIndex=" + (orderdetail_index || '');
                }
                var text = "添加客户";

                layer.open({
                    type: 2,
                    id:"user",
                    zIndex:19891015,
                    title:text,
                    content: url,
                    area: ['720px', '650px'],
                    shade: 0.3,
                    maxmin: false,
                    success: function(layero,index){
                    },
                    end:function(){
                    }
                });
            })
        });

        function child(obj) {
            var data = eval(obj);
            // 适应父容器高度
            $("#parentContainer").css("height", (data.height-100) + "px");
        }

        function parseRegions(tree){
            regions = [];//清空
            var region;
            var checkStatus;
            //根节点
            nodes = tree.getNodes();
            if(nodes.length==1){
                var root = nodes[0];
                checkStatus = root.getCheckStatus();
                if(checkStatus.checked == true){
                    if(checkStatus.half == false){
                        //all region(country)
                        region = {};
                        region.areaId = 0;
                        region.cityId = 0;
                        region.provinceId = 0;
                        region.areaType = 1;
                        regions.push(region);
                    }else{
                        parseSubRegions(root);
                    }
                }
            }
        }

        function parseSubRegions(node){
            if(!node || !node.isParent){
                return;
            }
            var nodes = node.children;
            var size = nodes.length;
            var subNode;
            var region;
            var parent;
            var checkStatus;
            for(var i = 0; i < size; i++){
                subNode = nodes[i];
                checkStatus = subNode.getCheckStatus();
                if(checkStatus.checked){
                    if(checkStatus.half == false) {
                        if (subNode.level == 1) {
                            //省
                            region = {};
                            region.areaId = 0;
                            region.cityId = 0;
                            region.provinceId = subNode.id;
                            region.areaType = 2;
                            regions.push(region);
                        } else if (subNode.level == 2) {
                            //市
                            region = {};
                            region.areaId = 0;
                            region.cityId = subNode.id;
                            parent = subNode.getParentNode();
                            region.provinceId = parent.id;
                            region.areaType = 3;
                            regions.push(region);
                        } else {
                            //区
                            region = {};
                            region.areaId = subNode.id;
                            parent = subNode.getParentNode();
                            region.cityId = parent.id;
                            parent = parent.getParentNode();
                            region.provinceId = parent.id;
                            region.areaType = 4;
                            regions.push(region);
                        }
                    }else{
                        parseSubRegions(subNode);
                    }
                }
            }
        }

        function refreshAreaTree(){
            $("input[name='subFlag']").removeClass("error");
            $("#subFlag-error").remove();
            var uType = $("#userType").val();
            if(uType == '2'){
                $("#divArea").show();
                $("#areaTree").show();
                $("#divProductCategory").show();
                //salesvalid('add');
                //20180608修改 限制电话和QQ必填  手机选填
                $("#phone").rules("add",'required');
                $("#span_phone").show();
                $("#qq").rules("add",'required');
                $("#span_qq").show();

                $("#divSubFlag").show();
                $("#subFlag1").show();
                $("#subFlag2").show();
                $("#subFlag3").show();
                $("#subFlag4").show();
                $("#subFlag5").show();
                $("#divSubFlag #span1").html("超级");
                $("#divSubFlag #span2").html("KA");
                $("#divSubFlag #span3").html("普通");
                $("#divSubFlag #span4").html("突击");
                $("#divSubFlag #span5").html("自动");
                var subFlag = $("input[name='subFlag']:checked").val();
                if (subFlag == "1") {
                    $("#divCustomer").show();
                } else {
                    $("#divCustomer").hide();
                }
            }else if(uType == '8'){
                $("#divArea").show();
                $("#areaTree").show();
                $("#divCustomer").hide();
                $("#divProductCategory").show();
                $("#divSubFlag").hide();
                salesvalid('remove');
            }else if(uType == '7'){
                $("#divArea").hide();
                $("#areaTree").hide();
                $("#divCustomer").hide();
                $("#divProductCategory").hide();
                salesvalid('add');
                $("#divSubFlag").show();
                $("#divSubFlag #span1").html("");
                $("#divSubFlag #span2").html("业务员");
                $("#divSubFlag #span3").html("跟单");
                $("#divSubFlag #span4").html("");
                $("#divSubFlag #span5").html("");
                $("#subFlag1").hide();
                $("#subFlag2").show();
                $("#subFlag3").show();
                $("#subFlag4").hide();
                $("#subFlag5").hide();
                $("#subFlag1").prop("checked","");
                $("#subFlag4").prop("checked","");
                $("#subFlag5").prop("checked","");
            } else if (uType=='10') {  //事业部帐号
                $("#divArea").hide();
                $("#areaTree").hide();
                $("#divCustomer").hide();
                $("#divProductCategory").show();
                // $("#customerTree").hide();
                $("#divSubFlag").show();
                $("#divSubFlag #span1").html("主管");
                $("#divSubFlag #span2").html("");
                $("#divSubFlag #span3").html("");
                $("#divSubFlag #span4").html("");
                $("#divSubFlag #span5").html("");
                $("#subFlag1").show();
                $("#subFlag2").hide();
                $("#subFlag3").hide();
                $("#subFlag4").hide();
                $("#subFlag5").hide();
                $("#subFlag2").prop("checked","");
                $("#subFlag3").prop("checked","");
                $("#subFlag4").prop("checked","");
                $("#subFlag5").prop("checked","");
                salesvalid('remove');
            } else if (uType=='11') {// 财务
                $("#divArea").hide();// 区域授权隐藏
                $("#areaTree").hide();
                $("#divCustomer").hide();// 客户列表隐藏
                $("#divProductCategory").hide();// 授权服务品类隐藏

                $("#divSubFlag").show();
                $("#divSubFlag #span1").html("财务");
                $("#divSubFlag #span2").html("审单员");
                $("#divSubFlag #span3").html("");
                $("#divSubFlag #span4").html("");
                $("#divSubFlag #span5").html("");
                $("#subFlag1").show();
                $("#subFlag2").show();
                $("#subFlag3").hide();
                $("#subFlag4").hide();
                $("#subFlag5").hide();
                $("#subFlag3").prop("checked","");
                $("#subFlag4").prop("checked","");
                $("#subFlag5").prop("checked","");
            } else{
                $("#divArea").hide();
                $("#areaTree").hide();
                $("#divCustomer").hide();
                $("#divProductCategory").hide();
                // $("#customerTree").hide();
                $("#divSubFlag").hide();
                salesvalid('remove');
            }
        }
        function salesvalid(action){
            //是否开启qq及手机号必填项检查
            if(action == 'add'){
                $("#mobile").rules("add",'required');
                $("#span_mobile").show();
                $("#qq").rules("add",'required');
                $("#span_qq").show();
            }else{
                $("#mobile").rules("remove");
                $("#span_mobile").hide();
                $("#qq").rules("remove");
                $("#span_qq").hide();
                $("#inputForm").validate().element($("#qq"));
                $("#inputForm").validate().element($("#mobile"));
            }
        }


        /**
         * 刷新选项卡
         * @param arr           选中客户
         * @param num           选中客户数
         * @param oldArr        以往授权客户
         * @param intersection  交集
         */
        function refresh(arr, num, oldArr, intersection){
            var salesCustomerIds = [];
            var ids;
            $("#salesCustomerIds").attr("value","");
            if (num > 0) {
                if (intersection.length <= 0) {
                    if (oldArr.length > 0) {
                        num += oldArr.length;
                    }
                }
                $("#result_count").show();
                $("#count").text(num);
            } else {
                $("#result_count").hide();
            }
            $("#tryWorkOrder").html("");
            $("#signed").html("");
            $("#notSigned").html("");
            // 如果没有交集
            if (intersection.length <= 0) {
                if (oldArr.length > 0) {
                    for (var i = 0; i < oldArr.length; i++) {
                        if (oldArr[i].contractFlag == 10) {
                            $("#tryWorkOrder").append("<div style='width: 45%;float: left;height: 35px'>"+oldArr[i].customerName+"</div>");
                        } else if (oldArr[i].contractFlag == 20) {
                            $("#signed").append("<div style='width: 45%;float: left;height: 35px'>"+oldArr[i].customerName+"</div>");
                        } else {
                            $("#notSigned").append("<div style='width: 45%;float: left;height: 35px'>"+oldArr[i].customerName+"</div>");
                        }
                    }
                }
            }
            if (arr.length > 0) {
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i].contractFlag == 10) {
                        $("#tryWorkOrder").append("<div style='width: 45%;float: left;height: 35px'>"+arr[i].customerName+"</div>");
                    } else if (arr[i].contractFlag == 20) {
                        $("#signed").append("<div style='width: 45%;float: left;height: 35px'>"+arr[i].customerName+"</div>");
                    } else {
                        $("#notSigned").append("<div style='width: 45%;float: left;height: 35px'>"+arr[i].customerName+"</div>");
                    }
                    salesCustomerIds.push(arr[i].id);
                }
                ids = salesCustomerIds.join(",");
            }
            $("#salesCustomerIds").val(ids);
        }
    </script>

</head>
<body>

<br/>

<div style="height: 667px;overflow: auto;margin: -16px 0 0 0;" id="parentContainer">
    <form:form id="inputForm" modelAttribute="user" method="post" class="form-horizontal" cssStyle="width: 95%;margin: 10px auto;">
        <form:hidden path="id"/>
        <form:hidden path="office.id"/>
        <sys:message content="${message}"/>
        <input type="hidden" name="salesCustomerIds" id="salesCustomerIds">
        <legend>用户信息<div class="line_"></div></legend>
        <div style="padding: 30px;margin-top: -25px;margin-bottom: -25px">
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>用户名称：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <form:input path="name" htmlEscape="false" maxlength="30" class="required" cssStyle="width: 250px" placeholder="输入用户名称"/>
                        </div>
                    </div>
                </div>

                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>登录帐号：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <input id="oldLoginName" name="oldLoginName" type="hidden" value="${user.loginName}">
                            <form:input path="loginName" htmlEscape="false" maxlength="20" class="required userName" cssStyle="width: 250px"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><c:if test="${empty user.id}"><span class="help-inline"><font color="red">*</font> </span></c:if>密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <input id="newPassword" name="newPassword" type="password" value="" maxlength="20" minlength="6"
                                   class="${empty user.id?'required':''}" style="width: 250px"/>
                            <c:if test="${not empty user.id}"><span class="help-inline">若不修改密码，请留空。</span></c:if>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><c:if test="${empty user.id}"><span class="help-inline"><font color="red">*</font> </span></c:if>确认密码：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <input id="confirmNewPassword" name="confirmNewPassword" type="password" value="" maxlength="20"
                                   minlength="6" equalTo="#newPassword" style="width: 250px"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><span id="span_mobile" style="display: none;" class="help-inline"><font color="red">*</font> </span>联系电话：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <form:input path="mobile" htmlEscape="false" maxlength="11" cssStyle="width: 250px" placeholder="输入11位手机号码"/>

                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px">座&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <form:input path="phone" htmlEscape="false" maxlength="16" cssStyle="width: 250px"/>

                        </div>
                    </div>
                </div>
            </div>

            <div class="row-fluid">
                <%--<div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px">邮&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;箱：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <form:input path="email" htmlEscape="false" maxlength="60" class="email" cssStyle="width: 250px"/>
                        </div>
                    </div>
                </div>--%>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><span id="span_qq" style="display: none;" class="help-inline"><font color="red">*</font> </span>Q&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Q：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <form:input path="qq" htmlEscape="false" maxlength="11" cssStyle="width: 250px"/>

                        </div>
                    </div>
                </div>
            </div>
        </div>


        <%--其它信息--%>
        <legend>其他信息<div class="line_"></div></legend>
        <div style="width: 90%;padding: 30px;margin-top: -25px;">
            <div class="row-fluid">
                <c:choose>
                    <c:when test="${userType != 7}">
                        <div class="span7">
                            <div class="control-group" style="border-bottom: 0px">
                                <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>用户类型：</label>
                                <div class="controls" style="margin-left: 100px;">
                                    <form:select path="userType" class="input-xlarge required">
                                        <form:option value="" label="请选择"/>
                                        <form:options items="${fns:getDictExceptListFromMS('sys_user_type','3,4,5,6,9')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%-- 切换为微服务 --%>
                                    </form:select>

                                </div>
                            </div>
                        </div>

                        <div class="span5">
                            <div class="control-group" id="divSubFlag" style="margin-left: -200px;border-bottom: 0px;margin-top:3px;">
                                <label class="control-label"></label>
                                <div class="controls">
                                    <span>
                                       <input id="subFlag1" name="subFlag" class="required" type="radio" value="0"><label for="subFlag1" id="span1"></label>
                                    </span>
                                    <span>
                                        <input id="subFlag2" name="subFlag" class="required" type="radio" value="1"><label for="subFlag2"id="span2"></label>
                                    </span>
                                    <span>
                                        <input id="subFlag3" name="subFlag" class="required" type="radio" value="2"><label for="subFlag3"id="span3"></label>
                                    </span>
                                    <span>
                                        <input id="subFlag4" name="subFlag" class="required" type="radio" value="3"><label for="subFlag4"id="span4"></label>
                                    </span>
                                    <span>
                                        <input id="subFlag5" name="subFlag" class="required" type="radio" value="4"><label for="subFlag5"id="span5"></label>
                                    </span>
                                    <span class="help-inline"><font color="red">*</font> </span>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="span2">
                            <div class="control-group">
                                <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>子类型：</label>
                                <div class="controls" style="margin-left: 100px;">
                                    <input type="hidden" name="userType" id="userType" value="7">
                                </div>
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${showChildren != null && showChildren eq true}">
                                <div class="span5" style="margin-left: 10px">
                                    <div class="control-group" id="divSubFlag" style="margin-left: -200px;">
                                        <label class="control-label"></label>
                                        <div class="controls">
                                            <span>
                                               <label><input name="subFlag" class="required" type="radio" value="1">业务</label>
                                            </span>
                                            <span>
                                               <label><input name="subFlag" class="required" type="radio" value="2">跟单</label>
                                            </span>
<%--                                            <c:choose>--%>
<%--                                                <c:when test="${user.subFlag == 1}">--%>
<%--                                                     <span>--%>
<%--                                                        <input name="subFlag" class="required" type="radio" value="1"><span>业务</span>--%>
<%--                                                    </span>--%>
<%--                                                </c:when>--%>
<%--                                                <c:otherwise>--%>
<%--                                                    <span>--%>
<%--                                                        <input name="subFlag" class="required" type="radio" value="2"><span>跟单</span>--%>
<%--                                                    </span>--%>
<%--                                                </c:otherwise>--%>
<%--                                            </c:choose>--%>
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="span5" style="margin-left: 10px">
                                <div class="control-group" id="divSubFlag" style="margin-left: -200px;">
                                    <label class="control-label"></label>
                                    <div class="controls">
                                        <span>
                                            <input id="subFlag1" name="subFlag" class="required" type="radio" value="0"><label for="subFlag1" id="span1"></label>
                                        </span>
                                        <span>
                                            <input id="subFlag2" name="subFlag" class="required" type="radio" value="1"><label for="subFlag2"id="span2"></label>
                                        </span>
                                        <span>
                                            <input id="subFlag3" name="subFlag" class="required" type="radio" value="2"><label for="subFlag3"id="span3"></label>
                                        </span>
                                        <span>
                                            <input id="subFlag4" name="subFlag" class="required" type="radio" value="3"><label for="subFlag4"id="span4"></label>
                                        </span>
                                        <span>
                                            <input id="subFlag5" name="subFlag" class="required" type="radio" value="4"><label for="subFlag5"id="span5"></label>
                                        </span>
                                        <span class="help-inline"><font color="red">*</font> </span>
                                    </div>
                                </div>
                                </div>
                            </c:otherwise>
                        </c:choose>

                    </c:otherwise>
                </c:choose>
            </div>


            <div class="row-fluid" style="margin-top: 15px;">
                <div class="control-group">
                    <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>用户角色：</label>
                    <div class="controls" style="margin-left: 104px;width: 90%;">
                        <c:forEach items="${allRoles}" var="roles" varStatus="i">
                            <div style="width: 18%;height:25px;float: left;text-align: left;">
                                <span>
                                    <input id="roleIdList${i.index+1}" name="roleIdList" class="" type="checkbox" value="${roles.id}"><label for="roleIdList${i.index+1}">${roles.name}</label>
                                </span>
                            </div>
                        </c:forEach>
                        <%--<span class="help-inline"><font color="red">*</font> </span>--%>
                    </div>
                </div>
            </div>

            <div class="row-fluid" style="margin-top: 15px;">
                <div class="control-group" id="divProductCategory">
                    <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>授权品类：</label>
                    <div class="controls" style="margin-left: 104px;width: 90%;">
                        <c:forEach items="${fns:getProductCategories()}" varStatus="i" var="category">
                            <div style="width: 18%;float: left;text-align: left;">
                                <span>
                                    <input id="categoryList${i.index+1}" name="productCategoryIds" type="checkbox" value="${category.id}"><label for="categoryList${i.index+1}">${category.name}</label>
                                </span>
                            </div>
                        </c:forEach>

                    </div>
                </div>
            </div>

            <div class="row-fluid" id="divCustomer" style="margin-top: 15px;">
                <div class="span6" style="margin-top: 15px;">
                    <div class="control-group" >
                        <label class="control-label" style="width: 104px">VIP客户：</label>
                        <div class="controls" style="margin-left: 104px;">
                            <div id="vipCustomerTree" class="input-block-level ztree" style="margin-top:3px;float:left;height:160px;overflow:auto;"></div>
                            <form:hidden path="customerIds"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row-fluid" id="divArea" style="margin-top: 15px;">
                <div class="span6" style="float: none;margin-left: 2px;">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px">区域授权：</label>
                        <div class="controls" style="margin-left: 104px;">
                            <div id="areaTree" class="input-block-level ztree" style="margin-top:3px;float:left;height:400px;overflow:auto;"></div>
                            <form:hidden path="areaIds"/>
                            <form:hidden path="areas" htmlEscape="false" />
                            <form:hidden path="regions" htmlEscape="false" />
                        </div>
                    </div>
                </div>
            </div>

            <div class="row-fluid" style="margin-top: 15px;">
                <div class="control-group">
                    <label class="control-label" style="width: 104px">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
                    <div class="controls" style="margin-left: 105px;">
                        <form:textarea path="remarks" htmlEscape="false" rows="2" maxlength="250" class="input-block-level" cssStyle="width: 656px;height: 60px;"/>
                    </div>
                </div>
            </div>
        </div>

        <%--delete on 2020-07-11--%>
        <%--<c:if test="${userType == 7}">--%>
            <%--<legend>客户<div style="border-bottom: 3.5px solid #0096DA;width: 35px;border-radius: 10px;"></div></legend>--%>
            <%--<div style="width: 732px;padding: 63px;margin-top: -62px;height: 300px;">--%>
                <%--<div>--%>
                    <%--<button class="add_button" id="add_button" type="button">--%>
                        <%--<i class="icon-plus"></i>&nbsp;&nbsp;添加客户--%>
                    <%--</button>--%>
                <%--</div>--%>

                <%--<div class="layui-tab layui-tab-brief" lay-filter="contract" style="border: 1px solid #DCDEE2;">--%>
                    <%--<ul class="layui-tab-title" style="background-color: #F8F8F9;">--%>
                        <%--<li data-flag="20" class="layui-this">已签约</li>--%>
                        <%--<li data-flag="30">未签约</li>--%>
                        <%--<li data-flag="10">试单</li>--%>
                        <%--<label id="result_count" style="display: none;float: right;line-height: 40px;margin-right: 15px;">共<i id="count" style="color: red"></i>个客户</label>--%>
                    <%--</ul>--%>

                    <%--<div class="layui-tab-content" style="height: 200px;overflow: auto;">--%>
                            <%--<div id="signed" class="layui-tab-item layui-show"></div>--%>
                            <%--<div id="notSigned" class="layui-tab-item"></div>--%>
                            <%--<div id="tryWorkOrder" class="layui-tab-item"></div>--%>
                    <%--</div>--%>
                <%--</div>--%>
            <%--</div>--%>
        <%--</c:if>--%>

        <c:if test="${not empty user.id}">
            <legend style="margin-top: -20px;">最后登录<div class="line_"></div></legend>
            <div style="padding: 30px;margin-top: -25px;margin-bottom: -25px">
                <div class="row-fluid">
                    <div class="span6">
                        <div class="control-group">
                            <label class="control-label" style="width: 104px">IP地址：</label>
                            <div class="controls" style="margin-left: 100px;">
                                <input readonly="readonly" style="width: 250px" type="text" value="${user.loginIp}">
                            </div>
                        </div>
                    </div>

                    <div class="span6">
                        <div class="control-group">
                            <label class="control-label" style="width: 104px">时&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;间：</label>
                            <div class="controls" style="margin-left: 100px;">
                                <input readonly="readonly" style="width: 250px" type="text" value="<fmt:formatDate value="${user.loginDate}" pattern="yyyy-MM-dd HH:mm:ss" />">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

    </form:form>
</div>

<div id="editBtn">
    <shiro:hasPermission name="sys:user:edit">
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;" onclick="$('#inputForm').submit()"/>&nbsp;
    </shiro:hasPermission>
    <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
</div>
<script class="removedscript" type="text/javascript">
    // 关闭页面
    function cancel() {
        top.layer.close(this_index);// 关闭本身
    }
    $(document).ready(function() {
       <c:if test="${user != null && user.id != null}">
            var roleIdList = ${user.roleIdList};
            for(var i = 0;i < roleIdList.length; i++) {
                $("input[type='checkbox'][name='roleIdList'][value=" + roleIdList[i] + "]").attr("checked", "checked");
            }

            <c:if test="${user.productCategoryIds.size() > 0}">
                var productCategoryIds = ${user.productCategoryIds};
                for(var i = 0;i < productCategoryIds.length; i++) {
                    $("input[type='checkbox'][name='productCategoryIds'][value=" + productCategoryIds[i] + "]").attr("checked", "checked");
                }
            </c:if>
        </c:if>


        <%--<c:if test="${customers != null && customers.size() > 0}">--%>
            <%--var arr = ${fns:toJson(customers)};--%>
            <%--for(var i = 0;i < arr.length; i++) {--%>
                <%--if (arr[i].contractFlag == 10) {--%>
                    <%--$("#tryWorkOrder").append("<div style='width: 45%;float: left;height: 35px'>"+arr[i].name+"</div>");--%>
                <%--} else if (arr[i].contractFlag == 20) {--%>
                    <%--$("#signed").append("<div style='width: 45%;float: left;height: 35px'>"+arr[i].name+"</div>");--%>
                <%--} else {--%>
                    <%--$("#notSigned").append("<div style='width: 45%;float: left;height: 35px'>"+arr[i].name+"</div>");--%>
                <%--}--%>
            <%--}--%>
            <%--$("#result_count").show();--%>
            <%--$("#count").text(arr.length);--%>
        <%--</c:if>--%>
        layui.use('element', function () {
            // tab的切换功能，切换事件监听等，需要依赖element模块
            var $ = layui.jquery,
                element = layui.element;

            // 监听tab切换
            element.on('tab(contract)', function(data){
                // console.log(this); //当前Tab标题所在的原始DOM元素
                // console.log(data.index); //得到当前Tab的所在下标
                // console.log(data.elem); //得到当前的Tab大容器
            });
        });
    });

</script>
</body>
</html>
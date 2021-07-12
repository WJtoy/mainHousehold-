<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/treeview.jsp" %>
    <title>用户管理</title>
    <meta name="decorator" content="default"/>
    <script type="text/javascript">
        var ctree;
        var regions = [];
        $(document).ready(function () {
            $("#loginName").focus();
            $("#inputForm").validate({
                rules: {
                    loginName: {remote: "${ctx}/sys/user/checkLoginName?oldLoginName=" + encodeURIComponent('${user.loginName}')}
                },
                messages: {
                    loginName: {remote: "用户登录名已存在"},
                    confirmNewPassword: {equalTo: "输入与上面相同的密码"}
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
                    // console.log($("#regions").val());
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
                    }

                   /*
                   var cids = $("#customerIds").val();
                    if(cids != null && cids.indexOf(",") != -1){
                        layerAlert("产品只能设定一个品牌","系统提示",true);
                        return false;
                    }
                    */
                    loading('正在提交，请稍等...');
                    form.submit();
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
        });


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
                $("#divSubFlag #span1").html("超级客服");
                $("#divSubFlag #span2").html("VIP客服");
                $("#divSubFlag #span3").html("普通客服");
                $("#divSubFlag #span4").html("突击客服");
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
                $("#subFlag1").hide();
                $("#subFlag2").show();
                $("#subFlag3").show();
                $("#subFlag4").hide();
                $("#subFlag1").prop("checked","");
                $("#subFlag4").prop("checked","");
            } else if (uType=='10') {  //事业部账号
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
                $("#subFlag1").show();
                $("#subFlag2").hide();
                $("#subFlag3").hide();
                $("#subFlag4").hide();
                $("#subFlag2").prop("checked","");
                $("#subFlag3").prop("checked","");
                $("#subFlag4").prop("checked","");
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
                $("#subFlag1").show();
                $("#subFlag2").show();
                $("#subFlag3").hide();
                $("#subFlag4").hide();
                $("#subFlag3").prop("checked","");
                $("#subFlag4").prop("checked","");
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
    </script>

</head>
<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/sys/user/list">用户列表</a></li>
    <li class="active"><a href="javascript:;">用户
        <shiro:hasPermission name="sys:user:edit">${not empty user.id?'修改':'添加'}</shiro:hasPermission>
        <shiro:lacksPermission name="sys:user:edit">查看</shiro:lacksPermission></a></li>
</ul>
<br/>
<form:form id="inputForm" modelAttribute="user" action="${ctx}/sys/user/save" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <sys:message content="${message}"/>
    <div class="control-group">
        <label class="control-label">头像:</label>
        <div class="controls">
            <form:hidden id="nameImage" path="photo" htmlEscape="false" maxlength="150" class="input-xlarge"/>
            <sys:ckfinder input="nameImage" type="images" uploadPath="/photo" selectMultiple="false" maxWidth="100"
                          maxHeight="100"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">归属公司:</label>
        <div class="controls">
            <sys:treeselect id="company" name="company.id" value="${user.company.id}" labelName="company.name"
                            labelValue="${user.company.name}"
                            title="公司" url="/sys/office/treeData?type=1" cssClass="required"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">归属部门:</label>
        <div class="controls">
            <sys:treeselect id="office" name="office.id" value="${user.office.id}" labelName="office.name"
                            labelValue="${user.office.name}"
                            title="部门" url="/sys/office/treeData?type=2" cssClass="required"
                            notAllowSelectParent="true"/>
        </div>
    </div>
    <%--<div class="control-group">
        <label class="control-label">工号:</label>
        <div class="controls">
            <form:input path="no" htmlEscape="false" maxlength="50" class="required"/>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>--%>
    <div class="control-group">
        <label class="control-label">姓名:</label>
        <div class="controls">
            <form:input path="name" htmlEscape="false" maxlength="30" class="required"/>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">登录名:</label>
        <div class="controls">
            <input id="oldLoginName" name="oldLoginName" type="hidden" value="${user.loginName}">
            <form:input path="loginName" htmlEscape="false" maxlength="20" class="required userName"/>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">密码:</label>
        <div class="controls">
            <input id="newPassword" name="newPassword" type="password" value="" maxlength="20" minlength="6"
                   class="${empty user.id?'required':''}"/>
            <c:if test="${empty user.id}"><span class="help-inline"><font color="red">*</font> </span></c:if>
            <c:if test="${not empty user.id}"><span class="help-inline">若不修改密码，请留空。</span></c:if>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">确认密码:</label>
        <div class="controls">
            <input id="confirmNewPassword" name="confirmNewPassword" type="password" value="" maxlength="20"
                   minlength="6" equalTo="#newPassword"/>
            <c:if test="${empty user.id}"><span class="help-inline"><font color="red">*</font> </span></c:if>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">邮箱:</label>
        <div class="controls">
            <form:input path="email" htmlEscape="false" maxlength="60" class="email"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">电话:</label>
        <div class="controls">
            <form:input path="phone" htmlEscape="false" maxlength="16"/>
            <span id="span_phone" style="display: none;" class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">手机:</label>
        <div class="controls">
            <form:input path="mobile" htmlEscape="false" maxlength="11"/>
            <span id="span_mobile" style="display: none;" class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">QQ:</label>
        <div class="controls">
            <form:input path="qq" htmlEscape="false" maxlength="11"/>
            <span id="span_qq" style="display: none;" class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <%--<div class="control-group">
        <label class="control-label">是否允许登录:</label>
        <div class="controls">
            <form:select path="loginFlag">
                <form:options items="${fns:getDictList('yes_no')}" itemLabel="label" itemValue="value"
                              htmlEscape="false"/>
            </form:select>
            <span class="help-inline"><font color="red">*</font> “是”代表此账号允许登录，“否”则表示此账号不允许登录</span>
        </div>
    </div>--%>
    <div class="control-group">
        <label class="control-label">用户类型:</label>
        <div class="controls">
            <form:select path="userType" class="input-xlarge required">
                <form:option value="" label="请选择"/>
                <form:options items="${fns:getDictExceptListFromMS('sys_user_type','3,4,5,6,9')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%-- 切换为微服务 --%>
            </form:select>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group" id="divSubFlag">
        <label class="control-label"></label>
        <div class="controls">
            <span>
                <input id="subFlag1" name="subFlag" class="required" type="radio" value="0"><span id="span1"></span>
            </span>
            <span>
                <input id="subFlag2" name="subFlag" class="required" type="radio" value="1"><span id="span2"></span>
            </span>
            <span>
                <input id="subFlag3" name="subFlag" class="required" type="radio" value="2"><span id="span3"></span>
            </span>
            <span>
                <input id="subFlag4" name="subFlag" class="required" type="radio" value="3"><span id="span4"></span>
            </span>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <%--
    <div class="control-group" id="divKefuSubFlag">
        <label class="control-label"></label>
        <div class="controls">
            <span>
                <form:radiobutton path="subFlag" value="0" class="required"></form:radiobutton>超级客户
            </span>
            <span>
                <form:radiobutton path="subFlag" value="1" class="required"></form:radiobutton>VIP客户
            </span>
            <span>
                <form:radiobutton path="subFlag" value="2" class="required"></form:radiobutton>普通客户
            </span>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    --%>
    <div class="control-group">
        <label class="control-label">用户角色:</label>
        <div class="controls">
            <form:checkboxes path="roleIdList" items="${allRoles}" itemLabel="name" itemValue="id" htmlEscape="false"
                             class="required"/>
            <span class="help-inline"><font color="red">*</font> </span>
        </div>
    </div>
    <div class="control-group" id="divProductCategory">
        <label class="control-label">授权服务品类:</label>
        <div class="controls">
            <ul class="ztree" id="productCategoryTree"></ul>
            <form:hidden path="productCategoryIds"/>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group" id="divCustomer">
                <label class="control-label">VIP客户:</label>
                <div class="controls">
                    <%--
                    <form:select path="customerIds" cssClass="input-block-level" multiple="multiple">
                    <form:options items="${allCustomers}" itemLabel="name" itemValue="id" htmlEscape="false"/>
                    </form:select>
                    --%>
                    <div id="vipCustomerTree" class="input-block-level ztree" style="margin-top:3px;float:left;height:400px;overflow:auto;"></div>
                    <form:hidden path="customerIds"/>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group" id="divArea">
                <label class="control-label">区域授权:</label>
                <div class="controls">
                    <div id="areaTree" class="input-block-level ztree" style="margin-top:3px;float:left;height:400px;overflow:auto;"></div>
                    <form:hidden path="areaIds"/>
                    <form:hidden path="areas" htmlEscape="false" />
                    <form:hidden path="regions" htmlEscape="false" />
                </div>
            </div>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">备注:</label>
        <div class="controls">
            <form:textarea path="remarks" htmlEscape="false" rows="2" maxlength="250" class="input-block-level" />
        </div>
    </div>
    <c:if test="${not empty user.id}">
        <div class="control-group">
            <label class="control-label">创建时间:</label>
            <div class="controls">
                <%--<label class="lbl"><fmt:formatDate value="${user.createDate}" type="both" dateStyle="full"/></label>--%>
                <label class="lbl"><fmt:formatDate value="${user.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></label>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">最后登陆:</label>
            <div class="controls">
                <label class="lbl">IP: ${user.loginIp}&nbsp;&nbsp;&nbsp;&nbsp;时间：
                    <%--<fmt:formatDate value="${user.loginDate}" type="both" dateStyle="full"/>--%>
                    <fmt:formatDate value="${user.loginDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
                </label>
            </div>
        </div>
    </c:if>
    <div class="form-actions">
        <shiro:hasPermission name="sys:user:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
        </shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
    </div>
</form:form>
</body>
</html>
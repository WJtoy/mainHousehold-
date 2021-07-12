<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/treeview.jsp" %>
    <title>客服管理</title>
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <style type="text/css">
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
        .line_{
            border-bottom: 3.5px solid #0096DA;
            width: 65px;
            border-radius: 10px;
        }
        .error{
            width: 300px;
        }
        p{
            margin: 0px 0px 0px 0px;
        }
    </style>

    <script type="text/javascript">
        var orderdetail_index = parent.layer.getFrameIndex(window.name);
        var this_index = top.layer.index;
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
        var regions = [];
        var clickTag = 0;
        $(document).ready(function() {
            var $btnSubmit = $("#btnSubmit");
            $("#loginName").focus();
            $("#inputForm").validate({
                rules: {
                    loginName: {remote: "${ctx}/sys/userKeFu/checkLoginName?oldLoginName=" + encodeURIComponent('${user.loginName}')},
                    /*newPassword:{levelLimit:true}*/
                },
                messages: {
                    loginName: {remote: "用户登录名已存在"},
                    confirmNewPassword: {equalTo: "两次密码输入不一致"},
                    // newPassword:{required:"请输入密码"}
                },
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
                    var officeId = $("#office\\.id").val();
                    if(officeId == ''){
                        layerError("请选择部门", "错误提示");
                        return false;
                    }


                    var subFlag = $('[name=subFlag]:checked').val();

                    if($("input[type='checkbox'][name='productCategoryIds']:checked").length == 0){
                        layerError("请选择授权品类", "错误提示");
                        return false;
                    }
                    if(subFlag == 1){
                        if($("input[type='checkbox'][name='customerIds']:checked").length == 0){
                            layerError("请选择授权VIP客户", "错误提示");
                            return false;
                        }
                    }
                    var newSubFlag = $("#newSubFlag").val();
                    if(newSubFlag != subFlag){
                        if((newSubFlag== 0 || newSubFlag == 1 || newSubFlag == 2) && (subFlag == 3 || subFlag == 4)){
                            layerError("请重新选择负责区域", "错误提示");
                            return false;
                        }
                    }
                    var entity = {};

                    entity["id"] = $("#id").val();
                    entity['userType'] = $("#userType").val();
                    entity['name'] = $("#name").val();
                    entity['loginName'] = $("#loginName").val();
                    entity['oldLoginName'] = $("#oldLoginName").val();
                    entity['newPassword'] = $("#newPassword").val();
                    entity['confirmNewPassword'] = $("#confirmNewPassword").val();
                    entity['mobile'] = $("#mobile").val();
                    entity['phone'] = $("#phone").val();
                    entity['qq'] = $("#qq").val();
                    entity['office.id'] = $("#office\\.id").val();
                    entity['managerFlag'] = $('[name=managerFlag]:checked').val();
                    entity['subFlag'] = subFlag;
                    entity['userRegionJson'] = $("#userRegionJson").val();

                    $("input[type='checkbox'][name='role']:checkbox:checked").each(function(i,element){
                        entity['roleList[' + i +'].id'] = this.value;
                    });

                    $("input[type='checkbox'][name='productCategoryIds']:checkbox:checked").each(function(i,element){
                        entity['productCategoryIds[' + i +']'] = this.value;
                    });
                    if(subFlag == 1){
                        $("input[type='checkbox'][name='customerIds']:checkbox:checked").each(function(i,element){
                            entity['customerList[' + i +'].id'] = this.value;
                        });
                    }

                    entity['remarks'] = $('#remarks').val();
                    clickTag = 1;
                    $btnSubmit.prop("disabled", true);

                    $.ajax({
                        url:"${ctx}/sys/userKeFu/save",
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
                                top.layer.close(this_index);//关闭本身
                                layerMsg("保存成功");
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
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
                            //var msg = eval(data);
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


            $("input[name='subFlag']").on("change",function(){
                editSubFlag();
            });

            $("input[name='role']").on("change",function(){
                var role = this.value;
                if(role == 14){
                    $("input[data-name='客服-总监']").prop("checked","");
                    $("input[data-name='客服-经理']").prop("checked","");
                }else if(role == 53){
                    $("input[data-name='客服-总监']").prop("checked","");
                    $("input[data-name='客服-主管']").prop("checked","");
                }else if(role == 39){
                    $("input[data-name='客服-经理']").prop("checked","");
                    $("input[data-name='客服-主管']").prop("checked","");
                }

            });

            $("input[name='managerFlag']").on("change",function(){

                var managerFlag = $("input[name='managerFlag']:checked").val();
                if(managerFlag == 1){
                    $("[name='role']").prop("checked", "");
                }else {
                    $("[name='subFlag']").prop("checked", "");
                    $("[name='role']").prop("checked", "");
                }
                editManagerFlag();
            });

            $(document).on('change',"#office\\.id",function (e) {
                editOffice();
            });

        });
        function editManagerFlag() {
            var managerFlag = $("input[name='managerFlag']:checked").val();
            if (managerFlag == 1) {
                $("input[data-name='客服-总监']").attr("disabled",false);
                $("label[for='客服-总监']").attr("style","");
                $("input[data-name='客服-经理']").attr("disabled",false);
                $("label[for='客服-经理']").attr("style","");
                $("input[data-name='客服主管']").attr("disabled",false);
                $("label[for='客服主管']").attr("style","");
                $("input[data-name='客服']").attr("disabled",false);
                $("label[for='客服']").attr("style","");
                $("input[data-name='网点管理']").attr("disabled",false);
                $("label[for='网点管理']").attr("style","");
                $("input[data-name='突击工单']").attr("disabled",false);
                $("label[for='突击工单']").attr("style","");
                $("input[data-name='开发主管']").attr("disabled",false);
                $("label[for='开发主管']").attr("style","");
                $("input[data-name='开发-开发']").attr("disabled",false);
                $("label[for='开发-开发']").attr("style","");
                $("input[data-name='客服助理']").attr("disabled",false);
                $("label[for='客服助理']").attr("style","");
                $("input[data-subFlag='vipKeFu']").attr("disabled",false);
                $("label[name='超级客服']").attr("style","");
                $("input[data-subFlag='autoKeFu']").attr("disabled",false);
                $("label[name='自动客服']").attr("style","");
                $("input[data-subFlag='tujiKeFu']").attr("disabled",false);
                $("label[name='突击客服']").attr("style","");
                $("input[data-subFlag='puKeFu']").attr("disabled",false);
                $("label[name='普通客服']").attr("style","");
                $("input[data-subFlag='daKeFu']").attr("disabled",false);
                $("label[name='大客服']").attr("style","");
                $("input[data-subFlag='kaKeFu']").attr("disabled",false);
                $("label[name='KA客服']").attr("style","");

            }else if(managerFlag == 0){
                $("input[data-name='客服-总监']").attr("disabled",true);
                $("label[for='客服-总监']").attr("style","color:#C5C8CE");
                $("input[data-name='客服-经理']").attr("disabled",true);
                $("label[for='客服-经理']").attr("style","color:#C5C8CE");
                $("input[data-name='客服主管']").attr("disabled",true);
                $("label[for='客服主管']").attr("style","color:#C5C8CE");
                $("input[data-name='客服']").attr("disabled",false);
                $("label[for='客服']").attr("style","");
                $("input[data-name='网点管理']").attr("disabled",false);
                $("label[for='网点管理']").attr("style","");
                $("input[data-name='突击工单']").attr("disabled",false);
                $("label[for='突击工单']").attr("style","");
                $("input[data-name='开发主管']").attr("disabled",false);
                $("label[for='开发主管']").attr("style","");
                $("input[data-name='开发-开发']").attr("disabled",false);
                $("label[for='开发-开发']").attr("style","");
                $("input[data-name='客服助理']").attr("disabled",false);
                $("label[for='客服助理']").attr("style","");
                $("input[data-subFlag='vipKeFu']").attr("disabled",true);
                $("label[name='超级客服']").attr("style","color:#C5C8CE");
                $("input[data-subFlag='autoKeFu']").attr("disabled",false);
                $("label[name='自动客服']").attr("style","");
                $("input[data-subFlag='tujiKeFu']").attr("disabled",false);
                $("label[name='突击客服']").attr("style","");
                $("input[data-subFlag='puKeFu']").attr("disabled",false);
                $("label[name='普通客服']").attr("style","");
                $("input[data-subFlag='daKeFu']").attr("disabled",false);
                $("label[name='大客服']").attr("style","");
                $("input[data-subFlag='kaKeFu']").attr("disabled",false);
                $("label[name='KA客服']").attr("style","");

            }else if(managerFlag == 2){
                $("input[data-name='客服-总监']").attr("disabled",true);
                $("label[for='客服-总监']").attr("style","color:#C5C8CE");
                $("input[data-name='客服-经理']").attr("disabled",true);
                $("label[for='客服-经理']").attr("style","color:#C5C8CE");
                $("input[data-name='客服主管']").attr("disabled",true);
                $("label[for='客服主管']").attr("style","color:#C5C8CE");
                $("input[data-name='客服']").attr("disabled",true);
                $("label[for='客服']").attr("style","color:#C5C8CE");
                $("input[data-name='网点管理']").attr("disabled",true);
                $("label[for='网点管理']").attr("style","color:#C5C8CE");
                $("input[data-name='突击工单']").attr("disabled",true);
                $("label[for='突击工单']").attr("style","color:#C5C8CE");
                $("input[data-name='开发主管']").attr("disabled",true);
                $("label[for='开发主管']").attr("style","color:#C5C8CE");
                $("input[data-name='开发-开发']").attr("disabled",true);
                $("label[for='开发-开发']").attr("style","color:#C5C8CE");
                $("input[data-name='客服助理']").attr("disabled",false);
                $("label[for='客服助理']").attr("style","");
                $("input[data-subFlag='vipKeFu']").attr("disabled",false);
                $("label[name='超级客服']").attr("style","");
                $("input[data-subFlag='autoKeFu']").attr("disabled",true);
                $("label[name='自动客服']").attr("style","color:#C5C8CE");
                $("input[data-subFlag='tujiKeFu']").attr("disabled",true);
                $("label[name='突击客服']").attr("style","color:#C5C8CE");
                $("input[data-subFlag='puKeFu']").attr("disabled",true);
                $("label[name='普通客服']").attr("style","color:#C5C8CE");
                $("input[data-subFlag='daKeFu']").attr("disabled",true);
                $("label[name='大客服']").attr("style","color:#C5C8CE");
                $("input[data-subFlag='kaKeFu']").attr("disabled",true);
                $("label[name='KA客服']").attr("style","color:#C5C8CE");

            }
        }

        function editSubFlag() {
            var subFlag = $("input[name='subFlag']:checked").val();
            if (subFlag == "1") {
                $("#divCustomer").show();
            } else {
                $("#divCustomer").hide();
            }
        }

        function editOffice() {
            var officeId = $("#office\\.id").val();
            if (officeId !='') {
                var name = $("#office\\.id").find("option:selected").text().replace(/&nbsp;|\s/g,"");
                $('.select2-chosen').text(name);
            }
        }
        function editUserRegion() {
            var subFlag = $('[name=subFlag]:checked').val();
            var userId = $("#id").val();
            if(subFlag == null){
                layerError("请选择客服类型", "错误提示");
                return false;
            }
            var text = "设置区域";
            var url = "${ctx}/sys/userKeFu/userRegion?userId="+ userId +"&subFlag=" + subFlag + "&parentIndex=" + (orderdetail_index || '');
            var area = ['954px', '640px'];
            top.layer.open({
                type: 2,
                id:"userRegion",
                zIndex:200,
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

        function refreshUserRegion(userRegion) {
            $("#newSubFlag").val(userRegion.subFlag);
            $("#userRegionJson").val(JSON.stringify(userRegion));
            $("input[name=subFlag]").attr("disabled",true);
            $("input[name=managerFlag]").attr("disabled",true);

            var subFlag = $('[name=subFlag]:checked').val();
            var userRegionJson = $("#userRegionJson").val();
            var userId = userRegion.userId;

            $.ajax({
                url:"${ctx}/sys/userKeFu/selectUserRegionNames",
                type: 'post',
                data:{userId:userId,userRegionJson:userRegionJson,subFlag:subFlag},
                success:function (e) {
                    if(e.success){
                        $("#userRegion").empty();
                        var regionName_sel=[];
                        for(var i=0,len=e.data.length;i<len;i++){
                            var regionName = e.data[i];
                            regionName_sel.push('<div style="margin-top: 5px">'+regionName +'</div>')
                        }
                        $("#userRegion").append(regionName_sel);
                    }else {

                    }
                },
                error:function (e) {
                    layerError("切换负责区域失败","错误提示");
                }
            });


        }
    </script>


</head>
<body>
<form:form id="inputForm" modelAttribute="user" method="post" action="${ctx}/sys/userKeFu/form" class="form-horizontal" cssStyle="margin-top: 25px;margin-left: 50px">
    <sys:message content="${message}"/>
    <form:hidden path="id"/>
    <form:hidden path="userType"/>
    <form:hidden path="customerIds"/>
    <input type="hidden" id="userRegionJson" value="">

    <legend>用户信息<div class="line_"></div></legend>
    <div style="padding: 30px;margin-top: -25px;margin-bottom: -25px">
        <div class="row-fluid">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>用户名称：</label>
                    <div class="controls" style="margin-left: 100px;">
                        <form:input path="name" htmlEscape="false" maxlength="30" class="required" cssStyle="width: 250px"/>

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
                        <input id="newPassword" name="newPassword" type="password" value=""  placeholder="${empty user.id?'输入6位密码':''}"
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
                    <label class="control-label" style="width: 104px"><span class="red">*</span>联系电话：</label>
                    <div class="controls" style="margin-left: 100px;">
                        <form:input path="mobile" htmlEscape="false" maxlength="11" cssStyle="width: 250px" class="required mobile" placeholder="输入11位手机号码"/>

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
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" style="width: 104px">Q&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Q：</label>
                    <div class="controls" style="margin-left: 100px;">
                        <form:input path="qq" htmlEscape="false" maxlength="15" cssStyle="width: 250px"/>

                    </div>
                </div>
            </div>
        </div>
    </div>


    <legend>其他信息<div class="line_"></div></legend>
    <div style="width: 90%;padding: 30px;margin-top: -25px;">
        <div class="row-fluid" style="margin-top: 15px;">
            <div class="control-group">
                <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：</label>
                <div class="controls" style="margin-left: 100px;">
                    <form:select path="office.id" class="input-xlarge required" cssStyle="width: 250px">
                        <form:option value="" label="请选择"/>
                        <form:options items="${offices}" itemLabel="name" itemValue="id" htmlEscape="false"/>
                    </form:select>

                </div>
            </div>
        </div>
        <div class="row-fluid" style="margin-top: 15px;">
            <div class="control-group">
                <label class="control-label" style="width: 104px"><span class="red">*</span>职&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;位：</label>
                <div class="controls" style="margin-left: 105px">
                    <div style="float: left;width: 87px">
                        <label><form:radiobutton path="managerFlag" value="1" cssClass="required" cssStyle="zoom: 1.3"></form:radiobutton>客服主管</label>
                    </div>
                    <div style="float: left;width: 87px">
                        <label><form:radiobutton path="managerFlag" value="0" cssClass="required" cssStyle="zoom: 1.3"></form:radiobutton>普通客服</label>
                    </div>
                    <div>
                        <label><form:radiobutton path="managerFlag" value="2" cssClass="required" cssStyle="zoom: 1.3"></form:radiobutton>客服助理</label>
                        <span class="help-inline" style="margin-left: 5px;"><span class="red">*</span>注：只有普通客服才会被分配工单</span>
                    </div>

                </div>
            </div>
        </div>
        <div class="row-fluid" style="margin-top: 15px;">
            <div class="control-group">
                <label class="control-label" style="width: 104px"><span class="red">*</span>客服类型：</label>
                <div class="controls" style="margin-left: 105px">
                    <input id="newSubFlag" type="hidden" value="${user.subFlag}">
                    <div style="width:87px;float: left;text-align: left;">
						<span>
							<label  name="超级客服"><form:radiobutton path="subFlag" value="0" cssClass="required" data-subFlag="vipKeFu" cssStyle="zoom: 1.3"></form:radiobutton>超级客服</label>
						</span>
                    </div>
                    <div style="width:87px;float: left;text-align: left;">
						<span>
							<label name="自动客服"><form:radiobutton path="subFlag" value="4" cssClass="required" data-subFlag="autoKeFu" cssStyle="zoom: 1.3"></form:radiobutton>自动客服</label>
						</span>
                    </div>

                    <div style="width:90px;float: left;text-align: left;">
                        <span>
							<label name="突击客服"><form:radiobutton path="subFlag" value="3" cssClass="required" data-subFlag="tujiKeFu"  cssStyle="zoom: 1.3"></form:radiobutton>突击客服</label>
						</span>
                    </div>
                    <div style="width:100px;float: left;text-align: left;">
                        <span>
							<label name="普通客服"><form:radiobutton path="subFlag" value="2" cssClass="required" data-subFlag="puKeFu" cssStyle="zoom: 1.3"></form:radiobutton>普通客服</label>
						</span>
                    </div>
                    <div style="width:130px;float: left;text-align: left;">
                        <span>
							<label name="大客服"><form:radiobutton path="subFlag" value="5" cssClass="required" data-subFlag="daKeFu" cssStyle="zoom: 1.3"></form:radiobutton>大客服（含KA）</label>
						</span>
                    </div>
                    <div style="width:90px;float: left;text-align: left;">
                        <span>
							<label name="KA客服"><form:radiobutton path="subFlag" value="1" cssClass="required" data-subFlag="kaKeFu" cssStyle="zoom: 1.3"></form:radiobutton>KA客服</label>
						</span>
                    </div>

                </div>
            </div>
        </div>
        <div class="row-fluid" style="margin-top: 15px;">
            <div class="control-group">
                <label class="control-label" style="width: 104px"><span class="red">*</span>角色权限：</label>
                <div class="controls" style="margin-left: 105px">
                    <c:forEach items="${roles}" varStatus="i" var="role">
                        <div style="width:87px;float: left;text-align: left;">
                                <span>
                                    <input id="roleIdList_${role.id}" name="role" data-name="${role.name}" class="required" type="checkbox" value="${role.id}" style="zoom: 1.3;"><label for="roleIdList_${role.id}">${role.name}</label>
                                </span>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>


        <div class="row-fluid" style="margin-top: 15px;">
            <div class="control-group" id="divProductCategory">
                <label class="control-label" style="width: 104px"><span class="red">*</span>授权品类：</label>
                <div class="controls" style="margin-left: 104px;width: 90%;">
                    <c:forEach items="${fns:getProductCategories()}" var="category">
                        <div style="width: 15%;float: left;text-align: left;padding-bottom: 10px;">
                                <span>
                                    <input id="category_${category.id}" name="productCategoryIds" style="zoom: 1.4;" type="checkbox" value="${category.id}"><label for="category_${category.id}">${category.name}</label>
                                </span>
                        </div>
                    </c:forEach>

                </div>
            </div>
        </div>

        <div class="row-fluid" id="divCustomer" style="margin-top: 0px;">
            <div class="span6" style="margin-top: 0px;">

                <label class="control-label" style="width: 104px"><span class="red">*</span>VIP客户：</label>
                <div class="controls" style="margin-left: 104px;">
                    <div id="vipCustomerTree" class="input-block-level ztree" style="margin-top:3px;float:left;height:240px;width:690px;overflow:auto;margin-left: -3px">
                        <c:forEach items="${fns:getVipCustomerListFromMS()}" var="customer">
                            <div style="font-size: 14px;float: left;width: 50%;padding-bottom: 15px;">
                                <input type="checkbox" style="zoom: 1.4" value="${customer.id}" name="customerIds" id="${customer.id}">${customer.name}
                            </div>

                        </c:forEach>

                    </div>
                </div>

            </div>
        </div>
        <div class="row-fluid" id="divArea" style="margin-top: 15px;">
            <div class="span6" style="width:100%;margin-left: 2px;">
                <div class="control-group">
                    <label class="control-label" style="width: 104px;">负责区域：</label>
                    <div class="controls" style="margin-left: 0px;float: left;background-color:#F8F8F9;width: 697px">
                        <c:choose>
                            <c:when test="${userRegionNames.size() > 0}">
                                <a class="btn btn-primary" href="javascript:void(0);" id="update" style="margin:10px 0px 10px 10px" onclick="editUserRegion()"><img src="${ctxStatic}/images/md_update.png" style="margin-left: -5px;width: 15px;margin-right: 5px">修改</a>
                            </c:when>
                            <c:otherwise>
                                <a class="btn btn-primary" href="javascript:void(0);"  style="margin:10px 0px 10px 10px" onclick="editUserRegion()">＋添加</a>
                            </c:otherwise>
                        </c:choose>

                            <div  id="userRegion" style="margin:0px 10px 10px 10px;overflow:auto;">
                                <c:forEach items="${userRegionNames}" var="userRegionName">
                                    <div style="margin-top: 5px">${userRegionName}</div>

                                </c:forEach>
                            </div>

                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid" style="margin-top: 15px;">
            <div class="control-group">
                <label class="control-label" style="width: 104px">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
                <div class="controls" style="margin-left: 105px;">
                    <form:textarea path="remarks" htmlEscape="false" rows="2" maxlength="250" class="input-block-level" cssStyle="width: 697px;height: 60px;"/>
                </div>
            </div>
        </div>
    </div>
    <c:if test="${not empty user.id}">
        <legend>最近登录<div class="line_"></div></legend>

        <div style="padding: 20px;margin-top: -25px;margin-bottom: -25px">
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px">IP地址：</label>
                        <div class="controls" style="margin-left: 110px;">
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

    <div style="height: 80px;float: left"></div>
    <div id="editBtn" class="line-row">
        <div style="margin-left: 76%;">
            <shiro:hasPermission name="sys:user:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 96px;height: 40px;margin-top: 10px;margin-bottom: 10px"/>
                &nbsp;</shiro:hasPermission>
            <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 96px;height: 40px;margin-top: 10px;margin-left: 13px;margin-bottom: 10px"/>
        </div>
    </div>
</form:form>

<script type="text/javascript">

    $(document).ready(function() {

        <c:if test="${user != null && user.id != null}">
        var roleIdList = ${user.roleIdList};
        for(var i = 0;i < roleIdList.length; i++) {
            $("input[type='checkbox'][name='role'][value=" + roleIdList[i] + "]").attr("checked", true);
        }

        <c:if test="${user.productCategoryIds.size() > 0}">
        var productCategoryIds = ${user.productCategoryIds};
        for(var j = 0;j < productCategoryIds.length; j++) {
            $("input[type='checkbox'][name='productCategoryIds'][value=" + productCategoryIds[j] + "]").attr("checked", "checked");
        }
        </c:if>
        <c:if test="${user.customerIds.size() > 0}">
            var customerIds = ${user.customerIds};
        for(var k = 0;k < customerIds.length; k++) {
            $("input[type='checkbox'][name='customerIds'][value=" + customerIds[k] + "]").attr("checked", "checked");
        }
        </c:if>
        </c:if>

        var userRegionHight = document.getElementById("userRegion").offsetHeight;
        if(userRegionHight > 215){
            document.getElementById("userRegion").style.height= "220px";
        }

        var userId = $("#id").val();
        if(userId == null || userId == ''){
            $("[name='subFlag'][value='2']:radio").prop("checked", "true");
        }


    });


    setTimeout('editOffice()',100);
    editSubFlag();
    editManagerFlag();
</script>
</body>
</html>

<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/treeview.jsp" %>
    <title>业务管理</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
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
    </style>
    <script type="text/javascript">
        var orderdetail_index = parent.layer.getFrameIndex(window.name);

        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function () {
            var $btnSubmit = $("#btnSubmit");
            $("#loginName").focus();
            $("#inputForm").validate({
                rules: {
                    loginName: {remote: "${ctx}/sys/userKeFu/checkLoginName?oldLoginName=" + encodeURIComponent('${user.loginName}')}
                },
                messages: {
                    loginName: {remote: "用户登录名已存在"},
                    confirmNewPassword: {equalTo: "两次密码输入不一致"}
                },
                submitHandler: function (form) {
                    var roleIdList = $("input[name='roleIdList']:checked");
                    if(roleIdList.length <= 0){
                        layerError("请选择用户角色", "错误提示");
                        return false;
                    }
                    // var subUserNames = $('#subUserNames').text();
                    //
                    // if(subUserNames.length == 0){
                    //     layerError("请选择下属用户", "错误提示");
                    //     return false;
                    // }
                    var options = {
                        url: "${ctx}/sys/userSales/save",
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

            $("input[name='subFlag']").on("change",function(){
                editSubFlag();
            });
        });

        function editSubFlag() {
            var subFlag = $("input[name='subFlag']:checked").val();
            $('#newSubFlag').val(subFlag);
            if (subFlag > 2) {
                $("#userUnderling").show();
            } else {
                $("#userUnderling").hide();
            }
        }

        function editUserUnderling() {
            var name = $('#name').val();
            var subFlag = $("input[name='subFlag']:checked").val();
            var userId = $("#id").val();
            if(name.length == 0){
                layerError("请输入用户名称", "错误提示");
                return false;
            }

            var text = "添加下属用户";
            var url = "${ctx}/sys/userSales/userUnderling?userId="+ userId +"&subFlag=" + subFlag + "&userName=" + name + "&parentIndex=" + (orderdetail_index || '');
            var area = ['640px', '657px'];
            top.layer.open({
                type: 2,
                id:"userUnderling",
                zIndex:19891019,
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

        function refreshUserUnderling(subUserIds,userNames) {
            $('#subUserNames').text(userNames);
            $("#userSubIds").val(subUserIds);
            $("input[name=subFlag]").attr("disabled",true);

        }
        function child(obj) {
            var data = eval(obj);
            // 适应父容器高度
            $("#parentContainer").css("height", (data.height-100) + "px");
        }

    </script>

</head>
<body>

<br/>

<div style="height: 667px;overflow: auto;margin: -16px 0 0 0;" id="parentContainer">
    <form:form id="inputForm" modelAttribute="user" method="post" class="form-horizontal" cssStyle="width: 95%;margin: 10px auto;">
        <form:hidden path="id"/>
        <form:hidden path="userType"/>
        <sys:message content="${message}"/>
        <input type="hidden" name="salesCustomerIds" id="salesCustomerIds">
        <input type="hidden" name="userSubIds" id="userSubIds">
        <legend>用户信息<div class="line_"></div></legend>
        <div style="padding: 30px;margin-top: -25px;margin-bottom: -25px">
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><span class="red">*</span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <form:input path="name" htmlEscape="false" maxlength="30" class="required" cssStyle="width: 250px" placeholder="输入业务名称"/>
                        </div>
                    </div>
                </div>

                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><span class="red">*</span>登录帐号：</label>
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
                        <label class="control-label" style="width: 104px"><c:if test="${empty user.id}"><span class="red">*</span></c:if>密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <input id="newPassword" name="newPassword" type="password" value="" maxlength="20" minlength="6" placeholder="${empty user.id?'输入6位密码':''}"
                                   class="${empty user.id?'required':''}" style="width: 250px"/>
                            <c:if test="${not empty user.id}"><span class="help-inline">若不修改密码，请留空。</span></c:if>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><c:if test="${empty user.id}"><span class="red">*</span></c:if>确认密码：</label>
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
                        <label class="control-label" style="width: 104px"><span class="red">*</span>Q&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Q：</label>
                        <div class="controls" style="margin-left: 100px;">
                            <form:input path="qq" htmlEscape="false" maxlength="11" class="required"  cssStyle="width: 250px"/>

                        </div>
                    </div>
                </div>
            </div>
        </div>


        <%--其它信息--%>
        <legend>其他信息<div class="line_"></div></legend>
        <div style="width: 90%;padding: 30px;margin-top: -25px;">

            <div class="row-fluid">
                <div class="control-group" id="divOfficeFlag">
                    <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：</label>
                    <div class="controls" style="margin-left: 100px;">
                        <form:select path="office.id" class="input-xlarge required" cssStyle="width: 250px">
                            <form:option value="" label="请选择"/>
                            <form:options items="${offices}" itemLabel="name" itemValue="id" htmlEscape="false"/>
                        </form:select>

                    </div>
                </div>
            </div>
            <div class="row-fluid" style="margin-top: 10px">
                <div class="control-group">
                    <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>职&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;位：</label>
                    <div class="controls" style="margin-left: 100px;">
                         <span>
                           <label name="业务主管"> <form:radiobutton path="subFlag" value="3" cssClass="required" data-subFlag="kaKeFu" cssStyle="zoom: 1.3"></form:radiobutton>业务主管</label>
                        </span>
                        <span>
                           <label name="业务"> <form:radiobutton path="subFlag" value="1" cssClass="required" data-subFlag="kaKeFu" cssStyle="zoom: 1.3"></form:radiobutton>业务</label>
                        </span>
                        <span>
                          <label name="跟单">  <form:radiobutton path="subFlag" value="2" cssClass="required" data-subFlag="kaKeFu" cssStyle="zoom: 1.3"></form:radiobutton>跟单</label>
                        </span>
                            <input type="hidden" id="newSubFlag" name="newSubFlag" value="${user.subFlag}">
                    </div>
                </div>
            </div>
            <div id="userUnderling" class="row-fluid" style="margin-top: 10px">
                <div class="span6" style="width:105%;margin-left: 2px;">
                    <div class="control-group">
                        <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>下属用户：</label>
                        <div class="controls"  style="margin-left: 0px;float: left;background-color:#F8F8F9;width: 658px">
                            <c:choose>
                                <c:when test="${subUserNames.length() > 0}">
                                    <a class="btn btn-primary" href="javascript:void(0);" id="update" style="margin:10px 0px 10px 10px" onclick="editUserUnderling()"><img src="${ctxStatic}/images/md_update.png" style="margin-left: -5px;width: 15px;margin-right: 5px">修改</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="btn btn-primary" href="javascript:void(0);"  style="margin:10px 0px 10px 10px" onclick="editUserUnderling()">＋添加</a>
                                </c:otherwise>
                            </c:choose>

                            <div  id="subUserNames" style="margin:0px 10px 10px 10px;overflow:auto;">${subUserNames}</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row-fluid" style="margin-top: 15px;">
                <div class="control-group">
                    <label class="control-label" style="width: 104px"><span class="help-inline"><font color="red">*</font> </span>角色权限：</label>
                    <div class="controls" style="margin-left: 104px;width: 90%;">
                        <c:forEach items="${allRoles}" var="roles" varStatus="i">
                            <div style="width: 18%;height:25px;float: left;text-align: left;">
                                <span>
                                    <input id="roleIdList${i.index+1}" name="roleIdList" class="" type="checkbox" value="${roles.id}"><label for="roleIdList${i.index+1}">${roles.name}</label>
                                </span>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>


            <div class="row-fluid" style="margin-top: 15px;">
                <div class="control-group">
                    <label class="control-label" style="width: 104px">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
                    <div class="controls" style="margin-left: 105px;">
                        <form:textarea path="remarks" htmlEscape="false" rows="2" maxlength="250" class="input-block-level" cssStyle="width: 661px;height: 60px;"/>
                    </div>
                </div>
            </div>
        </div>

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
    <shiro:hasPermission name="sys:userSales:edit">
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
        </c:if>
    });
    editSubFlag();
</script>
</body>
</html>
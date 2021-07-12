<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>客户账号管理</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <meta name="decorator" content="default"/>
    <style type="text/css">
        .x {
            width: 49%;
            float: left;
            margin-top: 5px;
        }
        .form-horizontal .control-label {
            width: 199px;
        }
        .form-horizontal .controls {
            margin-left: 168px;
        }
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0;
            width: 100%;
            height: 60px;
            background: #fff;
            z-index: 10;
            padding-left: 190px;
            border-top: 1px solid #e5e5e5;
        }
    </style>
    <script type="text/javascript">
        var orderdetail_index = parent.layer.getFrameIndex(window.name);

        var this_index = top.layer.index;
        $(document).ready(function() {
            $("#loginName").focus();
            $("#inputForm").validate({
                rules: {
                    loginName: {remote: "${ctx}/customer/md/customerAccount/checkLoginName?oldLoginName=" + encodeURIComponent('${!empty user.id?user.loginName:''}') +"&expectId=${empty user.id?'0':'' + user.id}"}
                    ,mobile: {remote:{
                            type: "post",
                            url: "${ctx}/customer/md/customerAccount/checkMasterPhone",
                            data: {
                               id: function() {
                                   return $("#id").val();
                               },
                               phone: function() {
                                   return $("#mobile").val();
                               }
                            },
                            dataType: "json",
                            dataFilter:function(data) {
                                var data = eval('('+data+')');  //字符串转换成json
                                if (data.success == false){
                                    return false;
                                } else {
                                    return true;
                                }
                            }
                        }
                    }
                },
                messages: {
                    loginName: {remote: "客户账号人员登录名已存在"},
                    mobile: {remote: "此手机号已被注册了"},
                    confirmNewPassword: {equalTo: "输入与上面相同的密码"}
                },
                submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍等...');
                    var $btnSubmit = $("#btnSubmit");
                    $.ajax({
                        url: "${ctx}/customer/md/customerAccount/save",
                        type: "POST",
                        data: $(form).serialize(),
                        dataType: "json",
                        success: function (data) {
                            //提交后的回调函数
                            if (loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if (ajaxLogout(data)) {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                top.layer.close(loadingIndex);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if (pframe) {
                                    pframe.repage();
                                }
                                top.layer.close(this_index);//关闭本身
                            } else {
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
                            if (loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data, null, "数据保存错误，请重试!");
                            //var msg = eval(data);
                            top.layer.close(loadingIndex);
                        },

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

            $("#userType").change(function(){
                if($("#userType").val()==3){
                    $("input[name='customerAccountProfile.orderApproveFlag'][value='0']").prop("checked",true);
                    $("input[name='customerAccountProfile.orderApproveFlag']").prop("disabled",true);
                }else{
                    $("input[name='customerAccountProfile.orderApproveFlag'][value='1']").prop("checked",true);
                    $("input[name='customerAccountProfile.orderApproveFlag']").removeAttr('disabled');
                }
            });

            switchEvent("#spanOrderApproveFlag", function () {
                $("#orderApproveFlag").val(1)
            }, function () {
                $("#orderApproveFlag").val(0)
            });
        });

		function editCustomerShop() {
			var userId = $('#id').val();
			var customerId = $('#customerAccountProfile\\.customer\\.id').val();
			var name = $('#name').val();

			if(customerId == '' || customerId < 0){
				layerError("请先选择客户", "错误提示");
				return false;
			}
			if(name.length == 0){
				layerError("请输入账号姓名", "错误提示");
				return false;
			}

			var text = "关联客户店铺";
			var url = "${ctx}/customer/md/customerAccount/customerShop?userId="+ userId +"&customerId=" + customerId + "&name=" + name + "&parentIndex=" + (orderdetail_index || '');
			var area = ['640px', '657px'];
			top.layer.open({
				type: 2,
				id:"customerShop",
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

		// 关闭页面
		function cancel() {
			top.layer.close(this_index);// 关闭本身
		}
		function refreshCustomerShop(data) {
			$('#customerShopNames').text(data.customerShopNames);
			$("#customerShops").val(JSON.stringify(data.customerShops));
		}
	</script>
</head>
<body>
<form:form id="inputForm" modelAttribute="user" action="${ctx}/customer/md/customerAccount/save" method="post" class="form-horizontal" style="margin: 0px;">
    <form:hidden path="id"/>
    <form:hidden path="customerAccountProfile.id"/>
    <input type="hidden" id="customerShops" name="customerShops">
    <sys:message content="${message}"/>
    <div class="control-group x" style="margin-top: 54px">
        <label class="control-label"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
        <div class="controls" style="margin-left: 198px">
            <c:choose>
                <c:when test="${user.customerAccountProfile.customer != null && user.customerAccountProfile.customer.id > 0}">
                    <form:hidden path="customerAccountProfile.customer.id" />
                    <input id="customerName" name="customerAccountProfile.customer.name" type="text" class="span4" readonly="true" value="${user.customerAccountProfile.customer.name}" style="width: 236px;" />
                </c:when>
                <c:otherwise>
                    <form:select path="customerAccountProfile.customer.id" cssClass="required" style="width:250px;">
                        <form:option value="" label="请选择"/>
                        <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
                    </form:select>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <div class="control-group x" style="margin-top: 54px">
        <label class="control-label" style="width: 136px"><span class="red">*</span>帐号类型：</label>
        <div class="controls" style="margin-left: 137px">
            <c:choose>
                <c:when test="${currentuser.userType eq 3}">
                    <form:hidden path="userType" />
                    <input id="userTypeName" name="userTypeName" type="text" style="width: 236px;" readonly="true" value="${fns:getDictLabelFromMS(user.getUserType(), 'sys_user_type', '')}" />
                </c:when>
                <c:otherwise>
                    <form:select path="userType" cssClass="required" style="width:250px;">
                        <form:option value="" label="请选择"/>
                        <form:options items="${fns:getDictInclueListFromMS('sys_user_type','3,4,9')}" itemLabel="label" itemValue="value" htmlEscape="false" /><%-- 切换为微服务 --%>
                    </form:select>

				</c:otherwise>
			</c:choose>
		</div>
	</div>
	<div style="width: 99%;float: left">
		<div class="control-group x">
			<label class="control-label"><span class="red">*</span>登录帐号：</label>
			<div class="controls" style="margin-left: 198px">
				<input id="oldLoginName" name="oldLoginName" type="hidden" value="${user.loginName}">
				<input type="text" id="loginName" name="loginName" maxlength="30" class="required" value="${user.loginName}" autocomplete="off" style="width: 236px;"/>

            </div>
        </div>
        <div class="control-group x">
            <label class="control-label" style="width: 142px"><span class="red">*</span>姓&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp名：</label>
            <div class="controls" style="margin-left: 137px">
                <form:input path="name" htmlEscape="false" maxlength="10" class="required" style="width: 236px;"/>
            </div>
        </div>
    </div>
    <div style="width: 99%;float: left">
        <div class="control-group x">
            <label class="control-label"><c:if test="${empty user.id}"><span class="red">*</span></c:if>密&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp码：</label>
            <div class="controls" style="margin-left: 198px">
                <input id="newPassword" name="newPassword" type="password" value="" maxlength="20" minlength="6" class=" ${empty user.id?'required':''}" style="width: 236px;"/>
            </div>
        </div>
        <div class="control-group x">
            <label class="control-label" style="width: 141px">确认密码：</label>
            <div class="controls" style="margin-left: 137px">
                <input id="confirmNewPassword" name="confirmNewPassword" type="password" value="" maxlength="20" minlength="6" equalTo="#newPassword" style="width: 236px;"/>
            </div>
        </div>
    </div>
    <div class="control-group x">
        <label class="control-label"><c:if test="${empty user.id}"><span class="red">*</span></c:if>手&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp机：</label>
        <div class="controls" style="margin-left: 198px">
<%--            <form:input path="mobile" htmlEscape="false" maxlength="11" class="mobile required" style="width: 236px;"/>--%>
            <input id="mobile" name="mobile" htmlEscape="false" type="tel" value="" maxlength="11" class=" ${empty user.id?'required mobile':'mobile'}" style="width: 236px;"/>

        </div>
    </div>
    <div class="control-group x">
        <label class="control-label" style="width: 136px">电&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp话：</label>
        <div class="controls" style="margin-left: 137px">
<%--            <form:input path="phone" htmlEscape="false" maxlength="16" cssClass="phone" style="width: 236px;"/>--%>
            <input id="phone" name="phone" htmlEscape="false" type="tel" value="" maxlength="16" class="phone" style="width: 236px;"/>
        </div>
    </div>
    <div class="control-group x" style="width: 100%">
        <label class="control-label">邮&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp箱：</label>
        <div class="controls">
            <form:input path="email" htmlEscape="false" maxlength="100" cssClass=" email" style="width:236px;"/>
        </div>
    </div>

    <div class="control-group x" style="width: 99%">
        <label class="control-label"><span class="red">*</span>订单审核：</label>
        <div class="controls">
            <c:choose>
                <c:when test="${not empty user.id}">
                    <c:choose>
                        <c:when test="${user.customerAccountProfile.orderApproveFlag==0}">
                                <span class="switch-off" id="spanOrderApproveFlag"
                                      style="zoom: 0.7;margin-top: 4px"></span>
                        </c:when>
                        <c:otherwise>
                                <span class="switch-on" id="spanOrderApproveFlag"
                                      style="zoom: 0.7;margin-top: 4px"></span>
                        </c:otherwise>
                    </c:choose>
                    <input type="hidden" value="${user.customerAccountProfile.orderApproveFlag}" name="customerAccountProfile.orderApproveFlag"
                           id="orderApproveFlag">
                </c:when>
                <c:otherwise>
                    <span class="switch-off" id="spanOrderApproveFlag" style="zoom: 0.7;margin-top: 4px"></span>
                    <input type="hidden" value="0" name="customerAccountProfile.orderApproveFlag" id="orderApproveFlag">
                </c:otherwise>
            </c:choose>
            <span class="help-inline" style="margin-top: -11px;font-size: 12px">开启后下单需主账号审核通过后才能生效</span>
        </div>

	</div>
	<div class="control-group x" style="width: 99%">
			<label class="control-label"><span class="help-inline"></span>关联店铺：</label>
			<div class="controls"  style="margin-left: 0px;float: left;background-color:#F8F8F9;width: 658px">
				<c:choose>
					<c:when test="${customerShopNames.length() > 0}">
						<a class="btn btn-primary" href="javascript:void(0);" id="update" style="margin:10px 0px 10px 10px" onclick="editCustomerShop()"><img src="${ctxStatic}/images/md_update.png" style="margin-left: -5px;width: 15px;margin-right: 5px">修改</a>
					</c:when>
					<c:otherwise>
						<a class="btn btn-primary" href="javascript:void(0);"  style="margin:10px 0px 10px 10px" onclick="editCustomerShop()">＋添加</a>
					</c:otherwise>
				</c:choose>
				<div  id="customerShopNames" style="margin:0px 10px 10px 10px;overflow:auto;">${customerShopNames}</div>
			</div>
	</div>
	<div class="control-group x" style="width: 99%">
		<label class="control-label" >备&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp注：</label>
		<div class="controls">
			<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" style="width:664px;"/>
		</div>
	</div>
	<c:if test="${not empty user.id}">
		<div class="row-fluid">

            <div class="control-group x">
                <label class="control-label">创建时间：</label>
                <div class="controls">
                    <input style="width: 236px;" type="text" readonly="readonly" class="required valid" value="<fmt:formatDate value="${user.createDate}" type="both" dateStyle="full"/>">
                </div>
            </div>


            <div class="control-group x">
                <label class="control-label" style="width: 136px">最后登录IP：</label>
                <div class="controls" style="margin-left: 137px">
                    <input style="width: 236px;" type="text" readonly="readonly" class="required valid" value="${user.loginIp}">
                </div>
            </div>


            <div class="control-group x">
                <label class="control-label">最后登录：</label>
                <div class="controls">
                    <input style="width: 236px;" type="text" readonly="readonly" class="required valid" value="<fmt:formatDate value="${user.loginDate}" type="both" dateStyle="full"/>">
                </div>
            </div>

        </div>
    </c:if>
    <div style="float: left;width: 99%;height:61px;">

    </div>
    <div>
        <div id="editBtn" class="line-row">
            <div class="control-group">
                <label class="control-label"></label>
                <div class="controls" style="margin-left: 370px;float: left;margin-top: 10px">
                    <shiro:hasPermission name="md:customeraccount:edit">
                        <input id="btnSubmit" class="btn btn-primary" type="submit"
                               style="width: 96px;height: 40px;"
                               value="保 存"/>&nbsp;</shiro:hasPermission>

                    <input style="margin-left: 20px;width: 96px;height: 40px;" id="btnCancel"
                           class="btn"
                           type="button" value="取 消" onclick="cancel()"/>

                </div>
            </div>
        </div>
    </div>
</form:form>
<script type="text/javascript">
    $(window).load(function() {
        //解决一些浏览器自动将登录帐号覆盖现有控件
        setTimeout(resetform, 2000);
    });
    function resetform(){
        $("#loginName").val("${user.loginName}");
        $("#loginName").text("${user.loginName}");
        $("#newPassword").val("");
        top.$.jBox.closeTip();
    }
</script>
</body>
</html>
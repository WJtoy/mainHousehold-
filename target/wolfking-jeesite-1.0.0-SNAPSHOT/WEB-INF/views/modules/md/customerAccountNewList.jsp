<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>客户账号管理</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<style type="text/css">
		.sort{color:#0663A2;cursor:pointer;}
		.input-large {
			width: 200px;
		}
		.table thead th,.table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
		}
		.form-search label {
			 margin-left: 0px;
		}
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		top.layer.closeAll();
		$(document).ready(function() {
			$('a[data-toggle=tooltip]').darkTooltip();

			// 表格排序
			var orderBy = $("#orderBy").val().split(" ");
			$("#contentTable th.sort").each(function(){
				if ($(this).hasClass(orderBy[0])){
					orderBy[1] = orderBy[1]&&orderBy[1].toUpperCase()=="DESC"?"down":"up";
					$(this).html($(this).html()+" <i class=\"icon icon-arrow-"+orderBy[1]+"\"></i>");
				}
			});
			$("#contentTable th.sort").click(function(){
				var order = $(this).attr("class").split(" ");
				var sort = $("#orderBy").val().split(" ");
				for(var i=0; i<order.length; i++){
					if (order[i] == "sort"){order = order[i+1]; break;}
				}
				if (order == sort[0]){
					sort = (sort[1]&&sort[1].toUpperCase()=="DESC"?"ASC":"DESC");
					$("#orderBy").val(order+" DESC"!=order+" "+sort?"":order+" "+sort);
				}else{
					$("#orderBy").val(order+" ASC");
				}
				page();
			});

            $("#btnSubmit").on("click", function(){
                $("#pageNo").val(1);
                var loginName = $("#loginName").val();
                var name = $("#name").val();
                var userType = $("#userType").val();
                var selCustomer = true;
				<c:choose>
					<c:when test="${fns:getUser().isCustomer()}">
                		selCustomer = false;
					</c:when>
					<c:when test="${fns:getUser().isSaleman()}">
						if (name.length >0 || loginName.length >0 || (name.length >0 && userType != 0) || (loginName.length >0 && userType >0)) {
							selCustomer = false;
						}
					</c:when>
                	<c:otherwise>
                		selCustomer = false;
					</c:otherwise>
				</c:choose>

                var val = $("#customerAccountProfile\\.customer\\.id").val();
                if ((val == undefined || val.length == 0) && selCustomer ==true) {
                    layerInfo("请选择客户!", "信息提示");
                    return false;
                }
                var url = "${ctx}/md/customerAccount/list";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });


			$('label').on({
				mouseenter:function(){
					var that = this;
					var className = $(this).attr("class");
					if("stop"==className){
						tips =layer.tips("<span style='color:#fff;'>停用</span>",that,{tips:[1,'#3E3E3E'],time:0,area: 'auto',maxWidth:500});
					}
				},
				mouseleave:function(){
					var className = $(this).attr("class");
					if("stop"==className){
						layer.close(tips);
					}
				}
			});
		});

		function disableOrEnable(line,obj,flag,userId,loginName,customerId){
			var content;
			var url;
			var msg;
			var title;
			// 停用

			var delFlag = $("#tr_"+line+" .delFlag").val();
			if (delFlag == 1) {
				title = "启用";
				content = '确认要启用【'+loginName+'】账号吗？';
				url = "${ctx}/md/customerAccount/enable?id="+userId+"&loginName="+loginName+"&customerAccountProfile.customer.id=" + customerId;

				msg = '正在启用账号，请稍等...'
			} else {
				title = "停用";
				content = '确认要停用【'+loginName+'】账号吗？';
				url = "${ctx}/md/customerAccount/delete?id="+userId+"&loginName="+loginName+"&customerAccountProfile.customer.id=" + customerId;
				msg = '正在停用账号，请稍等...'
			}
			layer.confirm(
					content,
					{
						btn: ['确定','取消'], //按钮
						title:'提示',
						cancel: function(index, layero){
							// 右上角叉
							if ($(obj).attr("class") == 'switch-off') {
								honeySwitch.showOn(obj);
							} else {
								honeySwitch.showOff(obj);
							}
						}
					}, function(index){
						layer.close(index);//关闭本身
						var loadingIndex = top.layer.msg(msg, {
							icon: 16,
							time: 0,//不定时关闭
							shade: 0.3
						});
						$.ajax({
							url: url,
							success:function (data) {
								// 提交后的回调函数
								if(loadingIndex) {
									setTimeout(function () {
										layer.close(loadingIndex);
									}, 2000);
								}
								if (data.success) {
									layerMsg(data.message);

									// 停用
									if (delFlag == 1) {
										$("#tr_"+line+" .delFlag").val(0);
										$("#tr_"+line+" .stop").css("color","");
										$("#tr_"+line+" .stop").attr("class", "noStop");

									} else {
										$("#tr_"+line+" .delFlag").val(1);
										$("#tr_"+line+" .noStop").css("color","red");
										$("#tr_"+line+" .noStop").attr("class", "stop");
									}



								} else {
									layerError("账号"+title+"失败:" + data.message, "错误提示");
									// 取消操作
									if ($(obj).attr("class") == 'switch-off') {
										honeySwitch.showOn(obj);
									} else {
										honeySwitch.showOff(obj);
									}
								}
								return false;
							},
							error: function (data) {
								ajaxLogout(data,null,"数据保存错误，请重试!");
								// 取消操作
								if ($(obj).attr("class") == 'switch-off') {
									honeySwitch.showOn(obj);
								} else {
									honeySwitch.showOff(obj);
								}
							},
						});
						return false;
					}, function(){
						// 取消操作
						if ($(obj).attr("class") == 'switch-off') {
							honeySwitch.showOn(obj);
						} else {
							honeySwitch.showOff(obj);
						}
					});
		}

		function editCustomerAccount(type,id) {
			var text = "添加子账号";
			var url = "${ctx}/md/customerAccount/form";
			var area = ['1000px', '580px'];
			if(type == 20){
				text = "修改子账号";
				area = ['1000px', '676px'];
				url = "${ctx}/md/customerAccount/form?id=" + id;
			}
			top.layer.open({
				type: 2,
				id:"user",
				zIndex:19,
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
		function resetPassword(id){
            if (id == undefined || id=='' || id == '0') {
                layerError('参数错误', '错误提示',true);
                return false;
            }
            top.layer.confirm("重置密码后，需要重新登录，<br/>确定要<font color='blue'>重置</font>密码吗?", {icon: 3, title:'系统确认'}, function(index){
                top.layer.close(index);//关闭本身
                // do something
                var data = {id:id};
                $.ajax({
                    cache: false,
                    type: "POST",
                    url: "${ctx}/md/customerAccount/resetPassword",
                    data: data,
                    success: function (data) {
                        if (data.success){
                            layerMsg("密码已重置成功。<br/>新的密码就是您的手机号<font color='#ff4500'>后六位</font>。",true)
                        }
                        else{
                            layerError(data.message,"错误提示",true);
                        }
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        layerError(thrownError,"错误提示",true);
                    }
                });
            },function(index){
                //cancel
            });
            return false;
        }

	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><br></li><li class="active"><a href="javascript:;">客户账号列表</a></li>
	</ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<c:set var="mdcustomerId" value="${currentuser.isCustomer()?currentuser.getCustomerAccountProfile().getCustomer().getId():user.getCustomerAccountProfile().getCustomer().getId()}"></c:set>
	<form:form id="searchForm" modelAttribute="user" action="${ctx}/md/customerAccount/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="20"/>
		<input id="orderBy" name="orderBy" type="hidden" value="${page.orderBy}"/>
			<c:choose>
				<c:when test="${currentuser.isCustomer()}">
					<li>
						<label>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
						<form:hidden path="customerAccountProfile.customer.id"/>
						<form:input path="customerAccountProfile.customer.name" readonly="true"/>
					</li>
				</c:when>
				<c:otherwise>
					<li>
						<label>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
						<form:select path="customerAccountProfile.customer.id" class="input-large">
							<form:option value="" label="所有"/>
							<form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
						</form:select>
					</li>
				</c:otherwise>
			</c:choose>
			<li>
				<label style="margin-left: 28px">登录账号：</label><form:input path="loginName" htmlEscape="false" maxlength="30" class="input-small" style="width:186px"/>
			</li>
			<li>
				<label style="margin-left: 28px">姓&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp名：</label><form:input path="name" htmlEscape="false" maxlength="30" class="input-small" style="width:186px"/>
			</li>
		    <li>
			    <label style="margin-left: 28px">联系电话：</label><input id="phone" name="phone" class="input-small" type="text" value="${user.phone}" maxlength="30" style="width:186px">
		    </li>
			<li>
				<label style="margin-left: 28px">账号类型：</label>
				<form:select path="userType" style="width:200px;">
					<form:option value="0" label="所有"/>
					<form:option value="3" label="主帐号"/>
					<form:option value="4" label="子帐号"/>
					<form:option value="9" label="查询帐号"/>
				</form:select>
			</li>
			<li class="btns">
				<input style="margin-left: 6px;width: 80px;height: 32px" id="btnSubmit" class="btn btn-primary" type="submit" value="查询" />
			</li>
			<li class="clearfix"></li>
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 100px;height: 30px" onclick="editCustomerAccount(10,null)">
		<i class="icon-plus-sign"></i>&nbsp;添加子账号
	</button>
	<table id="contentTable"  class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="49">序号</th>
				<th width="315">客户</th>
				<th width="182">登录账号</th>
				<th width="120">姓名</th>
				<th width="160">账号类型</th>

				<th width="160">订单审核</th>
				<th width="104">启用</th>
				<shiro:hasPermission name="md:customer:edit">
					<th width="200">操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${page.list}" var="user">
			<c:set var="index" value="${index+1}" />
			<tr id="tr_${index}">
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${user.customerAccountProfile.customer.name}</td>
				<td>
					<c:choose>
						<c:when test="${user.delFlag == 1}">
							<label class="stop" style="color: red;">${user.loginName}</label>
						</c:when>
						<c:otherwise>
							<label class="noStop">${user.loginName}</label></c:otherwise>
					</c:choose>
				</td>
				<td>${user.name}</td>
				<td>${not empty user.userTypeName?user.userTypeName:'账号类型错误'}</td><%-- 切换为微服务 --%>

				<td>
					<c:choose>
						<c:when test="${user.customerAccountProfile.orderApproveFlag == 1}">
							<label> 开启</label>
						</c:when>
						<c:otherwise>
							<label style="color: red;">关闭</label>
						</c:otherwise>
					</c:choose>
				</td>
				<td>
					<c:choose>
						<c:when test="${user.delFlag == 0}">
							<span class="switch-on"  style="zoom: 0.7"  onclick="disableOrEnable('${index}',this,'${user.delFlag}','${user.id}','${user.loginName}','${mdcustomerId}')"></span>
							<input type="hidden" value="${user.delFlag}" name="userDelFlag" id="userDelFlag" class="delFlag">
						</c:when>
						<c:when test="${user.delFlag == 1}">
							<span class="switch-off"  style="zoom: 0.7"  onclick="disableOrEnable('${index}',this,'${user.delFlag}','${user.id}','${user.loginName}','${mdcustomerId}')"></span>
							<input type="hidden" value="${user.delFlag}" name="userDelFlag" id="userDelFlag" class="delFlag">
						</c:when>
					</c:choose>
				</td>
				<shiro:hasPermission name="md:customer:edit">
				<td>
    				<a href="javascript:editCustomerAccount(20,'${user.id}')">修改</a>

					<a style="margin-left: 16px" href="javascript:;" onclick="resetPassword('${user.id}');">重置密码</a>
				</td>
				</shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>

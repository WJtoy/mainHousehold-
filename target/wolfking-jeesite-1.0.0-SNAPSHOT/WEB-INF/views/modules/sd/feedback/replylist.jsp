<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>${feedback.feedFrom}问题反馈 【${feedback.order.orderNo}】</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
	<link type="text/css" href="${ctxStatic}/weixin/css/weixin.css" rel="stylesheet">
	<script type="text/javascript" src="${ctxStatic}/scroll/jquery.slimscroll.min.js"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/feedback/tpl/replayContent.html" %>
	<script src="${ctxStatic}/common/Utils.js" type="text/javascript"></script>
	<!-- 禁用词 -->
	<md:filterDisabledWord />
	<script type="text/javascript">
        Order.rootUrl = "${ctx}";
        var this_index = top.layer.index;
	</script>
	<style type="text/css">
		.imgfile
		{
			max-width: 150px;
			max-height:150px;
		}
	</style>
</head>
<body>
<input type="hidden" id="attachCount" name="attachCount" value="${feedback.attachmentCount}" />
<c:set var="currentuser" value="${fns:getUser() }" />
<div style="height: 517px;" class="chatContainer">
	<div style="left: 0px; top: 0px;" class="chatMainPanel" id="chatMainPanel">
		<%--<div class="chatTitle">--%>
			<%--<div class="chatNameWrap">--%>
				<%--<p style="opacity: 1;" class="chatName" id="messagePanelTitle">${feedback.title}</p>--%>
			<%--</div>--%>
		<%--</div>--%>
		<div style="top: 0px; position: relative;" id="chat_chatmsglist" class="chatContent">
			<!-- context of sender -->
			<div class="chatItem ${ (feedback.createBy.userType eq 3 || feedback.createBy.userType eq 4 || feedback.createBy.userType eq 9)?'you':'me'}">
				<div class="chatItemContent">
					<div class="sign">
						<div class="avatar phone"></div>
						<div class="title">${feedback.createBy.name}</div>
					</div>
					<div class="cloud cloudText">
						<div class="cloudPannel" style="">
							<div class="cloudArrow "></div>
							<div class="cloudBody">
								<div class="cloudContent">
									<pre style="white-space:pre-wrap">${feedback.remarks}</pre>
									<pre class="time" style="height:15px;"> <fmt:formatDate
											value="${feedback.createDate}" pattern="yyyy-MM-dd  HH:mm" />
                                        </pre>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!--list-->
			<c:forEach items="${feedback.items}" var="item">
				<div class="chatItem ${(item.userType == 0)?'you':'me'}">
					<div class="chatItemContent">
						<div class="sign">
							<div class="avatar phone"></div>
							<div class="title">${item.createBy.name}</div>
						</div>
						<div class="cloud cloudText">
							<div class="cloudPannel" style="">
								<div class="cloudArrow "></div>
								<div class="cloudBody">
									<div class="cloudContent">
										<c:if test="${item.contentType == 0}"><pre style="white-space:pre-wrap">${item.remarks}</pre></c:if>
										<c:if test="${item.contentType == 1}">
											<div class="attach">
												<a href="javascript:;" onclick="viewPhoto('${ctxUpload}/${item.remarks}');">
													<img title="${attach.remarks}" src="${ctxUpload}/${item.remarks}" >
												</a>
												<%--<a href="${ctxUpload}/${item.remarks}" class="fancybox">--%>
													<%--<img src="${ctxUpload}/${item.remarks}" />--%>
												<%--</a>--%>
											</div>
										</c:if>
										<pre class="time" style="height:15px;"><font>${item.createDateString}</font></pre>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</c:forEach>
			<!--list end -->
		</div>
		<!--</div> scroll -->
		<div style="height: 55px;" id="chat_editor" class="chatOperator">
			<form:form id="inputForm" modelAttribute="feedback" action="#"
					   method="post" class="form-search">
				<input id="feedback.id" name="feedback.id" type="hidden"
					   value="${feedback.id}" />
				<input id="floor" name="floor" type="hidden"
					   value="${feedback.items.size()+1}">
				<shiro:lacksPermission name="sd:feedback:edit">
					<c:set var="canReply" value="false" />
					<c:set var="canClose" value="false" />
				</shiro:lacksPermission>
				<div style="" class="inputArea">
					<table width="100%" border="0">
						<tr>
							<td width="*">
                                    <textarea style="height: 20px;width:93%; overflow: hidden;" type="text"
											  id="remarks" name="remarks" maxlength="255"
											  class="chatInput lightBorder"></textarea>
							</td>
							<td width="260px" align="left" style="vertical-align: middle;">
								<a href="javascript:;" id="btnSend" class="btn btn-success"
								   onclick="Order.addFeedbackReply('${feedback.id}','${feedback.quarter}');"><b>回复</b>
								</a>
								<c:if test="${handleFeedback == true }">
									<a href="javascript:;" id="btnHandled" class="btn btn-success"
									   onclick="Order.handled('${feedback.order.id}','${feedback.quarter}');" title="点击此按钮，确认异常处理完成"><b>已处理</b>
									</a>
								</c:if>
								<c:if test="${feedback.attachmentCount<5}">
										<!-- 限制上传图片数量 -->
										<shiro:hasPermission name="sd:feedback:pic">
										<a id="btnAttach" href="javascript:void(0);"
										   class="btn btn-success" onclick="Order.addReplyAttach('${feedback.id}','${feedback.quarter}',this_index)">
											<b>上传附件</b>
										</a>
									</shiro:hasPermission>
								</c:if>
							</td>
						</tr>
					</table>
				</div>
			</form:form>
		</div>
	</div>
</div>
<script type="text/javascript">
    $(function(){
        $('#chat_chatmsglist').slimscroll(
            {
                height : '460px'
            });

        $("#remarks").focus();

        $('.fancybox').fancybox({
            openEffect : 'none',
            closeEffect : 'none'
        });
    });

    function closeFeedback()
    {
        if ($("#btnClose").prop("disabled") == true)
        {
            return false;
        }
        top.layer.confirm('确认关闭反馈吗?', {icon: 3, title:'系统确认'}, function(index){
            top.layer.close(index);//关闭本身
            // do something
            $("#inputForm").prop("action", "${ctx}/sd/feedback/close");
            $("#inputForm").submit();
        });
        return false;
    }

    // 添加附件后回调
    function addReplyItem(data)
    {
        Order.addReplyItem(data);
        var qty = $("#attachCount").val();
        if(Utils.isEmpty(qty)){
            $("#attachCount").val(1);
        }else{
            var iqty = parseInt(qty);
            if(iqty>=4){
                $("#btnAttach").hide();
            }
        }
    }
    var h = $(top.window).height()-20;
    var w = $(top.window).width()-20;

    function viewPhoto(url){

        var view_index = top.layer.open({
            id:'layer_viewphoto',
            zIndex:19891018,
            type: 1,
            title: false,
            closeBtn: 1,
            shade:0.3,
            area: [w+'px',h+'px'],
            shadeClose: true,
            content: '<img src="'+ url + '">'
        });
    }
</script>
<c:if test="${ (feedback.replyFlag ==1 and (currentuser.isCustomer() or currentuser.isSaleman()) ) or (feedback.replyFlag ==2 and currentuser.isKefu())}">
<script type="text/javascript">
    Order.readReply('${feedback.id}','${feedback.quarter}');
</script>
</c:if>
</html>
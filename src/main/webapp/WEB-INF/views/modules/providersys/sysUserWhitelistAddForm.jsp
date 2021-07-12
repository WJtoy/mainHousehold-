<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	 <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <title>添加白名单</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        var officeTree;
        var selectedTree;//zTree已选择对象
        var hiddenNodes=[];
        var selectedCount=0;

        var pre_ids = [];
        var ids = [];
		$(document).ready(function() {
            officeTree = $.fn.zTree.init($("#userTree"), setting, officeNodes);
            selectedTree = $.fn.zTree.init($("#selectedTree"), setting, "");
           /* $.fn.zTree.init($("#userTree"), setting, "");*/
            $("#inputForm").validate({
				submitHandler: function(form){
                    var ids = [], nodes = selectedTree.getNodes();
                    for(var i=0; i<nodes.length; i++) {
                        ids.push(nodes[i].id);
                    }
                    $("#userIds").val(ids);
                    var userIds = $("#userIds").val();
                    if(userIds==null || userIds==''){
                        layerError("请至少选择一名用户", "错误提示");
                        return false;
					}
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
                    $.ajax({
                        url:"${ctx}/provider/sys/userWhitelist/addWhitelist",
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
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
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

            $("#key").keyup(function () {      // 按键弹起时触发的事件；
                filter();
            });
        });

        var setting = {view: {selectedMulti:false,nameIsHTML:true,showTitle:false,dblClickExpand:false},
            data: {simpleData: {enable: true}},
            callback: {onClick: treeOnClick}};

        //点击选择项回调
        function treeOnClick(event, treeId, treeNode, clickFlag){
            $.fn.zTree.getZTreeObj(treeId).expandNode(treeNode);
       /*     if("officeTree"==treeId){
                $.get("${ctx}/provider/sys/userWhitelist/findUserTree?officeId=" + treeNode.id, function(userNodes){
                    $.fn.zTree.init($("#userTree"), setting, userNodes);
                });
            }*/
            if("userTree"==treeId){
                //alert(treeNode.id + " | " + ids);
                //alert(typeof ids[0] + " | " +  typeof treeNode.id);
                if($.inArray(String(treeNode.id), ids)<0){
                    selectedTree.addNodes(null, treeNode);
                    ids.push(String(treeNode.id));
                    selectedCount = selectedCount+1;
                    $("#selectedCount").text(selectedCount)
                }
            };
            if("selectedTree"==treeId){
                selectedTree.removeNode(treeNode);
                ids.splice($.inArray(String(treeNode.id), ids), 1);
                if(selectedCount>0){
                    selectedCount = selectedCount-1;
                    $("#selectedCount").text(selectedCount)
                }
            }
        };
        var officeNodes=[
            <c:forEach items="${userList}" var="user">
            {id:"${user.id}",
                pId:"0",
                name:"${user.name}"},
            </c:forEach>];

        function clearWhitelist() {
            layer.confirm('确定要清除已选白名单吗?', {
                btn : [ '确定', '取消' ]//按钮
            }, function(index) {
                layer.close(index);
               //officeTree = $.fn.zTree.init($("#userTree"), setting, officeNodes);
				pre_ids = [];
				ids = [];
                $("#selectedCount").text("0")
                selectedCount=0;
                $.fn.zTree.init($("#selectedTree"), setting, "");
            });
        }

        //过滤ztree显示数据
        function filter(){
            //var zTreeObj =officeTree;
            var zTreeObj = $.fn.zTree.getZTreeObj("userTree");
            //显示上次搜索后背隐藏的结点
            zTreeObj.showNodes(hiddenNodes);
            //查找不符合条件的叶子节点
            var _keywords=$("#key").val();
            function filterFunc(node){
                if(node.isParent||node.name.indexOf(_keywords)!=-1) return false;
                return true;
            };
            //用于展开树 原来的点击搜索不会展开
            if(_keywords.length>0){
                zTreeObj.expandAll(true);
            }else{
                zTreeObj.expandAll(false);
            }
            //获取不符合条件的叶子结点
            hiddenNodes=zTreeObj.getNodesByFilter(filterFunc);
            //隐藏不符合条件的叶子结点
            zTreeObj.hideNodes(hiddenNodes);
        };
  
        function cancel() {
            top.layer.close(this_index);//关闭本身
        }
	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
		  .form-horizontal {margin-top: 5px;}
		  .form-horizontal .control-label {width: 90px;}
		  .form-horizontal .controls {margin-left: 90px;}
	  </style>
  </head>
  <body>
    <sys:message content="${message}"/>
	<form:form id="inputForm" modelAttribute="sysUserWhiteList" action="" method="post" class="form-horizontal">
		<div class="row-fluid" style="margin-top: 5px">
			<div class="span12">
				<div class="control-group">
					<label class="control-label"><font style="color: red">*</font>到期日期：</label>
					<div class="controls">
						<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:120px;margin-left:4px"
							   maxlength="20" class="input-small Wdate required"
							   value=""
							   onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid" style="margin-top: 10px;margin-left: 8px">
			<div class="span6" style="border: solid 1px #DCDEE2;height: 530px;overflow-x:auto;">
				<input id="key" placeholder="请输入..." class="fromInput" style="margin-left: 40px;margin-top: 5px;width: 150px">
				<div id="userTree" class="ztree" style="margin-left: 40px"></div>
			</div>
			<%--<div class="span4" style="border: solid 1px #DCDEE2;border-left: none;height: 530px;margin-left: 0px;overflow-x:auto;">
				<input id="key" placeholder="请输入..." class="fromInput" style="margin-left: 40px;margin-top: 5px">
				<div id="userTree" class="ztree" style="margin-left: 40px"></div>
			</div>--%>
			<div class="span6" style="border: solid 1px #DCDEE2;border-left: none;height: 530px;margin-left: 0px;overflow-x:auto;">
				<div style="margin-left: 30px;margin-top: 10px"><span>已加入白名单</span><span style="float: right;font-size: 12px;padding-top: 5px;padding-right: 10px">已选择<span style="color: red" id="selectedCount">0</span>个</span></div>
				<div id="selectedTree" class="ztree" style="margin-left: 40px"></div>
			</div>
		</div>
		<input id="userIds" name="userIds" type="hidden">
	</form:form>

	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
		<div style="float: right;margin-top: 10px;margin-right: 20px">
           <shiro:hasPermission name="sys:whitelist:edit">
			   <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 76px;height: 35px" onclick="$('#inputForm').submit()" value="保 存"/>
			   <input id="btnClear" class="btn btn-warning" type="button" style="margin-right: 5px;width: 76px;height: 35px" onclick="clearWhitelist()" value="清 除"/>
		   </shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 76px;height: 35px" onclick="cancel()"/>
		</div>
	</div>
	<%--<shiro:hasPermission name="md:producttype:edit">
		<div style="background: white;height: 50px;position: absolute;bottom: 5px;width: 100%">
			<input id="btnSubmit" class="btn btn-primary" type="submit" style="margin-left: 570px;margin-top:10px" onclick="$('#inputForm').submit()" value="保 存"/>
		</div>
	</shiro:hasPermission>--%>
  </body>
</html>

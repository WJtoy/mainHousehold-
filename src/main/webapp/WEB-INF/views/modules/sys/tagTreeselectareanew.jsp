<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.net.*" %>
<%
    String nodesLevel="0";

    if(request.getParameter("nodesLevel")!=null){
        nodesLevel=request.getParameter("nodesLevel");
    }
%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp"%>
    <title>数据选择</title>
    <meta name="decorator" content="blank"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <script src="${ctxStatic}/area/tree-area.js"></script>
    <script type="text/javascript">
        var index = parent.layer.getFrameIndex(window.name);
        var treeArea = TreeArea('${ctx}');
        var key, lastValue = "", nodeList = [], type = getQueryString("type", "${url}");
        var tree, setting = {view:{selectedMulti:false,dblClickExpand:false},check:{enable:"${checked}",nocheckInherit:true},
            async:{enable:(type==3),url:"${ctx}/sys/user/treeDataNew",autoParam:["id=officeId"]},
            data:{simpleData:{enable:true}},callback:{
                onClick:function(event, treeId, treeNode){
                    var nameLevel = ${nameLevel eq null ? "3" : nameLevel};
                    if (treeNode.type == 3){  //type=3为市
                        // console.log("单击市后获取区/县列表");
                        treeArea.obtainDistrictDataByCity(tree, treeNode);
                    } else if (nameLevel == "4" && treeNode.type == 4) {  //区县
                        //console.log("单击市后获取街道列表");
                        //console.log("节点层级:"+nameLevel);
                        treeArea.obtainTownDataByDistrict(tree, treeNode);
                    }
                    else {
                        tree.expandNode(treeNode);
                    }
                },onCheck: function(e, treeId, treeNode){
                    var nodes = tree.getCheckedNodes(true);
                    for (var i=0, l=nodes.length; i<l; i++) {
                        tree.expandNode(nodes[i], true, false, false);
                    }
                    return false;
                },onAsyncSuccess: function(event, treeId, treeNode, msg){
                    var nodes = tree.getNodesByParam("pId", treeNode.id, null);
                    for (var i=0, l=nodes.length; i<l; i++) {
                        try{tree.checkNode(nodes[i], treeNode.checked, true);}catch(e){}
                    }
                    selectCheckNode();
                },onDblClick: function(event){
                    // top.$.jBox.getBox().find("button[value='ok']").trigger("click");
                    // top.layer.close(index);
                    event.preventDefault();
                    top.$("#layui-layer"+index).find(".layui-layer-btn0").trigger("click");
                }
            }
        };

        function expandNodes(nodes) {
            if (!nodes) return;
            for (var i=0, l=nodes.length; i<l; i++) {
                tree.expandNode(nodes[i], true, false, false);
                if (nodes[i].isParent && nodes[i].zAsync) {
                    expandNodes(nodes[i].children);
                }
            }
        }
        $(document).ready(function(){
            $.get("${ctx}${url}${fn:indexOf(url,'?')==-1?'?':'&'}&extId=${extId}&isAll=${isAll}&module=${module}&t="
                + new Date().getTime(), function(zNodes){
                // 初始化树结构
                tree = $.fn.zTree.init($("#tree"), setting, zNodes);
//                var nodesLevel=$("#nodesLevel").attr("value");
                var nodesLevel="<%=nodesLevel%>";
                if(nodesLevel=="")
                {
                    nodesLevel="0";
                }
                // 默认展开节点
                var nodes = tree.getNodesByParam("level", nodesLevel);
                for(var i=0; i<nodes.length; i++) {
                    tree.expandNode(nodes[i], true, false, false);
                }
                //异步加载子节点（加载用户）
                var nodesOne = tree.getNodesByParam("isParent", true);
                for(var j=0; j<nodesOne.length; j++) {
                    tree.reAsyncChildNodes(nodesOne[j],"!refresh",true);
                }
                selectCheckNode();
            });
            key = $("#key");
            key.bind("focus", focusKey).bind("blur", blurKey).bind("change cut input propertychange", searchNode);
            key.bind('keydown', function (e){if(e.which == 13){searchNode();}});
            setTimeout("search();", "300");
        });

        // 默认选择节点
        function selectCheckNode(){
            var areaLevel = "${areaLevel}";
            var ids = "${selectIds}".split(",");

            for(var i=0; i<ids.length; i++) {
                var node = tree.getNodeByParam("id", (type==3?"u_":"")+ids[i]);
                if("${checked}" == "true"){
                    try{tree.checkNode(node, true, true);}catch(e){}
                    tree.selectNode(node, false);
                }else{
                    if (node) {
                        tree.selectNode(node, true);
                    } else {
                        // 获取到数据为null
                        if (areaLevel === "2") {
                            // 区/县是动态加载
                            var selectParentId = ${empty selectParentId? 0 : selectParentId};
                            var parentNode = tree.getNodeByParam("id", selectParentId);
                            if (parentNode) {
                                //界面没有刷新能获取到父级区域id
                                treeArea.obtainDistrictDataByCity(tree, parentNode, ids[i]);
                            } else {
                                // 界面刷新后，无法取到父级区域id
                                // console.log("界面刷新后，无法取到父级区域id");
                                treeArea.loadAreaDataByAreaId(tree, ids[i], areaLevel);
                            }
                        } else if (areaLevel === "3") {
                            treeArea.loadAreaDataByAreaId(tree, ids[i], areaLevel);
                        }
                    }
                }
                <%--if(node && node.level === 1 && areaLevel === "2"){ //市--%>
                <%--    console.log("市获取区/县");--%>
                <%--    treeArea.obtainDistrictDataByCity(tree, node, "${selectIds}");--%>
                <%--}--%>
            }
        }
        function focusKey(e) {
            if (key.hasClass("empty")) {
                key.removeClass("empty");
            }
        }
        function blurKey(e) {
            if (key.get(0).value === "") {
                key.addClass("empty");
            }
            searchNode(e);
        }

        //搜索节点
        function searchNode() {
            // 取得输入的关键字的值
            var value = $.trim(key.get(0).value);

            // 按名字查询
            var keyType = "name";<%--
			if (key.hasClass("empty")) {
				value = "";
			}--%>

            // 如果和上次一次，就退出不查了。
            if (lastValue === value) {
                return;
            }

            // 保存最后一次
            lastValue = value;

            var nodes = tree.getNodes();
            // 如果要查空字串，就退出不查了。
            if (value == "") {
                showAllNode(nodes);
                return;
            }
            hideAllNode(nodes);
            nodeList = tree.getNodesByParamFuzzy(keyType, value);
            updateNodes(nodeList);
        }

        //隐藏所有节点
        function hideAllNode(nodes){
            nodes = tree.transformToArray(nodes);
            for(var i=nodes.length-1; i>=0; i--) {
                tree.hideNode(nodes[i]);
            }
        }

        //显示所有节点
        function showAllNode(nodes){
            nodes = tree.transformToArray(nodes);
            for(var i=nodes.length-1; i>=0; i--) {
                /* if(!nodes[i].isParent){
                    tree.showNode(nodes[i]);
                }else{ */
                if(nodes[i].getParentNode()!=null){
                    tree.expandNode(nodes[i],false,false,false,false);
                }else{
                    tree.expandNode(nodes[i],true,true,false,false);
                }
                tree.showNode(nodes[i]);
                showAllNode(nodes[i].children);
                /* } */
            }
        }

        //更新节点状态
        function updateNodes(nodeList) {
            tree.showNodes(nodeList);
            for(var i=0, l=nodeList.length; i<l; i++) {

                //展开当前节点的父节点
                tree.showNode(nodeList[i].getParentNode());
                //tree.expandNode(nodeList[i].getParentNode(), true, false, false);
                //显示展开符合条件节点的父节点
                while(nodeList[i].getParentNode()!=null){
                    tree.expandNode(nodeList[i].getParentNode(), true, false, false);
                    nodeList[i] = nodeList[i].getParentNode();
                    tree.showNode(nodeList[i].getParentNode());
                }
                //显示根节点
                tree.showNode(nodeList[i].getParentNode());
                //展开根节点
                tree.expandNode(nodeList[i].getParentNode(), true, false, false);
            }
        }

        // 开始搜索
        function search() {
            $("#search").slideToggle(200);
            $("#txt").toggle();
            $("#key").focus();
        }

    </script>
</head>
<body>
<div style="position:absolute;right:8px;top:15px;cursor:pointer;" onclick="search();">
    <i class="icon-search"></i><label id="txt">搜索</label>
</div>
<div id="search" class="form-search hide" style="padding:10px 0 0 13px;">
    <label for="key" class="control-label" style="padding:5px 5px 3px 0;">关键字：</label>
    <input type="text" class="empty" id="key" name="key" maxlength="50" style="width:110px;">
    <button class="btn" id="btn" onclick="searchNode()">搜索</button>
    <input type="hidden" id="nodesLevel" name="nodesLevel" value="<%=nodesLevel%>" />
</div>
<div id="tree" class="ztree" style="padding:15px 20px;"></div>
</body>
</html>
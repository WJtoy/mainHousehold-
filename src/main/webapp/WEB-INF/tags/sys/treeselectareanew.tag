<!-- 任意级别区域选择 -->
<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ attribute name="layerId" type="java.lang.String" required="false" description="窗口id"%>
<%@ attribute name="id" type="java.lang.String" required="true" description="编号"%>
<%@ attribute name="name" type="java.lang.String" required="true" description="隐藏域名称（ID）"%>
<%@ attribute name="value" type="java.lang.String" required="true" description="隐藏域值（ID）"%>
<%@ attribute name="labelName" type="java.lang.String" required="true" description="输入框名称（Name）"%>
<%@ attribute name="labelValue" type="java.lang.String" required="true" description="输入框值（Name）"%>
<%@ attribute name="levelValue" type="java.lang.String" required="true" description="等级值"%>
<%@ attribute name="parentValue" type="java.lang.String" required="false" description="父区域id"%>
<%@ attribute name="nodeLevel" type="java.lang.Boolean" required="false" description="是否将节点level存储在隐藏控件中"%>
<%@ attribute name="title" type="java.lang.String" required="true" description="选择框标题"%>
<%@ attribute name="url" type="java.lang.String" required="true" description="树结构数据地址"%>
<%@ attribute name="checked" type="java.lang.Boolean" required="false" description="是否显示复选框"%>
<%@ attribute name="allowClear" type="java.lang.Boolean" required="false" description="是否允许清除"%>
<%@ attribute name="cssClass" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="cssStyle" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="disabled" type="java.lang.String" required="false" description="是否限制选择，如果限制，设置为disabled"%>
<%@ attribute name="readonly" type="java.lang.String" required="false" description="是否限制选择，并内容设置为只读，按钮设置为disabled"%>
<%@ attribute name="nodesLevel" type="java.lang.String" required="false" description="菜单展开层数"%>
<%@ attribute name="nameLevel" type="java.lang.String" required="false" description="返回名称关联级别(值为4时加载到4级(乡/镇/街道))"%>
<%@ attribute name="filter" type="java.lang.String" required="false" description="动态从指定控件读取value"%>
<%@ attribute name="checkvalue" type="java.lang.String" required="false" description="检查控件的id"%>
<%@ attribute name="checkmessage" type="java.lang.String" required="false" description="检查提示信息"%>
<%@ attribute name="clearControler" type="java.lang.String" required="false" description="清空指定控件"%>
<%@ attribute name="tooltipPlacement" type="java.lang.String" required="false" description="tooltip方式显示错误信息时的位置"%>
<%@ attribute name="clearIdValue" type="java.lang.String" required="false" description="清除后id的默认值"%>
<%@ attribute name="canSelectLevel" type="java.lang.String" required="false" description="可选择等级，0开始"%>
<%@ attribute name="loadFourLevel" type="java.lang.String" required="false" description=""%>

<div class="input-append">
    <input id="${id}ParentId" name="${id}.parent.id" type="hidden" value="${parentValue}" />
    <input id="${id}Id" name="${name}" type="hidden"  value="${value}" />
    <input id="${id}Level" name="${id}Level" type="hidden" value="${levelValue}" />
    <input id="${id}Name" name="${labelName}" readonly="readonly" type="text" value="${labelValue}" maxlength="80" placeholder="请选择区域${nameLevel eq 4?',可选择街道':''}"
           class="${cssClass}" style="${cssStyle}"/><a id="${id}Button" href="javascript:" class="btn${(disabled eq 'true' || readonly eq 'true') ? ' disabled' : ' '}"${disabled eq 'true' ? ' disabled=\'true\'' : ' '} ><i class="icon-search"></i></a>&nbsp;&nbsp;
</div>
<script type="text/javascript">
    $("#${id}Button").click(function(){
        // 是否限制选择，如果限制，设置为disabled
        if($(this).attr("disabled")){
            return true;
        }

        if ($("#${id}Id").val() == "1") {
            layerMsg("不能修改省或直辖市的上级区域。");
            return;
        }

        if ("${checkvalue}" != ""){
            if($("#${checkvalue}").val() == ""){
                <%--top.layer.error("${checkmessage}","提示");--%>
                layerMsg("${checkmessage}");
                return;
            }
        }
        var nameLevel = ${nameLevel eq null ? "1" : nameLevel};

        var filtercontrol = "${empty filter ?"":filter}";
        var filtervalue = "";
        if(filtercontrol != ""){
            filtervalue = $("#${filter}").val();
            if(filtervalue == "undefined"){
                filtervalue = "";
            }
        }
        var btnlist = ['确定', '关闭'${allowClear?",'清除'":""}];
        var replyIdex = top.layer.open({
            type: 2,
            id:"${empty layerId?'':layerId}",
            zIndex: 29891015,
            title:'选择${title}',
            content: "${ctx}/tag/treeselectareanew?url="+encodeURIComponent("${url}${fn:indexOf(url,'?')==-1?'?':'&'}filter="+filtervalue)+"&module=&checked=${checked}&extId=&nodesLevel=${nodesLevel}&selectParentId=" + $("#${id}ParentId").val() + "&selectIds="+$("#${id}Id").val() + "&areaLevel=" + $("#${id}Level").val()+"&nameLevel=${empty nameLevel?"3":nameLevel}",
            shade: 0.3,
            area: ['320px', '420px'],
            maxmin: false,
            btn: btnlist,
            yes: function(index, layero){
                var iframeWin = parent.window['layui-layer-iframe' + index];
                var tree = iframeWin.tree;
                var ids = [], names = [], nodes = [];
                if ("${checked}" == "true"){
                    nodes = tree.getCheckedNodes(true);
                }else{
                    nodes = tree.getSelectedNodes();
                }
                var canSelectLevel = ${empty canSelectLevel?-1:canSelectLevel};
                for(var i=0; i<nodes.length; i++) {
                    //check
                    if (nodes[i].level < canSelectLevel){
                        layerAlert("不能选择此区域（"+nodes[i].name+"）请重新选择。","警告");
                        return false;
                    }
                    ids.push(nodes[i].id);
                    var t_node = nodes[i];
                    var t_name = "";
                    var name_l = 0;
                    var pnode = t_node.getParentNode();
                    if(pnode !=null){
                        $("#${id}ParentId").val(pnode.id);
                    }else{
                        $("#${id}ParentId").val("0");
                    }
                    do{
                        name_l++;
                        if(t_node!=null){
                            t_name = t_node.name + " " + t_name;
                            t_node = t_node.getParentNode();
                        }
                    }while(name_l < nameLevel);
                    names.push(t_name);//<c:if test="${!checked}">
                    $("#${id}Level").val(nodes[i].level);
                    break; // 如果为非复选框选择，则返回第一个选择  </c:if>
                }
                $("#${id}Id").val(ids).trigger('change');
                $("#${id}Name").val(names);

                if($("#${id}Id").attr("clearControler")!="")
                {
                    $("#${clearControler}").val("");
                }
                top.layer.close(index);
            },
            btn2: function(index, layero){
                top.layer.close(index);
            },
            btn3: function(index, layero){
                //clear
                $("#${id}Id").val("${clearIdValue}").trigger('change');
                $("#${id}Name").val("");
                $("${id}Level").val("0");
                $("${id}ParentId").val("0");
            },
            success: function(layero,index){
            }
        });

    });
</script>

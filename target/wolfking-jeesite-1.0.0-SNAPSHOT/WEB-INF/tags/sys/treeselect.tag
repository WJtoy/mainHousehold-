<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ attribute name="id" type="java.lang.String" required="true" description="编号"%>
<%@ attribute name="name" type="java.lang.String" required="true" description="隐藏域名称（ID）"%>
<%@ attribute name="value" type="java.lang.String" required="true" description="隐藏域值（ID）"%>
<%@ attribute name="nodeLevel" type="java.lang.Boolean" required="false" description="是否将节点level存储在隐藏控件中"%>
<%@ attribute name="labelName" type="java.lang.String" required="true" description="输入框名称（Name）"%>
<%@ attribute name="labelValue" type="java.lang.String" required="true" description="输入框值（Name）"%>
<%@ attribute name="title" type="java.lang.String" required="true" description="选择框标题"%>
<%@ attribute name="url" type="java.lang.String" required="true" description="树结构数据地址"%>
<%@ attribute name="checked" type="java.lang.Boolean" required="false" description="是否显示复选框"%>
<%@ attribute name="extId" type="java.lang.String" required="false" description="排除掉的编号（不能选择的编号）"%>
<%@ attribute name="notAllowSelectRoot" type="java.lang.Boolean" required="false" description="不允许选择根节点"%>
<%@ attribute name="notAllowSelectParent" type="java.lang.Boolean" required="false" description="不允许选择父节点"%>
<%@ attribute name="module" type="java.lang.String" required="false" description="过滤栏目模型（只显示指定模型，仅针对CMS的Category树）"%>
<%@ attribute name="selectScopeModule" type="java.lang.Boolean" required="false" description="选择范围内的模型（控制不能选择公共模型，不能选择本栏目外的模型）（仅针对CMS的Category树）"%>
<%@ attribute name="allowClear" type="java.lang.Boolean" required="false" description="是否允许清除"%>
<%@ attribute name="cssClass" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="cssStyle" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="disabled" type="java.lang.String" required="false" description="是否限制选择，如果限制，设置为disabled"%>
<%@ attribute name="readonly" type="java.lang.String" required="false" description="是否限制选择，并内容设置为只读，按钮设置为disabled"%>
<%@ attribute name="nodesLevel" type="java.lang.String" required="false" description="菜单展开层数"%>
<%@ attribute name="nameLevel" type="java.lang.String" required="false" description="返回名称关联级别"%>
<%@ attribute name="levelValue" type="java.lang.Integer" required="false" description="级别值(idLevel)"%>
<%@ attribute name="filter" type="java.lang.String" required="false" description="动态从指定控件读取value"%>
<%@ attribute name="checkvalue" type="java.lang.String" required="false" description="检查控件的id"%>
<%@ attribute name="checkmessage" type="java.lang.String" required="false" description="检查提示信息"%>
<%@ attribute name="clearControler" type="java.lang.String" required="false" description="清空指定控件"%>
<%@ attribute name="tooltipPlacement" type="java.lang.String" required="false" description="tooltip方式显示错误信息时的位置"%>
<%@ attribute name="clearIdValue" type="java.lang.String" required="false" description="清除后id的默认值"%>

<div class="input-append">
	<input id="${id}Id" name="${name}" class="${cssClass}" type="hidden"  value="${value}" />
	<c:if test="${nodeLevel}">
		<input id="${id}Level" name="${id}Level" type="hidden" value="${levelValue}" />
	</c:if>
	<input id="${id}Name" name="${labelName}" readonly="readonly" type="text" value="${labelValue}" maxlength="50" data-placement="${tooltipPlacement}"
		   class="${cssClass}" style="${cssStyle}"/><a id="${id}Button" href="javascript:" class="btn${(disabled eq 'true' || readonly eq 'true') ? ' disabled' : ' '}"${disabled eq 'true' ? ' disabled=\'true\'' : ' '} ><i class="icon-search"></i></a>&nbsp;&nbsp;
</div>
<script type="text/javascript">
    $("#${id}Button").click(function(){
        // 是否限制选择，如果限制，设置为disabled
        if($(this).attr("disabled")){
            return true;
        }
        if ("${checkvalue}" != ""){
            if($("#${checkvalue}").val() == ""){
                top.$.jBox.error("${checkmessage}","提示");
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

        // 正常打开
        top.$.jBox.open("iframe:${ctx}/tag/treeselect?url="+encodeURIComponent("${url}${fn:indexOf(url,'?')==-1?'?':'&'}filter="+filtervalue)+"&module=${module}&checked=${checked}&extId=${extId}&nodesLevel=${nodesLevel}&selectIds="+$("#${id}Id").val(), "选择${title}", 300, 420, {
            persistent:false,
            buttons:{"确定":"ok", ${allowClear?"\"清除\":\"clear\", ":""}"关闭":true}, submit:function(v, h, f){
                if (v=="ok"){
                    var tree = h.find("iframe")[0].contentWindow.tree;//h.find("iframe").contents();
                    var ids = [], names = [], nodes = [];
                    if ("${checked}" == "true"){
                        nodes = tree.getCheckedNodes(true);
                    }else{
                        nodes = tree.getSelectedNodes();
                    }
                    for(var i=0; i<nodes.length; i++) {//<c:if test="${checked}">
                        if (nodes[i].isParent){
                            continue; // 如果为复选框选择，则过滤掉父节点
                        }//</c:if><c:if test="${notAllowSelectRoot}">
                        if (nodes[i].level == 0){
                            top.$.jBox.tip("不能选择根节点（"+nodes[i].name+"）请重新选择。");
                            return false;
                        }//</c:if><c:if test="${notAllowSelectParent}">
                        if (nodes[i].isParent){
                            top.$.jBox.tip("不能选择父节点（"+nodes[i].name+"）请重新选择。");
                            return false;
                        }//</c:if><c:if test="${not empty module && selectScopeModule}">
                        if (nodes[i].module == ""){
                            top.$.jBox.tip("不能选择公共模型（"+nodes[i].name+"）请重新选择。");
                            return false;
                        }else if (nodes[i].module != "${module}"){
                            top.$.jBox.tip("不能选择当前栏目以外的栏目模型，请重新选择。");
                            return false;
                        }//</c:if>
                        ids.push(nodes[i].id);
                        var t_node = nodes[i];
                        var t_name = "";
                        var name_l = 0;
                        do{
                            name_l++;
                            if(t_node!=null){
                                t_name = t_node.name + " " + t_name;
                                t_node = t_node.getParentNode();
                            }
                        }while(name_l < nameLevel);
                        names.push(t_name);//<c:if test="${!checked}">
						<c:if test="${nodeLevel}">$("#${id}Level").val(nodes[i].level);</c:if>
                        break; // 如果为非复选框选择，则返回第一个选择  </c:if>
                    }
                    $("#${id}Id").val(ids).trigger('change');
                    $("#${id}Name").val(names);

                    if($("#${id}Id").attr("clearControler")!="")
                    {
                        $("#${clearControler}").val("");
                    }
                }//<c:if test="${allowClear}">
                else if (v=="clear"){
                    $("#${id}Id").val("${clearIdValue}").trigger('change');
                    $("#${id}Name").val("");
					<c:if test="${nodeLevel}">
                    $("${id}Level").val("");
					</c:if>
                }//</c:if>
            }, loaded:function(h){
                //$(".jbox-content", top.document).css("overflow-y","hidden");
                //$(".jbox-content", h).css("overflow-y","hidden");
                $("#jbox-iframe",h).prop("height","98%").prop("z-index",29891020);
            }
        });
    });
</script>

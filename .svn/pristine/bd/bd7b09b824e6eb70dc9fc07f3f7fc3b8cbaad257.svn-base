<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ include file="/WEB-INF/views/include/dialog.jsp"%>
<script type="text/javascript">
	/**
	 * 敏感词判断
	 * true:含有敏感词
	 */
	var forbiddenArray =[${fns:getAllWordsString()}];
	function hasForbiddenStr(str){
        if(forbiddenArray.length == 0){
            return false;
        }
        var destString = $.trim(str);
		var re = '(';
		for(var i=0;i<forbiddenArray.length;i++){
			if(i==forbiddenArray.length-1)
				re+=forbiddenArray[i];
			else
				re+=forbiddenArray[i]+"|";
		}
		re = re + ')';
		//定义正则表示式对象
		//利用RegExp可以动态生成正则表示式
		var pattern = new RegExp(re,"mi");
		return pattern.test(destString);
	}

	/**
	 * 筛选出敏感词
	 * @param str
	 * @returns array
	 */
	function filterForbiddenStr(str){
	    if(forbiddenArray.length == 0){
	        return null;
        }
		var destString = $.trim(str);
		var re = '(';
		for(var i=0;i<forbiddenArray.length;i++){
			if(i==forbiddenArray.length-1)
				re+=forbiddenArray[i];
			else
				re+=forbiddenArray[i]+"|";
		}
		re = re + ')';
		//定义正则表示式对象
		//利用RegExp可以动态生成正则表示式
		var pattern = new RegExp(re,"gmi");
		return destString.match(pattern);
	}
</script>

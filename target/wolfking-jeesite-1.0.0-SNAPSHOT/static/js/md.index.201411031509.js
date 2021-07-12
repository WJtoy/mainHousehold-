define('md.index', function(reuqire, exports, module) {
	var slider = require('slider');
	exports.init = function() {
		slider.init({
			auto : true,
			width : 2000,
			className : "cur",
			effect : "scrollx",
			titleId : "bannerTab",
			contentId : "bannerList",
			titleTag : "li",
			contentTag : "li"
		});
	}
});

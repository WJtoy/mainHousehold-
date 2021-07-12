$(function()
{
	var $this = $(".scroll_list");
	var scrollTimer;
	$this.hover(function()
	{
		clearInterval(scrollTimer);
	}, function()
	{
		scrollTimer = setInterval(function()
		{
			scrollNews($this);
		}, 500);
	}).trigger("mouseleave");

	function scrollNews(obj)
	{
		var $self = obj.find("ul");
		var lineHeight = $self.find("li:first").height();
		$self.animate(
		{
			"marginTop" : -lineHeight + "px"
		}, 600, function()
		{
			$self.css(
			{
				marginTop : 0
			}).find("li:first").appendTo($self);
		});
	}
});

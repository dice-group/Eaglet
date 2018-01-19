$.fn.inlineEdit = function(replaceWith) {
	
	
	$(this).hover(function() {
		$(this).addClass('hover');
	}, function() {
		$(this).removeClass('hover');
	});

	$(this).click(function() {

		var elem = $(this);
		console.log(elem);
		elem.hide();
		elem.after(replaceWith);
		replaceWith.focus();
		replaceWith.val(elem.text())

		replaceWith.blur(function() {

			if ($(this).val() != "") {
				elem.text($(this).val());
				
			}

			$(this).remove();
			elem.show();
		});


	});
};
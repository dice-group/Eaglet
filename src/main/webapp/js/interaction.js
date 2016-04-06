function uservalidation() {
	// get the form data using another method
	var loginName = $("input#user").val();

	$.ajax({
		url : "service/next",// servlet URL that gets first option as
		// parameter and returns JSON of to-be-populated
		// options
		type : "GET",// request type, can be GET
		data : {
			username : loginName,// data to be sent to the server
		},
		dataType : "json"// type of data returned
	}).done(function(data) {
		if (data.success) {
			var json_x = JSON.parse(data);
			text=json_x.getText();
			markings=json_x.getMarkings();
			$("#sidebar-content").html=generateText(text, markings);
			//$(text).appendTo("#sidebar-content");
		}
	}).fail(function(e) {
		// handle error
	});
};

function generateText(text, markings) {
	// add markups
	return textWithMarkups;
}

